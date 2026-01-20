-- Создание таблицы счетов пользователей
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    balance NUMERIC(38, 10) NOT NULL DEFAULT 0.0000000000,
    version BIGINT NOT NULL DEFAULT 0
);

-- Индекс для быстрого поиска счетов по пользователю
CREATE INDEX idx_accounts_user_id ON accounts (user_id);