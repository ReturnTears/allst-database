package com.allst.boot.mutation;

import java.util.List;
import java.util.Map;

public class ProfileCommand {

    private String deviceId;
    private String firmware;
    private List<String> labels;
    private Map<String, Double> thresholds;
    private List<String> channels;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, Double> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<String, Double> thresholds) {
        this.thresholds = thresholds;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }
}
