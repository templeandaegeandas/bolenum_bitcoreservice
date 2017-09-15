package com.oodles.coreservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicates;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.WalletRefreshService;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
/**
 * 
 * Bitcoin Core Services Bootstap
 *
 */
@Component
public class BootStrap implements ApplicationListener<ContextRefreshedEvent> {
	@Autowired WalletStoreService walletStoreService;
	public static Logger log = LoggerFactory.getLogger(BootStrap.class);
	
	// On application start this method will be automatically called
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		log.debug("onApplicationEvent");
		ConfirmedCoinSelector.minConfidenceLevel=1;
		//BitCoinPriceUpdateService.startService();
		//BitCoinPriceUpdateService.getCurrencyRate("USD");
		walletStoreService.loadAll();
		WalletRefreshService.startService();
		
	}
	
	// Swagger initialization
	 @Bean
	    public Docket swaggerSpringMvcPlugin() {
	        return new Docket(DocumentationType.SWAGGER_2)
	            .useDefaultResponseMessages(false)
	            .apiInfo(apiInfo())
	            .select()
	            .paths(Predicates.not(PathSelectors.regex("/error.*")))
	            .build();
	    }
	    
	    private ApiInfo apiInfo() {
	        return new ApiInfoBuilder()
	            .title("Bitcoin Core Services")
	            .description("Bitcoin Core services provides API to create wallet,Generate address of a wallet"
	            		+ ", Get balance of a wallet, perform transaction, Get transaction details of a transaction")
	            .version("1.0")
	            .build();
	    }
	
	
}
