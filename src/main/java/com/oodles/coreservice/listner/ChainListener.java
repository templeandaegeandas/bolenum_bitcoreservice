package com.oodles.coreservice.listner;

import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.listeners.NewBestBlockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
/**
 * Listener for block chain 
 *	
 * @author Murari Kumar
 */
public class ChainListener implements NewBestBlockListener {
	public static Logger log = LoggerFactory.getLogger(ChainListener.class);
	
	@Override
	public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
		int chainHeight=block.getHeight();
		if(chainHeight!=0){
			ConfirmedCoinSelector.setChainHeight(chainHeight);
		}
	}
}
