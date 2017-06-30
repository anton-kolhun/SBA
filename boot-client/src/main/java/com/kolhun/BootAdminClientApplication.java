package com.kolhun;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("com.kolhun")
@Import(value = de.codecentric.boot.admin.config.SpringBootAdminClientAutoConfiguration.class)
@PropertySource("classpath:application.properties")
public class BootAdminClientApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BootAdminClientApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BootAdminClientApplication.class);
    }
}
