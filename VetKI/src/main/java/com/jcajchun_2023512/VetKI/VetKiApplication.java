package com.jcajchun_2023512.VetKI;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class VetKiApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));
	}

	public static void main(String[] args) {
		SpringApplication.run(VetKiApplication.class, args);
	}

}
