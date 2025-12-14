# Stage 1: Build
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 먼저 다운로드
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY . .
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 타임존 설정 (UTC)
# Alpine 이미지의 기본 타임존은 UTC이므로 별도 설정 불필요
# 프론트엔드에서 각 지역 시간대로 변환하여 표시
ENV TZ=UTC

# 🔑 Netty QUIC 네이티브 의존성
RUN apt-get update \
 && apt-get install -y libgcc-s1 \
 && rm -rf /var/lib/apt/lists/*

# 로그 디렉토리 생성
RUN mkdir -p /var/log/mockops && \
    chmod 755 /var/log/mockops

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행 (prod 프로파일 사용)
# 환경변수는 docker run 시 전달
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080