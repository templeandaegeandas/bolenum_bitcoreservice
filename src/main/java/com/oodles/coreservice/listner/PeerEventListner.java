package com.oodles.coreservice.listner;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;

import com.oodles.coreservice.services.wallet.TransactionPoolManager;
/**
 * Listener for peer group
 * @author Murari Kumar
 */
public class PeerEventListner implements OnTransactionBroadcastListener {
	//private static final Logger log=Logger.getLogger(PeerEventListner.class);
	@Override
    public void onTransaction(Peer peer, Transaction tx) {
//		log.info("onTransaction");
	
		TransactionPoolManager.add(tx);
	}
}
