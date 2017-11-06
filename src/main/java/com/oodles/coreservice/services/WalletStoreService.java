/**
 * 
 */
package com.oodles.coreservice.services;

import java.util.Map;

import org.bitcoinj.wallet.Wallet;

import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.listner.CoinReceiveListner;

/**
 * @author chandan kumar singh
 * @date 06-Nov-2017
 */
public interface WalletStoreService {
	public void loadAll();
	public Wallet loadWallet(WalletInfo walletInfo);
	public void walletListner(Wallet wallet, WalletInfo walletInfo);
	public boolean saveWallet(Wallet wallet, String uuid);
	public Map<String, Wallet> getWalletMap();
	public void saveAll();
	public Wallet getWallet(WalletInfo walletInfo);
	public Map<String, CoinReceiveListner> getWalletAndListener();
}
