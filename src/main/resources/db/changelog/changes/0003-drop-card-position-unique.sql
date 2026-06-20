--liquibase formatted sql

--changeset artem:0003-drop-card-position-unique
--comment: Убрано ограничение на позиции карточек, порядок задаёт код сервиса

ALTER TABLE cards DROP CONSTRAINT IF EXISTS uq_draft_position;
ALTER TABLE cards DROP CONSTRAINT IF EXISTS uq_snapshot_position;

--rollback ALTER TABLE cards ADD CONSTRAINT uq_draft_position    UNIQUE (draft_id, position)    DEFERRABLE INITIALLY IMMEDIATE;
--rollback ALTER TABLE cards ADD CONSTRAINT uq_snapshot_position UNIQUE (snapshot_id, position) DEFERRABLE INITIALLY IMMEDIATE;
