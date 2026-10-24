package com.allst.boot.web;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SystemGraphqlController {

    @QueryMapping
    public String health() {
        return "ok";
    }
}
