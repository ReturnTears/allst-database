package com.allst.boot.cassandra.config;

import com.allst.boot.cassandra.common.ContactPoints;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.cql.Statements;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.ZoneId;

@Component
public class CassandraConfigGuard {

    private final CassandraProperties properties;

    public CassandraConfigGuard(CassandraProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validate() {
        ContactPoints.parse(properties.getContactPoints());
        Identifiers.requireDatacenter(properties.getLocalDatacenter());
        Identifiers.requireKeyspace(properties.getKeyspace());
        Identifiers.requireConsistency(properties.getRequest().getConsistency());
        try {
            ZoneId.of(properties.getZone());
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("zone 不是合法的时区");
        }
        if (properties.getRequest().getPageSize() < 1 || properties.getRequest().getPageSize() > 5000) {
            throw new IllegalArgumentException("page-size 必须在 1 到 5000 之间");
        }
        int localSize = properties.getPool().getLocalSize();
        int remoteSize = properties.getPool().getRemoteSize();
        if (localSize < 1 || localSize > 8 || remoteSize < 1 || remoteSize > 4) {
            throw new IllegalArgumentException("连接池 local-size 为 1 到 8，remote-size 为 1 到 4");
        }
        String username = Identifiers.blankToNull(properties.getUsername());
        String password = Identifiers.blankToNull(properties.getPassword());
        if ((username == null) != (password == null)) {
            throw new IllegalArgumentException("username 和 password 需要同时配置");
        }
        if (properties.getRequest().getTimeout().isNegative() || properties.getRequest().getTimeout().isZero()
                || properties.getRequest().getConnectTimeout().isNegative()
                || properties.getRequest().getConnectTimeout().isZero()) {
            throw new IllegalArgumentException("timeout 必须大于 0");
        }
        Statements.createKeyspace(properties.getKeyspace(), properties.getReplication().getStrategy(),
                properties.getReplication().getFactor(), properties.getReplication().getDatacenters());
    }
}
