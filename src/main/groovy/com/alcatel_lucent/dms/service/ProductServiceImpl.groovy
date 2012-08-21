package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.model.Product
import javax.ws.rs.Path
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import com.alcatel_lucent.dms.model.ProductBase
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-21
 * Time: 下午1:53
 * To change this template use File | Settings | File Templates.
 */

@Service("productService")

class ProductServiceImpl implements ProductService {

    @Autowired
    private DaoService dao;


    @Override
    Product create() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void delete(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Product retrieve(Long id) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Collection<ProductBase> retrieveAll(){
        println "dao=$dao"
        dao.retrieve('from ProductBase') as List<ProductBase>
    }
}
