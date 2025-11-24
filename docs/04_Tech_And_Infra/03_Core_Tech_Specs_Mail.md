# tech_req_mail_service

## 📧 `tech_req_mail_service.md` (이메일 서비스 기술 명세)

| **구분** | **요구사항** | **상세 구현 방안** | **처리 주체** |
| --- | --- | --- | --- |
| **주요 기능** | 팀원 초대장 발송 | **JWT 기반 초대 링크**를 포함하는 HTML 이메일을 지정된 수신자(`invitedEmail`)에게 발송합니다. | `MailService` 컴포넌트 |
| **핵심 의존성** | 이메일 전송 기능 확보 | **`spring-boot-starter-mail`** 의존성 사용. | `build.gradle` |
| **템플릿 엔진** | 동적 HTML 본문 생성 | **`Thymeleaf`** 템플릿 엔진 의존성을 사용하여 초대장 링크, 프로젝트 이름, 초대한 사용자 이름 등 동적 데이터를 이메일 본문에 주입합니다. | `MailService` 및 `ThymeleafConfig` |
| **SMTP 설정** | 인증 정보 관리 | `application.yml`에 SMTP 서버 주소, 포트, 사용자 이름, **앱 비밀번호 (App Password)**를 설정하여 보안을 확보합니다. | `application.yml` |
| **보안 요구사항** | 2차 인증 계정 사용 시 | Google/Naver 등의 서비스 계정 사용 시, 일반 비밀번호 대신 **반드시 '앱 비밀번호'를 발급받아** 설정에 사용하도록 명시합니다. | 운영 설정 (Configuration) |
| **발송 실패 처리** | 예외 처리 | 메일 발송은 외부 네트워크 환경에 의존하므로, 발송 실패 시 재시도 로직을 도입하거나 실패 기록을 로깅하여 운영 이슈에 대응할 수 있도록 합니다. | `MailService`의 예외 처리 로직 |