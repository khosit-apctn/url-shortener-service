CREATE TABLE IF NOT EXISTS short_urls
(
    id           BIGSERIAL PRIMARY KEY,
    short_code   VARCHAR(16)              NOT NULL UNIQUE,
    original_url TEXT                     NOT NULL,
    user_id      BIGINT                   REFERENCES users (id) ON DELETE SET NULL,
    is_active    BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_timestamp   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);