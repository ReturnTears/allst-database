CREATE TABLE IF NOT EXISTS t_feature (
    id BIGINT PRIMARY KEY,
    feature_key VARCHAR(64) NOT NULL,
    feature_value VARCHAR(128) NOT NULL,
    version_tag VARCHAR(16) NOT NULL
);
