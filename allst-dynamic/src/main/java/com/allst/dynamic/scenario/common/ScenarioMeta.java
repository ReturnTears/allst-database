package com.allst.dynamic.scenario.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScenarioMeta {
    private String code;
    private String name;
    private String category;
    private String path;
    private String document;
}
