package com.alcatel_lucent.dms.rest;

import java.util.Map;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.service.DictionaryService;

@Path("label")
@Component("labelREST")
public class LabelREST extends BaseREST {
	
	private static Logger log= Logger.getLogger(LabelREST.class);
	
    @Autowired
    private DictionaryService dictionaryService;
    
    @Override
    protected String doGetOrPost(Map<String, String> param) throws Exception {
    	for (String key : param.keySet()) {
    		log.info(key + "=" + param.get(key));
    	}
    	return null;
    }

}
