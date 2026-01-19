# Backend Interview Questions & Answers (AI-Mix Project)

이 문서는 `ai-mix` 프로젝트의 백엔드 기술 스택과 라이브러리 선정 이유에 대한 예상 면접 질문 및 답변을 정리한 것입니다.

---

## 1. Java & Spring Boot

### Q: 왜 Java 21을 선택했나요?

**A:** Java 21은 최신 **LTS(Long Term Support)** 버전으로, 장기간 안정적인 지원을 받을 수 있습니다. 가장 큰 이유는 **Virtual Threads(가상 스레드)**의 정식 지원입니다. 기존 플랫폼 스레드(OS 스레드 1:1 매핑) 방식은 I/O 블로킹 시 리소스 낭비가 심했으나, 가상 스레드를 사용하면 적은 리소스로도 높은 처리량(High Throughput)을 낼 수 있어, 대용량 트래픽 처리에 유리하기 때문입니다.

### Q: Spring Boot 3.x 버전을 사용한 이유는 무엇인가요?

**A:** Spring Boot 3는 **Jakarta EE 10**을 기반으로 하며, Java 17 이상을 강제합니다. 이는 최신 자바 기능을 적극적으로 활용할 수 있게 해줍니다. 또한 **GraalVM Native Image** 지원이 대폭 강화되어, 필요 시 스타트업 속도가 빠르고 메모리를 적게 차지하는 네이티브 이미지로 빌드할 수 있는 확장성을 고려했습니다.

---

## 2. Dependencies & Libraries

### Q: `com.github.f4b6a3:uuid-creator` (UUID v7) 라이브러리는 왜 사용했나요?

**A:** 기본 `java.util.UUID`는 **v4(완전 랜덤)** 방식을 사용합니다. 랜덤 UUID를 DB의 **Primary Key(PK)**로 사용하면, INSERT 시 인덱스 정렬을 위해 B-Tree 재정렬이 빈번하게 발생하여(Page Fragmentation) 성능이 저하됩니다.
반면 **UUID v7**은 **시간 순서(Timestamp)**가 포함되어 있어, 생성 순서대로 정렬되므로 DB 인덱싱 성능이 Auto Increment ID와 유사할 정도로 빠르면서도, 분산 환경에서의 유니크함을 보장하기 때문에 도입했습니다.

### Q: ModelMapper나 오버로딩 대신 `MapStruct`를 쓴 이유는?

**A:** `ModelMapper` 같은 라이브러리는 **리플렉션(Reflection)**을 사용하여 런타임에 매핑하므로 성능 오버헤드가 있고, 디버깅이 어렵습니다.
반면 **MapStruct**는 **컴파일 시점**에 매핑 코드를 구현체로 생성해주기 때문에(Annotation Processor), 순수 자바 코드를 직접 짠 것과 동일한 압도적인 런타임 성능을 가집니다. 또한 컴파일 타임에 필드 불일치 등의 오류를 미리 잡을 수 있어 타입 안정성이 높습니다.

### Q: `jjwt` (JWT Library) 0.13.0 최신 버전을 쓴 이유는?

**A:** Spring Security의 세션 기반 인증 대신 **Stateless(무상태)** 아키텍처를 지향하기 위해 JWT를 도입했습니다. 최신 `0.13.0` 버전을 사용한 이유는, 이전 버전에서 Deprecated된 서명 알고리즘 관련 API들이 정리되고 보안성이 강화되었기 때문입니다. 이를 통해 확장성 있는 인증/인가 시스템을 구축했습니다.

### Q: `spring-boot-starter-webflux` 의존성이 있던데, MVC 프로젝트 아닌가요?

**A:** 기본적으로는 `Spring MVC` 기반의 동기식 애플리케이션이지만, 외부 AI API(OpenAI 등)를 호출할 때 **WebClient**를 사용하기 위해 추가했습니다. 기존 `RestTemplate`은 유지보수 모드(Deprecated 예정)에 들어갔고, `WebClient`는 비동기/논블로킹 IO를 지원하여 외부 API 응답을 기다리는 동안 스레드를 점유하지 않아 효율적이기 때문입니다.

---

## 3. Architecture & Infrastructure

### Q: PostgreSQL을 선택한 이유는?

**A:** MySQL보다 **복잡한 쿼리 처리**와 **JSON 데이터 타입** 지원(JSONB)이 뛰어납니다. AI 프로젝트 특성상 프롬프트 설정이나 로그 등 비정형 데이터를 저장해야 할 일이 생길 수 있는데, PostgreSQL의 JSONB 인덱싱 기능을 활용하면 NoSQL(MongoDB 등)을 별도로 도입하지 않고도 유연하게 대처할 수 있기 때문입니다.

### Q: Redis의 용도는 무엇인가요?

**A:**

1.  **RefreshToken 저장**: DB에 저장하는 것보다 조회 속도가 빠르고, TTL(Time To Live) 기능을 통해 만료된 토큰을 자동 삭제할 수 있어 관리가 용이합니다.
2.  **로그아웃(BlackList) 처리**: JWT는 발급되면 만료될 때까지 유효하므로, 로그아웃 시 해당 토큰을 Redis 블랙리스트에 등록하여 접근을 차단하는 용도로 사용합니다.
3.  **이메일 인증 코드**: 3~5분의 짧은 유효시간을 가지는 임시 데이터를 관리하기에 인메모리 DB인 Redis가 최적입니다.

