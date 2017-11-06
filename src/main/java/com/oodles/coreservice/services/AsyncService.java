/**
 * 
 */
package com.oodles.coreservice.services;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.wallet.Wallet;

/**
 * @author chandan kumar singh
 * @date 03-Nov-2017
 */
public interface AsyncService {
	public void downloadBlockChainAsync(PeerGroup peerGroup,DownloadProgressTracker downloadProgressTracker,Wallet wallet) throws InterruptedException;
}
