# AI-MIX

**AI 기반 지식 공유 및 대결 플랫폼**

## 📖 프로젝트 소개

**AI-MIX**는 AI와의 상호작용을 통해 지식을 습득하고, 이를 바탕으로 다른 사용자와 경쟁하며 성장하는 **학습 플랫폼**입니다.

사용자는 OpenAI를 이용한 AI 챗봇과 실시간으로 대화하며 궁금증을 해결하고, 획득한 지식을 **지식 카드(Knowledge Card)** 형태로 저장하여 자신만의 위키를 구축할 수 있습니다.

더 나아가 **배틀 모드(Battle Mode)** 를 통해 실시간 퀴즈 대결을 펼치며 학습 동기를 부여 받을 수 있습니다.

### 🎯 기획 의도

기존의 정적인 학습 방식에서 벗어나, AI를 활용한 **능동적인 지식 탐구**와 **경쟁 요소**를 결합하여 학습의 재미와 효율성을 극대화하고자 기획되었습니다.

---

## 주요 기능

### 1. AI 챗봇 (AI Chatbot)

- **실시간 문맥 인식 대화**: OpenAI API를 활용하여 사용자의 질문 의도를 파악하고 정확한 답변을 제공합니다.
- **스트리밍 답변**: SSE(Server-Sent Events)와 유사한 경험을 제공하여 AI의 답변을 자연스럽게 전달합니다.
- **이어서 대화**: 사용자가 이어서 질문을 할 수 있도록 이전 대화를 유지하면서 맥락을 유지한채로 대화할 수 있습니다.

### 2. 배틀 모드 (Battle Mode)

- **실시간 퀴즈 대결**: 대화 내용을 기반으로 적절한 퀴즈를 생성하여 실시간으로 대결을 진행할 수 있습니다.
- **점수 시스템**: 정답 여부와 소요 시간을 기반으로 점수를 산정하여 경쟁의 묘미를 살렸습니다.

### 3. 지식 카드 & 위키 (Knowledge Wiki)

- **지식 자산화**: 학습한 내용을 카드 형태로 저장하고 관리할 수 있습니다.
- **시각화**: React Flow를 활용하여 지식 간의 연결 관계를 시각적으로 탐색할 수 있습니다.

### 4. Q&A 커뮤니티

- **집단지성**: AI가 해결하지 못한 심화 질문을 커뮤니티에 공유하여 다각적인 답변을 얻을 수 있습니다.
- **마크다운 에디터**: 코드 블록, 이미지 등을 포함한 풍부한 텍스트 작성을 지원합니다.

---

## 기술 스택 (Tech Stack)

### Frontend

| Category          | Technology                                                                                                            | Description                                      |
| ----------------- | --------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------ |
| **Core**          | ![React](https://img.shields.io/badge/React_19-61DAFB?style=flat-square&logo=react&logoColor=black)                   | 최신 React 기능을 활용한 컴포넌트 기반 UI 개발   |
| **Build**         | ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)                         | 빠르고 가벼운 개발 환경 및 빌드 최적화           |
| **State**         | ![Zustand](https://img.shields.io/badge/Zustand-orange?style=flat-square)                                             | 직관적이고 경량화된 전역 상태 관리               |
| **Routing**       | ![React Router](https://img.shields.io/badge/React_Router-CA4245?style=flat-square&logo=react-router&logoColor=white) | SPA(Single Page Application) 라우팅 처리         |
| **UI/UX**         | ![Framer Motion](https://img.shields.io/badge/Framer_Motion-0055FF?style=flat-square&logo=framer&logoColor=white)     | 부드러운 화면 전환 및 인터랙티브 애니메이션 구현 |
| **Visualization** | ![React Flow](https://img.shields.io/badge/React_Flow-FF0072?style=flat-square)                                       | 지식 노드 관계 시각화                            |

### Backend

| Category      | Technology                                                                                                                    | Description                                     |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------- |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.9-6DB33F?style=flat-square&logo=springboot&logoColor=white)       | 안정적이고 확장 가능한 RESTful API 서버 구축    |
| **Language**  | ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white)                           | 최신 Java 기능을 활용한 고성능 백엔드 로직 구현 |
| **Data**      | ![JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)                     | 객체 지향적인 데이터베이스 조작 및 ORM          |
| **Security**  | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) | JWT 기반의 견고한 인증/인가 시스템              |
| **AI**        | ![OpenAI](https://img.shields.io/badge/OpenAI_Java_SDK-412991?style=flat-square&logo=openai&logoColor=white)                  | GPT 모델 연동을 위한 공식 SDK 활용              |
| **Utils**     | ![MapStruct](https://img.shields.io/badge/MapStruct-transparent?style=flat-square)                                            | 효율적인 DTO <-> Entity 매핑 처리               |

### Infrastructure & Database

| Category     | Technology                                                                                                      | Description                                 |
| ------------ | --------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| **Database** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white) | 신뢰성 높은 관계형 데이터베이스             |
| **Cache**    | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)                | 세션 관리 및 데이터 캐싱을 통한 성능 최적화 |

---

## 🚀 시작하기

### 사전 요구사항 (Prerequisites)

- **Java 21** 이상
- **Node.js 18** 이상
- **PostgreSQL 12** 이상
- **Redis 6** 이상
- **OpenAI API Key**

### 설치 및 실행

1. **Repository Clone**

   ```bash
   git clone https://github.com/your-username/ai-mix.git
   ```

2. **Backend Setup**

   ```bash
   cd backend
   ./gradlew bootRun
   ```

3. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
