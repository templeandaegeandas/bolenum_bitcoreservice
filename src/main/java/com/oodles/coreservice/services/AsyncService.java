package com.oodles.coreservice.services;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.wallet.Wallet;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
/**
 *	A service that has methods to do a task asynchronously 
 *	@author Ajit Soman
 */
@Service
public class AsyncService {
	/**
	 * Download block chain Asynchronously and stop once completed
	 * @param peerGroup
	 * @param downloadProgressTracker
	 * @param wallet
	 * @throws InterruptedException
	 */
	@Async
	public void downloadBlockChainAsync(PeerGroup peerGroup,DownloadProgressTracker downloadProgressTracker,Wallet wallet) throws InterruptedException{
		peerGroup.startBlockChainDownload(downloadProgressTracker);
		downloadProgressTracker.await();
		peerGroup.stopAsync();
	}

}
