package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.TranslationHistory;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * DictionaryHistory REST service.
 * URL: /rest/translationCheck
 * Filter parameters:
 * appId		(required) application id
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
@Path("translationCheck")
@Component("translationCheckREST")
public class TranslationCheckREST extends BaseREST {

    @Autowired
    private TranslationService translationService;

    @Override
    String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long appId = Long.valueOf(requestMap.get("appId"));
        Collection<Label> labels = translationService.getLabelTranslationCheckResultByApp(appId);

        String sidx = StringUtils.defaultIfBlank(requestMap.get("sidx"), "sortNo");
        String sord = StringUtils.defaultString(requestMap.get("sord"), "asc");

        String[] orders = sidx.split("\\s*,\\s*");

        Collections.sort((ArrayList<Label>) labels, orders2Comparator(orders, sord));

        Map<String, String> filters = getGridFilters(requestMap);
        //filter ct errors first
        labels = filterCTErrors(labels, filters);
        labels = Util.filterCollection(labels, filters, true);
        requestMap.put("records", "" + labels.size());
        // filter by page
        labels = pageFilter(labels, requestMap);
        return toJSON(labels, requestMap);
    }

    private Collection<Label> filterCTErrors(Collection<Label> labels, Map<String, String> filters) {
        if (null == filters.get("ct.transWarnings")) return labels;
        final Integer errorCode = Integer.valueOf(filters.get("ct.transWarnings"));
        filters.remove("ct.transWarnings");
        CollectionUtils.filter(labels, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Collection<Map> errors = ((Label) object).getCt().getTransWarnings();
                for (Map error : errors) {
                    if (error.get("code").equals(errorCode)) return true;
                }
                return false;
            }
        });
        return labels;
    }


    private ComparatorChain orders2Comparator(String[] orders, String sord) {
        ComparatorChain comparator = new ComparatorChain();
        String sidx;
        for (String order : orders) {
            String[] idxOrder = order.split("\\s+");
            sidx = idxOrder[0];
            String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
            comparator.addComparator(new ObjectComparator(sidx, tmpOrd));
        }
        return comparator;
    }

    @Override
    Class getEntityClass() {
        return TranslationHistory.class;
    }

}
