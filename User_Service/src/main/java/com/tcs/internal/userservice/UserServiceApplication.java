package com.tcs.internal.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
public class UserServiceApplication extends WebMvcConfigurationSupport {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
