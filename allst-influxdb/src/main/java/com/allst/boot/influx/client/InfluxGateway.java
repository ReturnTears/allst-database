package com.allst.boot.influx.client;

import com.allst.boot.influx.common.InfluxAccessException;
import com.allst.boot.influx.common.InfluxResults;
import com.allst.boot.influx.config.InfluxClientConfiguration;
import com.allst.boot.influx.config.InfluxProperties;
import com.allst.boot.influx.sql.InfluxQl;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class InfluxGateway {

    private static final Logger log = LoggerFactory.getLogger(InfluxGateway.class);

    private final InfluxDB influxDB;
    private final InfluxProperties properties;
    private final TimeUnit batchPrecision;
    private final InfluxDB.ConsistencyLevel consistency;

    public InfluxGateway(InfluxDB influxDB, InfluxProperties properties) {
        this.influxDB = influxDB;
        this.properties = properties;
        this.batchPrecision = TimeUnit.valueOf(properties.getBatch().getPrecision());
        this.consistency = InfluxClientConfiguration.consistency(properties);
    }

    public QueryResult command(String influxQl) {
        return command(influxQl, properties.getDatabase());
    }

    public QueryResult command(String influxQl, String database) {
        log.debug("InfluxQL: {}", influxQl);
        try {
            QueryResult result = influxDB.query(new Query(influxQl, database, true));
            InfluxResults.requireSuccess(result);
            return result;
        } catch (InfluxAccessException ex) {
            throw ex;
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        } catch (RuntimeException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    @SuppressWarnings("deprecation")
    public boolean databaseExists(String database) {
        InfluxQl.requireIdent(database);
        try {
            return influxDB.databaseExists(database);
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    @SuppressWarnings("deprecation")
    public void createDatabase(String database) {
        InfluxQl.requireIdent(database);
        try {
            influxDB.createDatabase(database);
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    public void writeSync(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("写入点不能为空");
        }
        BatchPoints.Builder builder = BatchPoints.database(properties.getDatabase())
                .retentionPolicy(properties.getRetentionPolicy())
                .precision(batchPrecision)
                .consistency(consistency);
        for (Point point : points) {
            builder.point(point);
        }
        try {
            influxDB.write(builder.build());
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    public void writeAsync(Point point) {
        if (!influxDB.isBatchEnabled()) {
            throw new IllegalArgumentException("异步写入依赖客户端批量缓冲，请先打开 influx.batch.enabled");
        }
        try {
            influxDB.write(properties.getDatabase(), properties.getRetentionPolicy(), point);
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    public void writeLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("恢复内容不能为空");
        }
        try {
            influxDB.write(properties.getDatabase(), properties.getRetentionPolicy(), consistency,
                    TimeUnit.NANOSECONDS, lines);
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (InfluxDBException ex) {
            throw new InfluxAccessException(message(ex), ex, false);
        }
    }

    public void flush() {
        if (!influxDB.isBatchEnabled()) {
            throw new IllegalArgumentException("当前未开启批量写入，同步写入已经直接落库");
        }
        influxDB.flush();
    }

    public Pong ping() {
        try {
            Pong pong = influxDB.ping();
            if (pong == null || !pong.isGood()) {
                throw new InfluxAccessException("InfluxDB ping 未通过", null, true);
            }
            return pong;
        } catch (InfluxAccessException ex) {
            throw ex;
        } catch (InfluxDBIOException ex) {
            throw new InfluxAccessException("无法连接 InfluxDB: " + message(ex), ex, true);
        } catch (RuntimeException ex) {
            throw new InfluxAccessException(message(ex), ex, true);
        }
    }

    public boolean batchEnabled() {
        return influxDB.isBatchEnabled();
    }

    public InfluxProperties properties() {
        return properties;
    }

    private static String message(Throwable throwable) {
        String text = throwable.getMessage();
        return text == null || text.isBlank() ? throwable.getClass().getSimpleName() : text;
    }
}
