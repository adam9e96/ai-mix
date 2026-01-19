create type user_role as enum ('USER', 'ADMIN');

create type question_type_enum as enum ('SUBJECTIVE', 'OBJECTIVE');

create type answer_type_enum as enum ('USER', 'AI');

create type vote_type_enum as enum ('UP', 'DOWN');

create type source_type_enum as enum ('QNA', 'CHAT', 'BATTLE');

create type relationship_type_enum as enum ('PREREQUISITE', 'RELATED', 'PART_OF');

create type contribution_type_enum as enum ('CREATE', 'UPDATE', 'MISTAKE_REPORT');

create type battle_source_type_enum as enum ('CHAT', 'QNA');

create type message_sender_enum as enum ('USER', 'AI');

create type gpt_usage_type_enum as enum ('CHAT', 'QNA', 'BATTLE_QUESTION', 'BATTLE_SCORING', 'KNOWLEDGE_CARD');

create table users
(
    id            serial
        primary key,
    email         varchar(255) not null
        unique,
    password      varchar(255) not null,
    nickname      varchar(50)  not null
        unique,
    birth_date    date,
    is_agreed     boolean   default false,
    role          user_role default 'USER'::user_role,
    created_at    timestamp,
    last_login_at timestamp
);

create table user_profile
(
    id              serial
        primary key,
    user_id         integer not null
        constraint user_profile_user_id_unique
            unique
        references users
            on delete cascade,
    bio             text,
    avatar_url      varchar(255),
    settings        jsonb,
    graph_positions jsonb default '{}'::jsonb
);

create table chat_session
(
    id         uuid         not null
        primary key,
    user_id    integer      not null
        references users
            on delete cascade,
    title      varchar(255) not null,
    created_at timestamp    not null
);

create index idx_chat_session_user_id
    on chat_session (user_id);

create index idx_chat_session_created_at
    on chat_session (created_at desc);

create table chat_message
(
    id         uuid                not null
        primary key,
    session_id uuid                not null
        references chat_session
            on delete cascade,
    sender     message_sender_enum not null,
    message    text                not null,
    created_at timestamp           not null
);

create index idx_chat_message_session_id
    on chat_message (session_id);

create index idx_chat_message_created_at
    on chat_message (created_at desc);

create table battle
(
    id              uuid                    not null
        primary key,
    user_id         integer                 not null
        references users
            on delete cascade,
    source_type     battle_source_type_enum not null,
    source_id       uuid,
    level           varchar(10),
    total_questions integer default 0       not null,
    created_at      timestamp               not null
);

create index idx_battle_user_created_at
    on battle (user_id asc, created_at desc);

create table battle_question
(
    id             uuid                                                        not null
        primary key,
    battle_id      uuid                                                        not null
        references battle
            on delete cascade,
    question_text  text                                                        not null,
    correct_answer text                                                        not null,
    order_no       integer                                                     not null,
    question_type  question_type_enum default 'SUBJECTIVE'::question_type_enum not null,
    difficulty     varchar(20),
    choices        text
);

create index idx_battle_question_battle_order
    on battle_question (battle_id, order_no);

create table battle_answer
(
    id          uuid              not null
        primary key,
    battle_id   uuid              not null
        references battle
            on delete cascade,
    question_id uuid              not null
        references battle_question
            on delete cascade,
    user_answer text              not null,
    score       integer default 0 not null,
    feedback    text,
    created_at  timestamp         not null
);

create index idx_battle_answer_battle_id
    on battle_answer (battle_id);

create index idx_battle_answer_question_id
    on battle_answer (question_id);

create table qna_question
(
    id                 uuid         not null
        primary key,
    user_id            integer
                                    references users
                                        on delete set null,
    title              varchar(255) not null,
    body               text         not null,
    is_anonymous       boolean   default false,
    created_at         timestamp default CURRENT_TIMESTAMP,
    view_count         bigint    default 0,
    anonymous_password varchar(255),
    constraint check_anonymous_password
        check ((is_anonymous = false) OR ((is_anonymous = true) AND (anonymous_password IS NOT NULL)))
);

create index idx_qna_question_user_id
    on qna_question (user_id);

create index idx_qna_question_created_at
    on qna_question (created_at desc);

