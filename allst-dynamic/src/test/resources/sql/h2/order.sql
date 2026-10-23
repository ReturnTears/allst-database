CREATE TABLE IF NOT EXISTS t_order_0 (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS t_order_1 (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    created_at DATETIME NOT NULL
);
