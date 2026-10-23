package com.allst.dynamic.scenario.common;

import org.springframework.stereotype.Component;

@Component
public class RouteProbe {

    private final RouteProbeMapper routeProbeMapper;

    public RouteProbe(RouteProbeMapper routeProbeMapper) {
        this.routeProbeMapper = routeProbeMapper;
    }

    public String database() {
        return routeProbeMapper.currentDatabase();
    }
}
