# Release Notes

## v1.0.1 ()

### ✨ Added
- 프로젝트 내 닉네임 기능 추가
- 도메인 서버 담당 멤버 기능 추가

### 🔄 Changed
- 프로젝트 목록 API 프로젝트 검색 API로 변경
- 도메인 서버 목록 API 도메인 서버 검색 API로 변경
- Webhook Deploy 기능 실제 서버 상태 기반으로 변경

### 🐛 Fixed

### ⚠️ Breaking Changes
- 프로젝트
  - ❌ 기존 방식 (deprecated 예정)
    `GET /api/v1/projects?page=0&size=10`
  - ✅ 새로운 방식 (검색 API를 기본 목록으로 사용)
    `GET /api/v1/projects/search?page=0&size=10`
- 도메인 서버
  - ❌ 기존 방식 (deprecated 예정)
    `GET /api/v1/projects/{projectId}/servers?page=0&size=20`
  - ✅ 새로운 방식 (검색 API를 기본 목록으로 사용)
    `GET /api/v1/projects/{projectId}/servers/search?page=0&size=20`

**Webhook Deploy**

❌ 기존 방식 
- healthcheckUrl null 가능
- 변경할 서버 상태 직접 입력
```json
{
  "projectName": "exProject",
  "domainServerName": "exServer",
  "serverStatus": "DEPLOY",
  "healthCheckUrl": "https://my.com/health",
  "healthCheckInterval": "10m"
}
```
✅ 새로운 방식
- healthcheckUrl 필수
- 서버 상태 불필요
```json
{
  "projectName": "exProject",
  "domainServerName": "exServer",
  "healthCheckUrl": "https://my.com/health",
  "healthCheckInterval": "10m"
}
```

### 🧪 Known Issues

## v1.0.0 (2025-12-15)
- 초기 릴리즈