package com.oodles.coreservice.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.dao.TransactionDao;
import com.oodles.coreservice.domain.AddressInfo;
import com.oodles.coreservice.domain.TransactionInfo;
import com.oodles.coreservice.domain.TransactionParams;
import com.oodles.coreservice.dto.TransactionForm;
import com.oodles.coreservice.enums.DateFilter;
import com.oodles.coreservice.enums.TransactionStatus;
import com.oodles.coreservice.enums.TransactionType;
import com.oodles.coreservice.exception.BitcoinTransactionException;
import com.oodles.coreservice.exception.WalletException;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
import com.oodles.coreservice.services.wallet.PeerGroupProvider;
import com.oodles.coreservice.services.wallet.PeerGroupProvider.PeerGroupType;
import com.oodles.coreservice.services.wallet.TransactionBroadcastService;
import com.oodles.coreservice.services.wallet.TransactionPoolManager;

/**
 * A service that provide methods to perform transaction for both cold and hot
 * wallet Also that methods to get details regarding transaction
 * 
 * @author Murari Kumar and Ajit Soman
 */
@Service
public class TransactionServiceImpl implements TransactionService {
	@Autowired
	EnvConfiguration configuration;
	@Autowired
	NetworkParamService netparams;
	@Autowired
	TransactionDao transactionDao;
	@Autowired
	WalletStoreService walletStoreService;
	@Autowired
	TransactionBroadcastService transactionBroadcastService;
	@Autowired
	AddressInfoDao addressInfoDao;
	@Autowired
	ColdWalletService coldWalletService;

	TransactionParams transactionParams = new TransactionParams();
	public static Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

