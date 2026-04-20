package com.godare.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoDareApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoDareApplication.class, args);
	}
}