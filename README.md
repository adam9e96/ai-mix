# AI-MIX

**OpenAI API 기반 학습·지식 공유 플랫폼**

[Live Demo](https://ai-mix.adam9e96.dev/) | [Portfolio](https://www.notion.so/adam9e96/AI-MIX-34a9b563794081f88605eee8eeafa2d8)

## 프로젝트 소개

AI-MIX는 AI 채팅으로 학습한 내용을 지식 카드로 정리하고, Q&A와 퀴즈 배틀을 통해 이해도를 점검할 수 있는 학습 플랫폼입니다.

OpenAI API 연동에 그치지 않고 채팅 문맥 관리, JWT 인증, 사용자별 API Key 암호화, 토큰 사용량 집계, 지식 카드와 Q&A 데이터 모델링, Docker Compose 기반 배포까지 하나의 서비스 흐름으로 구현했습니다.

## 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 개발 기간 | 2025.12.03 ~ 2026.01.02 |
| 팀 구성 | 2인 팀 프로젝트 |
| 담당 역할 | 백엔드 API, 인증, OpenAI 연동, DB·Redis 설계, 배포 환경 구성 |
| 배포 주소 | [https://ai-mix.adam9e96.dev/](https://ai-mix.adam9e96.dev/) |
| GitHub | [https://github.com/adam9e96/ai-mix](https://github.com/adam9e96/ai-mix) |

### 담당 범위

- Spring Boot 기반 REST API와 도메인 로직 구현
- JWT·Refresh Token 인증 및 Redis 기반 이메일 인증 구현
- OpenAI Java SDK 연동과 사용자별 API Key 암호화 저장
- AI 채팅, 퀴즈 배틀, 지식 카드, Q&A API 구현
- PostgreSQL 데이터 모델과 Redis 임시 데이터 구조 설계
- Docker Compose, Nginx, GitHub Actions 기반 배포 환경 구성

### 검증 가능한 구현 수치

실제 사용량을 추정하지 않고 저장소에서 다시 확인할 수 있는 구현 수치만 정리했습니다.

| 항목 | 수치 |
| --- | ---: |
| 백엔드 API 핸들러 | 74개 |
| JPA 엔티티 | 18개 |
| 백엔드 테스트 | 2개 |
| 운영 컨테이너 | 4개 |
| 주요 도메인 | 인증, 채팅, 배틀, 지식 카드, Q&A, 사용자, 토큰 사용량 |

## 주요 기능

### 1. AI 채팅

- OpenAI API를 이용한 질문·답변과 세션별 대화 기록 관리
- 이전 메시지를 활용한 후속 질문 문맥 유지
- 완성된 응답을 프론트엔드 타이핑 애니메이션으로 순차 표시
- 사용자별 OpenAI API Key 등록과 토큰 사용량 집계

### 2. 퀴즈 배틀

- 학습 내용과 Q&A 데이터를 기반으로 GPT 퀴즈 생성
- 객관식·주관식 문제와 난이도별 평가
- 정답 여부와 풀이 시간을 반영한 점수 계산
- 배틀 결과와 사용자별 기록 조회

### 3. 지식 카드

- 채팅과 Q&A 내용을 지식 카드로 변환
- 관련 개념과 출처를 포함한 카드 관리
- React Flow 기반 지식 관계 시각화
- 좋아요와 기여 내역 관리

### 4. Q&A 커뮤니티

- 질문·답변 작성, 수정, 삭제와 답변 채택
- 태그 생성, 검색, 투표 기능
- React Flow 기반 질문 관계 그래프
- 마크다운과 코드 블록 표시

### 5. 인증과 사용자 관리

- Spring Security 기반 JWT 인증·인가
- Refresh Token을 HttpOnly 쿠키로 관리
- Redis TTL 기반 이메일 인증
- 사용자 프로필, 아바타, API Key 관리

## 기술 스택

### Backend

`Java 21` `Spring Boot 3.5.9` `Spring Security` `Spring Data JPA` `MapStruct` `OpenAI Java SDK` `JJWT`

### Frontend

`React 19` `Vite` `Zustand` `Axios` `React Router` `React Flow` `Recharts` `Framer Motion`

### Database & Infrastructure

`PostgreSQL` `Redis` `Docker Compose` `Nginx` `GitHub Actions` `Cloudflare Tunnel`

## 주요 개발 이슈

### 1. 채팅 문맥과 OpenAI 요청 관리

채팅 세션과 메시지를 분리해 대화 이력을 저장하고, 후속 질문 시 이전 메시지를 함께 구성해 OpenAI API에 전달했습니다. 사용자 API Key와 공용 API Key 사용을 구분하고 요청별 토큰 사용량을 기록하도록 구성했습니다.

### 2. 사용자 API Key 보호

사용자가 등록한 OpenAI API Key를 평문으로 저장하지 않도록 암호화 계층을 분리했습니다. API Key 암호화에 필요한 Key와 Salt는 환경변수로 주입하고 응답에서는 원문을 노출하지 않도록 구성했습니다.

### 3. 인증 상태와 임시 데이터 분리

사용자와 서비스 데이터는 PostgreSQL에 저장하고, 만료가 필요한 이메일 인증 데이터와 그래프 위치 데이터는 Redis로 분리했습니다. Access Token과 Refresh Token의 역할을 나누고 Refresh Token은 HttpOnly 쿠키로 전달했습니다.

### 4. 복합 학습 도메인 분리

채팅, 배틀, 지식 카드, Q&A가 서로 참조하면서도 개별적으로 변경될 수 있도록 패키지와 서비스를 도메인별로 나눴습니다. 배틀 조회·명령·평가 로직과 Q&A 검색·권한·투표 로직을 역할별 서비스로 분리했습니다.

### 5. 셀프 호스팅 배포

Ubuntu VM에서 Nginx, Spring Boot, PostgreSQL, Redis를 Docker Compose로 통합 운영했습니다. 공용 Nginx와 Cloudflare Tunnel을 통해 애플리케이션 컨테이너의 호스트 포트 직접 노출을 줄였습니다.

## 시작하기

### 사전 요구사항

- Java 21
- Node.js 18 이상
- Docker와 Docker Compose
- OpenAI API Key

### 환경변수 설정

루트의 `.env.example`을 복사해 `.env`를 만들고 실제 값을 입력합니다.

```bash
cp .env.example .env
```

주요 환경변수:

```text
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
AIMIX_JWT_SECRET_KEY
AIMIX_JWT_ISSUER
AI_MIX_GMAIL_APP_PASSWORD
API_KEY_ENCRYPTION_KEY
API_KEY_ENCRYPTION_SALT
OPENAI_API_KEY
```

실제 비밀값이 포함된 `.env` 파일은 저장소에 커밋하지 않습니다.

### 로컬 개발 실행

1. Backend

   ```bash
   cd backend/ai-mix-api
   ./gradlew bootRun
   ```

2. Frontend

   ```bash
   cd frontend/ai-mix
   npm install
   npm run dev
   ```

### Docker Compose 실행

```bash
docker compose up -d --build
```

기본 `.env.example` 기준 진입 주소는 `http://localhost:28080`입니다.

실행 구성:

- `ai-mix-prod-nginx`
- `ai-mix-prod-backend`
- `ai-mix-prod-postgres`
- `ai-mix-prod-redis`

## 운영 배포

### 배포 환경

- Domain: `https://ai-mix.adam9e96.dev`
- Server: `portfolio-server`
- Deploy path: `/srv/docker/ai-mix`
- Public access: Cloudflare Tunnel
- Reverse proxy: `/srv/docker/proxy` 공통 Nginx
- App runtime: Docker Compose

### 운영 구조

```text
Cloudflare Tunnel
  ai-mix.adam9e96.dev
    -> portfolio-proxy-nginx
      -> web Docker network
        -> aimix-nginx
          -> React static files
          -> aimix-backend
          -> uploads volume

aimix-backend
  -> aimix-postgres
  -> aimix-redis
  -> OpenAI API
```

운영 서버에서는 `docker-compose.prod.yml`을 사용합니다. `aimix-nginx`는 호스트 포트를 공개하지 않고 외부 `web` Docker 네트워크를 통해 공용 Nginx와 연결됩니다.

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### GitHub Actions 배포

`.github/workflows/deploy-prod.yml`은 `main` 브랜치 push 또는 수동 실행 시 동작합니다.

```text
test-backend
  -> Gradle test
deploy
  -> SSH 접속
  -> /srv/docker/ai-mix 에서 main pull
  -> docker-compose.prod.yml 기준 재빌드·재기동
  -> 내부 health check
  -> 외부 health check
```

필요한 GitHub Actions Secrets:

```text
VM_HOST
VM_PORT
VM_USER
VM_SSH_KEY
```


## Contact

- Developer: adam9e96
- Email: `adam9e96@gmail.com`
- GitHub: [https://github.com/adam9e96/ai-mix](https://github.com/adam9e96/ai-mix)
