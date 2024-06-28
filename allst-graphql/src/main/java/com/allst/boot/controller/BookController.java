package com.allst.boot.controller;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hutu
 * @since 2024-06-28 下午 11:02
 */
@RestController
@RequestMapping("/graphql")
public class BookController {

    @Autowired
    private GraphQL graphQL;

    @PostMapping("/book")
    public ResponseEntity<Object> executeQuery(@RequestBody String query) {
        ExecutionResult executionResult = graphQL.execute(query);
        return ResponseEntity.ok(executionResult.toSpecification());
    }
}
