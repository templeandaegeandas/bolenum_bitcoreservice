package com.oodles.coreservice.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.zxing.WriterException;
import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.dao.ApplicationDetailsDao;
import com.oodles.coreservice.domain.AddressInfo;
import com.oodles.coreservice.services.AuthenticationService;
import com.oodles.coreservice.services.HotWalletService;
import com.oodles.coreservice.services.KeyService;
import com.oodles.coreservice.services.QRCodeService;
import com.oodles.coreservice.services.WalletStoreService;
import com.oodles.coreservice.services.wallet.WalletRefreshService;
import com.oodles.coreservice.util.ObjectMapperUtil;
import com.oodles.coreservice.util.ResponseHandler;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 * This class has methods related to hot wallet's creation,getting balance,getting address,
 *  recovery etc... 
 *  
 *  @author Murari Kumar
 *
 */
@RestController
@RequestMapping("/api/v1")
public class HotWalletController {

	public static Logger log = LoggerFactory.getLogger(HotWalletController.class);
	@Autowired
	HotWalletService hotWalletService;
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
	
    @ApiOperation(value = "Create a hot bitcoin wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created a hot wallet"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to create a hot wallet")
    })
	@RequestMapping(value = "/hotwallet/create", method = RequestMethod.POST)
	public ResponseEntity<Object> createWallet(HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				Map<String, Object> map = hotWalletService.createWallet();
				return ResponseHandler.generateResponse("Hot wallet has been created successfully", HttpStatus.OK, false, map);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (URISyntaxException | WriterException | IOException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}

    @ApiOperation(value = "Get current bitcoin address of a hot wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting hot wallet address"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get hot wallet address")
    })
	@RequestMapping(value = "/hotwallet/address", method = RequestMethod.GET)
	public ResponseEntity<Object> getWalletAddress(@RequestParam String walletUuid,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				Address address = hotWalletService.getWalletAddress(walletUuid);
				Map<String, Object> map = qRCodeService.qrCodeGeneration(address.toString());
				return ResponseHandler.generateResponse("success", HttpStatus.OK, false, map);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (URISyntaxException | WriterException | IOException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}

    @ApiOperation(value = "Get primary bitcoin address of a hot wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting hot wallet address"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get hot wallet address")
    })
	@RequestMapping(value = "/hotwallet/primaryaddress", method = RequestMethod.GET)
	public ResponseEntity<Object> getPrimaryAddress(@RequestParam String walletUuid,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				log.info("fetching address");
				String address = hotWalletService.getPrimaryAddress(walletUuid);
				AddressInfo addr = addressInfoDao.getAddress(walletUuid);

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("address", address);
				map.put("file_name", addr.getQrCodeFilename());
				return ResponseHandler.generateResponse("success", HttpStatus.OK, false, map);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}
    
    @ApiOperation(value = "Generate fresh bitcoin address of a hot wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in generation of hot wallet address"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to generate hot wallet address")
    })
	@RequestMapping(value = "/hotwallet/freshaddress", method = RequestMethod.GET)
	public ResponseEntity<Object> getWalletRefreshAddress(@RequestParam String walletUuid,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				log.info("fetching address");
				HashMap<String, String> addressMap = hotWalletService.getWalletRefreshAddress(walletUuid);
				Map<String, Object> map = qRCodeService.NewqrCodeGeneration(addressMap.get("address"));
				map.put("label", "Label this address");
				map.put("received", addressMap.get("balance"));
				return ResponseHandler.generateResponse("success", HttpStatus.OK, false, map);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (URISyntaxException | WriterException | IOException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}
    
    @ApiOperation(value = "Get all bitcoin addresses of a hot wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting all hot wallet address"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get hot wallet address")
    })
	@RequestMapping(value = "/hotwallet/alladdress", method = RequestMethod.GET)
	public ResponseEntity<Object> getWalletAllAddress(@RequestParam String walletUuid,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				log.info("fetching address");
				List<AddressInfo> address = hotWalletService.getWalletAllAddress(walletUuid);
				return ResponseHandler.generateResponse("success", HttpStatus.OK, false, address);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}
    
    @ApiOperation(value = "Get bitcoin hot wallet balance",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in getting hot wallet balance"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to get hot wallet balance")
    })
	@RequestMapping(value = "/hotwallet/balance", method = RequestMethod.GET)
	public ResponseEntity<Object> getWalletBalance(@RequestParam String walletUuid,HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				log.debug(ObjectMapperUtil.mapObjectToString(walletUuid));
				boolean result = walletRefreshService.getStatus(WalletRefreshService.result);
				if (result == true) {
					return ResponseHandler.generateResponse("success", HttpStatus.OK, false, "Synchronizing");
				} else {
					String balance = hotWalletService.getWalletBalace(walletUuid);
					return ResponseHandler.generateResponse("Authentication success,wallet Balance", HttpStatus.OK,false, balance);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

    @ApiOperation(value = "Create a primary admin hot bitcoin wallet",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in generating wallet"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to generate wallet")
    })
	@RequestMapping(value = "/hotwallet/create/adminwallet", method = RequestMethod.POST)
	public ResponseEntity<Object> createAdminWallet(HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				Map<String, Object> walletDetail = hotWalletService.createAdminWallet();
				if (walletDetail != null) {
					return ResponseHandler.generateResponse("Success", HttpStatus.OK, false, walletDetail);
				} else {
					return ResponseHandler.generateResponse("Failed", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}
    
    @ApiOperation(value = "Update wallet address label",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in updating wallet address label"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to update balance wallet address label")
    })
	@RequestMapping(value = "/hotwallet/update/label", method = RequestMethod.PUT)
	public ResponseEntity<Object> updateLebel(@RequestBody AddressInfo addressInfo, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String address = addressInfo.getAddress();
				String label = addressInfo.getLabel();
				Integer i = hotWalletService.updateLebel(address, label);
				if (i >0) {
					return ResponseHandler.generateResponse("successFully updated", HttpStatus.OK, false, i);
				} else {
					return ResponseHandler.generateResponse("failed", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

    @ApiOperation(value = "Restore hot wallet from seed",response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success in restoring hot wallet from seed"),
            @ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
            @ApiResponse(code = 400, message = "Unable to restore hot wallet from seed")
    })
	@RequestMapping(value = "/hotwallet/wallet/restore", method = RequestMethod.GET)
	public ResponseEntity<Object> restoreWallet(@RequestParam("walletUuid") String walletUuid,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String response = hotWalletService.restoreWalletFromSeed(walletUuid);
				if (response != null) {
					return ResponseHandler.generateResponse("Wallet Restored Successfully", HttpStatus.OK, false,
							response);
				} else {
					return ResponseHandler.generateResponse("Something went wrong", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

}
