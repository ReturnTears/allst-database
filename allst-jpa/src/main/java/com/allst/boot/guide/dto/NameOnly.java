package com.allst.boot.guide.dto;

/**
 * 封闭接口投影。Spring Data 按 getter 生成查询，只选择需要的列。
 */
public interface NameOnly {

    Long getId();

    String getName();
}
