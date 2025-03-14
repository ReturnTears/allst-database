package com.allst.es.controller;

import com.allst.es.annotations.DecodeBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DemoController {
    /**
     * 加密后的参数： SGVsbG8gc3ByaW5nZG9jLmNu
     * 参数2：SGVsbG8g5L2g5aW9LCBIZWxsbyBXb3JsZA==
     */
    @PostMapping("/demo")
    public ResponseEntity<String> demo(@RequestBody @DecodeBody String payload) {
        return ResponseEntity.ok(payload);
    }
}