### Q: `MailConfig`를 직접 Java Config로 명시한 이유는? (최근 경험)

**A:** Spring Boot의 `application.yml` 자동 설정(Auto Configuration)은 편리하지만, 환경 변수가 누락되었을 때 디버깅이 어렵거나, 런타임에 예상치 못한 동작을 할 수 있습니다.
최근 **배포 환경 변수 이슈**를 겪으면서, `java-mail-sender` 빈을 직접 등록하고 `@Value`에 기본값을 설정하거나 로깅을 추가하는 등 **구성의 투명성과 견고함(Robustness)**을 확보하기 위해 명시적인 설정 클래스로 변경했습니다.

---

## 4. Security & Encryption

### Q: 비밀번호는 어떻게 저장하나요? (BCrypt vs AES)

**A:** 사용자 비밀번호는 **단방향 해시** 알고리즘인 **BCrypt**를 사용하여 저장합니다. 복호화가 불가능하므로 DB가 탈취되어도 비밀번호 원문을 알 수 없어 안전합니다.
반면, 사용자가 입력한 **OpenAI API Key** 등은 나중에 시스템이 복호화해서 사용해야 하므로, **AES-256 (GCM Mode)** 같은 **양방향 암호화** 방식을 사용하여 저장합니다. 이 프로젝트에서는 `ApiKeyEncryptionService`를 통해 이를 구현했습니다.

### Q: REST API에서 CSRF 설정을 끈 이유는? (`http.csrf(AbstractHttpConfigurer::disable)`)

**A:** CSRF(Cross-Site Request Forgery)는 주로 **세션(Cookie) 기반 인증**에서 발생하는 공격입니다. 이 프로젝트는 **JWT 토큰**을 Authorization 헤더에 담아 보내는 방식(Stateless)을 사용하므로, 브라우저가 자동으로 첨부하는 쿠키 의존성이 없어 CSRF 공격으로부터 안전하기 때문에 설정을 비활성화했습니다.

### Q: CORS 설정은 어떻게 했나요?

**A:** 프론트엔드와 백엔드 도메인이 다를 경우 브라우저 보안 정책(SOP)에 의해 요청이 차단됩니다. 이를 해결하기 위해 `WebConfig`에서 `addCorsMappings`를 통해 특정 오리진(프론트엔드 주소)을 허용하도록 설정했습니다. 보안을 위해 `*`(전체 허용)보다는 구체적인 도메인을 명시하는 것을 지향했습니다.

---

## 5. Performance & Concurrency

### Q: OSIV(Open Session In View)를 끈 이유는? (`jpa.open-in-view: false`)

**A:** OSIV가 켜져 있으면 뷰 렌더링 시점까지 DB 커넥션을 붙잡고 있어, 트래픽이 몰릴 경우 커넥션 풀이 고갈될 위험이 있습니다.
API 서버에서는 응답 데이터(JSON)를 만들고 나면 영속성 컨텍스트가 필요 없으므로, 트랜잭션이 끝나는 Service 계층에서 커넥션을 반환하도록 OSIV를 `false`로 설정하여 **DB 커넥션 효율성**을 높였습니다. 이로 인해 `LazyInitializationException`이 발생할 수 있지만, 이는 Fetch Join이나 DTO 변환을 통해 해결했습니다.

### Q: `@Async`는 어디에 사용했나요? (`AsyncConfig`)

**A:** 이메일 발송과 같이 응답 시간이 오래 걸리는 작업은 클라이언트를 기다리게 하면 안 됩니다. 이를 위해 `AsyncConfig`로 스레드 풀을 설정하고, `EmailService` 메서드에 `@Async`를 붙여 **비동기**로 처리했습니다. 이를 통해 주 실행 스레드(Tomcat 쓰레드)가 블로킹되지 않고 즉시 응답을 반환하여 사용자 경험을 개선했습니다.

---

## 6. Development Patterns

### Q: 예외 처리는 어떻게 중앙화했나요? (`GlobalExceptionHandler`)

**A:** 개별 컨트롤러에서 try-catch를 반복하는 대신, `@RestControllerAdvice`를 사용하여 전역적으로 예외를 잡습니다.
Spring 6(Boot 3)부터 도입된 **RFC 7807 (Problem Details for HTTP APIs)** 표준 스펙을 준수하기 위해 `ProblemDetail` 객체를 반환하거나, 커스텀 에러 응답 객체(`ErrorResponse`)를 정의하여 클라이언트에게 일관된 에러 형식을 제공합니다.

### Q: DTO(Data Transfer Object)를 사용하는 이유는?

**A:** JPA Entity를 그대로 컨트롤러에서 반환하면, 민감한 정보(비밀번호 등)가 노출되거나 순환 참조 문제가 발생할 수 있습니다. 또한 DB 스키마 변경이 API 스펙에 영향을 주게 됩니다.
따라서 요청(Request)과 응답(Response)에 맞는 별도의 DTO 클래스를 만들고, `MapStruct`를 통해 엔티티와 변환하여 **계층 간의 의존성을 분리**했습니다.
