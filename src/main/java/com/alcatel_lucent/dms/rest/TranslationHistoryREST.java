package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.TranslationHistory;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * DictionaryHistory REST service.
 * URL: /rest/dictHistory
 * Filter parameters:
 * dict		(required) dictionary id
 * <p/>
 * Sort parameters:
 * sidx		(optional) sort by, default is "operationTime" (desc)
 * sord		(optional) order, default is "ASC"
 * <p/>
 * Format parameters:
 * format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 * prop		(required) properties to be retrieved
 * for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested,
 * e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 * for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 * for tree: prop=<property_name_for_id>,<property_name_for_name>
 * idprop		(optional) property name for id, for grid only
 * rows		(optional) number of records to be retrieved, only be used when format is grid
 * page		(optional) current page, only be used when format is grid
 *
 * @author Guoshun Wu
 */
@Path("translationHistory")
@Component("translationHistoryREST")
public class TranslationHistoryREST extends BaseREST {

    @Override
    String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long transId = Long.valueOf(requestMap.get("transId"));
        @Language("HQL") String hql = "from TranslationHistory where parent.id =:transId";
        @Language("HQL") String countHql = "select count(*) from TranslationHistory where parent.id=:transId";
        Map param = new HashMap();
        param.put("transId", transId);

        String sidx = defaultIfBlank(requestMap.get("sidx"), "operationTime");
        String sord = StringUtils.defaultString(requestMap.get("sord"));
        hql += " order by " + sidx + " " + sord;

        Collection<TranslationHistory> data = retrieve(hql, param, countHql, param, requestMap);
        return toJSON(data, requestMap);
    }

    @Override
    Class getEntityClass() {
        return TranslationHistory.class;
    }

}
