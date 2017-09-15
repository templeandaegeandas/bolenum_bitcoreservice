package com.oodles.coreservice.exception;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.bitcoinj.core.InsufficientMoneyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//@ControllerAdvice
public class GlobalExceptionHandler {
	public static final Logger log=Logger.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public Map<String,Object> handleDataIntegrityViolationException(HttpServletResponse response,DataIntegrityViolationException ex){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("localizeMsg",ex.getLocalizedMessage());
		map.put("errMsg",ex.getMessage());
		map.put("timeStamp",new Date().getTime());
		map.put("status",HttpStatus.SC_BAD_REQUEST);
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
		map.put("status",HttpStatus.SC_BAD_REQUEST);
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
		map.put("status",HttpStatus.SC_BAD_REQUEST);
		map.put("isSuccess","false");
		response.setStatus(500);
		return map;
	}

}
