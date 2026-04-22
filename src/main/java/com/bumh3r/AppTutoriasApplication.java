package com.bumh3r;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class AppTutoriasApplication {

	@PostConstruct
	public void init() { TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City")); }

	public static void main(String[] args) { SpringApplication.run(AppTutoriasApplication.class, args); }

}
