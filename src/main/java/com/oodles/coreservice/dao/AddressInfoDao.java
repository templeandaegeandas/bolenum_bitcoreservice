package com.oodles.coreservice.dao;

/**
 * @author Murari Kumar
 */
import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oodles.coreservice.domain.AddressInfo;

public interface AddressInfoDao extends JpaRepository<AddressInfo,Serializable> {

	@Query("from AddressInfo a where a.walletUuid=:walletUuid")
	List<AddressInfo> getAllAddress(@Param("walletUuid") String walletUuid);
	
	@Transactional
	@Modifying
	@Query("update AddressInfo ad set ad.label =:label where ad.address=:address")

	public Integer updateLabel(@Param("address") String address,@Param("label") String label);
	
	@Transactional
	@Modifying
	@Query("update AddressInfo addr set addr.amount =?1 where addr.address =?2")
	public Integer updateAddressBalance(String amount,String address);
	public AddressInfo findByAddress(String address);
	
	@Query("select a from AddressInfo a where a.walletUuid=?1 and a.isPrimary='t'")
	public AddressInfo getAddress(@Param("walletUuid") String walletUuid);
}
