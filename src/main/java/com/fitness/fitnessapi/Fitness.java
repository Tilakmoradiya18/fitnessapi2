package com.fitness.fitnessapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Fitness {

	public static void main(String[] args) {
		SpringApplication.run(Fitness.class, args);
	}
}
