package com.oodles.coreservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oodles.coreservice.dao.ApplicationDetailsDao;
import com.oodles.coreservice.domain.ApplicationsDetail;
import com.oodles.coreservice.security.ApiKeyService;
import com.oodles.coreservice.security.SecretKeyService;
import com.oodles.coreservice.util.ResponseHandler;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 *	This class has method to register application with bitcoin core services
 *	@author Ajit Soman
 */
@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {
	@Autowired 
	ApplicationDetailsDao applicationDetailsDao;
	
    @ApiOperation(value = "Register Application with bitcoin core",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in application registration"),
            @ApiResponse(code = 400, message = "Unable to register your application")
    })
	@RequestMapping(value = "/auth/registration", method = RequestMethod.POST)
	public ResponseEntity<Object> generateRegistrationKeys(@RequestParam String applicationName) {
		String apiKey = ApiKeyService.generateApiKey();
		String secretKey = SecretKeyService.getSecretKey();
		if(apiKey!=null && secretKey!=null){
			ApplicationsDetail applicationsDetail = new ApplicationsDetail(secretKey, apiKey,applicationName);
			ApplicationsDetail savedAppDetail = applicationDetailsDao.save(applicationsDetail);
			return ResponseHandler.generateResponse("Your application is registered at bitcoin core", HttpStatus.OK, false, savedAppDetail);
		}else{
			return ResponseHandler.generateResponse("Unable to register your application", HttpStatus.BAD_REQUEST, true, null);
		}		
	}

}
