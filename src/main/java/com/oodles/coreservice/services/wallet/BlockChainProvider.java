package com.oodles.coreservice.services.wallet;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oodles.coreservice.listner.ChainListener;
import com.oodles.coreservice.services.NetworkParamService;

/**
 * Provider for block chain
 *
 * @author Murari Kumar
 */
@Component
public class BlockChainProvider {

	private static NetworkParamService networkParamService;
	//private static EnvConfiguration envConfiguration;

	// private static BlockChain chain;
	private static BlockChain chain;
	public static Logger log = LoggerFactory.getLogger(BlockChainProvider.class);
	@Autowired
	private NetworkParamService tempNetworkParamService;
//	@Autowired
//	private EnvConfiguration tempEnvConfiguration;

	@PostConstruct
	public void init() {
		log.debug("init() method called");
		networkParamService = tempNetworkParamService;
		//envConfiguration = tempEnvConfiguration;
	}

	/**
	 * Get block chain object
	 * 
	 * @return
	 */
	public static BlockChain get() {
		if (chain == null) {
			System.out.println(networkParamService);
			NetworkParameters params = networkParamService.getNetworkParameters();
			try {
				// PostgresFullPrunedBlockStore blockStore = new
				// PostgresFullPrunedBlockStore(params,1000,envConfiguration.getDBIp(),envConfiguration.getBlockStoreDBName(),envConfiguration.getDBUser(),envConfiguration.getDBPass());
				// //bitcoin1 For test Only
				// chain = new FullPrunedBlockChain(params, blockStore);
				BlockStore blockStore = new MemoryBlockStore(params);
				chain = new BlockChain(params, blockStore);

			} catch (BlockStoreException e) {
				e.printStackTrace();
				System.exit(1);
			}
			chain.addListener(new ChainListener());
		}
		return chain;
	}
}
