package com.allst.boot.influx.config;

import com.allst.boot.influx.schema.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "influx", name = "initialize-on-startup", havingValue = "true")
public class SchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final SchemaService schemaService;

    public SchemaInitializer(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化 InfluxDB 数据库、保留策略和连续查询");
        schemaService.init();
    }
}
