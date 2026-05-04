-- ============================================
-- AI-MIX Database Schema Initialization
-- Runs automatically on first docker-compose up
-- ============================================

-- ENUM types
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE question_type_enum AS ENUM ('SUBJECTIVE', 'OBJECTIVE');
CREATE TYPE answer_type_enum AS ENUM ('USER', 'AI');
CREATE TYPE vote_type_enum AS ENUM ('UP', 'DOWN');
CREATE TYPE source_type_enum AS ENUM ('QNA', 'CHAT', 'BATTLE');
CREATE TYPE relationship_type_enum AS ENUM ('PREREQUISITE', 'RELATED', 'PART_OF');
CREATE TYPE contribution_type_enum AS ENUM ('CREATE', 'UPDATE', 'MISTAKE_REPORT');
CREATE TYPE battle_source_type_enum AS ENUM ('CHAT', 'QNA');
CREATE TYPE message_sender_enum AS ENUM ('USER', 'AI');
CREATE TYPE gpt_usage_type_enum AS ENUM ('CHAT', 'QNA', 'BATTLE_QUESTION', 'BATTLE_SCORING', 'KNOWLEDGE_CARD');

-- users
CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    nickname      VARCHAR(50)  NOT NULL UNIQUE,
    birth_date    DATE,
    is_agreed     BOOLEAN   DEFAULT FALSE,
    role          user_role DEFAULT 'USER'::user_role,
    created_at    TIMESTAMP,
    last_login_at TIMESTAMP
);

-- user_profile
CREATE TABLE user_profile (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER NOT NULL UNIQUE REFERENCES users ON DELETE CASCADE,
    bio             TEXT,
    avatar_url      VARCHAR(255),
    settings        JSONB,
    graph_positions JSONB DEFAULT '{}'::jsonb,
    openai_api_key  VARCHAR(500)
);

-- chat_session
CREATE TABLE chat_session (
    id         UUID         NOT NULL PRIMARY KEY,
    user_id    INTEGER      NOT NULL REFERENCES users ON DELETE CASCADE,
    title      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);
CREATE INDEX idx_chat_session_user_id    ON chat_session (user_id);
CREATE INDEX idx_chat_session_created_at ON chat_session (created_at DESC);

-- chat_message
CREATE TABLE chat_message (
    id         UUID                NOT NULL PRIMARY KEY,
    session_id UUID                NOT NULL REFERENCES chat_session ON DELETE CASCADE,
    sender     message_sender_enum NOT NULL,
    message    TEXT                NOT NULL,
    created_at TIMESTAMP           NOT NULL
);
CREATE INDEX idx_chat_message_session_id ON chat_message (session_id);
CREATE INDEX idx_chat_message_created_at ON chat_message (created_at DESC);

-- battle
CREATE TABLE battle (
    id              UUID                    NOT NULL PRIMARY KEY,
    user_id         INTEGER                 NOT NULL REFERENCES users ON DELETE CASCADE,
    source_type     battle_source_type_enum NOT NULL,
    source_id       UUID,
    level           VARCHAR(10),
    total_questions INTEGER DEFAULT 0       NOT NULL,
    created_at      TIMESTAMP               NOT NULL
);
CREATE INDEX idx_battle_user_created_at ON battle (user_id ASC, created_at DESC);

-- battle_question
CREATE TABLE battle_question (
    id             UUID NOT NULL PRIMARY KEY,
    battle_id      UUID NOT NULL REFERENCES battle ON DELETE CASCADE,
    question_text  TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    order_no       INTEGER NOT NULL,
    question_type  question_type_enum DEFAULT 'SUBJECTIVE'::question_type_enum NOT NULL,
    difficulty     VARCHAR(20),
    choices        TEXT
);
CREATE INDEX idx_battle_question_battle_order ON battle_question (battle_id, order_no);

