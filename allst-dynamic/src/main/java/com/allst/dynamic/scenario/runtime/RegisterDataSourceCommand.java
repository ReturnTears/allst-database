package com.allst.dynamic.scenario.runtime;

import lombok.Data;

@Data
public class RegisterDataSourceCommand {
    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
