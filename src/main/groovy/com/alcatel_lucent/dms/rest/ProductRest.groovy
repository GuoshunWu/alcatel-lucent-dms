package com.alcatel_lucent.dms.rest

import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import com.alcatel_lucent.dms.model.ProductBase
import com.alcatel_lucent.dms.service.ProductService
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-21
 * Time: 下午6:11
 * To change this template use File | Settings | File Templates.
 */

@Path("products")
class ProductRest {

    @Autowired
    private ProductService productService

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Collection<ProductBase> retrieveAll(){
        println '='*100
        println "service=$productService"
    }
}
