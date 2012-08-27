package com.alcatel_lucent.dms.rest

import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import com.alcatel_lucent.dms.model.ProductBase
import com.alcatel_lucent.dms.service.ProductService

import com.alcatel_lucent.dms.SpringContext
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import com.alcatel_lucent.dms.service.DaoService
import com.alcatel_lucent.dms.service.JSONService
import com.alcatel_lucent.dms.model.ApplicationBase

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-21
 * Time: 下午6:11
 * To change this template use File | Settings | File Templates.
 */

@Path('mytest')
@Component('myTest')
class MyTest {

    @Autowired
    private DaoService dao;

    @Autowired
    private JSONService jsonService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getProducts() {
        Collection<ProductBase> result = dao.retrieve('FROM ProductBase');
        Map<String, Collection<String>> propFilter = []
        propFilter.ApplicationBase= ['name', 'id']
        propFilter.ProductBase=['applicationBases']+propFilter.ApplicationBase

        Map<Class, Map<String, String>> propRename = []

        propRename[ApplicationBase.class]=['name':'data','id':'attr']
        propRename[ProductBase.class]=['applicationBases':'children']+propRename[ApplicationBase.class]

        String jsonString = jsonService.toJSONString(result, propFilter, propRename);
        println jsonString
        
//        jsonString=jsonString.replaceAll('(\'attr\':)(\\d?)','$1{\'id\':$2}');
        return jsonString;
    }
}
