package com.allst.boot.metric;

import java.util.List;

public class MetricBatchCommand {

    private List<MetricWriteCommand> points;

    public List<MetricWriteCommand> getPoints() {
        return points;
    }

    public void setPoints(List<MetricWriteCommand> points) {
        this.points = points;
    }
}
