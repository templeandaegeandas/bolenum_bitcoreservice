package com.oodles.coreservice.dao;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oodles.coreservice.domain.WalletInfo;
import com.oodles.coreservice.enums.WalletStatus;
import com.oodles.coreservice.enums.WalletType;

@Transactional
public interface WalletDao extends JpaRepository<WalletInfo, Serializable> {
	@Query("SELECT w FROM WalletInfo w WHERE w.walletStatus=:walletStatus and w.walletType!=:walletType")
	public List<WalletInfo> getAllWalletOtherThanSpecifiedWallet(@Param("walletStatus") WalletStatus walletStatus,@Param("walletType") WalletType walletType);
	public WalletInfo findByWalletUuid(String walletUuid);

	WalletInfo findFirstByOrderByDateCreatedAsc();
	
}
 