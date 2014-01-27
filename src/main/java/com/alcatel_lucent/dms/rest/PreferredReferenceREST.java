package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.model.PreferredTranslation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * PreferredReference REST service.
 * URL: /rest/preferredTranslations
 * <p/>
 * Sort parameters:
 * sidx		(optional) sort by, default is "reference"
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
 * <p/>
 * filters	(optional) jqGrid-style filter string, in json format, e.g.
 * {"groupOp":"AND","rules":[{"field":"status","op":"eq","data":"2"}]}
 *
 * @author allany
 */
@Path("preferredTranslations")
@Component
@SuppressWarnings("unchecked")
public class PreferredReferenceREST extends BaseREST {

    public Class<PreferredReference> getEntityClass() {
        return PreferredReference.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
        String hql = "select pr, pt from PreferredReference as pr left join pr.preferredTranslations as pt";
        Map<String, Object> params = new HashMap<String, Object>();
        String countHql = "select count(*) from PreferredReference as pr left join pr.preferredTranslations as pt";
        Map<String, Object> countParams = new HashMap<String, Object>();

        Long languageId = null == requestMap.get("languageId") ? null : Long.valueOf(requestMap.get("languageId"));
        if (null != languageId) {
            hql += " with pt.language.id=:languageId";
            params.put("languageId", languageId);
            countHql += " with pt.language.id=:languageId";
            countParams.put("languageId", languageId);
        }

        Map<String, Object> filterParams = new HashMap<String, Object>();
        String filters = populateFiltersSQLFragment(requestMap, filterParams);
        hql += filters;
        params.putAll(filterParams);
        countHql+=filters;
        countParams.putAll(filterParams);

        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");

        hql += " order by " + defaultIfBlank(sidx, "reference") + " " + StringUtils.defaultString(sord) ;
        Collection<Object[]> result = retrieve(hql, params, countHql, params, requestMap);

        return toJSON(filledPreferredReference(result), requestMap);
    }

    private Collection<PreferredReference> filledPreferredReference(Collection<Object[]> objectArrayCollection) {
        if (objectArrayCollection.isEmpty()) return Collections.EMPTY_LIST;
        Collection<PreferredReference> preferredReferences = new ArrayList<PreferredReference>();
        //Dummy Language
        Language dummyLanguage = new Language();
        dummyLanguage.setId(-1L);
        dummyLanguage.setName(StringUtils.EMPTY);
        //Dummy PreferredTranslation
        PreferredTranslation dummyPreferredTranslation = new PreferredTranslation();
        dummyPreferredTranslation.setId(-1L);  // -1 indicate that preferredTranslation is not exists
        dummyPreferredTranslation.setTranslation(StringUtils.EMPTY);
        dummyPreferredTranslation.setLanguage(dummyLanguage);

        for (Object[] objArray : objectArrayCollection) {
            PreferredReference pr = (PreferredReference) objArray[0];
            PreferredTranslation pt = null == objArray[1] ? dummyPreferredTranslation : (PreferredTranslation) objArray[1];
            pr.setPt(pt);
            preferredReferences.add(pr);
        }
        return preferredReferences;
    }
}
