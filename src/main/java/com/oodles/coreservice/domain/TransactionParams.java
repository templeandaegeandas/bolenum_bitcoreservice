package com.oodles.coreservice.domain;

public class TransactionParams {

	private String walletId;
	private String transactionDescription;
	private Double transactionTradeAmount;
	private String receiverAddress;
	private Double transactionFee;
	
	public String getWalletId() {
		return walletId;
	}
	public void setWalletId(String walletId) {
		this.walletId = walletId;
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
	public String getReceiverAddress() {
		return receiverAddress;
	}
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}
	public Double getTransactionFee() {
		return transactionFee;
	}
	public void setTransactionFee(Double transactionFee) {
		this.transactionFee = transactionFee;
	}

	
}
