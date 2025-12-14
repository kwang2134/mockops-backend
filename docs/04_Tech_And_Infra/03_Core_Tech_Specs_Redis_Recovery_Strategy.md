## Redis Recovery Strategy: Forced SWR Mode

### 1. 💡 목적 (Purpose)

치명적인 Redis 장애 (마스터/레플리카 동시 다운 등) 복구 직후, **정합성이 깨진 캐시 데이터**를 클라이언트에게 제공하는 것을 방지하고 **서비스 중단 없이** 캐시를 최신 DB 데이터로 안전하게 재구축하기 위함.

### 2. 🔑 핵심 메커니즘 (Core Mechanism)

장애 복구 직후, 애플리케이션은 **`RECOVERY_MODE`** 플래그를 확인하여 Redis 캐시를 **Stale**로 간주하고, 모든 읽기 요청을 **DB로 강제 우회(Bypass)** 시켜 최신 데이터를 확보한 후 캐시를 덮어씁니다 (Write-Through).

### 3. ⚙️ 구현 요소 (Implementation Components)

| 요소 | 내용 | 책임 |
| :--- | :--- | :--- |
| **`Recovery Mode Flag`** | Redis에 저장된 전역 키 (예: `OPS:RECOVERY_MODE`). 존재 여부 또는 값 (`TRUE`)으로 모드 활성화 판단. | Infra / Operation |
| **`CachePort` 수정** | `get...FromCache()` 메소드에서 복구 모드 확인 로직 추가. | Application Layer |
| **복구 로직** | 캐시 조회 실패 시 DB에서 읽어 캐시를 덮어쓰는 로직 (기존 Cache-Aside 패턴 활용). | Application Layer |

### 4. 📝 `CachePort` 로직 상세 (Implementation Details)

`RedisMockApiCache` 및 `RedisCorsOriginCache`의 `get...FromCache` 계열 메소드에 다음 로직을 적용합니다.

#### 4.1. `isRecoveryMode()` 확인 메소드

`RedisTemplate`을 사용하여 `RECOVERY_MODE` 플래그 키의 존재 여부를 확인하는 메소드를 추가합니다.

```java
// RedisCacheCheckService (또는 모든 Cache 구현체에 공통 적용)
private boolean isRecoveryModeActive() {
    // Redis의 전역 키 OPS:RECOVERY_MODE 의 존재 여부 확인
    return Boolean.TRUE.equals(redisTemplate.hasKey("OPS:RECOVERY_MODE"));
}
```

#### 4.2. `getMockApiFromCache` 로직 수정 (SWR 강제 전환)

| 상태 | 동작 (수정 후) | 결과 |
| :--- | :--- | :--- |
| **`RECOVERY_MODE` 활성화** | 1. Redis에서 값을 읽지 않거나, 읽더라도 **즉시 무시**하고 **Cache Miss로 간주**합니다. | **무조건 DB 호출**로 이어져 최신 데이터를 확보합니다. |
| **일반 모드 (비활성화)** | 2. 기존 Cache-Aside 패턴 로직대로 Redis에서 값을 확인하고 반환합니다. | 정상적인 캐시 히트/미스 처리. |

**실제 캐싱 로직에 적용되는 흐름:**

```java
// MockApiCachePort의 getMockApiFromCache() 내부 로직
public Optional<MockCacheDto> getMockApiFromCache(...) {
    if (isRecoveryModeActive()) {
        log.warn("Recovery Mode Active. Bypassing cache read and forcing Cache Miss.");
        // 복구 모드에서는 캐시에서 무엇을 읽든 신뢰하지 않고 바로 Optional.empty() 반환
        return Optional.empty(); 
    }
    
    // (기존 로직) Redis에서 값을 읽어 반환
    // ...
}
```
### 5. ⚠️ 복구 시 운영 절차 (Operational Procedure during Recovery)

이 절차는 Redis 장애 알림을 받은 운영자가 수동으로 수행해야 하는 단계입니다.

#### 5.1. 수동 복구 스크립트 (`manual_cache_recovery.sh`)

