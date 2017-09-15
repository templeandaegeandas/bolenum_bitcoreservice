package com.oodles.coreservice.dao;

import java.io.Serializable;


import org.springframework.data.jpa.repository.JpaRepository;

import com.oodles.coreservice.domain.ApplicationsDetail;


public interface ApplicationDetailsDao extends JpaRepository<ApplicationsDetail,Serializable> {
	public ApplicationsDetail findByApiKey(String apikey);
}
