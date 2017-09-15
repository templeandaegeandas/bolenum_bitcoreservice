package com.oodles.coreservice.conf;

/**
 * Includes configuration related to development environment
 * @author Murari Kumar
 */
import org.springframework.beans.factory.annotation.Value;

public class DevelopmentEnv implements EnvConfiguration {
	
	@Value("${bitcoinCoreService.db.name.dev}")
	private String dbName;
	@Value("${bitcoinCoreService.db.driver}")
	private String dbDriverName;
	@Value("${bitcoinCoreService.db.port.dev}")
	private String dbPort;
	@Value("${bitcoinCoreService.db.ip.dev}")
	private String dbIpName;
	@Value("${bitcoinCoreService.db.auth.dev.user}")
	private String dbUserName;
	@Value("${bitcoinCoreService.db.auth.dev.pass}")
	private String dbPassword;
	@Value("${bitcoinCoreService.networkParameters.dev}")
	String params;
	@Value("${bitcoinCoreService.blockstore.db.name.dev}")
	String blockstoreDbName;
	@Value("${bitcoinCoreService.bitcoind.port.dev}")
	String bitcoindPort;
	@Value("${bitcoinCoreService.wallet.location.dev}")
	String walletLocation;
	@Value("${bitcoinCoreService.bitpay.url}")
	String bitpayUrl;
	
	@Value("${bitcoinCoreService.enableSecurity.dev}")
	boolean enableSecurity;
	
	@Value("${bitcoinCoreService.bitcoind.ip.dev}")
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
