package com.allst.dynamic.scenario.gray;

import com.allst.dynamic.scenario.common.DataSourceKeys;

/**
 * 显式版本头优先，其次白名单，再次按用户稳定取模。没有用户标识时不做百分比切流。
 */
public final class GrayDecider {

    private GrayDecider() {
    }

    public static String decide(GrayPolicy policy, String versionHeader, String userId) {
        if ("gray".equalsIgnoreCase(versionHeader)) {
            return DataSourceKeys.RELEASE_GRAY;
        }
        if ("stable".equalsIgnoreCase(versionHeader)) {
            return DataSourceKeys.RELEASE_STABLE;
        }
        if (userId != null && !userId.isBlank() && policy.getAllowUsers() != null
                && policy.getAllowUsers().contains(userId)) {
            return DataSourceKeys.RELEASE_GRAY;
        }
        int percent = policy.getPercent();
        if (userId != null && !userId.isBlank() && percent > 0) {
            int bucket = Math.floorMod(userId.hashCode(), 100);
            if (bucket < percent) {
                return DataSourceKeys.RELEASE_GRAY;
            }
        }
        return DataSourceKeys.RELEASE_STABLE;
    }

    public static String explain(String dataSource, String versionHeader, String userId) {
        if ("gray".equalsIgnoreCase(versionHeader)) {
            return "请求头 X-Gray-Version=gray，进入灰度库";
        }
        if ("stable".equalsIgnoreCase(versionHeader)) {
            return "请求头 X-Gray-Version=stable，进入稳定库";
        }
        if (DataSourceKeys.RELEASE_GRAY.equals(dataSource) && userId != null && !userId.isBlank()) {
            return "用户 " + userId + " 命中白名单或灰度比例，进入灰度库";
        }
        return "未命中灰度条件，进入稳定库";
    }
}
