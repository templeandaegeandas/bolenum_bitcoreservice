package com.oodles.coreservice.dto;

public class TransactionForm {

	private String txHash;
	private String fromAddress;
	private String toAddress;
	private Double txFee;
	private Double txAmount;
	private String txDescription;

	public TransactionForm(String txHash, String fromAddress, String toAddress, Double txFee, Double txAmount,
			String txDescription) {
		this.txHash = txHash;
		this.fromAddress = fromAddress;
		this.toAddress = toAddress;
		this.txFee = txFee;
		this.txAmount = txAmount;
		this.txDescription = txDescription;
	}

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public Double getTxFee() {
		return txFee;
	}

	public void setTxFee(Double txFee) {
		this.txFee = txFee;
	}

	public Double getTxAmount() {
		return txAmount;
	}

	public void setTxAmount(Double txAmount) {
		this.txAmount = txAmount;
	}

	public String getTxDescription() {
		return txDescription;
	}

	public void setTxDescription(String txDescription) {
		this.txDescription = txDescription;
	}

}
