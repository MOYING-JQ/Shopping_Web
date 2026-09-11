package com.moying.project_test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.moying.project_test.mapper")
public class ProjectTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTestApplication.class, args);
    }

}
