package com.allst.trino.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trino 连接、连接池和查询限制。默认值与 application.yml 保持一致，缺省配置也能启动。
 *
 * @author Hutu
 * @since 2026-10-23
 */
@ConfigurationProperties(prefix = "trino")
public class TrinoProperties {

    private final Jdbc jdbc = new Jdbc();
    private final Pool pool = new Pool();
    private final Query query = new Query();

    public Jdbc getJdbc() {
        return jdbc;
    }

    public Pool getPool() {
        return pool;
    }

    public Query getQuery() {
        return query;
    }

    public static class Jdbc {
        private String driverClassName = "io.trino.jdbc.TrinoDriver";
        private String url = "jdbc:trino://127.0.0.1:8080/tpch/tiny";
        private String username = "trino";
        private String password = "";
        private boolean ssl;
        private String sslVerification = "";
        private String sslTrustStorePath = "";
        private String sslTrustStorePassword = "";
        private String sslKeyStorePath = "";
        private String sslKeyStorePassword = "";
        private String source = "allst-trino";
        private String timezone = "";
        private String accessToken = "";
        private String extraCredentials = "";

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getSslVerification() {
            return sslVerification;
        }

        public void setSslVerification(String sslVerification) {
            this.sslVerification = sslVerification;
        }

        public String getSslTrustStorePath() {
            return sslTrustStorePath;
        }

        public void setSslTrustStorePath(String sslTrustStorePath) {
            this.sslTrustStorePath = sslTrustStorePath;
        }

        public String getSslTrustStorePassword() {
            return sslTrustStorePassword;
        }

        public void setSslTrustStorePassword(String sslTrustStorePassword) {
            this.sslTrustStorePassword = sslTrustStorePassword;
        }

        public String getSslKeyStorePath() {
            return sslKeyStorePath;
        }

        public void setSslKeyStorePath(String sslKeyStorePath) {
            this.sslKeyStorePath = sslKeyStorePath;
        }

        public String getSslKeyStorePassword() {
            return sslKeyStorePassword;
        }

        public void setSslKeyStorePassword(String sslKeyStorePassword) {
            this.sslKeyStorePassword = sslKeyStorePassword;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getExtraCredentials() {
            return extraCredentials;
        }

        public void setExtraCredentials(String extraCredentials) {
            this.extraCredentials = extraCredentials;
        }
    }

    public static class Pool {
        private int maximumPoolSize = 8;
        private int minimumIdle = 0;
        private long connectionTimeoutMs = 30000L;
        private long idleTimeoutMs = 600000L;
        private long maxLifetimeMs = 1800000L;

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public int getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(int minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public long getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public void setConnectionTimeoutMs(long connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
        }

        public long getIdleTimeoutMs() {
            return idleTimeoutMs;
        }

        public void setIdleTimeoutMs(long idleTimeoutMs) {
            this.idleTimeoutMs = idleTimeoutMs;
        }

        public long getMaxLifetimeMs() {
            return maxLifetimeMs;
        }

        public void setMaxLifetimeMs(long maxLifetimeMs) {
            this.maxLifetimeMs = maxLifetimeMs;
        }
    }

    public static class Query {
        private int timeoutSeconds = 60;
        private int maxRows = 500;
        private boolean readOnly = true;

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getMaxRows() {
            return maxRows;
        }

        public void setMaxRows(int maxRows) {
            this.maxRows = maxRows;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }
}
