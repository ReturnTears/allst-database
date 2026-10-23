-- 场景库初始化。账号沿用 application.yml 里的 root、空密码。
-- 旧练习库 master / slave 不在这里重建，见 doc/edit.sql。
-- 可重复执行：库和表都使用 IF NOT EXISTS。

CREATE DATABASE IF NOT EXISTS dyn_rw_master DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_rw_slave_1 DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_rw_slave_2 DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_order_0 DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_order_1 DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_tenant_a DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_tenant_b DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_stable DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_gray DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_shard_0 DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dyn_shard_1 DEFAULT CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS dyn_rw_master.t_article (
    id BIGINT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    author VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS dyn_rw_slave_1.t_article LIKE dyn_rw_master.t_article;
CREATE TABLE IF NOT EXISTS dyn_rw_slave_2.t_article LIKE dyn_rw_master.t_article;

CREATE TABLE IF NOT EXISTS dyn_order_0.t_order_0 (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_order_0_user (user_id)
);
CREATE TABLE IF NOT EXISTS dyn_order_0.t_order_1 LIKE dyn_order_0.t_order_0;
CREATE TABLE IF NOT EXISTS dyn_order_1.t_order_0 LIKE dyn_order_0.t_order_0;
CREATE TABLE IF NOT EXISTS dyn_order_1.t_order_1 LIKE dyn_order_0.t_order_0;

CREATE TABLE IF NOT EXISTS dyn_tenant_a.t_account (
    id BIGINT PRIMARY KEY,
    account_name VARCHAR(64) NOT NULL,
    level_name VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS dyn_tenant_a.t_ledger (
    id BIGINT PRIMARY KEY,
    tenant_id VARCHAR(32) NOT NULL,
    direction VARCHAR(8) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_ledger_biz (biz_no)
);
CREATE TABLE IF NOT EXISTS dyn_tenant_b.t_account LIKE dyn_tenant_a.t_account;
CREATE TABLE IF NOT EXISTS dyn_tenant_b.t_ledger LIKE dyn_tenant_a.t_ledger;

CREATE TABLE IF NOT EXISTS dyn_stable.t_feature (
    id BIGINT PRIMARY KEY,
    feature_key VARCHAR(64) NOT NULL,
    feature_value VARCHAR(128) NOT NULL,
    version_tag VARCHAR(16) NOT NULL
);
CREATE TABLE IF NOT EXISTS dyn_gray.t_feature LIKE dyn_stable.t_feature;

CREATE TABLE IF NOT EXISTS dyn_shard_0.t_event (
    id BIGINT PRIMARY KEY,
    shard_key VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    payload VARCHAR(256) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_event_shard (shard_key)
);
CREATE TABLE IF NOT EXISTS dyn_shard_1.t_event LIKE dyn_shard_0.t_event;
