package com.github.crimscon.autoconfigure.springdoc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mvc/test/")
public class MessageController {

    @GetMapping("/message")
    public String getMessage() {
        return "message";
    }

}
