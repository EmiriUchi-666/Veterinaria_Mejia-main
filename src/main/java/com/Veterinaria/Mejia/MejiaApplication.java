package com.Veterinaria.Mejia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class MejiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MejiaApplication.class, args);
	}

}
