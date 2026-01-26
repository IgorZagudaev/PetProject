CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL, -- например, "User"
    aggregate_id UUID NOT NULL,           -- ID пользователя
    event_type VARCHAR(255) NOT NULL,     -- например, "AccountCreate"
    body JSONB,                           -- сериализованные данные
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed BOOLEAN DEFAULT false
);