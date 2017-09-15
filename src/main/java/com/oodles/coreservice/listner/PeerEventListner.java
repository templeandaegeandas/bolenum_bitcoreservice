package com.oodles.coreservice.listner;

import org.apache.log4j.Logger;
import org.bitcoinj.core.AbstractPeerEventListener;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;

import com.oodles.coreservice.services.wallet.TransactionPoolManager;
/**
 * Listener for peer group
 * @author Murari Kumar
 */
public class PeerEventListner extends AbstractPeerEventListener {
	private static final Logger log=Logger.getLogger(PeerEventListner.class);
	@Override
    public void onTransaction(Peer peer, Transaction tx) {
		log.info("onTransaction");
		
		TransactionPoolManager.add(tx);
	}
}
