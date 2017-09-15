package com.oodles.coreservice.services.wallet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.oodles.coreservice.services.NetworkParamService;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.wallet.PeerGroupProvider.PeerGroupType;
/**
 * 
 * A class that has methods used to broadcast transactions to blockchain 
 * 
 * @author Murari Kumar
 *
 */
@Component
public class BroadcastThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(BroadcastThread.class);
	private Transaction tx;
	private Wallet wallet;

	private static PeerGroup peerGroup;
	@Autowired
	WalletStoreService walletStoreService;
	@Autowired
	private static NetworkParamService networkParamService;
	private static Vector<String> pendingThreads = new Vector<String>();
	private final static Set<TransactionBroadcast> runningBroadcasts = Collections
			.synchronizedSet(new HashSet<TransactionBroadcast>());

	// private final TransactionBroadcast broadcast ;
	@Autowired
	private NetworkParamService tempNetworkParamService;
	public BroadcastThread() {
		System.out.println("it is default constructor");
	}

	public BroadcastThread(Transaction tx, Wallet wallet, ThreadGroup threadGroup) {
		super(threadGroup, "broadcastThread");
		this.tx = tx;
		this.wallet = wallet;
		// broadcast = new TransactionBroadcast(peerGroup, tx);
	}
	
	public static void initailizePeerGroupAndRunningBroadcast() {
		if (peerGroup == null)
			peerGroup = PeerGroupProvider.get(PeerGroupType.WALLET_REFRESH);
	}
	@PostConstruct
	public void init() {
		System.out.println("init() in BroadcastThread");
		networkParamService = tempNetworkParamService;
		
	}
	
	/**
	 * Run a thread and add pending transaction to vector
	 */
	public void run() {
		pendingThreads.add(tx.getHashAsString());
		boolean brod = false;
		while (!brod) {
			try {
				Context context = new Context(networkParamService.getNetworkParameters());
				Context.propagate(context);
				ListenableFuture<Transaction> txFutrue = peerGroup.broadcastTransaction(tx).broadcast();

				// Transaction txAfterBroadcast = txFutrue.get();

				// broadcast.setMinConnections(peerGroup.getMinBroadcastConnections());
				// addListner(broadcast);
				// runningBroadcasts.add(broadcast);
				// broadcast.broadcast();

				Transaction transaction = txFutrue.get();
				brod = true;
				if (transaction != null) {
					TransactionConfidence confidence = transaction.getConfidence();
					log.debug("after.getDepthInBlocks:: " + confidence.getDepthInBlocks());
					log.debug("after.getConfidenceType:: " + confidence.getConfidenceType());
					log.debug("after.getConfidenceType:: " + tx.getHashAsString());
				}
			} catch (Exception e) {
				System.out.println("run() try block: " + e.getMessage());
			}
		}
		pendingThreads.remove(tx.getHashAsString());
	}
	/**
	 * Listener for transaction
	 * @param broadcast
	 */
	public void addListner(final TransactionBroadcast broadcast) {
		Futures.addCallback(broadcast.future(), new FutureCallback<Transaction>() {
			@Override
			public void onSuccess(Transaction transaction) {
				runningBroadcasts.remove(broadcast);
				try {
					wallet.receivePending(transaction, null);
					walletStoreService.saveWallet(wallet);
				} catch (VerificationException e) {
					System.out.println("addListner(): " + e.getMessage());
				} catch (Exception e) {
					System.out.println("addListner() excepetion block: " + e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				runningBroadcasts.remove(broadcast);
			}
		});
	}

	public static boolean isThreadActiveForTransactionHash(String hash) {
		return pendingThreads.contains(hash);
	}

}
