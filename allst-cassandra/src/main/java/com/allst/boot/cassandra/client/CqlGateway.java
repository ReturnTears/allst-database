package com.allst.boot.cassandra.client;

import com.allst.boot.cassandra.common.CassandraAccessException;
import com.allst.boot.cassandra.common.ContactPoints;
import com.allst.boot.cassandra.common.Identifiers;
import com.allst.boot.cassandra.config.CassandraProperties;
import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.DriverTimeoutException;
import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.servererrors.SyntaxError;
import com.datastax.oss.driver.api.core.servererrors.UnauthorizedException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
public class CqlGateway {

    private static final Logger log = LoggerFactory.getLogger(CqlGateway.class);
    private static final Duration DDL_TIMEOUT = Duration.ofSeconds(60);

    private final CassandraProperties properties;
    private final AtomicReference<CqlSession> session = new AtomicReference<>();
    private final ConsistencyLevel consistency;

    public CqlGateway(CassandraProperties properties) {
        this.properties = properties;
        this.consistency = DefaultConsistencyLevel.valueOf(Identifiers.requireConsistency(properties.getRequest().getConsistency()));
    }

    public ConsistencyLevel consistency() {
        return consistency;
    }

    public Duration timeout() {
        return properties.getRequest().getTimeout();
    }

    public ResultSet query(String cql, Consumer<BoundStatementBuilder> customizer) {
        BoundStatementBuilder builder = session().prepare(cql).boundStatementBuilder()
                .setConsistencyLevel(consistency)
                .setIdempotence(true)
                .setTimeout(timeout())
                .setPageSize(properties.getRequest().getPageSize());
        customizer.accept(builder);
        return execute(builder.build());
    }

    public BoundStatementBuilder mutation(String cql, boolean idempotent) {
        return session().prepare(cql).boundStatementBuilder()
                .setConsistencyLevel(consistency)
                .setIdempotence(idempotent)
                .setTimeout(timeout());
    }

    public void executeDdl(String cql) {
        SimpleStatement statement = SimpleStatement.newInstance(cql)
                .setTimeout(DDL_TIMEOUT)
                .setIdempotent(true);
        execute(statement);
    }

    public void executeEach(List<Statement<?>> statements, boolean wait) {
        if (!wait) {
            CqlSession current = session();
            for (Statement<?> statement : statements) {
                current.executeAsync(statement).whenComplete(this::logAsync);
            }
            return;
        }
        for (Statement<?> statement : statements) {
            execute(statement);
        }
    }

    public ResultSet execute(Statement<?> statement) {
        try {
            return session().execute(statement);
        } catch (RuntimeException ex) {
            if (ex instanceof CassandraAccessException access) {
                throw access;
            }
            throw translate(ex);
        }
    }

    public CqlSession session() {
        CqlSession current = session.get();
        if (current != null && !current.isClosed()) {
            return current;
        }
        synchronized (session) {
            current = session.get();
            if (current != null && !current.isClosed()) {
                return current;
            }
            try {
                CqlSession created = open();
                session.set(created);
                return created;
            } catch (CassandraAccessException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new CassandraAccessException(true, "Cassandra 不可用: " + message(ex), ex);
            }
        }
    }

    @PreDestroy
    public void close() {
        CqlSession current = session.getAndSet(null);
        if (current != null) {
            current.close();
        }
    }

    private CqlSession open() {
        DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, properties.getRequest().getTimeout())
                .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, properties.getRequest().getConnectTimeout())
                .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, properties.getRequest().getTimeout())
                .withInt(DefaultDriverOption.REQUEST_PAGE_SIZE, properties.getRequest().getPageSize())
                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency.name())
                .withBoolean(DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE, false)
                .withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, properties.getPool().getLocalSize())
                .withInt(DefaultDriverOption.CONNECTION_POOL_REMOTE_SIZE, properties.getPool().getRemoteSize())
                .build();
        CqlSessionBuilder builder = CqlSession.builder()
                .withConfigLoader(loader)
                .withLocalDatacenter(properties.getLocalDatacenter())
                .addContactPoints(ContactPoints.parse(properties.getContactPoints()));
        String username = Identifiers.blankToNull(properties.getUsername());
        if (username != null) {
            builder = builder.withAuthCredentials(username, properties.getPassword());
        }
        return builder.build();
    }

    private void logAsync(AsyncResultSet result, Throwable error) {
        if (error != null) {
            log.warn("异步写入失败: {}", message(error));
        }
    }

    private CassandraAccessException translate(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof InvalidQueryException
                || cause instanceof SyntaxError
                || cause instanceof UnauthorizedException
                || cause instanceof AlreadyExistsException) {
            return new CassandraAccessException(false, "Cassandra 拒绝请求: " + cause.getMessage(), cause);
        }
        if (cause instanceof AllNodesFailedException
                || cause instanceof NoNodeAvailableException
                || cause instanceof DriverTimeoutException
                || cause instanceof DriverException) {
            return new CassandraAccessException(true, "Cassandra 不可用: " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
        }
        return new CassandraAccessException(true, "Cassandra 访问失败: " + message(cause), cause);
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current && !(current instanceof DriverException)) {
            current = current.getCause();
        }
        return current;
    }

    private static String message(Throwable error) {
        String text = error.getMessage();
        return text == null || text.isBlank() ? error.getClass().getSimpleName() : text;
    }
}
