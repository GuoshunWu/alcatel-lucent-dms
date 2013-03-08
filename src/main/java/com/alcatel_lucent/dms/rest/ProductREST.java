package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.ProductBase;

@Path("products")
@Component("productREST")
public class ProductREST extends BaseREST {

	@Override
	Class getEntityClass() {
		return ProductBase.class;
	}
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String hql = "from ProductBase";
		String countHql = "select count(*) from ProductBase";
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
        hql += " order by " + sidx + " " + sord;
        Collection<ProductBase> products = retrieve(hql, null, countHql, null, requestMap);
        
        String format = requestMap.get("format");
        if (format != null && format.equals("tree")) { // construct tree parameters
	        requestMap.put("idProps", "id,id,id");
	        requestMap.put("dataProps", "data,name,name");
	        requestMap.put("childrenProps", "products,applicationBases");
	        requestMap.put("types", "prods,prod,app");
			return toJSON(new Products(products), requestMap);
        } else {
        	return toJSON(products, requestMap);
        }
	}
}


