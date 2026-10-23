package com.allst.es.scenario.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScenarioMeta {
    private String code;
    private String name;
    private String index;
    private String basePath;
    private String doc;
}
