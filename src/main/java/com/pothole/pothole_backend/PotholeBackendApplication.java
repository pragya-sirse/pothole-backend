package com.pothole.pothole_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PotholeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotholeBackendApplication.class, args);
	}

}
