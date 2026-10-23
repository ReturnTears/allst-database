package com.allst.dynamic.scenario;

import com.allst.dynamic.scenario.gray.GrayPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(GrayPolicy.class)
public class ScenarioConfiguration {
}