-- battle_answer
CREATE TABLE battle_answer (
    id          UUID              NOT NULL PRIMARY KEY,
    battle_id   UUID              NOT NULL REFERENCES battle ON DELETE CASCADE,
    question_id UUID              NOT NULL REFERENCES battle_question ON DELETE CASCADE,
    user_answer TEXT              NOT NULL,
    score       INTEGER DEFAULT 0 NOT NULL,
    feedback    TEXT,
    created_at  TIMESTAMP         NOT NULL
);
CREATE INDEX idx_battle_answer_battle_id   ON battle_answer (battle_id);
CREATE INDEX idx_battle_answer_question_id ON battle_answer (question_id);

-- qna_question
CREATE TABLE qna_question (
    id                 UUID         NOT NULL PRIMARY KEY,
    user_id            INTEGER      REFERENCES users ON DELETE SET NULL,
    title              VARCHAR(255) NOT NULL,
    body               TEXT         NOT NULL,
    is_anonymous       BOOLEAN   DEFAULT FALSE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    view_count         BIGINT    DEFAULT 0,
    anonymous_password VARCHAR(255),
    CONSTRAINT check_anonymous_password
        CHECK ((is_anonymous = FALSE) OR ((is_anonymous = TRUE) AND (anonymous_password IS NOT NULL)))
);
CREATE INDEX idx_qna_question_user_id    ON qna_question (user_id);
CREATE INDEX idx_qna_question_created_at ON qna_question (created_at DESC);
CREATE INDEX idx_qna_question_view_count ON qna_question (view_count DESC);

-- qna_answer
CREATE TABLE qna_answer (
    id          UUID                    NOT NULL PRIMARY KEY,
    question_id UUID                    NOT NULL REFERENCES qna_question ON DELETE CASCADE,
    user_id     INTEGER                 REFERENCES users ON DELETE SET NULL,
    answer_type answer_type_enum        NOT NULL,
    body        TEXT                    NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score       INTEGER   DEFAULT 0,
    is_accepted BOOLEAN   DEFAULT FALSE NOT NULL
);
CREATE INDEX idx_qna_answer_question_id ON qna_answer (question_id);
CREATE INDEX idx_qna_answer_user_id     ON qna_answer (user_id);
CREATE INDEX idx_qna_answer_created_at  ON qna_answer (created_at DESC);
CREATE INDEX idx_qna_answer_score       ON qna_answer (score DESC);
CREATE INDEX idx_qna_answer_is_accepted ON qna_answer (is_accepted) WHERE (is_accepted = TRUE);

