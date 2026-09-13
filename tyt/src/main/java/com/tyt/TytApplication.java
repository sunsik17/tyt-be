package com.tyt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TytApplication {

	public static void main(String[] args) {
		SpringApplication.run(TytApplication.class, args);
	}

}
