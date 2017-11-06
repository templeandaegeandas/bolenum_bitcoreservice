package com.oodles.coreservice.listner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.wallet.DefaultCoinSelector;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.services.TransactionService;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.TransactionPoolManager;

/**
 * Bitcoin receive listener for our application bitcoin wallets
 * 
 * @author Murari Kumar
 *
 */
@Component
public class CoinReceiveListner implements WalletCoinsReceivedEventListener {
	@Autowired
	WalletStoreService tempWalletStoreService;
	@Autowired
	TransactionService tempTransactionService;

	private static WalletStoreService walletStoreService;
	private static TransactionService transactionService;

	private static Logger log = LoggerFactory.getLogger(CoinReceiveListner.class);

	private WalletInfo walletInfo;

	private static boolean isAddListnerForUnconfirmedTransactionsRunning;
	private static Map<WalletInfo, Wallet> walletAndWalletInfoMap = new HashMap<WalletInfo, Wallet>();

	@PostConstruct
	public void init() {

		walletStoreService = tempWalletStoreService;
		transactionService = tempTransactionService;
	}

	public CoinReceiveListner() {

	}

	public CoinReceiveListner(WalletInfo walletInfo, Wallet wallet) {
		log.info("onCoinsReceived 1: {}", walletStoreService);
		this.walletInfo = walletInfo;
		if (!isAddListnerForUnconfirmedTransactionsRunning) {
			walletAndWalletInfoMap.put(walletInfo, wallet);
		}
	}

	public CoinReceiveListner(WalletInfo walletInfo) {
		// TODO Auto-generated constructor stub
		log.info("onCoinsReceived 2: {}", walletStoreService);
		this.walletInfo = walletInfo;

	}

	@Override
	public void onCoinsReceived(final Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		log.info("onCoinsReceived 3: {}", walletStoreService);
		TransactionConfidence confidence = tx.getConfidence();
		confidence.addEventListener(new TransactionConfidence.Listener() {
			@Override
			public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
				boolean result = DefaultCoinSelector.isSelectable(tx);
				if (result) {
					walletStoreService.saveWallet(wallet, walletInfo.getWalletUuid());
					// remove transaction from unconfirmed memory pool
					if (tx.getValueSentFromMe(wallet).value == 0) {
						TransactionPoolManager.remove(tx);
						addFutureCallback(tx, wallet, walletInfo.getWalletUuid());
					}
				}
			}
		});
	}

	private static CoinReceiveListner getWalletListner(WalletInfo walletInfo) {
		Map<String, CoinReceiveListner> map = walletStoreService.getWalletAndListener();
		CoinReceiveListner coinReceiveListner = map.get(walletInfo.getWalletUuid());
		log.debug("Exiting wallet listener: {}", coinReceiveListner);
		if (coinReceiveListner != null) {
			return coinReceiveListner;
		}
		return new CoinReceiveListner(walletInfo);
	}

	public static void addListnerForUnconfirmedTransactions() {
		log.debug("addListnerForUnconfirmedTransactions()");
		isAddListnerForUnconfirmedTransactionsRunning = true;
		Wallet wallet;
		WalletInfo walletInfo;
		Map.Entry<WalletInfo, Wallet> entry;
		Transaction tx;
		Iterator<Map.Entry<WalletInfo, Wallet>> entryIterator = walletAndWalletInfoMap.entrySet().iterator();
		while (entryIterator.hasNext()) {
			entry = entryIterator.next();
			wallet = entry.getValue();
			walletInfo = entry.getKey();
			Iterator<Transaction> iterator = wallet.getTransactions(false).iterator();
			while (iterator.hasNext()) {
				tx = iterator.next();
				if (tx.getValueSentFromMe(wallet).value == 0 && !ConfirmedCoinSelector.isConfirmed(tx)) {
					getWalletListner(walletInfo).addFutureCallback(tx, wallet, walletInfo.getWalletUuid());
				}
			}
		}
	}

	public void addFutureCallback(Transaction tx, final Wallet wallet, String uuid) {
		log.debug("addFutureCallback()");
		Futures.addCallback(tx.getConfidence().getDepthFuture(ConfirmedCoinSelector.getMinConfidenceLevel()),
				new FutureCallback<TransactionConfidence>() {
					@Override
					public void onSuccess(TransactionConfidence result) {
						log.debug("onSuccess method result: {}", result);
						// walletDirectTransactionService.saveTransaction(user,wallet,result);
						String walletUuid = null;
						try {

							Wallet adminWallet = walletStoreService.getWalletMap().get("adminWallet");
							if (adminWallet != null && Long.valueOf(wallet.getEarliestKeyCreationTime())
									.equals(adminWallet.getEarliestKeyCreationTime())) {
								walletUuid = "adminWallet";
							} else {
								if (uuid != null) {
									walletUuid = uuid;
								} else {
									walletUuid = String.valueOf(wallet.getEarliestKeyCreationTime());
								}
							}
							transactionService.saveTransactionReceiveInfo(wallet, tx, walletUuid);
						} catch (Exception e) {
							log.error("Issue in saving transaction: {}", e.getMessage());
							e.printStackTrace();
						}
						walletStoreService.saveWallet(wallet, walletUuid);
						// advertisementService.autoEnable(user);
					}

					@Override
					public void onFailure(Throwable t) {
						// throw new RuntimeException(t);
					}
				});
	}
}
