package it.finanze.sanita.fse2.ms.srvquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class SrvQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SrvQueryApplication.class, args);
	}

}
