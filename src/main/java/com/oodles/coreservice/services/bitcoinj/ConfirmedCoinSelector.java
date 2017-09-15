package com.oodles.coreservice.services.bitcoinj;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
/**
 * A class that is manages wallet balance 
 * 
 * @author Murari Kumar
 */
public class ConfirmedCoinSelector implements CoinSelector {
	public static Logger log = LoggerFactory
			.getLogger(ConfirmedCoinSelector.class);
	public static int minConfidenceLevel = 0;
	// chainHeight is current block in block chain
	private static int chainHeight = 0;

	private static ConfirmedCoinSelector confirmedCoinSelector;

	private ConfirmedCoinSelector() {

	}

	public static ConfirmedCoinSelector get() {
		if (confirmedCoinSelector == null) {
			confirmedCoinSelector = new ConfirmedCoinSelector();
		}
		return confirmedCoinSelector;
	}

	public static boolean isConfirmed(Transaction tx) {
		if (calculateConfirmations(tx) >= minConfidenceLevel) {
			return true;
		}
		return false;
	}

	public static int calculateConfirmations(Transaction tx) {

		try {
			// chainHeight is the block number at this transaction exist in
			// block chain
			int chainHeight = tx.getConfidence().getAppearedAtChainHeight();
			log.debug("Transaction Height "+chainHeight);
			if (ConfirmedCoinSelector.chainHeight != 0 && chainHeight != 0
					&& ConfirmedCoinSelector.chainHeight >= chainHeight) {
				log.debug("Chain height in Confirmed Coin Selector -->"+ConfirmedCoinSelector.chainHeight);
				return (ConfirmedCoinSelector.chainHeight - chainHeight + 1);
			} else {
				// we can't calculate confirmations because peerGroup does not
				// have information about
				// current block in block ledger
				return 0;
			}
		} catch (Exception e) {
			// exception indicate transaction is not confirmed yet.
			return 0;
		}
	}

	public static int getChainHeight() {

		return chainHeight;
	}

	public static void setChainHeight(int chainHeight) {
		if (chainHeight > ConfirmedCoinSelector.chainHeight) {
			ConfirmedCoinSelector.chainHeight = chainHeight;
			if(ConfirmedCoinSelector.chainHeight >= 809032)
			 log.debug("block height is updated by "+ConfirmedCoinSelector.chainHeight);
		}
	}

	public static void setMinConfidenceLevel(int minConfidenceLevel) {
		ConfirmedCoinSelector.minConfidenceLevel = minConfidenceLevel;
	}

	public static int getMinConfidenceLevel() {
		return minConfidenceLevel;
	}

	@Override
	public CoinSelection select(Coin biTarget,
			List<TransactionOutput> candidates) {
		// TODO Auto-generated method stub
		long target = biTarget.value;
		HashSet<TransactionOutput> selected = new HashSet<TransactionOutput>();
		// Sort the inputs by age*value so we get the highest "coindays" spent.
		// TODO: Consider changing the wallets internal format to track just
		// outputs and keep them ordered.
		ArrayList<TransactionOutput> sortedOutputs = new ArrayList<TransactionOutput>(
				candidates);
		// When calculating the wallet balance, we may be asked to select all
		// possible coins, if so, avoid sorting
		// them in order to improve performance.
		if (!biTarget.equals(NetworkParameters.MAX_MONEY)) {
			sortOutputs(sortedOutputs);
		}
		// Now iterate over the sorted outputs until we have got as close to the
		// target as possible or a little
		// bit over (excessive value will be change).
		long total = 0;
		for (TransactionOutput output : sortedOutputs) {
			if (total >= target)
				break;
			// Only pick chain-included transactions, or transactions that are
			// ours and pending.
			if (!shouldSelect(output.getParentTransaction()))
				continue;
			selected.add(output);
			total += output.getValue().value;
		}
		// Total may be lower than target here, if the given candidates were
		// insufficient to create to requested
		// transaction.
		return new CoinSelection(Coin.valueOf(total), selected);
	}

	@VisibleForTesting
	static void sortOutputs(ArrayList<TransactionOutput> outputs) {

		Collections.sort(outputs, new Comparator<TransactionOutput>() {
			@Override
			public int compare(TransactionOutput a, TransactionOutput b) {
				int depth1 = 0;
				int depth2 = 0;
				TransactionConfidence conf1 = a.getParentTransaction()
						.getConfidence();
				TransactionConfidence conf2 = b.getParentTransaction()
						.getConfidence();
				if (conf1.getConfidenceType() == TransactionConfidence.ConfidenceType.BUILDING)
					depth1 = conf1.getDepthInBlocks();
				if (conf2.getConfidenceType() == TransactionConfidence.ConfidenceType.BUILDING)
					depth2 = conf2.getDepthInBlocks();
				Coin aValue = a.getValue();
				Coin bValue = b.getValue();
				BigInteger aCoinDepth = BigInteger.valueOf(aValue.value)
						.multiply(BigInteger.valueOf(depth1));
				BigInteger bCoinDepth = BigInteger.valueOf(bValue.value)
						.multiply(BigInteger.valueOf(depth2));
				int c1 = bCoinDepth.compareTo(aCoinDepth);
				if (c1 != 0)
					return c1;
				// The "coin*days" destroyed are equal, sort by value alone to
				// get the lowest transaction size.
				int c2 = bValue.compareTo(aValue);
				if (c2 != 0)
					return c2;
				// They are entirely equivalent (possibly pending) so sort by
				// hash to ensure a total ordering.
				BigInteger aHash = a.getParentTransaction().getHash()
						.toBigInteger();
				BigInteger bHash = b.getParentTransaction().getHash()
						.toBigInteger();
				return aHash.compareTo(bHash);
			}
		});
	}

	/**
	 * Sub-classes can override this to just customize whether transactions are
	 * usable, but keep age sorting.
	 */
	protected boolean shouldSelect(Transaction tx) {

		return isSelectable(tx);
	}

	public static boolean isSelectable(Transaction tx) {

		// Only pick chain-included transactions, or transactions that are ours
		// and pending.
		TransactionConfidence confidence = tx.getConfidence();
		TransactionConfidence.ConfidenceType type = confidence
				.getConfidenceType();

		// first check weather transaction is created by us and status is
		// pending in such case we will
		// allow user to use this balance.
		boolean result = (type
				.equals(TransactionConfidence.ConfidenceType.BUILDING) || type
				.equals(TransactionConfidence.ConfidenceType.PENDING))
				&& confidence.getSource().equals(
						TransactionConfidence.Source.SELF);

		if ((result || isConfirmed(tx))) {
			return true;
		}
		return false;
	}

}
