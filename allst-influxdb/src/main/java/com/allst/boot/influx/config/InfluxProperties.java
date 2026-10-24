package com.allst.boot.influx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "influx")
public class InfluxProperties {

    private String url = "http://127.0.0.1:8086";
    private String username = "admin";
    private String password = "admin123";
    private String database = "allst_iot";
    private String retentionPolicy = "rp_raw_7d";
    private boolean initializeOnStartup = false;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private Duration writeTimeout = Duration.ofSeconds(10);
    private boolean gzip = true;
    private String consistency = "ONE";
    private int replicationFactor = 1;
    private String logLevel = "NONE";
    private Batch batch = new Batch();
    private Ops ops = new Ops();

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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    public boolean isInitializeOnStartup() {
        return initializeOnStartup;
    }

    public void setInitializeOnStartup(boolean initializeOnStartup) {
        this.initializeOnStartup = initializeOnStartup;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Ops getOps() {
        return ops;
    }

    public void setOps(Ops ops) {
        this.ops = ops;
    }

    public static class Batch {
        private boolean enabled = true;
        private int actions = 2000;
        private int flushIntervalMs = 200;
        private int jitterIntervalMs = 50;
        private int bufferLimit = 20000;
        private boolean dropOnQueueExhaustion = false;
        private String precision = "MILLISECONDS";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getActions() {
            return actions;
        }

        public void setActions(int actions) {
            this.actions = actions;
        }

        public int getFlushIntervalMs() {
            return flushIntervalMs;
        }

        public void setFlushIntervalMs(int flushIntervalMs) {
            this.flushIntervalMs = flushIntervalMs;
        }

        public int getJitterIntervalMs() {
            return jitterIntervalMs;
        }

        public void setJitterIntervalMs(int jitterIntervalMs) {
            this.jitterIntervalMs = jitterIntervalMs;
        }

        public int getBufferLimit() {
            return bufferLimit;
        }

        public void setBufferLimit(int bufferLimit) {
            this.bufferLimit = bufferLimit;
        }

        public boolean isDropOnQueueExhaustion() {
            return dropOnQueueExhaustion;
        }

        public void setDropOnQueueExhaustion(boolean dropOnQueueExhaustion) {
            this.dropOnQueueExhaustion = dropOnQueueExhaustion;
        }

        public String getPrecision() {
            return precision;
        }

        public void setPrecision(String precision) {
            this.precision = precision;
        }
    }

    public static class Ops {
        private String backupDir = "./data/influx-backup";
        private boolean allowShell = false;
        private String influxdCommand = "influxd";
        private String backupHost = "127.0.0.1:8088";

        public String getBackupDir() {
            return backupDir;
        }

        public void setBackupDir(String backupDir) {
            this.backupDir = backupDir;
        }

        public boolean isAllowShell() {
            return allowShell;
        }

        public void setAllowShell(boolean allowShell) {
            this.allowShell = allowShell;
        }

        public String getInfluxdCommand() {
            return influxdCommand;
        }

        public void setInfluxdCommand(String influxdCommand) {
            this.influxdCommand = influxdCommand;
        }

        public String getBackupHost() {
            return backupHost;
        }

        public void setBackupHost(String backupHost) {
            this.backupHost = backupHost;
        }
    }
}
