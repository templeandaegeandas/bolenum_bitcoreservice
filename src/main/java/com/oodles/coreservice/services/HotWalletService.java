package com.oodles.coreservice.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.hibernate.persister.walking.spi.WalkingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.dao.TransactionDao;
import com.oodles.coreservice.dao.WalletDao;
import com.oodles.coreservice.domain.AddressInfo;
import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.enums.TransactionStatus;
import com.oodles.coreservice.enums.WalletStatus;
import com.oodles.coreservice.enums.WalletType;
import com.oodles.coreservice.exception.WalletException;
import com.oodles.coreservice.services.bitcoinj.AddressBalance;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.TransactionPoolManager;
import com.oodles.coreservice.services.wallet.WalletRefreshService;

/**
 * A service that has methods to perform operation related to hot wallet
 * 
 * @author Murari Kumar
 */
@Service
public class HotWalletService {

	@Autowired
	WalletDao walletDao;
	@Autowired
	NetworkParamService networkParamService;
	@Autowired
	EnvConfiguration envConfiguration;
	@Autowired
	WalletStoreService walletStoreService;
	@Autowired
	AddressInfoDao addressInfo;
	@Autowired
	TransactionDao txDao;
	@Autowired
	QRCodeService qrcodeservice;
	@Autowired
	AddressInfoDao addressInfoDao;

	public static Logger log = LoggerFactory.getLogger(HotWalletService.class);

	/**
	 * Create a hot wallet
	 * 
	 * @return
	 * @throws URISyntaxException
	 * @throws WriterException
	 * @throws IOException
	 */
	public Map<String, Object> createWallet(String walletUuid) throws URISyntaxException, WriterException, IOException {
		   
			WalletInfo existingWalletInfo = walletDao.findByWalletUuid(walletUuid);
			log.debug("existing wallet info: {} ",existingWalletInfo);
		    if(existingWalletInfo != null) {
			    throw new WalletException("Wallet already exist by this UUID: "+walletUuid);
		    }
		    WalletInfo walletInfo = new WalletInfo();
			walletInfo.setWalletType(WalletType.HOT_WALLET);
			AddressInfo addrInfo = new AddressInfo();
			Map<String, Object> map = new HashMap<String, Object>();

			int bits = 128;
			SecureRandom random = new SecureRandom();
			DeterministicKeyChain determinstickeychain = new DeterministicKeyChain(random, bits);
			DeterministicSeed seed = determinstickeychain.getSeed();
			log.debug("create wallet seed: {}",  seed.getSeedBytes());
			Wallet wallet = Wallet.fromSeed(networkParamService.getNetworkParameters(), seed);

			ECKey eckey = new ECKey(); // elliptic curve cryptography class that
										// create unique public and private key.
			wallet.importKey(eckey);
			wallet.setCoinSelector(ConfirmedCoinSelector.get());
			long walletEarliestKeyCreationTime = wallet.getEarliestKeyCreationTime();
			String walletName = null;
			if (walletUuid == null || walletUuid.isEmpty()) {
				walletInfo.setWalletUuid(String.valueOf(walletEarliestKeyCreationTime));
				walletName = envConfiguration.getWalletLocation() + '/' + walletEarliestKeyCreationTime + ".dat";
				walletStoreService.getWalletMap().put(String.valueOf(walletEarliestKeyCreationTime), wallet);
				addrInfo.setWalletUuid(String.valueOf(walletEarliestKeyCreationTime));
				map.put("walletUuid", walletEarliestKeyCreationTime);
			} else {
				walletInfo.setWalletUuid(String.valueOf(walletUuid));
				walletName = envConfiguration.getWalletLocation() + '/' + walletUuid + ".dat";
				walletStoreService.getWalletMap().put(String.valueOf(walletUuid), wallet);
				addrInfo.setWalletUuid(String.valueOf(walletUuid));
				map.put("walletUuid", walletUuid);
			}
			WalletRefreshService.addWallet(wallet);
			walletInfo.setDateCreated(new Date());
			walletInfo.setWalletStatus(WalletStatus.ACTIVE);
			walletInfo.setTimpStamp(wallet.getEarliestKeyCreationTime());
			String seedCode = seed.getMnemonicCode().toString().replace(",", "").replace("[", "").replace("]", "");
			walletInfo.setWalletSeedToken(seedCode);
			Map<String, Object> qrMap = qrcodeservice.qrCodeGeneration(wallet.currentReceiveAddress().toString());
			addrInfo.setAddress(wallet.currentReceiveAddress().toString());
			addrInfo.setQrCodeFilename(qrMap.get("file_name").toString());
			addrInfo.setLabel("Primary Address");
			// addrInfo.setWalletUuid("" + wallet.getEarliestKeyCreationTime());
			addrInfo.setAmount("0.00 BTC");
			addrInfo.setIsPrimary(true);
			addressInfo.saveAndFlush(addrInfo);
			WalletInfo savedWalletInfo = walletDao.save(walletInfo);

			log.debug("walletName: {}", walletName);
			try {
				wallet.saveToFile(new File(walletName));
			} catch (IOException e) {
				e.printStackTrace();
			}

			walletStoreService.walletListner(wallet, savedWalletInfo);

			log.debug("wallet info: {}", wallet);
			return map;
	}

