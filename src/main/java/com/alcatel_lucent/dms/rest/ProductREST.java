package com.alcatel_lucent.dms.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Product;
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
@Produces({MediaType.APPLICATION_JSON + ";CHARSET=UTF-8", MediaType.TEXT_HTML + ";CHARSET=UTF-8"})
@Component("productREST")
@SuppressWarnings("unchecked")
public class ProductREST {

    private static Logger log= Logger.getLogger(ProductREST.class);
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Autowired
    private DaoService dao;

    @Autowired
    private JSONService jsonService;



    @GET
    /**
     * Populate all the product base and related application base json data for navigate tree in application management module.
     * */
    public String retrieveAllProductBase() {

        Collection<ProductBase> result = dao.retrieve("from ProductBase order by name");
        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("ApplicationBase", Arrays.asList("name", "id"));
        propFilter.put("ProductBase", Arrays.asList("name", "id", "applicationBases"));


        Map<Class, Map<String, String>> propRename = new HashMap<Class, Map<String, String>>();
        propRename.put(ApplicationBase.class, JSONObject.fromObject("{'name':'data', 'id':'attr'}"));
        propRename.put(ProductBase.class, JSONObject.fromObject("{'name':'data', 'id':'attr','applicationBases':'children'}"));

        String jsonString = jsonService.toTreeJSON(result, propFilter, propRename).toString();

        log.debug("In rest: "+jsonString);

        return jsonString;
    }

    @GET
    @Path("{productBase.id}")
    /**
     * Populate product version in a specific product json data base for product select in application management module.
     * */
    public String retrieveAllProductByProductBaseId(@PathParam("productBase.id") Long id){

        Map<String, Long>  params= new HashMap<String,Long>();
        params.put("baseId",id);
        Collection<Product> result = dao.retrieve("from Product where base.id = :baseId order by version",params);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Product", Arrays.asList("id","version"));

        String jsonString = jsonService.toSelectJSON(result, propFilter).toString();
        log.debug(jsonString);
        return jsonString;
    }
    
    @GET
    @Path("trans/productbases")
    /**
     * Populate all the product base json data for product select in translation management module.
     * */
    public String retrieveAllProductBaseForTrans(){
        Collection<ProductBase> result = dao.retrieve("from ProductBase order by name");
        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("ProductBase", Arrays.asList("name", "id"));

        String jsonString = jsonService.toTreeJSON(result, propFilter).toString();
        return jsonString;
    }
}

