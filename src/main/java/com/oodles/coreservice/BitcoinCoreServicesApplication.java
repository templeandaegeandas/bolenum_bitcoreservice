package com.oodles.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * Bitcoin Core Services 
 *
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.oodles.coreservice")
@EnableSwagger2
@EnableAsync
public class BitcoinCoreServicesApplication extends SpringBootServletInitializer{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	    return application.sources(BitcoinCoreServicesApplication.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(BitcoinCoreServicesApplication.class, args);
	}
	
}
