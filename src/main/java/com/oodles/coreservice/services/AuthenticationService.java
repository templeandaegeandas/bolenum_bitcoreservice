package com.oodles.coreservice.services;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.ApplicationDetailsDao;
import com.oodles.coreservice.domain.ApplicationsDetail;
/**
 *	A service that has method to authenticate request based on apiKey,signature and nonce
 *	@author Ajit Soman
 */
@Service
public class AuthenticationService {
	@Autowired
	ApplicationDetailsDao applicationDetailsDao;
	@Autowired
	KeyService keyService;
	@Autowired
	EnvConfiguration env;
	/**
	 * Request authenticator
	 * @param request
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	public boolean authenticateRequest(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException {
		if(env.isSecurityEnabled()){
			String apiKey = request.getHeader("apikey");
			String nonce = request.getHeader("nonce");
			String signature = request.getHeader("signature");
			
			ApplicationsDetail applicationsDetail = applicationDetailsDao.findByApiKey(apiKey);
			if(applicationsDetail!=null){
				String secretkey = applicationsDetail.getSecretKey();
				String finalSignature = keyService.getAuthSignature(nonce, secretkey);			
				if (signature.equals(finalSignature)) {
					return true;
				}else{
					return false;	
				}
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
}
