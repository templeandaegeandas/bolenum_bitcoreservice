package com.oodles.coreservice.services.wallet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.services.NetworkParamService;
import com.oodles.coreservice.services.WalletStoreService;

/**
 * A service that provide method to process transaction
 * 
 * @author Murari Kumar
 *
 */
@Service
public class TransactionPoolManager {

	private Logger log = Logger.getLogger(TransactionPoolManager.class);
	private static Vector<Entry> transactionPool;

	private static WalletStoreService walletStoreService;
	@Autowired
	private WalletStoreService tempWalletStoreService;

	@Autowired
	EnvConfiguration envConfig;
	@Autowired
	NetworkParamService tempNetParams;
	@Autowired
	AddressInfoDao addressInfoDao;

	@PostConstruct
	public void init() {
		log.debug("init() in TransactionPoolManager");
		walletStoreService = tempWalletStoreService;
	}

	static {
		transactionPool = new Vector<Entry>();
	}

	public static Entry find(Transaction tx) {
		Sha256Hash txHash = tx.getHash();
		Entry entry = null;
		Enumeration<Entry> enumeration = transactionPool.elements();
		while (enumeration.hasMoreElements()) {
			entry = enumeration.nextElement();
			if (entry.getTx().getHash().equals(txHash)) {
				return entry;
			}
		}
		return null;
	}

	public static void add(Transaction tx) {
		if (find(tx) != null) {

			return;
		}
		for (Map.Entry<String, Wallet> entry : walletStoreService.getWalletMap().entrySet()) {
			if (entry.getValue().isPendingTransactionRelevant(tx)) {
				transactionPool.add(new Entry(entry.getKey(), tx));
				/**
				 * You can get information about transaction done to
				 * bitcoin-core wallet from outside application instantly
				 * 
				 */
				break;
			}
		}
	}

	/**
	 * Get incoming transaction for a wallet
	 * 
	 * @param walletId
	 * @return
	 */
	public static List<Transaction> getIncomingTransactions(String walletId) {
		Enumeration<Entry> enumeration = transactionPool.elements();
		Entry entry = null;
		List<Transaction> transactions = new ArrayList<Transaction>();
		while (enumeration.hasMoreElements()) {
			entry = enumeration.nextElement();

			if (entry.getId().equals(walletId)) {
				transactions.add(entry.getTx());
			}

		}
		return transactions;
	}

	public static void remove(Transaction tx) {
		Entry entry = find(tx);
		if (entry == null) {
			return;
		}
		transactionPool.remove(entry);
	}

	static class Entry {
		private String id;
		private Transaction tx;

		Entry(String id, Transaction tx) {
			this.id = id;
			this.tx = tx;
		}

		public String getId() {
			return id;
		}

		public Transaction getTx() {
			return tx;
		}

	}

	public static void addTransaction(Transaction tx) {
		if (find(tx) != null) {
			return;
		}
		for (Map.Entry<String, Wallet> entry : walletStoreService.getWalletMap().entrySet()) {
			if (entry.getValue().isPendingTransactionRelevant(tx)) {
				transactionPool.add(new Entry(entry.getKey(), tx));
				break;
			}
		}
	}
}