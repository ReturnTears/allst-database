CREATE TABLE IF NOT EXISTS t_event (
    id BIGINT PRIMARY KEY,
    shard_key VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    payload VARCHAR(256) NOT NULL,
    created_at DATETIME NOT NULL
);
