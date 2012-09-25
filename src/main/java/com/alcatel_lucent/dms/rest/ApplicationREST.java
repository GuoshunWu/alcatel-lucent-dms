package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Application REST service.
 * URL: /rest/applications
 * Filter parameters:
 *   prod		(required) product id, in which product all dictionaries will be retrieved
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "base.name"
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   idprop		(optional) property name for id, for grid only
 *   rows		(optional) number of records to be retrieved, only be used when format is grid
 *   page		(optional) current page, only be used when format is grid
 *   		
 * @author allany
 *
 */
@Path("applications")
@Component("applicationREST")
@SuppressWarnings("unchecked")
public class ApplicationREST extends BaseREST {

    @Autowired
    private DictionaryService dictionaryService;

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long prodId = Long.valueOf(requestMap.get("prod"));
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
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

    	String rows = requestMap.get("rows");
    	String page = requestMap.get("page");
        Collection<Application> resultSet;
    	if (rows == null) {	// not paged
    		resultSet = dao.retrieve(hSQL, params);
    	} else {	// paged
    		int first = (page == null ? 0 : (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
    		resultSet = dao.retrieve(hSQL, params, first, Integer.parseInt(rows));
    		// get total records
            String countHSQL = "select p.applications.size from Product p where p.id=:id";
            Integer records = (Integer) dao.retrieveOne(countHSQL, params);
            requestMap.put("records", "" + records);
    	}
        
		// additional properties
    	String prop = requestMap.get("prop");
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary = dictionaryService.getAppTranslationSummary(prodId);
			Collection<Long> allLanguageId = dao.retrieve("select id from Language");
			for(Application app : resultSet) {
				Map<Long, int[]> appSummary = summary.get(app.getId());
				if (appSummary == null) {
					appSummary = new HashMap<Long, int[]>();
				}
				fillZero(allLanguageId, appSummary);
				app.setS(appSummary);
			}
		}

        return toJSON(resultSet, requestMap);
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

	private void fillZero(Collection<Long> langIds, Map<Long, int[]> map) {
		for (Long langId : langIds) {
			if (!map.containsKey(langId)) {
				map.put(langId, new int[] {0, 0, 0});
			}
		}
	}

}

