package com.allst.boot.reading;

import java.util.List;

public class ReadingBatchCommand {

    private List<ReadingWriteCommand> readings;

    public List<ReadingWriteCommand> getReadings() {
        return readings;
    }

    public void setReadings(List<ReadingWriteCommand> readings) {
        this.readings = readings;
    }
}
