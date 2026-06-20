--liquibase formatted sql

--changeset artem:0002-soften-draft-cards
--comment: Мягкая валидация черновиков

-- Заголовок черновика может быть пустым на этапе черновика
ALTER TABLE quiz_drafts ALTER COLUMN title DROP NOT NULL;

-- Содержательные поля карточки могут быть незаполнены в черновике
ALTER TABLE cards ALTER COLUMN type DROP NOT NULL;
ALTER TABLE cards ALTER COLUMN question_text DROP NOT NULL;
ALTER TABLE cards ALTER COLUMN timer_seconds DROP NOT NULL;

-- Таймер строго положителен только для опубликованных карточек; в черновике допустим NULL
ALTER TABLE cards DROP CONSTRAINT IF EXISTS cards_timer_seconds_check;
ALTER TABLE cards ADD CONSTRAINT timer_seconds_positive
    CHECK (snapshot_id IS NULL OR (timer_seconds IS NOT NULL AND timer_seconds > 0));

-- Согласованность ответа с типом требуется только для снапшота; черновик может быть в промежуточном состоянии
ALTER TABLE cards DROP CONSTRAINT answer_matches_type;
ALTER TABLE cards ADD CONSTRAINT answer_matches_type CHECK (
    snapshot_id IS NULL OR (
        (type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE') AND options IS NOT NULL AND accepted_texts IS NULL)
        OR
        (type = 'TEXT_INPUT' AND accepted_texts IS NOT NULL AND options IS NULL)
    )
);

-- Опубликованная карточка всегда полностью заполнена
ALTER TABLE cards ADD CONSTRAINT snapshot_card_complete CHECK (
    snapshot_id IS NULL OR (type IS NOT NULL AND question_text IS NOT NULL AND timer_seconds IS NOT NULL)
);

--rollback ALTER TABLE cards DROP CONSTRAINT snapshot_card_complete;
--rollback ALTER TABLE cards DROP CONSTRAINT answer_matches_type;
--rollback ALTER TABLE cards ADD CONSTRAINT answer_matches_type CHECK ((type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE') AND options IS NOT NULL AND accepted_texts IS NULL) OR (type = 'TEXT_INPUT' AND accepted_texts IS NOT NULL AND options IS NULL));
--rollback ALTER TABLE cards DROP CONSTRAINT timer_seconds_positive;
--rollback ALTER TABLE cards ADD CONSTRAINT cards_timer_seconds_check CHECK (timer_seconds > 0);
--rollback ALTER TABLE cards ALTER COLUMN timer_seconds SET NOT NULL;
--rollback ALTER TABLE cards ALTER COLUMN question_text SET NOT NULL;
--rollback ALTER TABLE cards ALTER COLUMN type SET NOT NULL;
--rollback ALTER TABLE quiz_drafts ALTER COLUMN title SET NOT NULL;
