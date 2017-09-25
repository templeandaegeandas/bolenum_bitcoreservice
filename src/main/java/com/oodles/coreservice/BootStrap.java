package com.oodles.coreservice;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@Autowired
	WalletStoreService walletStoreService;

	@Value("${bitcoinCoreService.QRcode.location}")
	private String qrFilePath; //qr code file location 

	@Value("${bitcoinCoreService.wallet.location.dev}") 
	private String walletFilePath; // wallet file location
	
	public static Logger log = LoggerFactory.getLogger(BootStrap.class);

	// On application start this method will be automatically called
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		log.debug("onApplicationEvent");
		ConfirmedCoinSelector.minConfidenceLevel = 1;
		// BitCoinPriceUpdateService.startService();
		// BitCoinPriceUpdateService.getCurrencyRate("USD");
		walletStoreService.loadAll();
		WalletRefreshService.startService();
		//create initial directories  
		createInitDirectories();
	}

	// Swagger initialization
	@Bean
	public Docket swaggerSpringMvcPlugin() {
		return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).apiInfo(apiInfo()).select()
				.paths(Predicates.not(PathSelectors.regex("/error.*"))).build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Bitcoin Core Services")
				.description("Bitcoin Core services provides API to create wallet,Generate address of a wallet"
						+ ", Get balance of a wallet, perform transaction, Get transaction details of a transaction")
				.version("1.0").build();
	}

	/**
	 * this will create the wallet and qrcode file location at application start time if location not exist
	 * @description createInitDirectories 
	 * @param 
	 * @return void 
	 * @exception
	 * 
	 */
	private void createInitDirectories() {
		Path qrPath = Paths.get(qrFilePath);
		Path walletPath =  Paths.get(walletFilePath);
		if (!Files.exists(qrPath)) {
			if (new File((qrFilePath)).mkdirs()) {
				log.debug("qrcode location created");
			} else {
				log.debug("qrcode location creation failed");
			}
		} else {
			log.debug("qrcode location exists");
		}
		if (!Files.exists(walletPath)) {
			if (new File((walletFilePath)).mkdirs()) {
				log.debug("wallet file location created");
			} else {
				log.debug("wallet file location creation failed");
			}
		} else {
			log.debug("wallet file location exists");
		}
		
	}

}