이 스크립트를 운영 서버에 미리 준비해 두고, 장애 발생 시 **새 마스터 Redis의 주소**를 인자로 받아 실행합니다.

```bash
#!/bin/bash
# Description: Redis 마스터 장애 복구 후 캐시 데이터 정합성 확보를 위한 스크립트

# 사용법: ./manual_cache_recovery.sh <새_마스터_IP> <포트>
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <New_Master_IP> <Port>"
    exit 1
fi

REDIS_HOST="$1"
REDIS_PORT="$2"
RECOVERY_MODE_KEY="OPS:RECOVERY_MODE"
TTL_SECONDS=300 # 5분 동안 복구 모드 유지

echo "--- Redis 캐시 수동 복구 절차 시작 ---"
echo "대상 마스터: $REDIS_HOST:$REDIS_PORT"

# 1. 복구 모드 활성화 (애플리케이션이 캐시를 사용하지 않도록 강제)
echo "[STEP 1/3] Recovery Mode 키 설정 ($TTL_SECONDS 초 유지)..."
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" SET "$RECOVERY_MODE_KEY" "TRUE" EX "$TTL_SECONDS"
if [ $? -ne 0 ]; then
    echo "[ERROR] Redis 연결 실패 또는 Recovery Mode 키 설정 실패."
    exit 1
fi

# 2. 오염된 캐시 키 패턴 검색 및 삭제 (선별적 FLUSH)
echo "[STEP 2/3] 오염 가능성 있는 캐시 키 삭제 시작..."

# Mock API 캐시 패턴 삭제 (MOCK:*)
echo "  - Mock API 캐시 삭제 (MOCK:*)"
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" --scan --pattern "MOCK:*" | xargs -r redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" DEL

# CORS Origin 캐시 패턴 삭제 (project:cors:*)
echo "  - CORS Origin 캐시 삭제 (project:cors:*)"
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" --scan --pattern "project:cors:*" | xargs -r redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" DEL

echo "[INFO] 캐시 데이터 선별 삭제 완료."

# 3. 자동 재구축 대기 및 최종 상태 안내
echo "[STEP 3/3] 자동 재구축 시작 및 최종 상태 안내:"
echo "  - 애플리케이션은 SWR 모드(Recovery Mode)에서 DB에서 읽어 캐시를 재구축합니다."
echo "  - Recovery Mode는 $TTL_SECONDS 초 후 자동으로 해제됩니다."
echo "  - (선택) 즉시 해제하려면: redis-cli -h $REDIS_HOST -p $REDIS_PORT DEL $RECOVERY_MODE_KEY"

echo "--- 복구 절차 완료 ---"
```

#### 5.2 운영 실행 순서

1. **장애 감지 및 알림 수신**: 모니터링 툴(Slack/Email 알림)을 통해 Redis 마스터 장애를 확인합니다.

2. **Redis 인스턴스 복구**: 죽은 인스턴스(들)를 재시작합니다. (**RDB/AOF 파일 자동 로드**)

3. **토폴로지 수동 확인**: 마스터-레플리카가 서로 연결되었는지 (`INFO replication` 명령 등으로) 확인합니다. (수동 승격이 필요한 경우, 직접 승격 명령을 실행할 수 있습니다.)

4. 스크립트 실행: 새 마스터의 IP와 포트를 확인하고 `manual_cache_recovery.sh` 스크립트를 실행합니다.

    - 예: `./manual_cache_recovery.sh 10.0.0.5 6379`

5. **자동 재구축**: 스크립트가 `OPS:RECOVERY_MODE` 키를 설정하고 캐시 데이터를 삭제하면, 애플리케이션이 DB에서 데이터를 읽어 캐시를 재구축하기 시작합니다.

6. **모드 해제 확인**: 5분 후 키가 자동으로 만료되거나, 재구축이 충분히 이루어졌다고 판단되면 수동으로 `DEL OPS:RECOVERY_MODE` 명령을 실행하여 복구 모드를 해제합니다.

