--liquibase formatted sql

--changeset artem:0001-init-quiz-schema
--comment: Начальная схема квизов — агрегат, черновик, снапшот, карточки

CREATE TYPE visibility AS ENUM ('PUBLIC', 'PRIVATE');
CREATE TYPE card_type AS ENUM ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT_INPUT');

CREATE TABLE quizzes (
    id         UUID PRIMARY KEY,
    owner_id   UUID NOT NULL,
    visibility visibility NOT NULL DEFAULT 'PRIVATE'
);

CREATE INDEX idx_quizzes_owner_id ON quizzes (owner_id);

CREATE TABLE quiz_drafts (
    id          UUID PRIMARY KEY,
    quiz_id     UUID NOT NULL UNIQUE REFERENCES quizzes (id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE quiz_snapshots (
    id           UUID PRIMARY KEY,
    quiz_id      UUID NOT NULL UNIQUE REFERENCES quizzes (id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  VARCHAR(1000),
    created_at   TIMESTAMPTZ NOT NULL,
    modified_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE cards (
    id             UUID PRIMARY KEY,
    draft_id       UUID REFERENCES quiz_drafts (id)    ON DELETE CASCADE,
    snapshot_id    UUID REFERENCES quiz_snapshots (id) ON DELETE CASCADE,
    position       INT NOT NULL CHECK (position >= 0),
    type           card_type NOT NULL,
    question_text  VARCHAR(500) NOT NULL,
    timer_seconds  INT NOT NULL CHECK (timer_seconds > 0),
    options        JSONB,
    accepted_texts TEXT[],
    CONSTRAINT one_parent CHECK ((draft_id IS NOT NULL) <> (snapshot_id IS NOT NULL)),
    CONSTRAINT answer_matches_type CHECK (
        (type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE') AND options IS NOT NULL AND accepted_texts IS NULL)
        OR
        (type = 'TEXT_INPUT' AND accepted_texts IS NOT NULL AND options IS NULL)
    ),
    CONSTRAINT options_max  CHECK (options IS NULL OR jsonb_array_length(options) <= 8),
    CONSTRAINT accepted_max CHECK (accepted_texts IS NULL OR cardinality(accepted_texts) <= 50),
    CONSTRAINT uq_draft_position    UNIQUE (draft_id, position)    DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT uq_snapshot_position UNIQUE (snapshot_id, position) DEFERRABLE INITIALLY IMMEDIATE
);

--rollback DROP TABLE cards;
--rollback DROP TABLE quiz_snapshots;
--rollback DROP TABLE quiz_drafts;
--rollback DROP TABLE quizzes;
--rollback DROP TYPE card_type;
--rollback DROP TYPE visibility;
