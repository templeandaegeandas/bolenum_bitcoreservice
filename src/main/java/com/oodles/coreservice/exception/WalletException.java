package com.oodles.coreservice.exception;

public class WalletException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public WalletException(String msg){
		super(msg);
	}
}
