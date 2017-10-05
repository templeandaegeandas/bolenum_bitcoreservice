package com.oodles.coreservice.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
/**
 *	A service that has method to generate signature
 *	@author Nimish Karan
 */
@Service
public class KeyService {
	private Logger log = LoggerFactory.getLogger(KeyService.class);
	/**
	 * Generate a authorization signature
	 * @param nonce
	 * @param secretKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public  String getAuthSignature(String nonce, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException    {

		    byte[] hmacData ;
		    String encodedKey=null;
		    log.debug("get auth signature: {}",secretKey);
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
