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
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oodles.coreservice.constant.ResponseHandlerConstant;

@RestControllerAdvice
public class GlobalExceptionHandler {
	public static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * to handle wallet exception if wallet already exist by uuid @description
	 * handleWalletException @param @return Map<String,Object> @exception
	 *
	 */
	@ExceptionHandler(WalletException.class)
	public Map<String, Object> handleWalletException(HttpServletResponse response, WalletException ex) {
		log.error("wallet exception:{}", ex.getMessage());
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ResponseHandlerConstant.MESSAGE, ex.getMessage());
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.BAD_REQUEST.value());
		map.put(ResponseHandlerConstant.ERROR, true);
		map.put(ResponseHandlerConstant.DATA, null);
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date().getTime());
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return map;
	}

	/**
	 * to handle data integration exception if primary and foreign key
	 * voilation @description
	 * handleDataIntegrityViolationException @param @return
	 * Map<String,Object> @exception
	 *
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public Map<String, Object> handleDataIntegrityViolationException(HttpServletResponse response,
			DataIntegrityViolationException ex) {
		log.error("Data Integrity Violation exception:{}", ex.getMessage());
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ResponseHandlerConstant.LOCAL_MSG, ex.getLocalizedMessage());
		map.put(ResponseHandlerConstant.MESSAGE, ex.getMessage());
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date().getTime());
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.BAD_REQUEST.value());
		map.put(ResponseHandlerConstant.ERROR, true);
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return map;
	}

	/**
	 * to handle memory insufficient memory exception @description
	 * handleInsufficientMoneyException @param @return
	 * Map<String,Object> @exception
	 *
	 */
	@ExceptionHandler(InsufficientMoneyException.class)
	public Map<String, Object> handleInsufficientMoneyException(HttpServletResponse resp,
			InsufficientMoneyException ex) {
		log.error("Data Integrity Violation exception:{}", ex.getMessage());
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ResponseHandlerConstant.LOCAL_MSG, ex.getLocalizedMessage());
		map.put(ResponseHandlerConstant.MESSAGE, ex.getMessage());
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date().getTime());
		map.put(ResponseHandlerConstant.ERROR, true);
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
		resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return map;
	}

	/**
	 * to handle file related exception @description
	 * handleIOException @param @return Map<String,Object> @exception
	 *
	 */
	@ExceptionHandler(IOException.class)
	public Map<String, Object> handleIOException(HttpServletResponse response, IOException ex) {
		log.error("IO Exception:{}", ex.getMessage());
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ResponseHandlerConstant.LOCAL_MSG, ex.getLocalizedMessage());
		map.put(ResponseHandlerConstant.MESSAGE, ex.getMessage());
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date().getTime());
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.BAD_REQUEST.value());
		map.put(ResponseHandlerConstant.ERROR, true);
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		return map;
	}

	/**
	 * to handle over all exception in application @description
	 * handleAllException @param @return Map<String,Object> @exception
	 *
	 */
	@ExceptionHandler(Exception.class)
	public Map<String, Object> handleAllException(HttpServletResponse response, Exception ex) {
		log.error("All Exception:{}", ex.getMessage());
		ex.printStackTrace();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ResponseHandlerConstant.LOCAL_MSG, ex.getLocalizedMessage());
		map.put(ResponseHandlerConstant.MESSAGE, ex.getMessage());
		map.put(ResponseHandlerConstant.TIME_STAMP, new Date().getTime());
		map.put(ResponseHandlerConstant.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
		map.put(ResponseHandlerConstant.ERROR, true);
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return map;
	}

}
