package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.service.TranslationService;

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
 *   prod		(optional) product id, in which product all dictionaries will be retrieved
 *   app		(optional) app id
 *   at least one of parameter "prod" and "app" must be specified
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
    private TranslationService translationService;
    
	@Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
    	return Application.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long prodId = null, appId = null;
    	String prod = requestMap.get("prod");
    	String app = requestMap.get("app");
    	if (prod != null && !prod.isEmpty()) {
    		prodId = Long.valueOf(prod);
    	}
    	if (app != null && !app.isEmpty()) {
    		appId = Long.valueOf(app);
    	}
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
        if (Arrays.asList("name", "dictNum").contains(sidx)) {
            sidx = sidx.equals("name") ? "base.name" : "dictionaries.size";
        }
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "base.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	String hSQL, countHSQL;
    	Map<String, Long> params = new HashMap<String, Long>();
    	if (appId != null) {
    		if (sidx.equals("labelNum")) {
    			hSQL = "select app from Application app join app.dictionaries d where app.id=:id" +
    					" group by app order by sum(d.labels.size) " + sord;
    		} else {
    			hSQL = "select app from Application app where app.id=:id order by app." + sidx + " " + sord;
    		}
        	countHSQL = "select count(*) from Application where id=:id";
	        params.put("id", appId);
    	} else {
    		if (sidx.equals("labelNum")) {
    			hSQL = "select app from Product p join p.applications app join app.dictionaries d where p.id=:id" +
    				" group by app order by sum(d.labels.size) " + sord;
    		} else {
    			hSQL = "select app from Product p join p.applications app where p.id=:id order by app." + sidx + " " + sord;
    		}
        	countHSQL = "select p.applications.size from Product p where p.id=:id";
	        params.put("id", prodId);
    	}

        Collection<Application> resultSet = retrieve(hSQL, params, countHSQL, params, requestMap);
        
		// additional properties
    	String prop = requestMap.get("prop");
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary;
			if (appId != null) {
				summary = translationService.getAppTranslationSummaryByApp(appId);
			} else {
				summary = translationService.getAppTranslationSummaryByProd(prodId);
			}
			Collection<Long> allLanguageId = dao.retrieve("select id from Language");
			for(Application appli : resultSet) {
				Map<Long, int[]> appSummary = summary.get(appli.getId());
				if (appSummary == null) {
					appSummary = new HashMap<Long, int[]>();
				}
				fillZero(allLanguageId, appSummary);
				appli.setS(appSummary);
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
        String hql = "select appBase from Product p ,ApplicationBase appBase" +
        		" where p.id =:id and appBase.productBase= p.base" +
        		" and not exists (from Product product join product.applications app where app.base=appBase and product.id =:id)" +
        		" and exists(from Application a where a.base=appBase)";
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
    /**
     * Sibling Application have same base with current application
     * */
    public String retrieveSiblingApplications(@PathParam("application.id") Long id) {
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

