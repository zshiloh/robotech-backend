package com.robotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "com.robotech.controller",
    "com.robotech.service",
    "com.robotech.repository",
    "com.robotech.config",
    "com.robotech.security",
    "com.robotech.exception"
})
public class RobotechBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotechBackendApplication.class, args);
    }

}