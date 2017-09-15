package com.oodles.coreservice.conf;


/**
 * Deals with Environment configuration bean creation based on <b>pro</b> profile
 * @author Murari Kumar
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("pro")
public class ProductionProfile {
@Bean
public EnvConfiguration getProductionConfig(){
	return new ProductionEnv();
	}
}
