package com.macrowing.indaas.upm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.macrowing")
public class IndaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndaasApplication.class, args);
    }

}
