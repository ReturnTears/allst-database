package com.allst.boot.guide.jackson;

/**
 * Jackson 视图。Internal 继承 Public，因此内部接口会带上公开字段。
 */
public final class Views {

    private Views() {
    }

    public interface Public {
    }

    public interface Internal extends Public {
    }
}
