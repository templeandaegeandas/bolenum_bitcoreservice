package com.oodles.coreservice.coin;

import java.net.URL;
import java.net.URLConnection;
import java.util.ListIterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oodles.coreservice.conf.EnvConfiguration;

/**
 * This class has the method to get currency value for a bitcoin
 *	@author Shivam Gupta
 */
@Component
public class BitCoinPriceUpdateService extends Thread {
	private static BitCoinPriceUpdateService obj;
	private static EnvConfiguration envConfiguration;
	@Autowired
	EnvConfiguration tempEnvConfiguration;
	
	String url; 
	public static JSONArray bitcoinExchangeRate=null;
	public BitCoinPriceUpdateService() {
		
	}
	@PostConstruct
	public void init() {
		envConfiguration=tempEnvConfiguration;
	}

	
	public static void startService()
	{
		
		if(obj==null || !obj.isAlive())
		{
			obj=new BitCoinPriceUpdateService();
			obj.start();
		}
	}
	public static void stopService()
	{
		if(obj!=null && obj.isAlive())
		{
			obj.interrupt();
		}
	}
	@Override
	public void run()
	{
		while(!this.isInterrupted())
		{
			try
			{
				URLConnection con=new URL(envConfiguration.getBitpayUrl()).openConnection();
				
				JSONParser parser = new JSONParser();
				bitcoinExchangeRate = (JSONArray)parser.parse(IOUtils.toString(con.getInputStream()));
				
			}
			catch(Exception e)
			{
				System.out.println("error while reading url "+e);
			}
			try{
				this.wait(1000*60);	//updated in 1 min as per info available on https://bitpay.com/bitcoin-exchange-rates
			}
			catch(Exception e){
				
			}
		}	
	}
	/**
	 * @return currency rate for a particular currency
	 */
	public static double getCurrencyRate(String currency) {
		double rate=0.0;
		if(bitcoinExchangeRate!=null){
			@SuppressWarnings("rawtypes")
			ListIterator itr=bitcoinExchangeRate.listIterator();
			while(itr.hasNext())
			{
				JSONObject obj=(JSONObject) itr.next();
				if(currency.trim().equalsIgnoreCase((String)obj.get("code"))){
					rate=Double.parseDouble(obj.get("rate").toString());
					break;
				}
			} 
		}
		return rate;
	}
}
