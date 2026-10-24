package com.allst.boot.cassandra.config;

import com.allst.boot.cassandra.schema.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final CassandraProperties properties;
    private final SchemaService schemaService;

    public SchemaInitializer(CassandraProperties properties, SchemaService schemaService) {
        this.properties = properties;
        this.schemaService = schemaService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isInitializeOnStartup()) {
            return;
        }
        log.info("启动时初始化 keyspace {}", properties.getKeyspace());
        schemaService.init();
    }
}
