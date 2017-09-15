package com.oodles.coreservice.security;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;
/**
 *	A class which has methods to generate Hmac SHA256 Signature for application
 *
 *	@author Nimish Karan
 */
public class SignatureGenerator extends SecretKeyService {

	public static String nonce = String.valueOf(System.currentTimeMillis());
	/**
	 * Generate signature
	 * 
	 * @param nonce
	 * @param secretKey
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static String generateHmacSHA256Signature(String nonce, String secretKey)   throws GeneralSecurityException {
		
	    byte[] hmacData ;
	    String encodedKey=null;
	    try {
	        SecretKeySpec secretkey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
	        Mac mac = Mac.getInstance("HmacSHA256");
	        mac.init(secretkey);
	        hmacData = mac.doFinal(nonce.getBytes("UTF-8"));
	        encodedKey = Base64.getEncoder().encodeToString(hmacData);	   
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    }
	  return encodedKey;
		
	}

}