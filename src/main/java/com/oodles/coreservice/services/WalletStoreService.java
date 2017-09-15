package com.oodles.coreservice.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.Wallet;
import org.bitcoinj.store.UnreadableWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.WalletDao;
import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.enums.WalletStatus;
import com.oodles.coreservice.enums.WalletType;
import com.oodles.coreservice.listner.CoinReceiveListner;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
/**
 * A Service that Manages all hot wallet and this service has methods 
 * to load wallet and save wallet
 * @author Murari Kumar
 */
@Service
public class WalletStoreService {
	public static Logger log = LoggerFactory.getLogger(WalletStoreService.class);
	@Autowired
	WalletDao walletDao;
	@Autowired
	EnvConfiguration envConfiguration;

	Map<String, Wallet> walletMap = new HashMap<String, Wallet>();
	/**
	 * Load all wallet to map
	 */
	public void loadAll() {
		List<WalletInfo> walletInfoList = walletDao.getAllWalletOtherThanSpecifiedWallet(WalletStatus.ACTIVE,WalletType.COLD_WALLET);
		Iterator<WalletInfo> itr = walletInfoList.iterator();
		while (itr.hasNext()) {
			WalletInfo walletInfo = itr.next();
			Wallet wallet = loadWallet(walletInfo);
			if (wallet != null) {
				walletMap.put(walletInfo.getWalletUuid(), wallet);
			}
		}
	}
	/**
	 * Load wallet individually
	 * @param walletInfo
	 * @return
	 */
	public Wallet loadWallet(WalletInfo walletInfo) {
		Wallet wallet = null;
		try {
			wallet = Wallet.loadFromFile(new File(getWalletName(walletInfo.getWalletUuid())));
			if (wallet != null) {
				wallet.setCoinSelector(ConfirmedCoinSelector.get());
				// ConfirmedCoinSelector.setMinConfidenceLevel(3);
				walletListner(wallet, walletInfo);
			}
		} catch (UnreadableWalletException e) {
			log.error("loadWallet() IOException caught msg:" + e.getMessage());
		} catch (Exception e) {
			log.error("loadWallet() General Exception caught msg:" + e.getMessage());
		}
		return wallet; // return wallet current balance
	}
	/**
	 * Add bitcoin listener for wallet
	 * @param wallet
	 * @param walletInfo
	 */
	public void walletListner(Wallet wallet, WalletInfo walletInfo) {
		wallet.addEventListener(new CoinReceiveListner(walletInfo, wallet));
		// wallet.allowSpendingUnconfirmedTransactions();
	}
	/**
	 * Get wallet location
	 * @param walletUuid
	 * @return
	 */
	private String getWalletName(String walletUuid) {
		String fileName = envConfiguration.getWalletLocation() + '/' + walletUuid + ".dat";
		return fileName;
	}
	/**
	 * Save wallet to disk
	 * @param wallet
	 * @return
	 */
	public boolean saveWallet(Wallet wallet) {
		String walletUuid;
		Wallet adminWallet=getWalletMap().get("adminWallet");
		if(adminWallet!=null && Long.valueOf(wallet.getEarliestKeyCreationTime()).equals(adminWallet.getEarliestKeyCreationTime())){
			walletUuid="adminWallet";
		}else{
			walletUuid = String.valueOf(wallet.getEarliestKeyCreationTime());
		}
		String walletName = envConfiguration.getWalletLocation() + '/' + walletUuid + ".dat";
		WalletInfo walletInfo = walletDao.findByWalletUuid(walletUuid);
		if(walletInfo.getWalletType().equals(WalletType.COLD_WALLET)){
			return true;
		}
		try {
			wallet.saveToFile(new File(walletName));
			return true; // indicates wallet save successfully
		} catch (IOException e) {
			log.error("saveWallet() exception caught msg:" + e.getMessage());
			return false;
		}
	}
	/**
	 * Get hot wallets
	 * @return
	 */
	public Map<String, Wallet> getWalletMap() {
		return walletMap;
	}
	/**
	 * Save all wallet
	 */
	public void saveAll() {
		System.out.println("saveAll()");
		try {
			Iterator<Wallet> itr = walletMap.values().iterator();
			while (itr.hasNext()) {
				saveWallet(itr.next());
			}
		} catch (NullPointerException exception) {
			System.out.println("saveAll() walletMap is null " + exception.getMessage());
		} catch (Exception e) {
			System.out.println("saveAll() general exception block:" + e.getMessage());
		}
	}
	/**
	 * Get wallet object
	 * @param walletInfo
	 * @return
	 */
	public Wallet getWallet(WalletInfo walletInfo) {
		return walletMap.get(walletInfo.getWalletUuid());
	}

}
