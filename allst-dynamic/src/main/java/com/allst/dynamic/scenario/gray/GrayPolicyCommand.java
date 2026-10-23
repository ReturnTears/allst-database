package com.allst.dynamic.scenario.gray;

import lombok.Data;

import java.util.List;

@Data
public class GrayPolicyCommand {
    private Integer percent;
    private List<String> allowUsers;
}