-- qna_tag
CREATE TABLE qna_tag (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- qna_question_tag
CREATE TABLE qna_question_tag (
    question_id UUID    NOT NULL REFERENCES qna_question ON DELETE CASCADE,
    tag_id      INTEGER NOT NULL REFERENCES qna_tag ON DELETE CASCADE,
    PRIMARY KEY (question_id, tag_id)
);
CREATE INDEX idx_qna_question_tag_tag_id ON qna_question_tag (tag_id);

-- knowledge_card
CREATE TABLE knowledge_card (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL UNIQUE,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    one_line_definition TEXT         NOT NULL,
    core_points         JSONB     DEFAULT '[]'::jsonb NOT NULL,
    common_mistakes     JSONB     DEFAULT '[]'::jsonb,
    related_concepts    JSONB     DEFAULT '[]'::jsonb,
    source_type         source_type_enum,
    source_id           UUID,
    contributor_id      BIGINT    REFERENCES users ON DELETE SET NULL,
    view_count          BIGINT    DEFAULT 0 NOT NULL,
    upvote_count        BIGINT    DEFAULT 0 NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_published        BOOLEAN   DEFAULT TRUE NOT NULL
);
CREATE INDEX idx_knowledge_card_slug             ON knowledge_card (slug);
CREATE INDEX idx_knowledge_card_title            ON knowledge_card (title);
CREATE INDEX idx_knowledge_card_contributor      ON knowledge_card (contributor_id);
CREATE INDEX idx_knowledge_card_published        ON knowledge_card (is_published);
CREATE INDEX idx_knowledge_card_updated_at       ON knowledge_card (updated_at DESC);
CREATE INDEX idx_knowledge_card_upvote_count     ON knowledge_card (upvote_count DESC);
CREATE INDEX idx_knowledge_card_view_count       ON knowledge_card (view_count DESC);
CREATE INDEX idx_knowledge_card_core_points      ON knowledge_card USING GIN (core_points);
CREATE INDEX idx_knowledge_card_related_concepts ON knowledge_card USING GIN (related_concepts);
CREATE INDEX idx_knowledge_card_source           ON knowledge_card (source_type, source_id);

-- knowledge_map
CREATE TABLE knowledge_map (
    id                BIGSERIAL PRIMARY KEY,
    from_card_id      BIGINT NOT NULL REFERENCES knowledge_card ON DELETE CASCADE,
    to_card_id        BIGINT NOT NULL REFERENCES knowledge_card ON DELETE CASCADE,
    relationship_type relationship_type_enum NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_knowledge_map_from_to UNIQUE (from_card_id, to_card_id)
);
CREATE INDEX idx_knowledge_map_from_card         ON knowledge_map (from_card_id);
CREATE INDEX idx_knowledge_map_to_card           ON knowledge_map (to_card_id);
CREATE INDEX idx_knowledge_map_relationship_type ON knowledge_map (relationship_type);

-- card_contribution
CREATE TABLE card_contribution (
    id                BIGSERIAL PRIMARY KEY,
    card_id           BIGINT NOT NULL REFERENCES knowledge_card ON DELETE CASCADE,
    contributor_id    BIGINT NOT NULL REFERENCES users ON DELETE CASCADE,
    contribution_type contribution_type_enum NOT NULL,
    description       TEXT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_card_contribution_card_id          ON card_contribution (card_id);
CREATE INDEX idx_card_contribution_contributor       ON card_contribution (contributor_id);
CREATE INDEX idx_card_contribution_created_at        ON card_contribution (created_at DESC);
CREATE INDEX idx_card_contribution_type              ON card_contribution (contribution_type);
CREATE INDEX idx_card_contribution_contributor_type  ON card_contribution (contributor_id, contribution_type);

-- gpt_token_usage
CREATE TABLE gpt_token_usage (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users ON DELETE CASCADE,
    usage_date        DATE NOT NULL,
    usage_type        gpt_usage_type_enum NOT NULL,
    model             VARCHAR(50),
    prompt_tokens     INTEGER   DEFAULT 0 NOT NULL,
    completion_tokens INTEGER   DEFAULT 0 NOT NULL,
    total_tokens      INTEGER   DEFAULT 0 NOT NULL,
    request_count     INTEGER   DEFAULT 1 NOT NULL,
    is_user_api_key   BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_gpt_token_usage_user_date_type_key UNIQUE (user_id, usage_date, usage_type, is_user_api_key)
);
CREATE INDEX idx_user_date           ON gpt_token_usage (user_id, usage_date);
CREATE INDEX idx_usage_date          ON gpt_token_usage (usage_date);
CREATE INDEX idx_user                ON gpt_token_usage (user_id);
CREATE INDEX idx_user_date_type_key  ON gpt_token_usage (user_id, usage_date, usage_type, is_user_api_key);

-- knowledge_card_like
CREATE TABLE knowledge_card_like (
    id      BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL REFERENCES knowledge_card ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users ON DELETE CASCADE,
    UNIQUE (card_id, user_id)
);

-- qna_answer_vote
CREATE TABLE qna_answer_vote (
    id        BIGSERIAL PRIMARY KEY,
    answer_id UUID   NOT NULL REFERENCES qna_answer ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users ON DELETE CASCADE,
    vote_type vote_type_enum NOT NULL,
    CONSTRAINT uk_qna_answer_vote UNIQUE (answer_id, user_id)
);
CREATE INDEX idx_qna_answer_vote_answer      ON qna_answer_vote (answer_id);
CREATE INDEX idx_qna_answer_vote_user        ON qna_answer_vote (user_id);
CREATE INDEX idx_qna_answer_vote_answer_user ON qna_answer_vote (answer_id, user_id);