	/**
	 * Get primary wallet address of a wallet
	 * 
	 * @param walletUuid
	 * @return
	 */
	public String getPrimaryAddress(String walletUuid) {
		AddressInfo address = addressInfoDao.getAddress(walletUuid);
		return address.getAddress();

	}

	/**
	 * Get current receive wallet address of a wallet
	 * 
	 * @param walletUuid
	 * @return
	 */
	public Address getWalletAddress(String walletUuid) {
		Wallet wallet = null;
		wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
		Address address = wallet.currentReceiveAddress();
		return address;
	}

	/**
	 * Generate a new wallet address
	 * 
	 * @param walletUuid
	 * @return
	 */
	public HashMap<String, String> getWalletRefreshAddress(String walletUuid) {
		AddressInfo addrInfo = new AddressInfo();
		Wallet wallet = null;
		wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
		// Address address = wallet.freshReceiveAddress();

		ECKey eckey = new ECKey();
		wallet.importKey(eckey);
		walletStoreService.saveWallet(wallet);
		Address address = eckey.toAddress(wallet.getParams());

		addrInfo.setAddress(address.toString());
		addrInfo.setLabel("Label this address");
		addrInfo.setWalletUuid(walletUuid);
		addrInfo.setAmount("0.00 BTC");
		addressInfo.saveAndFlush(addrInfo);
		HashMap<String, String> map = new HashMap<String, String>();
		String balance = wallet.getBalance(new AddressBalance(address)).toFriendlyString();
		map.put("address", address.toString());
		map.put("balance", balance);

		return map;
	}

	/**
	 * Get all wallet address
	 * 
	 * @param walletUuid
	 * @return
	 */
	public List<AddressInfo> getWalletAllAddress(String walletUuid) {
		// List<Address> address = wallet.getIssuedReceiveAddresses();
		List<AddressInfo> address = addressInfo.getAllAddress(walletUuid);
		return address;
	}

