package com.oodles.coreservice.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AddressInfo {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String address;
	private String amount;
	private String label;
	private String walletUuid;
	public String qrCodeFilename;
	private String walletBalance;
	private Boolean isPrimary;
	
	
	public Boolean getIsPrimary() {
		return isPrimary;
	}
	public void setIsPrimary(Boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	public String getWalletBalance() {
		return walletBalance;
	}
	public void setWalletBalance(String walletBalance) {
		this.walletBalance = walletBalance;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getWalletUuid() {
		return walletUuid;
	}
	public void setWalletUuid(String walletUuid) {
		this.walletUuid = walletUuid;
	}
	public String getQrCodeFilename() {
		return qrCodeFilename;
	}
	public void setQrCodeFilename(String qrCodeFilename) {
		this.qrCodeFilename = qrCodeFilename;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	

}
