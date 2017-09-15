package com.oodles.coreservice.conf;


/**
 * Includes configuration related to development environment
 * 
 * @author Murari Kumar
 */
import org.springframework.beans.factory.annotation.Value;

public class ProductionEnv implements EnvConfiguration {
	
	@Value("${bitcoinCoreService.db.name.pro}")
	public String dbName;
	@Value("${bitcoinCoreService.db.driver}")
	public String dbDriverName;
	@Value("${bitcoinCoreService.db.port.pro}")
	public String dbPort;
	@Value("${bitcoinCoreService.db.ip.pro}")
	public String dbIpName;
	@Value("${bitcoinCoreService.db.auth.pro.user}")
	public String dbUserName;
	@Value("${bitcoinCoreService.db.auth.pro.pass}")
	public String dbPassword;
	@Value("${bitcoinCoreService.networkParameters.pro}")
	String params;
	@Value("${bitcoinCoreService.blockstore.db.name.pro}")
	String blockstoreDbName;
	@Value("${bitcoinCoreService.bitcoind.port.pro}")
	String bitcoindPort;
	@Value("${bitcoinCoreService.wallet.location.pro}")
	String walletLocation;
	@Value("${bitcoinCoreService.bitpay.url}")
	String bitpayUrl;
	
	@Value("${bitcoinCoreService.enableSecurity.pro}")
	boolean enableSecurity;
	
	@Value("${bitcoinCoreService.bitcoind.ip.pro}")
	String bitcoindUrl;
	
	@Override
	public String getDBName() {
		return dbName;
	}

	@Override
	public String getDBDriver() {
		return dbDriverName;
	}

	@Override
	public String getDBPort() {
		return dbPort;
	}

	@Override
	public String getDBIp() {
		return dbIpName;
	}

	@Override
	public String getDBUser() {
		return dbUserName;
	}

	@Override
	public String getDBPass() {
		return dbPassword;
	}
	
	@Override
	public String getNetworkParams(){
		return params;
	}

	@Override
	public String getBlockStoreDBName() {
		return blockstoreDbName;
	}

	@Override
	public int getBitcoindPort() {
		return Integer.parseInt(bitcoindPort);
	}

	@Override
	public String getWalletLocation() {
		return walletLocation;
	}
	@Override
	public String getBitpayUrl() {
		return bitpayUrl;
	}

	@Override
	public boolean isSecurityEnabled() {
		return enableSecurity;
	}
	
	@Override
	public String getBitcoindIp() {
		return bitcoindUrl;
	}
}
