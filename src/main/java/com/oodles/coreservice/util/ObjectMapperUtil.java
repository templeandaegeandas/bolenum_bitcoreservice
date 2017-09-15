package com.oodles.coreservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Basic Object Mapper
 * @author Murari Kumar
 * 
 *
 */
public class ObjectMapperUtil {
	public static String mapObjectToString(Object object){
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString =null;
		try {
			jsonInString = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}
}
