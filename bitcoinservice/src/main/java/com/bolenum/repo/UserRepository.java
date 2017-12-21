package com.bolenum.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bolenum.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	public User findByEmailId(String emailId);
	 
	public User findByUserId(Long id);

	public User findByMobileNumber(String mobileNumber);

}