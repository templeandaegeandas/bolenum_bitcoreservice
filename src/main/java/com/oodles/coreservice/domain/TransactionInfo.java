package com.oodles.coreservice.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.bitcoinj.core.Transaction;

import com.oodles.coreservice.enums.CurrencyType;
import com.oodles.coreservice.enums.ProtocolType;
import com.oodles.coreservice.enums.TransactionStatus;
import com.oodles.coreservice.enums.TransactionType;
import com.oodles.coreservice.enums.WalletTransactionType;

@Entity
public class TransactionInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")	
	@Temporal(TemporalType.TIMESTAMP)	
	private Date dateCreated;
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")	
	@Temporal(TemporalType.TIMESTAMP)	
	private Date dateUpdated;
	private String transactionHash;
	private Double transactionFee;
	private Integer settingFee;
	private String transactionDescription;
	private Double transactionTradeAmount;
	private String senderAddress;
	private String receiverAddress;
	private Integer transactionConfirmation;
	private CurrencyType currenyType;
	private String walletUuid;
	private String txConfirmation;
	private String memo;
	private String exchangeRate;
	private TransactionStatus transactionStatus;
	private ProtocolType protocolType;
	private WalletTransactionType walletTransactionType;
	private TransactionType transactionType;
	private Transaction transaction;
	public TransactionInfo(){
		
	}
	
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getTransactionConfirmation() {
		return transactionConfirmation;
	}
	public void setTransactionConfirmation(Integer transactionConfirmation) {
		this.transactionConfirmation = transactionConfirmation;
	}
	public CurrencyType getCurrenyType() {
		return currenyType;
	}
	public void setCurrenyType(CurrencyType currenyType) {
		this.currenyType = currenyType;
	}
	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	public ProtocolType getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreatedDate() {
		return dateCreated;
	}
	public void setCreatedDate(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getUpdatedDate() {
		return dateUpdated;
	}
	public void setUpdatedDate(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getTransactionHash() {
		return transactionHash;
	}
	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}
	public Double getTransactionFee() {
		return transactionFee;
	}
	public void setTransactionFee(Double transactionFee) {
		this.transactionFee = transactionFee;
	}
	public Integer getSettingFee() {
		return settingFee;
	}
	public void setSettingFee(Integer settingFee) {
		this.settingFee = settingFee;
	}
	public String getTransactionDescription() {
		return transactionDescription;
	}
	public void setTransactionDescription(String transactionDescription) {
		this.transactionDescription = transactionDescription;
	}
	public Double getTransactionTradeAmount() {
		return transactionTradeAmount;
	}
	public void setTransactionTradeAmount(Double transactionTradeAmount) {
		this.transactionTradeAmount = transactionTradeAmount;
	}
	public String getSenderAddress() {
		return senderAddress;
	}
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}
	public String getReceiverAddress() {
		return receiverAddress;
	}
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}
	public String getWalletUuid() {
		return walletUuid;
	}
	public void setWalletUuid(String walletUuid) {
		this.walletUuid = walletUuid;
	}
	public String getTxConfirmation() {
		return txConfirmation;
	}
	public void setTxConfirmation(String txConfirmation) {
		this.txConfirmation = txConfirmation;
	}
	
	public String getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public WalletTransactionType getWalletTransactionType() {
		return walletTransactionType;
	}
	public void setWalletTransactionType(WalletTransactionType walletTransactionType) {
		this.walletTransactionType = walletTransactionType;
	}
	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	
	
}
