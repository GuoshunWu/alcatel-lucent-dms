package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
@Service("productService")
@Path("mytest")
public class ProductServiceImpl implements ProductService{

    @Autowired
    private DaoService dao;

    @Override
    public Product create() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Product retrieve(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Collection<ProductBase> retrieveAll() {
        System.out.println("dao="+dao);
        Collection<ProductBase> pBases=null;
        if(null!=dao){
            pBases = (Collection<ProductBase> )dao.retrieve("from ProductBase",new String[]{"applicationBases"});

        }
        ProductBase pb=new ProductBase();
        pb.setName("TestProductBase");
        return Arrays.asList(pb);

    }
}
