package com.oodles.coreservice.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

import org.springframework.stereotype.Service;
/**
 *	A class which has methods to generate Secret key for application
 *	@author Nimish Karan
 */
@Service
public class SecretKeyService {
	/**
	 *  
	 * Generate secret key
	 */
	public static String getSecretKey() {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(256);
			SecretKey skey = kgen.generateKey();
			return DatatypeConverter.printBase64Binary(skey.getEncoded()).toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
