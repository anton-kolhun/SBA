package com.kolhun.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class LandingController {

    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(LandingController.class);

    @RequestMapping
    public String indexPage() {
        logger.debug("hello");
        return "200";
    }
}
