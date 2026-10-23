package com.allst.dynamic.scenario.runtime;

import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/datasources")
public class DataSourceCatalogController {

    private final RuntimeDataSourceService runtimeDataSourceService;

    public DataSourceCatalogController(RuntimeDataSourceService runtimeDataSourceService) {
        this.runtimeDataSourceService = runtimeDataSourceService;
    }

    @GetMapping
    public ScenarioResponse<DataSourceSnapshot> list() {
        return ScenarioResponse.ok(runtimeDataSourceService.snapshot());
    }
}
