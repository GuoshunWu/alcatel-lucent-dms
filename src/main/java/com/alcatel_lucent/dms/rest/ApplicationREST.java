package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.DictionaryService;
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
    
    @Autowired
    private DictionaryService dictionaryService;

    /**
     *  @param id Product id.
     *  @param rows how many rows we want to have into the grid
     *  @param page the requested page
     *  @param sidx index row - i.e. user click to sort
     *  @param sord the direction
     * */
    @GET
    public String retrieveAllApplicationsByProductId(@QueryParam("prod") Long prodId,
    												 @QueryParam("prop") String prop,
                                                     @QueryParam("rows") Integer rows,
                                                     @QueryParam("page") Integer page,
                                                     @QueryParam("sidx") String sidx,
                                                     @QueryParam("sord") String sord,
                                                     @QueryParam("format") String format) {
    	return retrieveAllApplicationsByProductIdPOST(prodId, prop, rows, page, sidx, sord, format);
    }
    
    @POST
    public String retrieveAllApplicationsByProductIdPOST(@QueryParam("prod") Long prodId,
    												 @QueryParam("prop") String prop,
                                                     @QueryParam("rows") Integer rows,
                                                     @QueryParam("page") Integer page,
                                                     @QueryParam("sidx") String sidx,
                                                     @QueryParam("sord") String sord,
                                                     @QueryParam("format") String format) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", prodId);

        String hSQL = "select app from Product p join p.applications app where p.id=:id";
        if (Arrays.asList("name", "dictNum").contains(sidx)) {
            sidx = sidx.equals("name") ? "base.name" : "dictionaries.size";
        }
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "base.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
        hSQL += " order by app." + sidx + " " + sord;

        Collection<Application> resultSet;
    	if (rows == null) {	// all in one page
    		resultSet = dao.retrieve(hSQL, params);
    	} else {	// paged
    		int first = (page == null ? 0 : (page.intValue() - 1) * rows.intValue());
    		resultSet = dao.retrieve(hSQL, params, first, rows.intValue());
    	}
        
		// additional properties
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary = dictionaryService.getAppTranslationSummary(prodId);
			for(Application app : resultSet) {
				app.setS(summary.get(app.getId()));
			}
		}


        try {
        	if (format == null) {
        		return jsonService.toJSONString(resultSet, prop);
        	} else if (format.equals("grid")) {
        		int records;
	    		if (rows == null) {
	    			rows = records = resultSet.size();
	    			page = 1;
	    		} else {
	                String countHSQL = "select p.applications.size from Product p where p.id=:id";
	                records = (Integer) dao.retrieveOne(countHSQL, params);
	                log.debug("page=" + page + ",rows=" + rows + ", records=" + records + ", sidx=" + sidx + ", sord=" + sord);
	    			if (page == null) {
	    				page = 1;
	    			}
	    		}
        		return jsonService.toGridJSON(resultSet, rows, page, records, "id", prop).toString();
        	}
        	return null;
        } catch (Exception e) {
        	e.printStackTrace();
        	log.error(e);
        	throw new BusinessException(e.toString());
        }
    }

    /**
     * @param id product base id
     */

    @GET
    @Path("base/{product.id}")
    public String retrieveAllApplicationsBaseNotExistsProductByProductId(@PathParam("product.id") Long id) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", id);
        //TODO: optimize the hql statement
        String hql = "select appBase from Product p ,ApplicationBase appBase where p.id =:id and appBase.productBase= p.base and not exists (from Product product join product.applications app where app.base=appBase and product.id =:id)";
        Collection<ApplicationBase> appBases = dao.retrieve(hql, params);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("ApplicationBase", Arrays.asList("id", "name"));

        return jsonService.toSelectJSON(appBases, propFilter).toString();

    }

    /**
     * @param id applicationBase id
     */

    @GET
    @Path("apps/{applicationBase.id}")
    public String retrieveAllApplicationsByApplicationBaseId(@PathParam("applicationBase.id") Long id) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", id);
        String hql = "from Application where base.id=:id";
        Collection<Application> appBases = dao.retrieve(hql, params);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Application", Arrays.asList("id", "version"));

        return jsonService.toSelectJSON(appBases, propFilter).toString();

    }

    @GET
    @Path("appssamebase/{application.id}")
    public String retrieveAllApplicationsWithSameAppBaseByAppId(@PathParam("application.id") Long id) {
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", id);

        String hql = "from Application where base.id=(select app.base.id from Application as app where app.id=:id)";
        Collection<Application> appBases = dao.retrieve(hql, params);

        Map<String, Collection<String>> propFilter = new HashMap<String, Collection<String>>();
        propFilter.put("Application", Arrays.asList("id", "version"));

        return jsonService.toSelectJSON(appBases, propFilter).toString();


    }


}

