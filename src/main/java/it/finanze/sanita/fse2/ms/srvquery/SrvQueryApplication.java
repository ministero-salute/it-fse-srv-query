/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** 
 * SpringBoot Application 
 *
 */
@SpringBootApplication
@ComponentScan
public class SrvQueryApplication {

	/** 
	 * Runs the SpringBoot Application 
	 * @param args  The args given to main for application start 
	 */
	public static void main(String[] args) {
		SpringApplication.run(SrvQueryApplication.class, args);
	}

}
