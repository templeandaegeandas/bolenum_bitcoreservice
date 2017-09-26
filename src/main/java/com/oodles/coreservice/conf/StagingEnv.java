package com.oodles.coreservice.conf;

/**
 * Includes configuration related to development environment
 * @author Murari Kumar
 */
import org.springframework.beans.factory.annotation.Value;

public class StagingEnv implements EnvConfiguration {
	
	@Value("${bitcoinCoreService.db.name.stag}")
	private String dbName;
	@Value("${bitcoinCoreService.db.port.stag}")
	private String dbPort;
	@Value("${bitcoinCoreService.db.ip.stag}")
	private String dbIpName;
	@Value("${bitcoinCoreService.db.auth.stag.user}")
	private String dbUserName;
	@Value("${bitcoinCoreService.db.auth.stag.pass}")
	private String dbPassword;
	@Value("${bitcoinCoreService.networkParameters.stag}")
	String params;
	@Value("${bitcoinCoreService.blockstore.db.name.stag}")
	String blockstoreDbName;
	@Value("${bitcoinCoreService.bitcoind.port.stag}")
	String bitcoindPort;
	@Value("${bitcoinCoreService.wallet.location.stag}")
	String walletLocation;
	@Value("${bitcoinCoreService.bitpay.url}")
	String bitpayUrl;
	
	@Value("${bitcoinCoreService.enableSecurity.stag}")
	boolean enableSecurity;
	
	@Value("${bitcoinCoreService.bitcoind.ip.stag}")
	String bitcoindUrl;
	
	@Override
	public String getDBName() {
		return dbName;
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
