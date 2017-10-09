package com.oodles.coreservice.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.oodles.coreservice.enums.WalletStatus;
import com.oodles.coreservice.enums.WalletType;

@Entity
public class WalletInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "WALLET_ID")
	private Long id;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUpdated;
	@Column(unique = true)
	private String walletUuid;
	private WalletType walletType;
	private WalletStatus walletStatus;

	private String walletSeedToken;
	private Long timpStamp;
	private String walletEarliestKeyCreationTime;

	@OneToMany
	@JoinColumn(name = "WALLET_ID")
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

	public String getWalletUuid() {
		return walletUuid;
	}

	public void setWalletUuid(String walletUuid) {
		this.walletUuid = walletUuid;
	}

	public WalletType getWalletType() {
		return walletType;
	}

	public void setWalletType(WalletType walletType) {
		this.walletType = walletType;
	}

	public WalletStatus getWalletStatus() {
		return walletStatus;
	}

	public void setWalletStatus(WalletStatus walletStatus) {
		this.walletStatus = walletStatus;
	}

	public String getWalletSeedToken() {
		return walletSeedToken;
	}

	public void setWalletSeedToken(String walletSeedToken) {
		this.walletSeedToken = walletSeedToken;
	}

	public Long getTimpStamp() {
		return timpStamp;
	}

	public void setTimpStamp(Long timpStamp) {
		this.timpStamp = timpStamp;
	}

	public String getWalletEarliestKeyCreationTime() {
		return walletEarliestKeyCreationTime;
	}

	public void setWalletEarliestKeyCreationTime(String walletEarliestKeyCreationTime) {
		this.walletEarliestKeyCreationTime = walletEarliestKeyCreationTime;
	}
}