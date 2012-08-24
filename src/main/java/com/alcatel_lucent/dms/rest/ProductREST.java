package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.service.ProductService;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
@Path("products")
@Component
public class ProductREST {

//    private ProductService productService =(ProductService)SpringContext.getContext().getBean("productService");

    @Autowired
    private ProductService productService;


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAll() {

        JSONArray uriArray = productService.retrieveAll();
//        Collection<ProductBase> result;
//        return jsonservice.fromObject(result, config);
        System.out.println(uriArray);
        return uriArray.toString();
    }
}
