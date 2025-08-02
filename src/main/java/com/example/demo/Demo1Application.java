package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class Demo1Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Demo1Application.class, args);
        }
    }