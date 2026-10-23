package com.allst.es.scenario.suggest;

import lombok.Data;

import java.util.List;

@Data
public class SuggestCommand {
    private String id;
    private String name;
    private String category;
    private Integer weight;
    private List<String> inputs;
}