	/**
	 * Create hot wallet transaction
	 * 
	 * @param transactionparams
	 * @return
	 * @throws Exception
	 */
	@Override
	public TransactionInfo createTransaction(TransactionParams transactionparams) throws Exception {
		String transactionHash = null;
		Wallet wallet = null;

		Address receiverAddress;
		Context.propagate(new Context(netparams.getNetworkParameters()));
		log.debug("netparams.getNetworkParameters():{}",netparams.getNetworkParameters());
		receiverAddress = Address.fromBase58(netparams.getNetworkParameters(), transactionparams.getReceiverAddress());
		log.debug("receiverAddress {} ",receiverAddress);
		String amount = String.valueOf(transactionparams.getTransactionTradeAmount());
		String fee = String.valueOf(transactionparams.getTransactionFee());
		log.debug("create trnasaction receive Address: {}", transactionparams.getReceiverAddress());
		final File walletFile = new File(
				configuration.getWalletLocation() + "/" + transactionparams.getWalletId() + ".dat");
		wallet = (Wallet) walletStoreService.getWalletMap().get(transactionparams.getWalletId());
		if (walletFile.exists()) {
			if (wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE)
					.equals(wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE))) {
				Coin btcCoin = Coin.parseCoin(amount);
				// Wallet.SendRequest.DEFAULT_FEE_PER_KB = Coin.ZERO;

				SendRequest request = SendRequest.to(receiverAddress, btcCoin);
				// request.feePerKb = Coin.ZERO;
				request.ensureMinRequiredFee = false;
				// request.fee = Coin.valueOf(10000);
				if (transactionparams.getTransactionFee() != null) {
					request.feePerKb = Coin.parseCoin(fee);
				}
				// request.feePerKb = Coin.ZERO;
				request.changeAddress = wallet.freshReceiveAddress();

				Transaction transaction = wallet.sendCoinsOffline(request);
				TransactionPoolManager.addTransaction(transaction);
				log.debug("wallet.getBalance(): {}", wallet.getBalance());
				log.debug("wallet.getBalance(BalanceType.AVAILABLE): {}", wallet.getBalance(BalanceType.AVAILABLE));
				log.debug("wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE): {}",
						wallet.getBalance(BalanceType.AVAILABLE_SPENDABLE));
				log.debug("wallet.getBalance(BalanceType.ESTIMATED: {}", wallet.getBalance(BalanceType.ESTIMATED));
				log.debug("wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE): {}",
						wallet.getBalance(BalanceType.ESTIMATED_SPENDABLE));
				walletStoreService.saveWallet(wallet, transactionparams.getWalletId());
				transactionHash = request.tx.getHashAsString();

				TransactionInfo transactionInfo = saveTransactionDetails(receiverAddress.toString(), wallet, transactionHash,
						Double.valueOf((amount.toString())), transactionparams.getWalletId(),
						transactionparams.getTransactionFee());
				if (transactionHash == null) {
					return null;
				} else {
				
					return transactionInfo;
				}
			} else {
				throw new BitcoinTransactionException(
						"Your previous transaction has not confirmed yet,So you can't perform this transaction. Please try after some time");
			}
		} else {
			throw new FileNotFoundException("Wallet not found");
		}
	}

	/**
	 * Save Transaction Details
	 * 
	 * @param address
	 * @param wallet
	 * @param txHash
	 * @param tradeAmount
	 * @param walletUuid
	 * @param transactionFee
	 */
	@Override
	public TransactionInfo saveTransactionDetails(String address, Wallet wallet, String txHash, Double tradeAmount,
			String walletUuid, Double transactionFee) {
		TransactionInfo txInfo = new TransactionInfo();
		txInfo.setCreatedDate(new Date());
		txInfo.setTransactionType(TransactionType.SENT);
		txInfo.setReceiverAddress(address);
		txInfo.setSenderAddress(wallet.currentReceiveAddress().toString());
		txInfo.setTransactionHash(txHash);
		txInfo.setTransactionTradeAmount(tradeAmount);
		txInfo.setWalletUuid(walletUuid);
		txInfo.setTransactionFee(0.0001);
		if (transactionFee != null) {
			txInfo.setTransactionFee(transactionFee);
		}
		txInfo.setTransactionStatus(TransactionStatus.PENDING);
		return transactionDao.saveAndFlush(txInfo);

	}

	/**
	 * Create a cold wallet transaction
	 * 
	 * @param transactionParams
	 * @return
	 * @throws WalletException
	 * @throws AddressFormatException
	 * @throws InsufficientMoneyException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws BitcoinTransactionException
	 */
	@Override
	public String transactionForColdWallet(TransactionParams transactionParams)
			throws WalletException, AddressFormatException, InsufficientMoneyException, InterruptedException,
			ExecutionException, BitcoinTransactionException {

		Address receiverAddress = Address.fromBase58(netparams.getNetworkParameters(),
				transactionParams.getReceiverAddress());
		Object object = coldWalletService.getColdWalletMap().get(transactionParams.getWalletId());
		if (object != null) {
			if (object instanceof Wallet) {
				Wallet coldWallet = (Wallet) object;
				if (coldWallet.getBalance(BalanceType.ESTIMATED_SPENDABLE)
						.equals(coldWallet.getBalance(BalanceType.AVAILABLE_SPENDABLE))) {
					String amount = String.valueOf(transactionParams.getTransactionTradeAmount());
					String fee = String.valueOf(transactionParams.getTransactionFee());
					Coin btcCoin = Coin.parseCoin(amount);
					// SendRequest.DEFAULT_FEE_PER_KB = Coin.ZERO;

					SendRequest request = SendRequest.to(receiverAddress, btcCoin);
					request.ensureMinRequiredFee = false;
					request.feePerKb = Coin.valueOf(10000);
					if (transactionParams.getTransactionFee() != null) {
						request.feePerKb = Coin.parseCoin(fee);
					}
					// request.feePerKb = Coin.ZERO;
					request.changeAddress = coldWallet.freshReceiveAddress();
					PeerGroup peerGroup = PeerGroupProvider.get(PeerGroupType.WALLET_REFRESH);
					Wallet.SendResult result = coldWallet.sendCoins(peerGroup, request);
					String txHash = result.broadcastComplete.get().getHashAsString();
					saveTransactionDetails(receiverAddress.toString(), coldWallet, txHash,
							Double.valueOf((amount.toString())), transactionParams.getWalletId(),
							transactionParams.getTransactionFee());
					return txHash;
				} else {
					throw new BitcoinTransactionException(
							"Your previous transaction has not confirmed yet,So you can't perform this transaction. Please try after some time");
				}
			} else {
				return object.toString();
			}
		} else {
			throw new WalletException("Wallet not found");
		}
	}

	/**
	 * Saved Receive transaction details
	 * 
	 * @param Uuid
	 * @param tradeAmount
	 * @param confirmation
	 */
	@Override
	public void saveReceivedtransactionDetails(String Uuid, Double tradeAmount, int confirmation) {
		TransactionInfo txInfo = new TransactionInfo();
		txInfo.setCreatedDate(new Date());
		txInfo.setTransactionTradeAmount(tradeAmount);
		txInfo.setWalletUuid(Uuid);
		txInfo.setTransactionConfirmation(confirmation);
		transactionDao.saveAndFlush(txInfo);
	}

	/**
	 * Get transaction hash confirmation in bulk
	 * 
	 * @param hashList
	 * @return
	 */
	@Override
	public List<HashMap<String, String>> getConfirmationByHash(String[] hashList) {

		List<HashMap<String, String>> listOfTxs = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		for (int i = 0; i < hashList.length; i++) {
			map = new HashMap<String, String>();
			Transaction tx = transactionBroadcastService.getTxMap().get(hashList[i]);
			if (tx == null) {
				map.put("transactonHash", hashList[i]);
				map.put("txStatus", TransactionStatus.PENDING.toString());
				map.put("confirmations", String.valueOf(0));
				listOfTxs.add(map);
			} else {
				if (ConfirmedCoinSelector.calculateConfirmations(tx) >= ConfirmedCoinSelector.minConfidenceLevel) {
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
	 * Get Credit and debit sum for a wallet
	 * 
	 * @param walletId
	 * @return
	 */
	@Override
	public Map<String, Double> getCreditDebitTotalByWalletId(String walletId) {
		Double creditAmount = transactionDao.getTotalAmountByTransactionType(walletId, TransactionType.RECEIVED);
		Double debitAmount = transactionDao.getTotalAmountByTransactionType(walletId, TransactionType.SENT);
		Double totalFee = transactionDao.getTotalFee(walletId, TransactionType.SENT);
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("creditAmount", creditAmount);
		map.put("debitAmount", debitAmount);
		map.put("totalFee", totalFee);
		return map;
	}

	/**
	 * Get paginated transaction list
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param columnName
	 * @param sortingDirection
	 * @param walletId
	 * @return
	 */
	@Override
	public Page<TransactionInfo> getPaginatedTransactionList(int pageNumber, int pageSize, String columnName,
			String sortingDirection, String walletId) {
		PageRequest request;
		Direction direction;
		if (sortingDirection.equals("ASC")) {
			direction = Sort.Direction.ASC;
		} else {
			direction = Sort.Direction.DESC;
		}
		request = new PageRequest(pageNumber - 1, pageSize, direction, columnName);
		return transactionDao.getTransactionListByWalletId(request, walletId);

	}

	/**
	 * Search on transaction list with pagination
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param columnName
	 * @param sortingDirection
	 * @param walletId
	 * @param searchText
	 * @return
	 */
	@Override
	public Page<TransactionInfo> searchPaginatedTransactionList(int pageNumber, int pageSize, String columnName,
			String sortingDirection, String walletId, String searchText) {
		PageRequest request;
		Direction direction;
		if (sortingDirection.equals("ASC")) {
			direction = Sort.Direction.ASC;
		} else {
			direction = Sort.Direction.DESC;
		}
		request = new PageRequest(pageNumber - 1, pageSize, direction, columnName);
		return transactionDao.searchTransectionListByWalletId(request, searchText, walletId);
	}

	/**
	 * Search with filter on transaction list with pagination
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param columnName
	 * @param sortingDirection
	 * @param walletId
	 * @param searchText
	 * @param filter
	 * @return
	 */
	@Override
	public Page<TransactionInfo> searchWithFilterPaginatedTransactionList(int pageNumber, int pageSize,
			String columnName, String sortingDirection, String walletId, String searchText, DateFilter filter) {
		PageRequest request;
		Direction direction;
		if (sortingDirection.equals("ASC")) {
			direction = Sort.Direction.ASC;
		} else {
			direction = Sort.Direction.DESC;
		}
		request = new PageRequest(pageNumber - 1, pageSize, direction, columnName);
		if (filter.equals(DateFilter.LAST_WEEK)) {
			Date date = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
			c.add(Calendar.DATE, -i - 7);
			Date start = c.getTime();
			c.add(Calendar.DATE, 6);
			Date end = c.getTime();
			return transactionDao.searchWithFilterTransectionListByWalletId(request, searchText, start, end, walletId);
		} else if (filter.equals(DateFilter.LAST_MONTH)) {
			Date startDate = getPreMonthStartDate();
			Date endDate = getPreMonthEndDate();
			return transactionDao.searchWithFilterTransectionListByWalletId(request, searchText, startDate, endDate,
					walletId);
		} else {
			Date startDate = getPreYearStartDate();
			Date endDate = getPreYearEndDate();
			return transactionDao.searchWithFilterTransectionListByWalletId(request, searchText, startDate, endDate,
					walletId);
		}
	}

	/**
	 * Get month start date
	 * 
	 * @return
	 */
	private static Date getPreMonthStartDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date preMonthStartDate = cal.getTime();
		return preMonthStartDate;
	}

	/**
	 * get current month end date
	 * 
	 * @return
	 */
	private static Date getPreMonthEndDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR_OF_DAY, 11);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date preMonthEndDate = cal.getTime();
		return preMonthEndDate;
	}

	/**
	 * Get year start date
	 * 
	 * @return
	 */
	private static Date getPreYearStartDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.add(Calendar.YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 00);
		Date preMonthDate = cal.getTime();
		return preMonthDate;
	}

	/**
	 * Get year end date
	 * 
	 * @return
	 */
	private static Date getPreYearEndDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR_OF_DAY, 11);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date preToPreMonthDate = cal.getTime();
		return preToPreMonthDate;
	}

	/**
	 * Save receive transaction info asynchronously
	 * 
	 * @param wallet
	 * @param tx
	 * @param walletUuid
	 */
	@SuppressWarnings("deprecation")
	@Async
	@Override
	public void saveTransactionReceiveInfo(Wallet wallet, Transaction tx, String walletUuid) {
		log.info("Received Bitcoins for wallet: {}, with tx hash: {}", walletUuid, tx.getHashAsString());
		// TransactionInfo existingTxInfo =
		// transactionDao.checkDuplicateTransaction(tx.getHashAsString(),
		// TransactionType.RECEIVED);
		TransactionInfo txInfo = null;
		TransactionInfo existingTxInfo = transactionDao.findByTransactionHash(tx.getHashAsString());
		if (existingTxInfo == null) {
			txInfo = new TransactionInfo();
			txInfo.setCreatedDate(new Date());
			txInfo.setTransactionConfirmation(ConfirmedCoinSelector.calculateConfirmations(tx));
			txInfo.setTransactionHash(tx.getHashAsString());
			txInfo.setWalletUuid(walletUuid);
			txInfo.setTransactionType(TransactionType.RECEIVED);
			String amount = tx.getValueSentToMe(wallet).toFriendlyString().replace("BTC", "");
			Double transferAmount = Double.parseDouble(amount);
			transferAmount = (double) Math.round(transferAmount * 100000000);
			transferAmount = transferAmount / 100000000;
			txInfo.setTransactionTradeAmount(transferAmount);
			txInfo.setTransactionFee(0.000);
			List<TransactionOutput> outputs = tx.getOutputs();
			List<TransactionInput> inputs = tx.getInputs();

			String senderAddress = null;
			try {
				senderAddress = inputs.get(0).getFromAddress().toString();
			} catch (Exception e) {
				log.error("sender address error: {}", e.getMessage());
				e.printStackTrace();
			}
			String receivedAddress = null;
			try {
				receivedAddress = outputs.get(0).getAddressFromP2PKHScript(netparams.getNetworkParameters()).toString();
			} catch (Exception e) {
				log.error("received address error: {}", e.getMessage());
				e.printStackTrace();
			}
			log.debug("receive address: {}", receivedAddress);
			if (receivedAddress != null) {
				AddressInfo addrInfo = addressInfoDao.findByAddress(receivedAddress);
				if (addrInfo != null) {
					Double balance = Double.parseDouble(addrInfo.getAmount().replace("BTC", ""));
					Double newBalance = balance + transferAmount;
					addressInfoDao.updateAddressBalance(String.valueOf(newBalance) + " BTC", receivedAddress);
					txInfo.setReceiverAddress(receivedAddress);
				} else {
					String address = outputs.get(1).getAddressFromP2PKHScript(netparams.getNetworkParameters())
							.toString();
					try {
						AddressInfo info = addressInfoDao.findByAddress(address);
						Double balance = Double.parseDouble(info.getAmount().replace("BTC", ""));
						Double newBalance = balance + transferAmount;
						addressInfoDao.updateAddressBalance(String.valueOf(newBalance) + " BTC", address);
					} catch (Exception e) {
						e.printStackTrace();
					}
					txInfo.setReceiverAddress(address);
				}
			}
			txInfo.setSenderAddress(senderAddress);
			boolean status = tx.isPending();

			if (status == true) {
				txInfo.setTransactionStatus(TransactionStatus.PENDING);
			} else {
				txInfo.setTransactionStatus(TransactionStatus.COMPLETED);
			}

			try {
				sendReceiverTransactionDetails(txInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			transactionDao.saveAndFlush(txInfo);
			log.info("Transaction detail has been saved");
		} else {
			log.info("Duplicate transaction hash has prevented from saving into database");
		}
	}

	/**
	 * Get cold wallet transaction hash confirmation
	 * 
	 * @param walletName
	 * @param txHash
	 * @return
	 * @throws WalletException
	 */
	@Override
	public int getConfirmationForColdWalletTx(String walletName, String txHash) throws WalletException {
		Object object = coldWalletService.getColdWalletMap().get(walletName);
		if (object instanceof Wallet) {
			Wallet wallet = (Wallet) object;
			Transaction tx = wallet.getTransaction(Sha256Hash.wrap(txHash));
			return ConfirmedCoinSelector.calculateConfirmations(tx);
		} else {
			throw new WalletException("Wallet is not synchronized");
		}
	}

	private void sendReceiverTransactionDetails(TransactionInfo txInfo) {
		/**
		 * If you want to send bitcoin receive information to your Application use
		 * uncomment below code and provide endpoint of your app URL
		 */
		TransactionForm transactionForm = new TransactionForm(txInfo.getTransactionHash(), txInfo.getSenderAddress(), txInfo.getReceiverAddress(), txInfo.getTransactionFee(), txInfo.getTransactionTradeAmount(), txInfo.getTransactionDescription());
		 final String uri = configuration.getBolenumURL();
		 log.info("Sending data to " + uri);
		 RestTemplate restTemplate = new RestTemplate();
		 String result = restTemplate.postForObject(uri, transactionForm,
		 String.class);
		 log.info("result:" + result);

	}
}
