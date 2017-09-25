package com.oodles.coreservice.exception;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.bitcoinj.core.InsufficientMoneyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oodles.coreservice.constant.ResponseHandlerConstant;

@RestControllerAdvice
public class GlobalExceptionHandler {
	public static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
	/**
	 * to handle wallet exception if wallet already exist by uuid
	 * @description handleWalletException
	 * @param 
	 * @return Map<String,Object>
	 * @exception 
	 *
	 */
	@ExceptionHandler(WalletException.class)
	public Map<String,Object> handleWalletException(HttpServletResponse response,WalletException ex){
		
		Map<String,Object> map=new HashMap<String, Object>();
		
		map.put(ResponseHandlerConstant.MESSAGE,ex.getMessage());
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.BAD_REQUEST);
		map.put(ResponseHandlerConstant.ERROR,true);
		map.put(ResponseHandlerConstant.DATA,null);
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date());
		response.setStatus(400);
		return map;
	}
	
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public Map<String,Object> handleDataIntegrityViolationException(HttpServletResponse response,DataIntegrityViolationException ex){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("localizeMsg",ex.getLocalizedMessage());
		map.put("errMsg",ex.getMessage());
		map.put("timeStamp",new Date().getTime());
		map.put("status",HttpStatus.BAD_REQUEST);
		map.put("isSuccess","false");
		response.setStatus(500);
		return map;
	}
	@ExceptionHandler(InsufficientMoneyException.class)
	@ResponseBody
	public Map<String,Object>handleInsufficientMoneyException(HttpServletResponse resp, InsufficientMoneyException ex){
		Map<String,Object>map=new HashMap<String,Object>();
		map.put("LocalizeMsg", ex.getLocalizedMessage());
		map.put("errMsg", ex.getMessage());
		map.put("timestamp", new Date().getTime());
		map.put("isSuccess", "false");
		resp.setStatus(500);
		ex.printStackTrace();
		return map;
	}
		
	@ExceptionHandler(IOException.class)
	@ResponseBody
	public Map<String,Object> handleIOException(HttpServletResponse response,IOException ex){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("localizeMsg",ex.getLocalizedMessage());
		map.put("errMsg",ex.getMessage());
		map.put("timeStamp",new Date().getTime());
		map.put("status",HttpStatus.BAD_REQUEST);
		map.put("isSuccess","false");
		response.setStatus(405);
		return map;
	}
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String,Object> handleAllException(HttpServletResponse response,Exception ex){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("localizeMsg",ex.getLocalizedMessage());
		map.put("errMsg",ex.getMessage());
		map.put("timeStamp",new Date().getTime());
		map.put("status",HttpStatus.BAD_REQUEST);
		map.put("isSuccess","false");
		response.setStatus(500);
		return map;
	}

}
