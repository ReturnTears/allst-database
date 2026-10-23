package com.allst.dynamic.scenario;

import com.allst.dynamic.scenario.common.ScenarioMeta;
import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scenario")
public class ScenarioCatalogController {

    @GetMapping
    public ScenarioResponse<List<ScenarioMeta>> list() {
        List<ScenarioMeta> scenarios = List.of(
                new ScenarioMeta("rw", "读写分离", "多数据源", "/scenario/rw", "docs/datasource-scenarios/01-读写分离.md"),
                new ScenarioMeta("sharding", "分库分表", "多数据源", "/scenario/sharding", "docs/datasource-scenarios/02-分库分表.md"),
                new ScenarioMeta("tenant", "租户隔离", "动态数据源", "/scenario/tenant", "docs/datasource-scenarios/03-租户隔离.md"),
                new ScenarioMeta("gray", "灰度发布", "动态数据源", "/scenario/gray", "docs/datasource-scenarios/04-灰度发布.md"),
                new ScenarioMeta("shard", "数据分片", "动态数据源", "/scenario/shard", "docs/datasource-scenarios/05-数据分片.md"),
                new ScenarioMeta("transaction", "多数据源事务", "补充", "/scenario/tenant/transfer", "docs/datasource-scenarios/06-主从粘滞与多数据源事务.md")
        );
        return ScenarioResponse.ok(scenarios);
    }
}
