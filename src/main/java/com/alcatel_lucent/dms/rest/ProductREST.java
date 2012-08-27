package com.alcatel_lucent.dms.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.JSONService;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
@Path("products")
@Component("productREST")
public class ProductREST {

//    private ProductService productService =(ProductService)SpringContext.getContext().getBean("productService");

    @Autowired
    private DaoService dao;

    @Autowired
    private JSONService jsonService;


    @SuppressWarnings("unchecked")
	@GET
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAll() {

        Collection<ProductBase> result = dao.retrieve("FROM ProductBase");
        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("ApplicationBase", Arrays.asList("name", "id"));
        propFilter.put("ProductBase", Arrays.asList("name", "id","applicationBases"));


        Map<Class, Map<String, String>> propRename = new HashMap<Class, Map<String, String>>();
        propRename.put(ApplicationBase.class, JSONObject.fromObject("{'name':'data', 'id':'attr'}"));
        propRename.put(ProductBase.class, JSONObject.fromObject("{'name':'data', 'id':'attr','applicationBases':'children'}"));

        String jsonString = jsonService.toJSONString(result, propFilter, propRename);
        jsonString = jsonString.replaceAll("(\"attr\":)(\\d?)", "$1{\"id\":$2}");

//        System.out.println("DEBUG, jsonString=" + jsonString);
        return jsonString;
    }
}
