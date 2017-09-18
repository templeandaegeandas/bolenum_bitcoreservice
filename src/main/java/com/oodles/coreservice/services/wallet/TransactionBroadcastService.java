package com.oodles.coreservice.services.wallet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oodles.coreservice.services.NetworkParamService;
import com.oodles.coreservice.services.WalletStoreService;

/**
 * A service that uses <b>BroadcastThread</b> class methods to broadcast transactions to blockchain 
 * 
 * @author Murari Kumar
 *
 */
@Component
public class TransactionBroadcastService extends Thread {
	public static Logger log = LoggerFactory.getLogger(TransactionBroadcastService.class);
	@Autowired
	WalletStoreService tempWalletStoreService;
	private static TransactionBroadcastService thread;
	private static WalletStoreService walletStoreService;
	@Autowired
	private NetworkParamService networkParamService;
	private ThreadGroup threadGroup;
	@Autowired
	private NetworkParamService tempNetworkParamService;

	private static Map<String, Transaction> txMap = new HashMap<String, Transaction>();

	public TransactionBroadcastService() {
		threadGroup = new ThreadGroup("broadcastThreads");
	}

	@PostConstruct
	public void init() {
		networkParamService = tempNetworkParamService;
		walletStoreService = tempWalletStoreService;
	}
	/**
	 * broadcast pending transaction to blockchain
	 */
	@Override
	public void run() {
		int count = 0;
		while (!isInterrupted()) {
			log.debug("count: " + count);
			try {
				Iterator<Wallet> iterator = walletStoreService.getWalletMap().values().iterator();
				while (iterator.hasNext()) {
					Wallet wallet = iterator.next();
					// Log.info("wallet details "+wallet);
					Iterator<Transaction> itr = wallet.getTransactions(true).iterator();
					while (itr.hasNext()) {
						Transaction tx = itr.next();
						if (tx != null) {
							addTransaction(tx);
						}
						Context context = new Context(networkParamService.getNetworkParameters());
						Context.propagate(context);
						TransactionConfidence confidence = tx.getConfidence();
						if (confidence.getDepthInBlocks() < 1
								|| confidence.getConfidenceType().equals(ConfidenceType.PENDING)) {
							BroadcastThread.initailizePeerGroupAndRunningBroadcast();
							if (!BroadcastThread.isThreadActiveForTransactionHash(tx.getHashAsString())) {
								log.debug("BroadcastThread is started for "+tx.getHashAsString()+" which has getDepthInBlocks "+confidence.getDepthInBlocks()+" and type status "+confidence.getConfidenceType());
								BroadcastThread thread = new BroadcastThread(tx, wallet, threadGroup);
								count++;
								thread.start();
							}
						}
					}
				}

				try {
					Thread.sleep(1000 * 60);
				} catch (InterruptedException e) {
					log.debug("sleep try block");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Start transaction broadcast service
	 */
	public static void startService() {
		if (thread == null || !thread.isAlive()) {
			thread = new TransactionBroadcastService();
			thread.start();
		}
	}
	/**
	 * Stop transaction broadcast service
	 */
	public static void stopService() {
		if (thread != null) {
			thread.threadGroup.interrupt();
			thread.interrupt();
		}
	}
	/**
	 * Add transaction to txMap
	 * @param tx
	 */
	public void addTransaction(Transaction tx) {
		txMap.put(tx.getHashAsString(), tx);
	}
	/**
	 * Get txMap
	 * @return
	 */
	public Map<String, Transaction> getTxMap() {
		return txMap;
	}
}
