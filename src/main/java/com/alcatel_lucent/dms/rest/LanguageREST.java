package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.JSONService;
import com.alcatel_lucent.dms.util.Util;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
@Path("languages")
@Produces({MediaType.APPLICATION_JSON + ";CHARSET=UTF-8", MediaType.TEXT_HTML + ";CHARSET=UTF-8"})
@Component("languageREST")
@SuppressWarnings("unchecked")
public class LanguageREST {

    private static Logger log = Logger.getLogger(LanguageREST.class);

    @Autowired
    private DaoService dao;

    @Autowired
    private JSONService jsonService;


    @GET
    /**
     * Populate all the product base and related application base json data for navigate tree in application management module.
     * */
    public String retrieveAllLanguages() {

        Collection<Language> result = dao.retrieve("from Language order by name");
        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Language", Arrays.asList("name", "id"));
        return jsonService.toTreeJSON(result, propFilter).toString();

    }
}

