package com.oodles.coreservice.exception;

import org.springframework.http.HttpStatus;

public interface CustomGenericException {
	public HttpStatus getStatus();
	public String getErrorMessage();
	public String getMessage();
	public Integer getErrorCode();
}
