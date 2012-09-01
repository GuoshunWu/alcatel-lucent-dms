package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.JSONService;
import com.alcatel_lucent.dms.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.*;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
@Path("applications")
@Produces({MediaType.APPLICATION_JSON + ";CHARSET=UTF-8", MediaType.TEXT_HTML + ";CHARSET=UTF-8"})
@Component("applicationREST")
@SuppressWarnings("unchecked")
public class ApplicationREST {

    private static Logger log= Logger.getLogger(ApplicationREST.class);
    @Context
    UriInfo uriInfo;
//    @Context
//    Request request;

    @Context
    HttpServletRequest request;


    @Autowired
    private DaoService dao;

    @Autowired
    private JSONService jsonService;

    @GET
    @Path("{product.version.id}")
    /**
     *  @param id Product id.
     *  @param rows how many rows we want to have into the grid
     *  @param page the requested page
     *  @param sidx index row - i.e. user click to sort
     *  @param sord the direction
     * */
    public String retrieveAllApplicationsByProductId(@PathParam("product.version.id") Long id,
                                                     @QueryParam("rows") Long rows,
                                                     @QueryParam("page") Long page,
                                                     @QueryParam("sidx") String sidx,
                                                     @QueryParam("sord") String sord) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("productId", id);

        String hSQL = "from Application where product.id=:productId";
        String countHSQL = "select new map(count(*) as records) " + hSQL;
        Map<String, Long> result = (Map<String, Long>) dao.retrieveOne(countHSQL, params);

        Long records = result.get("records");


        Long totalPages = Long.valueOf(0);
        if (records > 0) {
            totalPages = (Double.valueOf(Math.ceil(records.doubleValue() / rows)).longValue());
        }
        if (page > totalPages) page = totalPages;
        log.debug("page=" + page + ",rows=" + rows + ", records=" + records + ", totalPages=" + totalPages + ", sidx='" + sidx + "', sord=" + sord);

        hSQL = "from Application where product.id=:productId";
        if(Arrays.asList("name","dictNum").contains(sidx)){
            if(sidx.equals("name")){
                sidx = "base.name";
            }else if(sidx.equals("dictNum")){
                sidx = "dictionaries.size";
            }
        }
        hSQL+=" order by "+sidx + " "+sord;

        Collection<Application> resultSet = dao.retrieve(hSQL, params);

        Map<String, Object> pageData = new HashMap<String, Object>();
        pageData.put("page", page);
        pageData.put("total", totalPages);
        pageData.put("records", records);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Application", Arrays.asList("id", "cell"));


        String jsonString = jsonService.toGridJSON(resultSet, pageData, propFilter).toString();

        return jsonString;
    }

    /**
     * @param info
     */
    public void test(String info) {

    }
}

