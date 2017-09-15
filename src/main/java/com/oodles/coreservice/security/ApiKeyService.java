package com.oodles.coreservice.security;


import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.stereotype.Service;
/**
 *	A class which has methods to generate API key for application
 *	@author Nimish Karan
 */
@Service
public class ApiKeyService {
	/**
	 *  
	 * Generate API key
	 */
	public static String generateApiKey() {
		try {
			UUID key = UUID.randomUUID();
			String uuid = key.toString();
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(uuid.getBytes());
			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16));
			}
			return sb.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
