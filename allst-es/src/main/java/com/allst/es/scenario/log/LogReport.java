package com.allst.es.scenario.log;

import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.TrendPoint;
import lombok.Data;

import java.util.List;

@Data
public class LogReport {
    private String from;
    private String to;
    private String interval;
    private long total;
    private long errorCount;
    private double errorRate;
    private double costP50;
    private double costP95;
    private double costP99;
    private List<NamedCount> byLevel;
    private List<NamedCount> byService;
    private List<NamedCount> topEvents;
    private List<TrendPoint> trend;
}
