/**
 * 
 */
package com.oodles.coreservice.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.springframework.data.domain.Page;

import com.oodles.coreservice.domain.TransactionInfo;
import com.oodles.coreservice.domain.TransactionParams;
import com.oodles.coreservice.enums.DateFilter;
import com.oodles.coreservice.exception.BitcoinTransactionException;
import com.oodles.coreservice.exception.WalletException;

/**
 * @author chandan kumar singh
 * @date 03-Nov-2017
 */
public interface TransactionService {
	public TransactionInfo createTransaction(TransactionParams transactionparams) throws Exception;
	public TransactionInfo saveTransactionDetails(String address, Wallet wallet, String txHash, Double tradeAmount,
			String walletUuid, Double transactionFee);
	public String transactionForColdWallet(TransactionParams transactionParams)
			throws WalletException, AddressFormatException, InsufficientMoneyException, InterruptedException,
			ExecutionException, BitcoinTransactionException;
	public void saveReceivedtransactionDetails(String Uuid, Double tradeAmount, int confirmation);
	public List<HashMap<String, String>> getConfirmationByHash(String[] hashList);
	public Map<String, Double> getCreditDebitTotalByWalletId(String walletId);
	public Page<TransactionInfo> getPaginatedTransactionList(int pageNumber, int pageSize, String columnName,
			String sortingDirection, String walletId);
	public Page<TransactionInfo> searchPaginatedTransactionList(int pageNumber, int pageSize, String columnName,
			String sortingDirection, String walletId, String searchText);
	public int getConfirmationForColdWalletTx(String walletName, String txHash) throws WalletException;
	public void saveTransactionReceiveInfo(Wallet wallet, Transaction tx, String walletUuid);
	public Page<TransactionInfo> searchWithFilterPaginatedTransactionList(int pageNumber, int pageSize,
			String columnName, String sortingDirection, String walletId, String searchText, DateFilter filter);
}