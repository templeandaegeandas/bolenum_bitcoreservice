package com.bolenum.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bolenum.model.User;
import com.bolenum.model.UserCoin;

public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

	UserCoin findByTokenNameAndUser(String tokenName, User user);
	
	UserCoin findByWalletAddress(String walletAddress);
}