	/**
	 * Get wallet balance
	 * 
	 * @param walletUuid
	 * @return
	 */
	public String getWalletBalace(String walletUuid) {
		Wallet wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
		/**
		 * We are using ESTIMATED_SPENDABLE to fix zero balance on transaction
		 * issue Please note user will only able to spend confirmed bitcoins
		 * which you can get through AVAILABLE_SPENDABLE
		 */
		log.debug("wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE): {}", wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE));
		log.debug("wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE): {}", wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE));

		String balance = wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE).toFriendlyString();
		return balance;
	}

	/**
	 * Create a admin wallet
	 * 
	 * @return
	 */
	public Map<String, Object> createAdminWallet() {
		File file = new File(envConfiguration.getWalletLocation() + "/" + "adminWallet.dat");
		Map<String, Object> map = new HashMap<String, Object>();
		if (!file.exists()) {
			AdminWallet();
		}
		Wallet wallet = null;
		try {
			wallet = Wallet.loadFromFile(file);
			// wallet=(Wallet)
			// walletStoreService.getWalletMap().get("adminWallet");
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("address", wallet.currentReceiveAddress().toString());
		map.put("Name", "adminWallet");
		map.put("balance", wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE).toFriendlyString());
		log.debug("Wallet Map: {} ", walletStoreService.getWalletMap().keySet());
		return map;
	}

	public void AdminWallet() {
		WalletInfo walletInfo = new WalletInfo();
		AddressInfo addrInfo = new AddressInfo();
		int bits = 128;
		SecureRandom random = new SecureRandom();
		DeterministicKeyChain determinstickeychain = new DeterministicKeyChain(random, bits);
		DeterministicSeed seed = determinstickeychain.getSeed();
		log.debug("Admin wallet seed: {}", seed.getSeedBytes());
		Wallet wallet = Wallet.fromSeed(networkParamService.getNetworkParameters(), seed);

		ECKey eckey = new ECKey(); // elliptic curve cryptography class that
									// create unique public and private key.
		wallet.importKey(eckey);
		wallet.setCoinSelector(ConfirmedCoinSelector.get());
		String walletName = envConfiguration.getWalletLocation() + '/' + "adminWallet.dat";
		walletStoreService.getWalletMap().put("adminWallet", wallet);
		WalletRefreshService.addWallet(wallet);

		walletInfo.setDateCreated(new Date());
		walletInfo.setWalletUuid("adminWallet");
		walletInfo.setWalletStatus(WalletStatus.ACTIVE);
		walletInfo.setWalletType(WalletType.HOT_WALLET);
		addrInfo.setAddress(wallet.currentReceiveAddress().toString());
		addrInfo.setLabel("Primary Address");
		addrInfo.setWalletUuid("adminWallet");
		addrInfo.setAmount("0.00 BTC");
		addrInfo.setIsPrimary(true);
		addressInfo.saveAndFlush(addrInfo);
		WalletInfo savedWalletInfo = walletDao.save(walletInfo);
		log.debug("walletName : {}", walletName);
		try {
			wallet.saveToFile(new File(walletName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		walletStoreService.walletListner(wallet, savedWalletInfo);
	}

	/**
	 * Update wallet label
	 * 
	 * @param address
	 * @param lebel
	 * @return
	 */
	public Integer updateLebel(String address, String lebel) {
		return addressInfo.updateLabel(address, lebel);
	}

	// public Integer
	// This method is used for calculate Transaction Status
	public List<Map<String, String>> getAllAddressBalance(String walletUuid) {
		// Wallet wallet = (Wallet)
		// walletStoreService.getWalletMap().get(walletUuid);
		Map<String, String> json;
		List<Map<String, String>> list = new ArrayList<>();

		List<AddressInfo> address = addressInfo.getAllAddress(walletUuid);
		int size = address.size();

		for (int i = 0; i < size; i++) {
			Address address1 = null;
			json = new HashMap<String, String>();
			try {

				address1 = Address.fromBase58(networkParamService.getNetworkParameters(), address.get(i).getAddress());
			} catch (AddressFormatException e) {
				e.printStackTrace();
			}
			// String balance = wallet.getBalance(new
			// AddressBalance(address1)).toFriendlyString();
			json.put("address", address.get(i).getAddress());

			list.add(json);

		}

		return list;
	}

	/**
	 * Get incoming transaction detail and update transaction status based on
	 * confirmation
	 * 
	 * @param walletUuid
	 */
	public void getIncommingTransaction(String walletUuid) {
		Wallet wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
		if (wallet != null) {
			List<Transaction> incommingTransaction = TransactionPoolManager.getIncomingTransactions(walletUuid);
			log.debug("incommingTransaction " + incommingTransaction);
			incommingTransaction.addAll(wallet.getTransactions(false));
			Iterator<Transaction> itr = incommingTransaction.iterator();
			while (itr.hasNext()) {
				Transaction tr = itr.next();
				txDao.updateTxConfirmation(ConfirmedCoinSelector.calculateConfirmations(tr), tr.getHashAsString(),
						TransactionStatus.PENDING);
				if (ConfirmedCoinSelector.calculateConfirmations(tr) >= ConfirmedCoinSelector.minConfidenceLevel) {
					txDao.updateTransactionStatus(TransactionStatus.COMPLETED, tr.getHashAsString());
				}
			}
		}
	}

	/**
	 * Get transaction status
	 * 
	 * @param walletUuid
	 * @return
	 */
	public List<HashMap<String, String>> getTransactionStatus(String walletUuid) {
		Wallet wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
		List<HashMap<String, String>> listOfTxs = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		if (wallet != null) {
			List<Transaction> incommingTransaction = TransactionPoolManager.getIncomingTransactions(walletUuid);
			incommingTransaction.addAll(wallet.getTransactions(false));
			Iterator<Transaction> itr = incommingTransaction.iterator();
			while (itr.hasNext()) {
				map = new HashMap<String, String>();
				Transaction tr = itr.next();
				Context context = new Context(networkParamService.getNetworkParameters());
				Context.propagate(context);
				if (ConfirmedCoinSelector.calculateConfirmations(tr) >= ConfirmedCoinSelector.minConfidenceLevel) {
					map.put("transactonHash", tr.getHashAsString());
					map.put("txStatus", TransactionStatus.COMPLETED.toString());
					map.put("confirmations", String.valueOf(ConfirmedCoinSelector.calculateConfirmations(tr)));
					listOfTxs.add(map);

				} else {
					map.put("transactonHash", tr.getHashAsString());
					map.put("txStatus", TransactionStatus.PENDING.toString());
					map.put("confirmations", String.valueOf(ConfirmedCoinSelector.calculateConfirmations(tr)));
					listOfTxs.add(map);
				}

			}
			return listOfTxs;
		}
		return null;
	}

	/**
	 * Get transaction hash confirmation in bulk
	 * 
	 * @param walletUuid
	 * @param hashList
	 * @return
	 */
	public List<HashMap<String, String>> getConfirmationByWallet(String walletUuid, String[] hashList) {
		List<HashMap<String, String>> listOfTxs = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		for (int i = 0; i < hashList.length; i++) {
			map = new HashMap<String, String>();
			Wallet wallet = (Wallet) walletStoreService.getWalletMap().get(walletUuid);
			Sha256Hash txHash = Sha256Hash.wrap(hashList[i]);
			Transaction tx = wallet.getTransaction(txHash);
			if (tx == null) {
				map.put("transactonHash", hashList[i]);
				map.put("txStatus", TransactionStatus.PENDING.toString());
				map.put("confirmations", String.valueOf(0));
				listOfTxs.add(map);
			} else {
				if (ConfirmedCoinSelector.calculateConfirmations(tx) > ConfirmedCoinSelector.minConfidenceLevel) {
					map.put("transactonHash", tx.getHashAsString());
					map.put("txStatus", TransactionStatus.COMPLETED.toString());
					map.put("confirmations", String.valueOf(ConfirmedCoinSelector.calculateConfirmations(tx)));
					listOfTxs.add(map);

				} else {
					map.put("transactonHash", tx.getHashAsString());
					map.put("txStatus", TransactionStatus.PENDING.toString());
					map.put("confirmations", String.valueOf(ConfirmedCoinSelector.calculateConfirmations(tx)));
					listOfTxs.add(map);
				}
			}
		}
		return listOfTxs;
	}

	/**
	 * Restore wallet from seed code
	 * 
	 * @param walletUuid
	 * @return
	 */
	public String restoreWalletFromSeed(String walletUuid) {
		WalletInfo walletInfo = walletDao.findByWalletUuid(walletUuid);
		String seedCode = walletInfo.getWalletSeedToken();
		Long timeStamp = walletInfo.getTimpStamp();
		String passphrase = "";
		try {
			DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, timeStamp);

			Wallet wallet = Wallet.fromSeed(networkParamService.getNetworkParameters(), seed);
			wallet.setCoinSelector(ConfirmedCoinSelector.get());
			walletStoreService.getWalletMap().put("" + wallet.getEarliestKeyCreationTime(), wallet);
			WalletRefreshService.addWallet(wallet);
			walletStoreService.walletListner(wallet, walletInfo);
			walletStoreService.saveWallet(wallet);
		} catch (UnreadableWalletException e) {
			e.printStackTrace();
		}
		return "restored Wallet";

	}
}
