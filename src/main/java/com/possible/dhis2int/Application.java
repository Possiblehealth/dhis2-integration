package com.possible.dhis2int;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.possible.dhis2int")
public class Application {
	public static void main(String[] args) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		SpringApplication.run(Application.class, args);
	}
}