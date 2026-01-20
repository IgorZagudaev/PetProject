-- Переименовываем колонку password в password_hash (сохраняем данные)
ALTER TABLE users
RENAME COLUMN password TO password_hash;

-- Добавляем новые колонки с дефолтными значениями
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

-- Устанавливаем значение created_at для существующих записей
UPDATE users
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

-- Добавляем CHECK constraint для статуса (после заполнения данных)
ALTER TABLE users
ADD CONSTRAINT users_status_check
CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED'));

-- Создаем триггер для автоматического обновления updated_at
/*
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();*/