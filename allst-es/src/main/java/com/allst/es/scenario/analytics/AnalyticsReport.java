package com.allst.es.scenario.analytics;

import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.TrendPoint;
import lombok.Data;

import java.util.List;

@Data
public class AnalyticsReport {
    private String from;
    private String to;
    private String interval;
    private long total;
    private long uniqueUsers;
    private double sumAmount;
    private double avgAmount;
    private List<NamedCount> byStatus;
    private List<NamedCount> byChannel;
    private List<NamedCount> byCategory;
    private List<NamedCount> byProvince;
    private List<TrendPoint> trend;
}
