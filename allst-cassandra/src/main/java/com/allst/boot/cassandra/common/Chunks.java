package com.allst.boot.cassandra.common;

import java.util.ArrayList;
import java.util.List;

public final class Chunks {

    private Chunks() {
    }

    public static <T> List<List<T>> split(List<T> items, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("分片大小必须大于 0");
        }
        List<List<T>> chunks = new ArrayList<>();
        for (int offset = 0; offset < items.size(); offset += size) {
            chunks.add(List.copyOf(items.subList(offset, Math.min(offset + size, items.size()))));
        }
        return chunks;
    }
}
