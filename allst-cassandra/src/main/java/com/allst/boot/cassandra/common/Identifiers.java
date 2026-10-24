package com.allst.boot.cassandra.common;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class Identifiers {

    private static final Pattern KEYSPACE = Pattern.compile("[a-z][a-z0-9_]{0,47}");
    private static final Pattern DATACENTER = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Pattern REGION = Pattern.compile("[a-z0-9_-]{1,32}");
    private static final Pattern LABEL = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final Pattern MODEL = Pattern.compile("[A-Za-z0-9._-]{1,32}");
    private static final Pattern TAG_KEY = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,31}");
    private static final Pattern SNAPSHOT = Pattern.compile("[A-Za-z0-9_-]{1,40}");

    private Identifiers() {
    }

    public static String requireKeyspace(String keyspace) {
        if (keyspace == null || !KEYSPACE.matcher(keyspace).matches() || keyspace.startsWith("system")) {
            throw new IllegalArgumentException("keyspace 只允许小写字母、数字和下划线，并且不能以 system 开头");
        }
        return keyspace;
    }

    public static String requireDatacenter(String datacenter) {
        if (datacenter == null || !DATACENTER.matcher(datacenter).matches()) {
            throw new IllegalArgumentException("datacenter 只允许字母、数字、下划线和中划线");
        }
        return datacenter;
    }

    public static String requireId(String value, String label) {
        String text = blankToNull(value);
        if (text == null || !ID.matcher(text).matches()) {
            throw new IllegalArgumentException(label + "只允许字母、数字、下划线和中划线，最长 64");
        }
        return text;
    }

    public static String requireRegion(String value) {
        String text = blankToNull(value);
        if (text == null || !REGION.matcher(text).matches()) {
            throw new IllegalArgumentException("region 只允许小写字母、数字、下划线和中划线");
        }
        return text;
    }

    public static String requireLabel(String value, String label) {
        String text = blankToNull(value);
        if (text == null || !LABEL.matcher(text).matches()) {
            throw new IllegalArgumentException(label + "只允许字母、数字、下划线和中划线，最长 32");
        }
        return text;
    }

    public static String optionalModel(String value) {
        return optionalName(value, "deviceModel");
    }

    public static String optionalName(String value, String label) {
        String text = blankToNull(value);
        if (text == null) {
            return null;
        }
        if (!MODEL.matcher(text).matches()) {
            throw new IllegalArgumentException(label + "只允许字母、数字、点、下划线和中划线");
        }
        return text;
    }

    public static String requireTagKey(String value) {
        if (value == null || !TAG_KEY.matcher(value).matches()) {
            throw new IllegalArgumentException("tag 的键只允许字母、数字和下划线，并且以字母开头");
        }
        return value;
    }

    public static String requireSnapshot(String tag) {
        if (tag == null || !SNAPSHOT.matcher(tag).matches()) {
            throw new IllegalArgumentException("快照名只允许字母、数字、下划线和中划线，最长 40");
        }
        return tag;
    }

    public static String requireConsistency(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("consistency 不能为空");
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "LOCAL_ONE", "LOCAL_QUORUM", "ONE", "QUORUM" -> value.trim().toUpperCase(Locale.ROOT);
            default -> throw new IllegalArgumentException("consistency 只允许 LOCAL_ONE、LOCAL_QUORUM、ONE、QUORUM");
        };
    }

    public static void requireFactor(int factor) {
        if (factor < 1 || factor > 9) {
            throw new IllegalArgumentException("replication factor 必须在 1 到 9 之间");
        }
    }

    public static void requireDatacenterFactors(Map<String, Integer> datacenters) {
        if (datacenters == null || datacenters.isEmpty()) {
            throw new IllegalArgumentException("NetworkTopologyStrategy 至少要配置一个 datacenter");
        }
        datacenters.forEach((name, factor) -> {
            requireDatacenter(name);
            requireFactor(factor);
        });
    }

    public static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