create index idx_qna_question_view_count
    on qna_question (view_count desc);

create table qna_answer
(
    id          uuid                    not null
        primary key,
    question_id uuid                    not null
        references qna_question
            on delete cascade,
    user_id     integer
                                        references users
                                            on delete set null,
    answer_type answer_type_enum        not null,
    body        text                    not null,
    created_at  timestamp default CURRENT_TIMESTAMP,
    score       integer   default 0,
    is_accepted boolean   default false not null
);

create index idx_qna_answer_question_id
    on qna_answer (question_id);

create index idx_qna_answer_user_id
    on qna_answer (user_id);

create index idx_qna_answer_created_at
    on qna_answer (created_at desc);

create index idx_qna_answer_score
    on qna_answer (score desc);

create index idx_qna_answer_is_accepted
    on qna_answer (is_accepted)
    where (is_accepted = true);

create table qna_tag
(
    id   serial
        primary key,
    name varchar(50) not null
        unique
);

create table qna_question_tag
(
    question_id uuid    not null
        references qna_question
            on delete cascade,
    tag_id      integer not null
        references qna_tag
            on delete cascade,
    primary key (question_id, tag_id)
);

create index idx_qna_question_tag_tag_id
    on qna_question_tag (tag_id);

create table knowledge_card
(
    id                  bigserial
        primary key,
    title               varchar(255)                        not null
        unique,
    slug                varchar(255)                        not null
        unique,
    one_line_definition text                                not null,
    core_points         jsonb     default '[]'::jsonb       not null,
    common_mistakes     jsonb     default '[]'::jsonb,
    related_concepts    jsonb     default '[]'::jsonb,
    source_type         source_type_enum,
    source_id           uuid,
    contributor_id      bigint
                                                            references users
                                                                on delete set null,
    view_count          bigint    default 0                 not null,
    upvote_count        bigint    default 0                 not null,
    created_at          timestamp default CURRENT_TIMESTAMP not null,
    updated_at          timestamp default CURRENT_TIMESTAMP not null,
    is_published        boolean   default true              not null
);

comment on table knowledge_card is '개념 카드 테이블 - 지식백과의 핵심 개념을 구조화된 카드 형태로 관리';

comment on column knowledge_card.id is '기본 키 (BIGSERIAL)';

comment on column knowledge_card.title is '카드 제목 (유니크)';

comment on column knowledge_card.slug is 'URL 친화적 슬러그 (유니크)';

comment on column knowledge_card.one_line_definition is '한 줄 정의';

comment on column knowledge_card.core_points is '핵심 포인트 (JSON 배열) 예: ["Stateless", "Resource 중심"]';

comment on column knowledge_card.common_mistakes is '자주 틀리는 오해 (JSON 배열) 예: ["REST = JSON ❌"]';

comment on column knowledge_card.related_concepts is '관련 개념 ID 목록 (JSON 배열) 예: [2, 5, 8]';

comment on column knowledge_card.source_type is '출처 타입: QNA, CHAT, BATTLE';

comment on column knowledge_card.source_id is '출처 ID (UUID) - QNA 질문 ID, CHAT 세션 ID, BATTLE ID 등';

comment on column knowledge_card.contributor_id is '기여자 ID (카드를 생성한 사용자)';

comment on column knowledge_card.view_count is '조회수';

comment on column knowledge_card.upvote_count is '추천 수';

comment on column knowledge_card.created_at is '생성 시각 (DEFAULT CURRENT_TIMESTAMP)';

comment on column knowledge_card.updated_at is '수정 시각 (DEFAULT CURRENT_TIMESTAMP, 애플리케이션 레벨에서 @PreUpdate로 업데이트)';

comment on column knowledge_card.is_published is '공개 여부';

create index idx_knowledge_card_slug
    on knowledge_card (slug);

create index idx_knowledge_card_title
    on knowledge_card (title);

create index idx_knowledge_card_contributor
    on knowledge_card (contributor_id);

create index idx_knowledge_card_published
    on knowledge_card (is_published);

create index idx_knowledge_card_updated_at
    on knowledge_card (updated_at desc);

create index idx_knowledge_card_upvote_count
    on knowledge_card (upvote_count desc);

create index idx_knowledge_card_view_count
    on knowledge_card (view_count desc);

