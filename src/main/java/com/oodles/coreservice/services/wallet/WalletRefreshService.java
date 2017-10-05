package com.oodles.coreservice.services.wallet;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oodles.coreservice.dao.WalletDao;
import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.listner.CoinReceiveListner;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.PeerGroupProvider.PeerGroupType;

/**
 * A service that deals with wallet synchronization on application start
 * 
 * @author Murari Kumar
 *
 */
@Component
public class WalletRefreshService extends Thread {
	private static Logger log = LoggerFactory.getLogger(ConfirmedCoinSelector.class);
	public static WalletStoreService walletStoreService;
	public static WalletDao walletDao;
	private static WalletRefreshService thread;
	// private static BlockChain chain;
	private static AbstractBlockChain chain;
	public static PeerGroup peerGroup;

	private static boolean syncForFirstTime = true;
	public static boolean result = true;

	@Autowired
	private WalletStoreService tempWalletStoreService;
	@Autowired
	private WalletDao tempWalletDao;

	@PostConstruct
	public void init() {
		walletStoreService = tempWalletStoreService;
		walletDao = tempWalletDao;
	}

	/**
	 * Add wallet to block chain and setup peerGroup
	 */
	public static void initWalletRefreshService() {
		chain = BlockChainProvider.get();
		peerGroup = PeerGroupProvider.get(PeerGroupType.WALLET_REFRESH);
		// chain.addWallet(wallet);
		Iterator<Wallet> iterator = walletStoreService.getWalletMap().values().iterator();
		int chainHeight = 0;
		int lastBlockSeenHeight = 0;
		while (iterator.hasNext()) {
			log.debug("inside while loop");
			Wallet wallet = iterator.next();
			wallet.cleanup();
			wallet.clearTransactions(0);
			// long walletTime;
			lastBlockSeenHeight = wallet.getLastBlockSeenHeight();
			if (chainHeight < lastBlockSeenHeight) {
				chainHeight = lastBlockSeenHeight;
			}
			chain.addWallet(wallet);
		}
		WalletInfo walletInfo = walletDao.findFirstByOrderByDateCreatedAsc();
		if (walletInfo != null) {
			if (walletInfo.getWalletUuid().equals("adminWallet")) {
				Wallet adminWallet = walletStoreService.getWalletMap().get("adminWallet");
				peerGroup.setFastCatchupTimeSecs(adminWallet.getEarliestKeyCreationTime());
				log.debug("getEarliestKeyCreationTime  of adminWallet: {} ", adminWallet.getEarliestKeyCreationTime());
			} else {
				// to do syncing fast
				// peerGroup.setFastCatchupTimeSecs(Long.valueOf(walletInfo.getWalletUuid()));
				if (walletInfo.getWalletEarliestKeyCreationTime() != null) {
					peerGroup.setFastCatchupTimeSecs(Long.parseLong(walletInfo.getWalletEarliestKeyCreationTime()));
				}
				log.debug("getEarliestKeyCreationTime  of Other wallet: {} ",
						walletInfo.getWalletEarliestKeyCreationTime());
			}
		}
		ConfirmedCoinSelector.setChainHeight(chainHeight);
		CoinReceiveListner.addListnerForUnconfirmedTransactions();
		// peerGroup.setFastCatchupTimeSecs(time);
	}

	/**
	 * Download block chain
	 */
	@Override
	public void run() {
		log.debug("run() method");
		peerGroup.start();
		// peerGroup.awaitRunning();
		while (!isInterrupted()) {
			log.debug("going to download");
			try {
				peerGroup.downloadBlockChain();
				chain = BlockChainProvider.get();
				chain.drainOrphanBlocks();
				log.debug("Full Downloaded...!!");
				result = false;
				if (syncForFirstTime) {
					// PeerGroupProvider.upgrade(peerGroup);
					TransactionBroadcastService.startService();
					syncForFirstTime = false;
				}
			} catch (Exception e) {
				log.error("downloadBlockChain try block exception: " + e.getMessage());
			}

			// sleep thread for 6 minutes
			try {
				TransactionBroadcastService.startService();
				sleep(1000 * 60 * 6);
				TransactionBroadcastService.stopService();

			} catch (InterruptedException e) {
				log.error("sleep catch block 1: " + e.getMessage());
			}
		}

		peerGroup.stop();
		walletStoreService.saveAll();
	}

	/**
	 * Start wallet refresh service
	 */
	public static void startService() {
		initWalletRefreshService();

		if (thread == null || !thread.isAlive()) {
			thread = new WalletRefreshService();
			thread.start();
		}
	}

	/**
	 * Stop wallet refresh service
	 */
	public static void stopService() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	public static AbstractBlockChain getBlockChain() {
		return chain;
	}

	/**
	 * Add wallet to chain
	 * 
	 * @param wallet
	 */
	public static void addWallet(Wallet wallet) {
		log.debug("addWallet()");
		if (wallet != null && chain != null) {
			chain.addWallet(wallet);

		}

	}

	public boolean getStatus(boolean result) {
		return result;
	}

	/**
	 * Remove wallet from block chain
	 * 
	 * @param wallet
	 */
	public static void removeWallet(Wallet wallet) {
		log.debug("removeWallet()");
		if (wallet != null && chain != null) {
			chain.removeWallet(wallet);
		}
	}
}
