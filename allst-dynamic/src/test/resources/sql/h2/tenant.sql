CREATE TABLE IF NOT EXISTS t_account (
    id BIGINT PRIMARY KEY,
    account_name VARCHAR(64) NOT NULL,
    level_name VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS t_ledger (
    id BIGINT PRIMARY KEY,
    tenant_id VARCHAR(32) NOT NULL,
    direction VARCHAR(8) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL
);
