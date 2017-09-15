package com.oodles.coreservice.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

public class Escrow {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ESCROW_ID")
	private Long id;
	private Date dateCreated;
	private Date dateUpdated;
	private Integer senderWalletId;
	private Integer intermidiateWalletId;
	private Integer adminWalletId;
	private Double feeBetweenSenderIntermidiateWallet;
	private Double feeBetweenIntermidiateAdminWallet;
	private Double feeBetweenIntermidateReceiverWallet;
	private Double amount;
	private String escrow_status;
	@OneToMany
	@JoinColumn(name="ESCROW_ID")
	private Set<TransactionInfo> transactions = new HashSet<TransactionInfo>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public Integer getSenderWalletId() {
		return senderWalletId;
	}
	public void setSenderWalletId(Integer senderWalletId) {
		this.senderWalletId = senderWalletId;
	}
	public Integer getIntermidiateWalletId() {
		return intermidiateWalletId;
	}
	public void setIntermidiateWalletId(Integer intermidiateWalletId) {
		this.intermidiateWalletId = intermidiateWalletId;
	}
	public Integer getAdminWalletId() {
		return adminWalletId;
	}
	public void setAdminWalletId(Integer adminWalletId) {
		this.adminWalletId = adminWalletId;
	}
	public Double getFeeBetweenSenderIntermidiateWallet() {
		return feeBetweenSenderIntermidiateWallet;
	}
	public void setFeeBetweenSenderIntermidiateWallet(Double feeBetweenSenderIntermidiateWallet) {
		this.feeBetweenSenderIntermidiateWallet = feeBetweenSenderIntermidiateWallet;
	}
	public Double getFeeBetweenIntermidiateAdminWallet() {
		return feeBetweenIntermidiateAdminWallet;
	}
	public void setFeeBetweenIntermidiateAdminWallet(Double feeBetweenIntermidiateAdminWallet) {
		this.feeBetweenIntermidiateAdminWallet = feeBetweenIntermidiateAdminWallet;
	}
	public Double getFeeBetweenIntermidateReceiverWallet() {
		return feeBetweenIntermidateReceiverWallet;
	}
	public void setFeeBetweenIntermidateReceiverWallet(Double feeBetweenIntermidateReceiverWallet) {
		this.feeBetweenIntermidateReceiverWallet = feeBetweenIntermidateReceiverWallet;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getEscrow_status() {
		return escrow_status;
	}
	public void setEscrow_status(String escrow_status) {
		this.escrow_status = escrow_status;
	}
	public Set<TransactionInfo> getTransactions() {
		return transactions;
	}
	public void setTransactions(Set<TransactionInfo> transactions) {
		this.transactions = transactions;
	}

}
