package com.allst.boot.guide.dto;

/**
 * 开放投影也可以只声明需要的字段。这里和 {@link NameOnly} 一样用作返回值示例。
 */
public interface EmployeeNameView {

    Long getId();

    String getName();
}
