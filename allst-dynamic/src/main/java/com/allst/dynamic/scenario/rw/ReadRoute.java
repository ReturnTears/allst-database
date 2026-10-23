package com.allst.dynamic.scenario.rw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 读路径走 rwSlave 组。请求头 X-Force-Master=true，或本请求已经写过主库时，改走 rwMaster。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadRoute {
}
