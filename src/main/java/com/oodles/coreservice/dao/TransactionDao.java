package com.oodles.coreservice.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oodles.coreservice.domain.TransactionInfo;
import com.oodles.coreservice.enums.TransactionStatus;
import com.oodles.coreservice.enums.TransactionType;

public interface TransactionDao extends JpaRepository<TransactionInfo, Serializable> {

	@Query(value = "select * from transaction_info t where t.wallet_uuid=?1 order by date_created desc", nativeQuery = true)
	List<TransactionInfo> getTransactionDetails(String walletUuid);

	@Query(value = "select count(*) from transaction_info where date_created > current_date - interval '1 week' and date_created < current_date + interval '1 day'", nativeQuery = true)
	Long getTransactionHistoryPerWeek();

	@Query(value = "select count(*) from transaction_info where date_created > current_date - interval '1 month' and date_created < current_date + interval '1 day'", nativeQuery = true)
	Long getTransactionHistoryPerMonth();

	@Query(value = "select count(*) from transaction_info where date_created > current_date - interval '1 year' and date_created < current_date + interval '1 day'", nativeQuery = true)
	Long getTransactionHistoryPerYear();

	@Transactional
	@Modifying
	@Query("update TransactionInfo t set t.transactionStatus=?1 where transactionHash=?2")
	public Integer updateTransactionStatus(TransactionStatus transactionStatus, String transactionHash);
	
	@Transactional
	@Modifying
	@Query("update TransactionInfo t set t.transactionConfirmation=?1 where t.transactionHash=?2 and t.transactionStatus=?3")
	public Integer updateTxConfirmation(Integer txConfirmation, String transactionHash,TransactionStatus transactionStatus);
	
	public TransactionInfo findById(Long id);

	public TransactionInfo findByTransactionHash(String txHash);
	
	@Query("select sum(t.transactionTradeAmount) from TransactionInfo t where t.walletUuid=?1 and t.transactionType=?2")
	Double getTotalAmountByTransactionType(String walletUuid,TransactionType transactionType);
	
	@Query("select t from TransactionInfo t where t.walletUuid=:walletUuid")
	Page<TransactionInfo> getTransactionListByWalletId(Pageable pageable,@Param("walletUuid") String walletId);
	
	@Query("select t from TransactionInfo t where (t.walletUuid=:walletUuid) and (lower(transactionHash) like :searchText% or lower(senderAddress) like :searchText% or lower(receiverAddress) like :searchText%)")
	Page<TransactionInfo> searchTransectionListByWalletId(Pageable pageable, @Param("searchText") String searchText,@Param("walletUuid") String walletId);

	@Query("select t from TransactionInfo t where (t.walletUuid=:walletUuid) and (t.dateCreated >= :startDate and t.dateCreated <= :endDate) and (lower(transactionHash) like :searchText% or lower(senderAddress) like :searchText% or lower(receiverAddress) like :searchText%)")
	Page<TransactionInfo> searchWithFilterTransectionListByWalletId(Pageable pageable, @Param("searchText") String searchText,@Param("startDate")Date startDate,@Param("endDate")Date endDate,@Param("walletUuid") String walletId);
	
	@Query("select t from TransactionInfo t where t.transactionHash=?1 and t.transactionType=?2")
	TransactionInfo checkDuplicateTransaction(String transactionHash,TransactionType transactionType);
	
	@Query("select sum(t.transactionFee) from TransactionInfo t where t.walletUuid=?1 and t.transactionType=?2")
	Double getTotalFee(String walletUuid,TransactionType transactionType);
}
