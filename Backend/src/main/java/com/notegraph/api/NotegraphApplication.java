package com.notegraph.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotegraphApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotegraphApplication.class, args);
	}

}
