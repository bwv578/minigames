package com.games.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.games.controllers", "com.games.application", "com.games.config", "com.games.models"})
public class MinigamessApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(MinigamessApplication.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(MinigamessApplication.class, args);
	}

}
