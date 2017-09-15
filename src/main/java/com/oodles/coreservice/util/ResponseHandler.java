package com.oodles.coreservice.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.oodles.coreservice.constant.ResponseHandlerConstant;

/**
 * Http response handler
 * 
 * @author Ajit Soman
 *
 */
public class ResponseHandler {
	/**
	 * Generate Http response
	 * @param message
	 * @param status
	 * @param error
	 * @param resObj
	 * @return
	 */
	public static ResponseEntity<Object> generateResponse(String message,HttpStatus status,Boolean error,Object resObj){
		Map<String,Object> map=new HashMap<String, Object>();
		try{
			
		
		map.put(ResponseHandlerConstant.MESSAGE,message);
		map.put(ResponseHandlerConstant.STATUS, status);
		map.put(ResponseHandlerConstant.ERROR,error);
		map.put(ResponseHandlerConstant.DATA,resObj);
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date());
		return new ResponseEntity<Object>(map,status);
		}catch(Exception e){
			e.printStackTrace();
			map.clear();
			map.put(message, e.getMessage());
			map.put("Status", HttpStatus.INTERNAL_SERVER_ERROR);
			map.put("TimeStamp",new Date());
			return new ResponseEntity<Object>(map,status);
		}
	}

}
