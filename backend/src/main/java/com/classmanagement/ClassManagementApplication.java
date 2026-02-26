package com.classmanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.classmanagement.mapper")
public class ClassManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClassManagementApplication.class, args);
    }
}

