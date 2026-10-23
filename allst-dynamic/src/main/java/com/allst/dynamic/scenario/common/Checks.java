package com.allst.dynamic.scenario.common;

import java.math.BigDecimal;

public final class Checks {

    private Checks() {
    }

    public static String requireText(String value, String name, int max) {
        if (value == null || value.isBlank()) {
            throw new ScenarioException(400, name + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new ScenarioException(400, name + "长度不能超过" + max);
        }
        return trimmed;
    }

    public static long requireUserId(Long userId) {
        if (userId == null || userId < 0) {
            throw new ScenarioException(400, "userId 必须是非负整数");
        }
        return userId;
    }

    public static BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ScenarioException(400, "amount 必须大于 0");
        }
        return amount;
    }
}
