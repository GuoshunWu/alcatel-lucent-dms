package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.JSONService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
@Path("applications")
@Produces({MediaType.APPLICATION_JSON + ";CHARSET=UTF-8", MediaType.TEXT_HTML + ";CHARSET=UTF-8"})
@Component("applicationREST")
@SuppressWarnings("unchecked")
public class ApplicationREST {

    private static Logger log = Logger.getLogger(ApplicationREST.class);
//    @Context
//    UriInfo uriInfo;
//    @Context
//    Request request;

//    @Context
//    HttpServletRequest request;


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
                                                     @QueryParam("rows") int rows,
                                                     @QueryParam("page") int page,
                                                     @QueryParam("sidx") String sidx,
                                                     @QueryParam("sord") String sord) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("productId", id);

        String hSQL = "from Application where product.id=:productId";
        String countHSQL = "select count(*) as records " + hSQL;
        Long records = (Long) dao.retrieveOne(countHSQL, params);

        log.debug("page=" + page + ",rows=" + rows + ", records=" + records + ", sidx=" + sidx + ", sord=" + sord);

        hSQL = "from Application where product.id=:productId";
        if (Arrays.asList("name", "dictNum").contains(sidx)) {
            sidx = sidx.equals("name") ? "base.name" : "dictionaries.size";
        }
        hSQL += " order by " + sidx + " " + sord;

        Collection<Application> resultSet = dao.retrieve(hSQL, params);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Application", Arrays.asList("id", "cell"));

        String jsonString = jsonService.toGridJSON(resultSet, rows, page, records.intValue(), propFilter).toString();

        return jsonString;
    }

    /**
     * @param info
     */
    public void test(String info) {

    }
}