create index idx_knowledge_card_core_points
    on knowledge_card using gin (core_points);

create index idx_knowledge_card_related_concepts
    on knowledge_card using gin (related_concepts);

create index idx_knowledge_card_source
    on knowledge_card (source_type, source_id);

create table knowledge_map
(
    id                bigserial
        primary key,
    from_card_id      bigint                              not null
        references knowledge_card
            on delete cascade,
    to_card_id        bigint                              not null
        references knowledge_card
            on delete cascade,
    relationship_type relationship_type_enum              not null,
    created_at        timestamp default CURRENT_TIMESTAMP not null,
    constraint uk_knowledge_map_from_to
        unique (from_card_id, to_card_id)
);

comment on table knowledge_map is '지식 맵 관계 테이블 - 개념 카드 간의 관계를 정의하여 지식 네트워크 구성';

comment on column knowledge_map.id is '기본 키 (BIGSERIAL)';

comment on column knowledge_map.from_card_id is '출발 카드 ID (관계의 시작점)';

comment on column knowledge_map.to_card_id is '도착 카드 ID (관계의 끝점)';

comment on column knowledge_map.relationship_type is '관계 타입: PREREQUISITE(선행 개념), RELATED(관련 개념), PART_OF(포함 관계)';

comment on column knowledge_map.created_at is '생성 시각 (DEFAULT CURRENT_TIMESTAMP)';

comment on constraint uk_knowledge_map_from_to on knowledge_map is '같은 두 카드 간의 관계는 하나만 존재 가능';

create index idx_knowledge_map_from_card
    on knowledge_map (from_card_id);

create index idx_knowledge_map_to_card
    on knowledge_map (to_card_id);

create index idx_knowledge_map_relationship_type
    on knowledge_map (relationship_type);

create table card_contribution
(
    id                bigserial
        primary key,
    card_id           bigint                              not null
        references knowledge_card
            on delete cascade,
    contributor_id    bigint                              not null
        references users
            on delete cascade,
    contribution_type contribution_type_enum              not null
        constraint card_contribution_contribution_type_check
            check ((contribution_type)::text = ANY
                   (ARRAY [('CREATE'::character varying)::text, ('UPDATE'::character varying)::text, ('MISTAKE_REPORT'::character varying)::text])),
    description       text,
    created_at        timestamp default CURRENT_TIMESTAMP not null
);

comment on table card_contribution is '카드 기여 이력 테이블 - 개념 카드에 대한 사용자 기여 내역을 기록 (칭호 시스템, 기여도 통계용)';

comment on column card_contribution.id is '기본 키 (BIGSERIAL)';

comment on column card_contribution.card_id is '관련 개념 카드 ID';

comment on column card_contribution.contributor_id is '기여자 ID';

comment on column card_contribution.contribution_type is '기여 타입: CREATE(카드 생성), UPDATE(카드 수정), MISTAKE_REPORT(오답 보고)';

comment on column card_contribution.description is '기여 설명';

comment on column card_contribution.created_at is '생성 시각 (DEFAULT CURRENT_TIMESTAMP)';

create index idx_card_contribution_card_id
    on card_contribution (card_id);

create index idx_card_contribution_contributor
    on card_contribution (contributor_id);

create index idx_card_contribution_created_at
    on card_contribution (created_at desc);

create index idx_card_contribution_type
    on card_contribution (contribution_type);

create index idx_card_contribution_contributor_type
    on card_contribution (contributor_id, contribution_type);

create table gpt_token_usage
(
    id                bigserial
        primary key,
    user_id           bigint                              not null
        constraint fk_gpt_token_usage_user
            references users
            on delete cascade,
    usage_date        date                                not null,
    usage_type        gpt_usage_type_enum                 not null,
    model             varchar(50),
    prompt_tokens     integer   default 0                 not null,
    completion_tokens integer   default 0                 not null,
    total_tokens      integer   default 0                 not null,
    request_count     integer   default 1                 not null,
    created_at        timestamp default CURRENT_TIMESTAMP not null,
    updated_at        timestamp default CURRENT_TIMESTAMP not null,
    constraint uk_gpt_token_usage_user_date_type
        unique (user_id, usage_date, usage_type)
);

