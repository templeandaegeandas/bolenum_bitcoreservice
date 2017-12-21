/**
 * 
 */
package com.bolenum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bolenum.services.BTCWalletService;

/**
 * @author chandan kumar singh
 * @date 21-Dec-2017
 */
@RestController
@RequestMapping(value = "/api/v1")
public class ListenerController {
	@Autowired
	private BTCWalletService bTCWalletService;

	@RequestMapping(value = "/block/notification", method = RequestMethod.POST)
	public ResponseEntity<Object> getBlockTxList(@RequestBody String data) {
		bTCWalletService.getBlock(data);
		return new ResponseEntity(HttpStatus.OK);
	}

}
