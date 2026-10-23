package com.allst.dynamic.scenario.tenant;

import lombok.Data;

@Data
public class RegisterTenantCommand {
    private String tenantId;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
