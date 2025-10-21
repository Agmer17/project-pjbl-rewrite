package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NewBoysanDbApplication { 

	public static void main(String[] args) {
		SpringApplication.run(NewBoysanDbApplication.class, args);
	}

}
