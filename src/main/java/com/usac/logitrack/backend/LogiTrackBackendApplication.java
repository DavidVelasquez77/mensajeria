package com.usac.logitrack.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.usac.logitrack.backend", "controller", "service", "repository", "model"})
public class LogiTrackBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogiTrackBackendApplication.class, args);
    }

}
