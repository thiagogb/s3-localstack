package br.com.s3.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for S3 Console
 * A Spring Shell application for managing AWS S3 buckets and files
 */
@SpringBootApplication
public class ConsoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsoleApplication.class, args);
	}

} 