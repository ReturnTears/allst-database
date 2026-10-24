package com.allst.boot.cassandra.common;

import java.nio.ByteBuffer;
import java.util.Base64;

public final class PageTokens {

    private PageTokens() {
    }

    public static String encode(ByteBuffer pagingState) {
        ByteBuffer copy = copy(pagingState);
        if (copy == null) {
            return null;
        }
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        if (bytes.length == 0) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static ByteBuffer decode(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (!token.matches("[A-Za-z0-9_-]{1,4096}")) {
            throw new IllegalArgumentException("pagingState 格式不正确");
        }
        try {
            return ByteBuffer.wrap(Base64.getUrlDecoder().decode(token));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("pagingState 无法解码");
        }
    }

    public static ByteBuffer copy(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        ByteBuffer source = buffer.duplicate();
        byte[] bytes = new byte[source.remaining()];
        source.get(bytes);
        return bytes.length == 0 ? null : ByteBuffer.wrap(bytes);
    }
}
