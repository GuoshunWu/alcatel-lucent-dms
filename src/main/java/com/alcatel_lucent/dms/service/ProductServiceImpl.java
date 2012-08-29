package com.alcatel_lucent.dms.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */

@Service("productService")
@SuppressWarnings("unchecked")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private DaoService dao;

    JsonConfig config = new JsonConfig();


    public Product create() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public Product retrieve(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JSONArray retrieveAll() {

        config.setJsonPropertyFilter(new PropertyFilter() {
            List<String> excludesFields = Arrays.asList("products", "dictionaryBases", "productBase", "applications");

            @Override
            public boolean apply(Object source, String name, Object value) {
                return excludesFields.contains(name);
            }
        });

        Collection<ProductBase> pBases = (Collection<ProductBase>) dao.retrieve("from ProductBase", new String[]{"applicationBases"});
        return JSONArray.fromObject(pBases,config);
    }

}
