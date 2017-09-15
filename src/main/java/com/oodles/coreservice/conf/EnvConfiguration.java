package com.oodles.coreservice.conf;

/**
 * 
 * Common interface for all environment profile
 *
 * @author Murari Kumar
 */
public interface EnvConfiguration {
	String getDBName();

	String getDBDriver();

	String getDBPort();

	String getDBIp();

	String getDBUser();

	String getDBPass();
	
	String getNetworkParams();
	
	String getBlockStoreDBName();
	
	int getBitcoindPort();
	
	String getWalletLocation();
	
	String getBitpayUrl();
	
	boolean isSecurityEnabled();
	
	String getBitcoindIp();

}
