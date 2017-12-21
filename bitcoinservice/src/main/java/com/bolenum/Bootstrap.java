/**
 * 
 */
package com.bolenum;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author chandan kumar singh
 * @date 21-Dec-2017
 */
@Component
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {

	//@Autowired
	//private BTCWalletService bTCWalletService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//bTCWalletService.blockEventListener();
	}

}
