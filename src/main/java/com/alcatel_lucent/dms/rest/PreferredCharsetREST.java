package com.alcatel_lucent.dms.rest;

import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.LanguageService;

/**
 * Preferred Charset REST service.
 * URL: /rest/preferredCharset
 * Filter parameters:
 *   dict:		(required) dictionaries, list of id split by ","
 *   language:	(required) language id
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   idprop		(optional) property name for id, for grid only
 *   The result is not paged, that means "rows" and "page" parameter will not be supported.
 *   		
 * @author allany
 *
 */
@Path("preferredCharset")
@Component("preferredCharsetREST")
public class PreferredCharsetREST extends BaseREST {

	@Autowired
	private LanguageService languageService;
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String dict = requestMap.get("dict");
		Long langId = Long.valueOf(requestMap.get("language"));
		DictionaryLanguage dl = new DictionaryLanguage();
		Language language = (Language) dao.retrieve(Language.class, langId);
		String langCode = languageService.getPreferredLanguageCode(toIdList(dict), langId);
		Charset charset = languageService.getPreferredCharset(toIdList(dict), langId);
		dl.setLanguageCode(langCode);
		dl.setCharset(charset);
		return toJSON(dl, requestMap);
	}

	@Override
	Class getEntityClass() {
		return null;
	}

}
