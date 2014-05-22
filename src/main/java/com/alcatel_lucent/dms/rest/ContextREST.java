package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Context;

/**
 * Context REST service.
 * URL: /rest/contexts
 * Sort parameters:
 * sidx		(optional) sort by, default is "name"
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
 * The result is not paged, that means "rows" and "page" parameter will not be supported.
 *
 * @author allany
 */
@Path("contexts")
@Component("contextREST")
public class ContextREST extends BaseREST {

    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
        return Context.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long prodId = requestMap.get("prod") == null ? null : Long.valueOf(requestMap.get("prod"));
        Long appId = requestMap.get("app") == null ? null : Long.valueOf(requestMap.get("app"));

        log.info("prodId={}, appId={}", prodId, appId);
        String filterHql = "";
        @Language("HQL") String hql = "from Context";
        @Language("HQL") String countHql = "select count(*) from Context ";
        Map<String, Object> params = new HashMap<String, Object>();
        if (prodId != null) {
            filterHql = " where id in (select distinct l.context.id c from Label l join l.dictionary dct join dct.applications app join app.products prod where prod.id =:prodId)";
            params.put("prodId", prodId);
        } else if (appId != null) {
            filterHql = " where id in (select distinct l.context.id from Label l join l.context join l.dictionary dct join dct.applications app where app.id= :appId)";
            params.put("appId", appId);
        }
        hql += filterHql;
        countHql += filterHql;

        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        if (Strings.isNullOrEmpty(sidx)) {
            sidx = "c.name";
        }
        if (Strings.isNullOrEmpty(sord)) {
            sord = "ASC";
        }
        hql += " order by " + sidx + " " + sord;
        Collection<Context> contexts = retrieve(hql, params, countHql, params, requestMap);
        return toJSON(contexts, requestMap);
    }

}
