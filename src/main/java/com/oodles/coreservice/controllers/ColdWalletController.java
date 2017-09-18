package com.oodles.coreservice.controllers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.dao.ApplicationDetailsDao;
import com.oodles.coreservice.exception.WalletException;
import com.oodles.coreservice.services.AuthenticationService;
import com.oodles.coreservice.services.ColdWalletService;
import com.oodles.coreservice.services.KeyService;
import com.oodles.coreservice.services.QRCodeService;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.wallet.WalletRefreshService;
import com.oodles.coreservice.util.ResponseHandler;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 * This class has methods related to cold wallet's creation,synchronization,getting balance,
 * getting address, Removing cold wallet from block chain and re-download cold wallet on lost 
 *
 *@author Ajit Soman
 */
@RestController
@RequestMapping("/api/v1")
public class ColdWalletController {

	public static Logger log = LoggerFactory.getLogger(ColdWalletController.class);
	@Autowired
	WalletStoreService walletStoreService;
	@Autowired
	ApplicationDetailsDao applicationDetailsDao;
	@Autowired
	KeyService keyService;
	@Autowired
	AuthenticationService authenticationService;
	@Autowired
	QRCodeService qRCodeService;
	@Autowired
	WalletRefreshService walletRefreshService;
	@Autowired
	AddressInfoDao addressInfoDao;
	@Autowired
	ColdWalletService coldWalletService;
	
    @ApiOperation(value = "Create a cold bitcoin wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in generating wallet"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to generate wallet")
    })
	@RequestMapping(value = "/coldwallet/create", method = RequestMethod.GET) // Make it post
	public void createColdWallet(HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		try {
			if (authenticationService.authenticateRequest(request)) {
				coldWalletService.createColdWallet(response);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Authentication unsuccessful");
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
		} catch (IOException | WalletException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
		}
	}
    
    @ApiOperation(value = "Synchronize cold bitcoin wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in synchronizing wallet"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to synchronize wallet")
    })
	@RequestMapping(value = "/coldwallet/synchronize", method = RequestMethod.POST)
	public ResponseEntity<Object> synchronizeColdWallet(@RequestParam MultipartFile file,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
					String walletUuid = coldWalletService.synchronizeColdWallet(file);
				return ResponseHandler.generateResponse("Synchonizing your wallet", HttpStatus.OK, false,walletUuid);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (IOException | UnreadableWalletException | BlockStoreException | InterruptedException | WalletException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		} 
	}
    
    @ApiOperation(value = "Get cold bitcoin wallet balance",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting wallet balance"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get wallet balance")
    })
	@RequestMapping(value = "/coldwallet/balance", method = RequestMethod.GET)
	public ResponseEntity<Object> getColdWallet(@RequestParam String walletUuid,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String balance = coldWalletService.coldWalletBalance(walletUuid);
				if (balance != null) {
					return ResponseHandler.generateResponse("Success", HttpStatus.OK, false, balance);
				} else {
					return ResponseHandler.generateResponse("Failed", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (WalletException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}

    @ApiOperation(value = "Get cold bitcoin wallet address",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting wallet address"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get wallet address")
    })
	@RequestMapping(value = "/coldwallet/address", method = RequestMethod.GET)
	public ResponseEntity<Object> getColdWalletAddress(@RequestParam String walletUuid,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String address = coldWalletService.getColdWalletAddress(walletUuid);
				if (address != null) {
					return ResponseHandler.generateResponse("Success", HttpStatus.OK, false, address);
				} else {
					return ResponseHandler.generateResponse("Failed", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (WalletException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}
    
    @ApiOperation(value = "Remove cold wallet from blockchain",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in removing cold wallet from block chain"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to remove cold wallet from block chain")
    })
	@RequestMapping(value = "/coldwallet/remove/blockchain", method = RequestMethod.POST)
	public ResponseEntity<Object> removeColdWalletFromBlockChain(@RequestParam String walletUuid,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				coldWalletService.removeColdWalletFromBlockChain(walletUuid);
				return ResponseHandler.generateResponse("Cold wallet has been disconnected from blockchain successfully", HttpStatus.OK, false, null);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (WalletException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}

    @RequestMapping(value = "/coldwallet/redownload", method = RequestMethod.GET) 
	public void Wallet(@RequestParam String walletUuid,
			HttpServletRequest request,HttpServletResponse response) throws IOException {
		try {
			if (authenticationService.authenticateRequest(request)) {
				coldWalletService.recoverColdWallet(walletUuid, response);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Authentication unsuccessful");
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException  e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
		} catch(WalletException | UnreadableWalletException | IOException e){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
		}
	}

}

