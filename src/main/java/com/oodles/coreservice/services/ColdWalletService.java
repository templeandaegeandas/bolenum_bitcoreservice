package com.oodles.coreservice.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.WalletDao;
import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.enums.WalletStatus;
import com.oodles.coreservice.enums.WalletType;
import com.oodles.coreservice.exception.WalletException;
import com.oodles.coreservice.listner.CoinReceiveListner;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.TransactionBroadcastService;
import com.oodles.coreservice.services.wallet.WalletRefreshService;
/**
 *	A service that has methods to perform operation related to cold wallet
 *	@author Ajit Soman
 */
@Service
public class ColdWalletService {
	Map<String, Object> coldWalletMap = new HashMap<String, Object>();
	public static Logger log = LoggerFactory.getLogger(ColdWalletService.class);
	@Autowired
	TransactionBroadcastService transactionBroadcastService;
	@Autowired
	AsyncService asyncService;
	@Autowired
	NetworkParamService networkParamService;
	@Autowired
	WalletDao walletDao;
	@Autowired
	WalletStoreService walletStoreService;
	@Autowired
	EnvConfiguration envConfiguration;
	/**
	 * Create cold wallet and send inputstream of wallet file in response
	 * @param response
	 * @throws IOException
	 * @throws WalletException
	 */
	public void createColdWallet(HttpServletResponse response) throws IOException, WalletException {

		int bits = 128;
		SecureRandom random = new SecureRandom();
		DeterministicKeyChain determinstickeychain = new DeterministicKeyChain(random, bits);
		DeterministicSeed seed = determinstickeychain.getSeed();
		log.debug("create Cold Wallet seed " + seed.getSeedBytes());
		Wallet wallet = Wallet.fromSeed(networkParamService.getNetworkParameters(), seed);
		ECKey eckey = new ECKey();
		wallet.importKey(eckey);

		WalletInfo walletInfo = new WalletInfo();
		walletInfo.setDateCreated(new Date());
		walletInfo.setWalletUuid("" + wallet.getEarliestKeyCreationTime());
		walletInfo.setWalletStatus(WalletStatus.ACTIVE);
		String seedCode = seed.getMnemonicCode().toString().replace(",", "").replace("[", "").replace("]", "");
		walletInfo.setWalletSeedToken(seedCode);
		walletInfo.setWalletType(WalletType.COLD_WALLET);
		WalletInfo savedWalletInfo = walletDao.save(walletInfo);
		if (savedWalletInfo != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wallet.saveToFileStream(baos);
			ByteArrayOutputStream buffer = (ByteArrayOutputStream) baos;
			byte[] bytes = buffer.toByteArray();
			InputStream inputStream = new ByteArrayInputStream(bytes);
			String fileName = walletInfo.getWalletUuid() + ".dat";
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			org.apache.commons.io.IOUtils.copy(inputStream, response.getOutputStream());
			response.setContentType("application/octet-stream");
			response.flushBuffer();
		} else {
			throw new WalletException("Unable to create wallet");
		}
	}
	/**
	 * Synchronize a cold wallet with block chain
	 * 
	 * @param file
	 * @return
	 * @throws UnreadableWalletException
	 * @throws IOException
	 * @throws BlockStoreException
	 * @throws InterruptedException
	 * @throws WalletException
	 */
	public String synchronizeColdWallet(MultipartFile file)
			throws UnreadableWalletException, IOException, BlockStoreException, InterruptedException, WalletException {
		String originalFileName = file.getOriginalFilename();
		int dot = originalFileName.lastIndexOf(".");
		String extension = (dot == -1) ? "" : originalFileName.substring(dot + 1);
		if (extension.equals("dat")) {
				Wallet wallet = Wallet.loadFromFileStream(file.getInputStream());
				WalletInfo walletInfo = walletDao.findByWalletUuid(String.valueOf(wallet.getEarliestKeyCreationTime()));
				if (walletInfo != null) {
					Object object = coldWalletMap.get(String.valueOf(wallet.getEarliestKeyCreationTime()));
					if(object instanceof Wallet){
						throw new WalletException("Wallet is already synchronized");
					}
					BlockStore blockStore = new MemoryBlockStore(networkParamService.getNetworkParameters());
					BlockChain blockChain = new BlockChain(networkParamService.getNetworkParameters(), blockStore);
					blockChain.addWallet(wallet);
					wallet.setCoinSelector(ConfirmedCoinSelector.get());
					walletStoreService.walletListner(wallet, walletInfo);
					PeerGroup peerGroup = new PeerGroup(networkParamService.getNetworkParameters(), blockChain);
					peerGroup.addWallet(wallet);
					peerGroup.addAddress(new PeerAddress(InetAddress.getByName(envConfiguration.getBitcoindIp()),
							envConfiguration.getBitcoindPort()));
					coldWalletMap.put(String.valueOf(wallet.getEarliestKeyCreationTime()), 0 + "% Synchronized wallet");
					DownloadProgressTracker downloadProgressTracker = new DownloadProgressTracker() {
						@Override
						public void doneDownload() {
							coldWalletMap.put(String.valueOf(wallet.getEarliestKeyCreationTime()), wallet);
							WalletRefreshService.addWallet(wallet);
//							PeerGroup existingPeerGroup = PeerGroupProvider.get(PeerGroupType.WALLET_REFRESH);
//							existingPeerGroup.addWallet(wallet);
						}

						@Override
						protected void progress(double pct, int blocksLeft, Date date) {
							Object tempObject = coldWalletMap.get(String.valueOf(wallet.getEarliestKeyCreationTime()));
							if(!(tempObject instanceof Wallet)){
								coldWalletMap.put(String.valueOf(wallet.getEarliestKeyCreationTime()),
										Math.round(pct) + "% Synchronized wallet");
							}
							super.progress(pct, blocksLeft, date);
						}
					};

					peerGroup.startAsync();
					wallet.cleanup();
					wallet.clearTransactions(0);
					blockChain.drainOrphanBlocks();
					peerGroup.setFastCatchupTimeSecs(wallet.getEarliestKeyCreationTime());
					asyncService.downloadBlockChainAsync(peerGroup, downloadProgressTracker, wallet);
					return String.valueOf(wallet.getEarliestKeyCreationTime());
				} else {
					throw new WalletException("Wallet does not belong to our application");
				}
		} else {
			throw new WalletException("Wallet must have extention with .dat");
		}
	}
	/**
	 * Remove wallet from blockchain and stop synchronization
	 * @param walletUuid
	 * @throws WalletException
	 */
	public void removeColdWalletFromBlockChain(String walletUuid) throws WalletException{
		WalletInfo walletInfo = walletDao.findByWalletUuid(walletUuid);
		if(walletInfo!=null){
			Object object = coldWalletMap.get(walletUuid);
			if (object != null && object instanceof Wallet) {
				Wallet wallet  = (Wallet) object;
				WalletRefreshService.removeWallet(wallet);
				wallet.removeCoinsReceivedEventListener(new CoinReceiveListner(walletInfo, wallet));
				//wallet.removeEventListener(new CoinReceiveListner(walletInfo, wallet));
				coldWalletMap.remove(walletUuid);
			} else {
				throw new WalletException("Wallet is not synchronized yet with blockchain");
			}
		}else{
			throw new WalletException("Wallet does not exists");
		}
	}
	/**
	 * Get current synchronized cold wallet
	 * @return
	 */
	public Map<String, Object> getColdWalletMap() {
		return coldWalletMap;
	}
	/**
	 * Get cold wallet address
	 * @param walletUuid
	 * @return
	 * @throws WalletException
	 */
	public String getColdWalletAddress(String walletUuid) throws WalletException {
		Object object = coldWalletMap.get(walletUuid);
		if (object != null) {
			if (object instanceof Wallet) {
				return ((Wallet) object).currentReceiveAddress().toString();
			} else {
				return object.toString();
			}
		} else {
			throw new WalletException("Wallet not found");
		}
	}
	/**
	 * Get cold wallet balance
	 * @param walletUuid
	 * @return
	 * @throws WalletException
	 */
	public String coldWalletBalance(String walletUuid) throws WalletException {
		Object object = coldWalletMap.get(walletUuid);
		if (object != null) {
			if (object instanceof Wallet) {
					log.debug("wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE)------->"
						+ ((Wallet) object).getBalance(BalanceType.AVAILABLE_SPENDABLE));
					log.debug("wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE)------->"
						+ ((Wallet) object).getBalance(BalanceType.ESTIMATED_SPENDABLE));
				
				return ((Wallet) object).getBalance(BalanceType.ESTIMATED_SPENDABLE).toFriendlyString();
			} else {
				return object.toString();
			}
		} else {
			throw new WalletException("Wallet not found");
		}
	}
	/**
	 * Recover cold wallet on lost
	 * 
	 * @param walletUuid
	 * @param response
	 * @throws WalletException
	 * @throws UnreadableWalletException
	 * @throws IOException
	 */
	public void recoverColdWallet(String walletUuid, HttpServletResponse response) throws WalletException, UnreadableWalletException, IOException{
		WalletInfo walletInfo = walletDao.findByWalletUuid(walletUuid);
		if(walletInfo!=null){
			String seedCode = walletInfo.getWalletSeedToken();
	        String passphrase = "";
	        Long earliestCreationTime = Long.valueOf(walletInfo.getWalletUuid());
	        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, earliestCreationTime);
	        Wallet wallet = Wallet.fromSeed(networkParamService.getNetworkParameters(), seed);
	     
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wallet.saveToFileStream(baos);
			ByteArrayOutputStream buffer = (ByteArrayOutputStream) baos;
			byte[] bytes = buffer.toByteArray();
			InputStream inputStream = new ByteArrayInputStream(bytes);
			String fileName = walletInfo.getWalletUuid() + ".dat";
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			org.apache.commons.io.IOUtils.copy(inputStream, response.getOutputStream());
			response.setContentType("application/octet-stream");
			response.flushBuffer();
		}else{
			throw new WalletException("Wallet not found");
		}
	}
}
