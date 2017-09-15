package com.oodles.coreservice.services;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oodles.coreservice.conf.EnvConfiguration;
/**
 *	A service that has method to get bitcoin network parameter 
 *	@author Murari Kumar
 */
@Service
public class NetworkParamService {
	@Autowired
	EnvConfiguration envConfiguration;

	public NetworkParameters getNetworkParameters() {

		if (envConfiguration.getNetworkParams().equals("testNetParams")) {
			NetworkParameters testParams = TestNet3Params.get();
			return testParams;
		} else {
			NetworkParameters mainParams = MainNetParams.get();
			return mainParams;
		}
	}
}
