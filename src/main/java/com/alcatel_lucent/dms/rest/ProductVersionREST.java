package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Product;


@Path("products/version")
@Component("productVersionREST")
public class ProductVersionREST extends BaseREST {

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String base = requestMap.get("base");
		String hql = "select obj from Product obj where obj.base.id=:baseId";
		String countHql = "select products.size from ProductBase where id=:baseId";
		Map param = new HashMap();
		param.put("baseId", Long.parseLong(base));
		Collection<Product> products = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(products, requestMap);
	}

	@Override
	Class getEntityClass() {
		return Product.class;
	}

}
