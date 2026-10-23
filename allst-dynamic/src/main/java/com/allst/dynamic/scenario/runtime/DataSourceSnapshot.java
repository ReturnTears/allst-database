package com.allst.dynamic.scenario.runtime;

import lombok.Data;

import java.util.Set;

@Data
public class DataSourceSnapshot {
    private Set<String> dataSources;
    private Set<String> groups;
}
