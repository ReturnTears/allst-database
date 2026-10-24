package com.allst.boot.cassandra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "cassandra")
public class CassandraProperties {

    private String contactPoints = "127.0.0.1:9042";
    private String localDatacenter = "datacenter1";
    private String keyspace = "allst_iot";
    private String username = "";
    private String password = "";
    private boolean initializeOnStartup = false;
    private String zone = "Asia/Shanghai";
    private Replication replication = new Replication();
    private Request request = new Request();
    private Pool pool = new Pool();
    private Ops ops = new Ops();

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getLocalDatacenter() {
        return localDatacenter;
    }

    public void setLocalDatacenter(String localDatacenter) {
        this.localDatacenter = localDatacenter;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
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

    public boolean isInitializeOnStartup() {
        return initializeOnStartup;
    }

    public void setInitializeOnStartup(boolean initializeOnStartup) {
        this.initializeOnStartup = initializeOnStartup;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Replication getReplication() {
        return replication;
    }

    public void setReplication(Replication replication) {
        this.replication = replication;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Ops getOps() {
        return ops;
    }

    public void setOps(Ops ops) {
        this.ops = ops;
    }

    public static class Replication {
        private String strategy = "SimpleStrategy";
        private int factor = 1;
        private Map<String, Integer> datacenters = new LinkedHashMap<>();

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public int getFactor() {
            return factor;
        }

        public void setFactor(int factor) {
            this.factor = factor;
        }

        public Map<String, Integer> getDatacenters() {
            return datacenters;
        }

        public void setDatacenters(Map<String, Integer> datacenters) {
            this.datacenters = datacenters;
        }
    }

    public static class Request {
        private Duration timeout = Duration.ofSeconds(5);
        private Duration connectTimeout = Duration.ofSeconds(5);
        private String consistency = "LOCAL_ONE";
        private int pageSize = 500;

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public String getConsistency() {
            return consistency;
        }

        public void setConsistency(String consistency) {
            this.consistency = consistency;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }

    public static class Pool {
        private int localSize = 2;
        private int remoteSize = 1;

        public int getLocalSize() {
            return localSize;
        }

        public void setLocalSize(int localSize) {
            this.localSize = localSize;
        }

        public int getRemoteSize() {
            return remoteSize;
        }

        public void setRemoteSize(int remoteSize) {
            this.remoteSize = remoteSize;
        }
    }

    public static class Ops {
        private String backupDir = "./data/cassandra-backup";

        public String getBackupDir() {
            return backupDir;
        }

        public void setBackupDir(String backupDir) {
            this.backupDir = backupDir;
        }
    }
}
