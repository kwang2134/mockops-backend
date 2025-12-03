package com.mockops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableJpaAuditing
@SpringBootApplication
public class MockOpsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockOpsBackendApplication.class, args);
	}

}
