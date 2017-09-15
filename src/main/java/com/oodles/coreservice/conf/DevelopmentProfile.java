package com.oodles.coreservice.conf;

/**
 * Deals with Environment configuration bean creation based on <b>dev</b> profile
 * @author Murari Kumar
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")

public class DevelopmentProfile {
	@Bean
	public EnvConfiguration getDevelopmentConfig(){
		return new DevelopmentEnv();
	}

}
