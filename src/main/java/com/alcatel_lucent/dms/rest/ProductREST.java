package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.ProductService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
@Path("products")
public class ProductREST {
    private ProductService productService= (ProductService) SpringContext.getContext().getBean("productService");
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ProductBase> retrieveAll(){
        return productService.retrieveAll();
    }
}
