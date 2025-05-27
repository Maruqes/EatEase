package com.eatease.eatease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EatEaseApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(EatEaseApplication.class, args);
		Testing test = context.getBean(Testing.class);
		test.criar();
		//hello world
	}
}
