package com.oodles.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * Bitcoin Core Services 
 *
 */
@SpringBootApplication
//@Configuration
//@EnableAutoConfiguration
//@ComponentScan("com.oodles.coreservice")
@EnableSwagger2
@EnableAsync
public class BitcoinCoreServicesApplication {
	public static void main(String[] args) {
		SpringApplication.run(BitcoinCoreServicesApplication.class, args);
	}
}