comment on table gpt_token_usage is 'GPT 토큰 사용량 추적 테이블 - 사용자별, 날짜별, 사용 유형별 토큰 사용량 집계';

comment on column gpt_token_usage.id is '기본 키';

comment on column gpt_token_usage.user_id is '사용자 ID (FK: users.id)';

comment on column gpt_token_usage.usage_date is '사용 날짜 (날짜별 집계용)';

comment on column gpt_token_usage.usage_type is '사용 유형 (CHAT, QNA, BATTLE_QUESTION, BATTLE_SCORING, KNOWLEDGE_CARD)';

comment on column gpt_token_usage.model is '사용한 모델명 (예: gpt-4o-mini)';

comment on column gpt_token_usage.prompt_tokens is '프롬프트 토큰 수 (입력)';

comment on column gpt_token_usage.completion_tokens is '완료 토큰 수 (출력)';

comment on column gpt_token_usage.total_tokens is '총 토큰 수 (prompt_tokens + completion_tokens)';

comment on column gpt_token_usage.request_count is 'API 호출 횟수 (같은 날짜, 같은 유형의 호출 횟수)';

comment on column gpt_token_usage.created_at is '생성 시각';

comment on column gpt_token_usage.updated_at is '마지막 업데이트 시각';

create index idx_user_date
    on gpt_token_usage (user_id, usage_date);

create index idx_usage_date
    on gpt_token_usage (usage_date);

create index idx_user
    on gpt_token_usage (user_id);

create table knowledge_card_like
(
    id      bigserial
        primary key,
    card_id bigint not null
        references knowledge_card
            on delete cascade,
    user_id bigint not null
        references users
            on delete cascade,
    unique (card_id, user_id)
);

create table qna_answer_vote
(
    id        bigserial
        primary key,
    answer_id uuid           not null
        constraint fk_qna_answer_vote_answer
            references qna_answer
            on delete cascade,
    user_id   bigint         not null
        constraint fk_qna_answer_vote_user
            references users
            on delete cascade,
    vote_type vote_type_enum not null,
    constraint uk_qna_answer_vote
        unique (answer_id, user_id)
);

comment on table qna_answer_vote is 'QnA 답변 추천/비추천 테이블 - 사용자가 답변에 추천/비추천한 기록';

comment on column qna_answer_vote.id is '기본 키';

comment on column qna_answer_vote.answer_id is '답변 ID (FK: qna_answer.id)';

comment on column qna_answer_vote.user_id is '사용자 ID (FK: users.id)';

comment on column qna_answer_vote.vote_type is '추천 타입 (UP: 추천, DOWN: 비추천)';

create index idx_qna_answer_vote_answer
    on qna_answer_vote (answer_id);

create index idx_qna_answer_vote_user
    on qna_answer_vote (user_id);

create index idx_qna_answer_vote_answer_user
    on qna_answer_vote (answer_id, user_id);


ALTER TABLE user_profile
    ADD COLUMN openai_api_key VARCHAR(500);


ALTER TABLE gpt_token_usage
    ADD COLUMN is_user_api_key BOOLEAN DEFAULT false;

CREATE INDEX idx_user_date_type_key
    ON gpt_token_usage(user_id, usage_date, usage_type, is_user_api_key);

-- 기존 고유 제약 조건 삭제 (is_user_api_key 미포함)
ALTER TABLE gpt_token_usage
    DROP CONSTRAINT IF EXISTS uk_gpt_token_usage_user_date_type;

-- 새로운 고유 제약 조건 추가 (is_user_api_key 포함)
ALTER TABLE gpt_token_usage
    ADD CONSTRAINT uk_gpt_token_usage_user_date_type_key
        UNIQUE (user_id, usage_date, usage_type, is_user_api_key);

-- 기존 고유 제약 조건 삭제 (is_user_api_key 미포함)
ALTER TABLE gpt_token_usage
    DROP CONSTRAINT IF EXISTS uk_gpt_token_usage_user_date_type;

-- 새로운 고유 제약 조건 추가 (is_user_api_key 포함)
ALTER TABLE gpt_token_usage
    ADD CONSTRAINT uk_gpt_token_usage_user_date_type_key
        UNIQUE (user_id, usage_date, usage_type, is_user_api_key);