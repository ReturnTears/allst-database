package com.allst.es.scenario.siem;

import com.allst.es.scenario.common.NamedCount;
import com.allst.es.scenario.common.TrendPoint;
import lombok.Data;

import java.util.List;

@Data
public class SiemDashboard {
    private String from;
    private String to;
    private String interval;
    private long total;
    private List<SecurityAlert> alerts;
    private List<NamedCount> byEventType;
    private List<NamedCount> bySeverity;
    private List<TrendPoint> trend;
}
