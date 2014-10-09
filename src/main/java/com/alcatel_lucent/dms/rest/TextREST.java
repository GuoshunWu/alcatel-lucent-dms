package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.service.TextService;

/**
 * Text REST service.
 * URL: /rest/texts
 * Filter parameters:
 *   context	(required) context id
 *   prod		(optional) product id
 *   app		(optional) application id
 *   text		(optional) search text (case insensitive)
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "reference"
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
@Path("texts")
@Component("textREST")
public class TextREST extends BaseREST {
	
	@Autowired
	private TextService textService;

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long ctxId = Long.valueOf(requestMap.get("context"));
    	Long prodId = requestMap.get("prod") == null ? null : Long.valueOf(requestMap.get("prod"));
    	Long appId = requestMap.get("app") == null ? null : Long.valueOf(requestMap.get("app"));
		String text = requestMap.get("text");
		String hql = "from Text where context.id=:ctxId";
		String countHql = "select count(*) from Text where context.id=:ctxId";
		Map param = new HashMap();
		param.put("ctxId", ctxId);
		if (prodId != null) {
			hql = "from Text obj where exists(select label from Product p join p.applications a join a.dictionaries d join d.labels label where label.text=obj and p.id=:prodId and label.context.id=:ctxId and label.removed=false)";
			countHql = "select count(*) from Text obj where exists(select label from Product p join p.applications a join a.dictionaries d join d.labels label where label.text=obj and p.id=:prodId and label.context.id=:ctxId and label.removed=false)"; 
			param.put("prodId", prodId);
		} else if (appId != null) {
			hql = "from Text obj where exists(select label from Application a join a.dictionaries d join d.labels label where label.text=obj and a.id=:appId and label.context.id=:ctxId and label.removed=false)";
			countHql = "select count(*) from Text obj where exists(select label from Application a join a.dictionaries d join d.labels label where label.text=obj and a.id=:appId and label.context.id=:ctxId and label.removed=false)";
			param.put("appId", appId);
		}
		if (text != null) {
			hql += " and reference like :reference";
			countHql += " and reference like :reference";
			param.put("reference", "%" + text + "%");
		}
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "reference";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	if (prodId != null || appId != null) {
    		sidx = "obj." + sidx;
    	}
    	hql += " order by " + sidx + " " + sord;
		Collection<Text> texts = retrieve(hql, param, countHql, param, requestMap);
		
		// additional calculation fields
		textService.populateTranslationSummary(texts);
		textService.populateRefs(texts);
		
		return toJSON(texts, requestMap);
	}

	@Override
	Class getEntityClass() {
		return Text.class;
	}

}
