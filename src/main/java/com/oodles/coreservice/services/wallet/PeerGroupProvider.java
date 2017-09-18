package com.oodles.coreservice.services.wallet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.listner.PeerEventListner;
import com.oodles.coreservice.services.NetworkParamService;
import com.oodles.coreservice.services.bitcoinj.ConfirmedCoinSelector;
/**
 * Provider for peerGroup
 * 
 * @author Murari Kumar
 *
 */
@Component
public class PeerGroupProvider {
	public static Logger log = LoggerFactory.getLogger(ConfirmedCoinSelector.class);
	private static NetworkParamService networkParamService;
	private static EnvConfiguration envConfiguration;
	
	@Autowired
	private NetworkParamService tempNetworkParamService;
	@Autowired
	private EnvConfiguration tempEnvConfiguration;
	
	private static Map<PeerGroupType,PeerGroup> map = new HashMap<PeerGroupType,PeerGroup>();
	
	
	public enum PeerGroupType{
		WALLET_REFRESH,
		COIN_SELECTOR,
		BROADCAST
	}
	
	@PostConstruct
	public void init() {
		networkParamService = tempNetworkParamService;
		envConfiguration=tempEnvConfiguration;
	}
	private static NetworkParameters params;
	/**
	 * Get peerGroup object
	 * @param type
	 * @return
	 */
	public static PeerGroup get(PeerGroupType type){
		PeerGroup peerGroup=map.get(type);
		if(peerGroup==null){
			params=networkParamService.getNetworkParameters();
			peerGroup=createPeerGroup(type);
			map.put(type, peerGroup);
		}
		return peerGroup;
	}
	/**
	 * Create peerGroup for application
	 * @param type
	 * @return
	 */
	private static PeerGroup createPeerGroup(PeerGroupType type){
		log.debug("createPeerGroup()");
		final PeerGroup peerGroup;
		
		if(type==PeerGroupType.WALLET_REFRESH){
			peerGroup = new PeerGroup(params, BlockChainProvider.get());
			peerGroup.setMaxConnections(1);
			//peerGroup.addEventListener(new PeerEventListner());
			//peerGroup.setDownloadTxDependencies(true);
			peerGroup.addOnTransactionBroadcastListener(new PeerEventListner());
			if(!addLocalHost(peerGroup)){
				upgrade(peerGroup);
			}
		}
		else{
	     	peerGroup = new PeerGroup(params);
			peerGroup.setMaxConnections(50);
			
			if(params.equals(MainNetParams.get())){
				peerGroup.setMinBroadcastConnections(10);
				peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			}
			else{
				peerGroup.setMinBroadcastConnections(1);
				if(!addLocalHost(peerGroup)){
					peerGroup.addPeerDiscovery(new DnsDiscovery(params));
				}
			}
		}
		peerGroup.setUserAgent("PeerMonitor", "1.0");
		return peerGroup;
	}
	/**
	 * PeerGroup connection upgrader	
	 * @param peerGroup
	 */
	public static void upgrade(PeerGroup peerGroup){
		log.debug("upgrade()");
		if(peerGroup!=null){
			peerGroup.setMaxConnections(5);
			peerGroup.addPeerDiscovery(new DnsDiscovery(params));
		}
	}
	/**
	 * Add connection information of bitcoind to peerGroup
	 * @param peerGroup
	 * @return
	 */
	private static boolean addLocalHost(PeerGroup peerGroup){
		log.debug("addLocalHost()");
		boolean result=false;
		try {
			String hostName=envConfiguration.getDBIp();
			log.debug("hostName in addLocalHost "+hostName);
			InetAddress inetAddress = InetAddress.getByName(hostName);
			int port = envConfiguration.getBitcoindPort();
			log.debug("port in addLocalHost method "+port);
			Socket socket = new Socket(inetAddress, port);
			if (socket.isConnected()) {
				peerGroup.addAddress(new PeerAddress(inetAddress, port));
				result = true;
			}	
			socket.close();
		}catch (IOException e) {
			log.error(e.getMessage());
		}
		return result;
	}
}
