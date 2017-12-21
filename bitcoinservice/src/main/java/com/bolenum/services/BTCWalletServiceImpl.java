/**
 * 
 */
package com.bolenum.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bolenum.enums.TransactionStatus;
import com.bolenum.enums.TransactionType;
import com.bolenum.enums.TransferStatus;
import com.bolenum.model.Transaction;
import com.bolenum.model.User;
import com.bolenum.model.UserCoin;
import com.bolenum.repo.TransactionRepo;
import com.bolenum.repo.UserCoinRepository;
import com.bolenum.repo.UserRepository;
import com.bolenum.util.ResourceUtils;
import com.neemre.btcdcli4j.core.BitcoindException;
import com.neemre.btcdcli4j.core.CommunicationException;
import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.domain.Block;
import com.neemre.btcdcli4j.core.domain.PaymentOverview;
import com.neemre.btcdcli4j.core.domain.enums.PaymentCategories;

/**
 * @author chandan kumar singh
 * @date 22-Sep-2017
 */
@Service
public class BTCWalletServiceImpl implements BTCWalletService {

	private static final Logger logger = LoggerFactory.getLogger(BTCWalletServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransactionRepo transactionRepo;

	@Autowired
	private UserCoinRepository userCoinRepository;

	@Value("${admin.email}")
	private String adminEmail;

	@Override
	public void getBlock(String blockHash) {
		try {
			BtcdClient client = ResourceUtils.getBtcdProvider();
			Block block = client.getBlock(blockHash);
			transactions(block.getTx(), client);
		} catch (BitcoindException | CommunicationException e) {
			logger.error("getBlock error: {}", e);
		}
	}

	private void transactions(List<String> txs, BtcdClient client) {
		if (txs.isEmpty()) {
			return;
		}
		txs.forEach(txId -> {
			com.neemre.btcdcli4j.core.domain.Transaction tran = getTx(client, txId);
			if (tran != null) {
				List<PaymentOverview> details = tran.getDetails();
				details.forEach(paymentOverview -> {
					logger.info("paymentOverview Category: {}", paymentOverview.getCategory());
					if (PaymentCategories.RECEIVE.equals(paymentOverview.getCategory())) {
						String account = paymentOverview.getAccount();
						if (account.isEmpty()) {
							saveTxForAdmin(tran);
						} else {
							saveTxForUser(account, tran);
						}
					}
				});
			}
		});
	}

	private com.neemre.btcdcli4j.core.domain.Transaction getTx(BtcdClient client, String txId) {
		try {
			return client.getTransaction(txId);
		} catch (BitcoindException | CommunicationException e) {
			logger.debug("not wallet tx: {}", txId);
			return null;
		}
	}

	/**
	 * save deposit transaction of users
	 * 
	 */
	private void saveTxForUser(String account, com.neemre.btcdcli4j.core.domain.Transaction tran) {
		try {
			Long userId = Long.valueOf(account);
			User user = userRepository.findByUserId(userId);
			UserCoin coin = userCoinRepository.findByTokenNameAndUser("BTC", user);
			Transaction transaction = transactionRepo.findByTxHash(tran.getTxId());
			if (transaction == null) {
				transaction = new Transaction();
				transaction.setToUser(user);
				transaction.setTxAmount(tran.getAmount().doubleValue());
				transaction.setTxHash(tran.getTxId());
				transaction.setTransactionType(TransactionType.INCOMING);
				transaction.setTransactionStatus(TransactionStatus.DEPOSIT);
				transaction.setCurrencyName("BTC");
				transaction.setTransferStatus(TransferStatus.COMPLETED);
				if (coin != null) {
					transaction.setToAddress(coin.getWalletAddress());
				}
				transactionRepo.save(transaction);
			}
		} catch (NumberFormatException e) {
			logger.error("invalid account of user {}", e);
		}

	}

	/**
	 * save deposit transaction of admin
	 * 
	 */

	private void saveTxForAdmin(com.neemre.btcdcli4j.core.domain.Transaction tran) {
		User admin = userRepository.findByEmailId(adminEmail);
		UserCoin coin = userCoinRepository.findByTokenNameAndUser("BTC", admin);
		Transaction transaction = transactionRepo.findByTxHash(tran.getTxId());
		if (transaction == null) {
			transaction = new Transaction();
			transaction.setToUser(admin);
			transaction.setTxHash(tran.getTxId());
			transaction.setTransactionType(TransactionType.INCOMING);
			transaction.setTransactionStatus(TransactionStatus.DEPOSIT);
			transaction.setCurrencyName("BTC");
			transaction.setTransferStatus(TransferStatus.COMPLETED);
			if (coin != null) {
				transaction.setToAddress(coin.getWalletAddress());
			}
			transactionRepo.save(transaction);
		}
	}
}