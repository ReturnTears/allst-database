package com.allst.dynamic.scenario.gray;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "allst.datasource.gray")
public class GrayPolicy {
    private int percent;
    private List<String> allowUsers = new ArrayList<>();

    public void replace(int percent, List<String> allowUsers) {
        this.percent = percent;
        this.allowUsers = new ArrayList<>(allowUsers);
    }
}
