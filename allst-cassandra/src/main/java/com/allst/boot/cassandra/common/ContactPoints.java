package com.allst.boot.cassandra.common;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public final class ContactPoints {

    private ContactPoints() {
    }

    public static List<InetSocketAddress> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("contact-points 不能为空");
        }
        String[] parts = raw.split(",");
        List<InetSocketAddress> addresses = new ArrayList<>();
        for (String part : parts) {
            String item = part.trim();
            if (item.isEmpty()) {
                continue;
            }
            String host = item;
            int port = 9042;
            int colon = item.lastIndexOf(':');
            if (colon > 0 && item.indexOf(':') == colon) {
                host = item.substring(0, colon);
                String portText = item.substring(colon + 1);
                if (!portText.matches("\\d{1,5}")) {
                    throw new IllegalArgumentException("contact-points 端口不正确: " + item);
                }
                port = Integer.parseInt(portText);
            }
            if (!host.matches("[A-Za-z0-9._-]{1,253}") || port < 1 || port > 65535) {
                throw new IllegalArgumentException("contact-points 地址不正确: " + item);
            }
            addresses.add(InetSocketAddress.createUnresolved(host, port));
        }
        if (addresses.isEmpty() || addresses.size() > 8) {
            throw new IllegalArgumentException("contact-points 需要 1 到 8 个地址");
        }
        return List.copyOf(addresses);
    }
}
