package com.oodles.coreservice.controllers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.InsufficientMoneyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oodles.coreservice.dao.TransactionDao;
import com.oodles.coreservice.domain.TransactionInfo;
import com.oodles.coreservice.domain.TransactionParams;
import com.oodles.coreservice.enums.DateFilter;
import com.oodles.coreservice.exception.BitcoinTransactionException;
import com.oodles.coreservice.exception.WalletException;
import com.oodles.coreservice.services.AuthenticationService;
import com.oodles.coreservice.services.HotWalletService;
import com.oodles.coreservice.services.TransactionService;
import com.oodles.coreservice.util.ResponseHandler;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 * This class has methods for performing transaction and getting details related to a particular 
 * transaction for both hot and cold wallet 
 *
 *	@author Murari Kumar and Ajit Soman
 */
@RestController
@RequestMapping("/api/v1")
public class TransactionController {

	@Autowired
	TransactionService transactionservice;
	@Autowired
	TransactionDao transactionDao;
	@Autowired
	HotWalletService hotWalletService;
	@Autowired
	AuthenticationService authenticationService;

	@ApiOperation(value = "Create a bitcoin transaction", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in transaction"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to process transaction") })
	@RequestMapping(value = "/hotwallet/transaction", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Object> WalletTransaction(@RequestBody TransactionParams transactionparams,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String response = null;
				try {
					response = transactionservice.createTransaction(transactionparams);
				} catch (Exception e) {
					return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
				}

				if (response != null) {
					return ResponseHandler.generateResponse("Transaction has been successfully completed",
							HttpStatus.OK, false, response);
				} else {
					return ResponseHandler.generateResponse("Transaction is not completed", HttpStatus.BAD_REQUEST,
							true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e1) {
			return ResponseHandler.generateResponse(e1.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get a transaction history", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction history"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction history") })
	@RequestMapping(value = "/transaction/list", method = RequestMethod.GET)
	public ResponseEntity<Object> getTransactionHistory(@RequestParam String walletUuid, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				hotWalletService.getIncommingTransaction(walletUuid);
				List<TransactionInfo> details = transactionDao.getTransactionDetails(walletUuid);
				System.out.println(details);
				if (!details.isEmpty()) {
					return ResponseHandler.generateResponse("Here is the transaction history", HttpStatus.OK, false,
							details);
				} else {
					return ResponseHandler.generateResponse("there is no transaction history for this wallet id ",
							HttpStatus.BAD_REQUEST, false, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Create a cold wallet bitcoin transaction", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in transaction"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to process transaction") })
	@RequestMapping(value = "/coldwallet/transaction", method = RequestMethod.POST)
	public ResponseEntity<Object> getColdWallet(@RequestBody TransactionParams transactionparams,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				String txHash = transactionservice.transactionForColdWallet(transactionparams);
				if (txHash != null) {
					return ResponseHandler.generateResponse("Success", HttpStatus.OK, false, txHash);
				} else {
					return ResponseHandler.generateResponse("Failed", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch (WalletException | AddressFormatException | InsufficientMoneyException | InterruptedException
				| ExecutionException | BitcoinTransactionException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}

	@ApiOperation(value = "Get transaction history count per week,month and year", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction count"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction count") })
	@RequestMapping(value = "/transaction/history/count", method = RequestMethod.GET)
	public ResponseEntity<Object> getTransactionsCount(HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				HashMap<String, Long> map = new HashMap<String, Long>();
				map.put("TransactionsPerWeek", transactionDao.getTransactionHistoryPerWeek());
				map.put("TransactionsPerMonth", transactionDao.getTransactionHistoryPerMonth());
				map.put("TransactionsPerYear", transactionDao.getTransactionHistoryPerYear());
				map.put("TotalTransactions", transactionDao.count());

				if (!map.isEmpty()) {
					return ResponseHandler.generateResponse("history/week", HttpStatus.OK, false, map);
				} else {
					return ResponseHandler.generateResponse("there is no transaction history", HttpStatus.OK, false,
							null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get wallet's transaction status", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction status"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction status") })
	@RequestMapping(value = "/transaction/user/status", method = RequestMethod.GET)
	public ResponseEntity<Object> getUserTransactionStatus(@RequestParam String walletUuid,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				List<HashMap<String, String>> response = hotWalletService.getTransactionStatus(walletUuid);
				System.out.println("getUserTransactionStatus  " + response);
				if (response != null) {
					return ResponseHandler.generateResponse("Here is the transaction status", HttpStatus.OK, false,
							response);
				} else {
					return ResponseHandler.generateResponse("there is no transaction status for this wallet id ",
							HttpStatus.BAD_REQUEST, false, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get transaction hash confirmation", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction hash confirmation"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction hash confirmation") })
	@RequestMapping(value = "/transaction/hash/confirmation", method = RequestMethod.GET)
	public ResponseEntity<Object> confirmationBasedOnHash(@RequestParam String[] hash, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				List<HashMap<String, String>> confirmation = transactionservice.getConfirmationByHash(hash);
				if (confirmation != null) {
					return ResponseHandler.generateResponse("success", HttpStatus.OK, true, confirmation);
				} else {
					return ResponseHandler.generateResponse("failure", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get transaction hash confirmation using walletUuid", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction hash confirmation"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction hash confirmation") })
	@RequestMapping(value = "/transaction/hash/confirmation/by/walletuuid", method = RequestMethod.GET)
	public ResponseEntity<Object> confirmationBasedOnWallet(@RequestParam String walletUuid,
			@RequestParam String[] hash, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				List<HashMap<String, String>> response = hotWalletService.getConfirmationByWallet(walletUuid, hash);
				if (response != null) {
					return ResponseHandler.generateResponse("success", HttpStatus.OK, true, response);
				} else {
					return ResponseHandler.generateResponse("failure", HttpStatus.BAD_REQUEST, true, null);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get total credit,debit and fee details for a wallet", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting total credit,debit and fee details"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get total credit,debit and fee details") })
	@RequestMapping(value = "/transaction/credit/debit/total", method = RequestMethod.GET)
	public ResponseEntity<Object> getCreditDebitTotalByWalletId(@RequestParam String walletId,
			HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				Map<String, Double> response = transactionservice.getCreditDebitTotalByWalletId(walletId);
				return ResponseHandler.generateResponse("success", HttpStatus.OK, true, response);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}

	@ApiOperation(value = "Get transaction list for a wallet with pagination", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction list"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction list") })
	@RequestMapping(value = "/transaction/list/paginated", method = RequestMethod.GET)
	public ResponseEntity<Object> getPaginatedTransactionList(@RequestParam int pageNumber, @RequestParam int pageSize,
			@RequestParam String walletId, @RequestParam(required = false) String searchText,
			@RequestParam(required = false) DateFilter filter, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				if (filter != null) {
					if (searchText == null) {
						searchText = "";
					}
					Page<TransactionInfo> transactionList = transactionservice.searchWithFilterPaginatedTransactionList(
							pageNumber, pageSize, "dateCreated", "DESC", walletId, searchText, filter);
					return ResponseHandler.generateResponse("success", HttpStatus.OK, true, transactionList);
				} else if (searchText != null && filter == null) {
					Page<TransactionInfo> transactionList = transactionservice.searchPaginatedTransactionList(
							pageNumber, pageSize, "dateCreated", "DESC", walletId, searchText);
					return ResponseHandler.generateResponse("success", HttpStatus.OK, true, transactionList);
				} else {
					Page<TransactionInfo> transactionList = transactionservice.getPaginatedTransactionList(pageNumber,
							pageSize, "dateCreated", "DESC", walletId);
					return ResponseHandler.generateResponse("success", HttpStatus.OK, true, transactionList);
				}
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		}
	}


	@ApiOperation(value = "Get Cold wallet transaction hash confirmation using walletUuid", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success in getting transaction hash confirmation"),
			@ApiResponse(code = 401, message = "You are not authorized. Please ensure that your are sending nonce,signature and apikey"),
			@ApiResponse(code = 400, message = "Unable to get transaction hash confirmation") })
	@RequestMapping(value = "/transaction/coldwallet/hash/confirmation", method = RequestMethod.GET)
	public ResponseEntity<Object> ColdWalletconfirmationBasedOnWallet(@RequestParam String walletUuid,
			@RequestParam String hash, HttpServletRequest request) {
		try {
			if (authenticationService.authenticateRequest(request)) {
				int response = transactionservice.getConfirmationForColdWalletTx(walletUuid, hash);
				return ResponseHandler.generateResponse("success", HttpStatus.OK, true, response);
			} else {
				return ResponseHandler.generateResponse("Authentication unsuccessful", HttpStatus.UNAUTHORIZED, true,
						null);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.UNAUTHORIZED, true, null);
		} catch(WalletException e){
			return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, true, null);
		}
	}
}