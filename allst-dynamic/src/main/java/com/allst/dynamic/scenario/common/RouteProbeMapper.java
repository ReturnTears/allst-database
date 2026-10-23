package com.allst.dynamic.scenario.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RouteProbeMapper {

    @Select("SELECT DATABASE()")
    String currentDatabase();
}
