package com.oodles.coreservice.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ApplicationsDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String secretKey;
	private String apiKey;
	private String applicationName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public ApplicationsDetail(String secretKey, String apiKey, String applicationName) {
		super();
		this.secretKey = secretKey;
		this.apiKey = apiKey;
		this.applicationName = applicationName;
	}
	public ApplicationsDetail() {
		super();
	}

}
