ALTER TABLE outbox
    ADD retry_counter SMALLINT DEFAULT 0;
