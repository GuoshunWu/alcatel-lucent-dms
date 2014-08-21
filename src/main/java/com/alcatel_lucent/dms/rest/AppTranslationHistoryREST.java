package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.TranslationHistory;
import com.google.common.base.Strings;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * DictionaryHistory REST service.
 * URL: /rest/appTranslationHistory
 * Filter parameters:
 * appId (required) dictionary id
 * <p/>
 * filters	(optional) jqGrid-style filter string, in json format, e.g.
 * {"groupOp":"AND","rules":[{"field":"base.name","op":"eq","data":"2"},{"field":"format","op":"eq","data":"dct"}]}
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
 * @author allany
 */
@Path("appTranslationHistory")
@Component("appTranslationHistoryREST")
public class AppTranslationHistoryREST extends BaseREST {

    private static final SimpleDateFormat dFmt = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long appId = Long.valueOf(requestMap.get("appId"));
        @Language("HQL") String baseSQL = "from Application as a join a.dictionaries as d join d.labels as l join l.text as labelText join labelText.translations as t join t.histories as h where a.id = :appId";
        @Language("HQL") String hql = "select l,h " + baseSQL;
        @Language("HQL") String countHql = "select count(*) " + baseSQL;
        Map<String, Object> param = new HashMap();
        param.put("appId", appId);

        Map<String, String> filters = getGridFilters(requestMap);
        int i = 0;
        String hqlWhere = "";
        if (filters != null && !filters.isEmpty()) {

            Set<Map.Entry<String, String>> filterEntries = filters.entrySet();
            for (Map.Entry<String, String> filterEntry : filterEntries) {
                String fieldName = filterEntry.getKey();
                Object value = filterEntry.getValue();
                hqlWhere += " and " + fieldName;
                if (Arrays.asList("h.operationType", "h.status", "h.parent.text.context.name").contains(fieldName)) {
                    if (!"h.parent.text.context.name".equals(fieldName)) {
                        value = Integer.parseInt((String) value);
                    }
                    hqlWhere += " =:p" + i;
                    param.put("p" + i, value);
                } else {
                    hqlWhere += " like :p" + i;
                    param.put("p" + i, "%" + value + "%");
                }
                i++;
            }
        }

        String from = requestMap.get("from");
        String to = requestMap.get("to");
        if (!Strings.isNullOrEmpty(from) && !Strings.isNullOrEmpty(to)) {
            hqlWhere += " and h.operationTime >=" + ":p" + i;
            param.put("p" + i, dFmt.parse(from));
            i++;
            hqlWhere += " and h.operationTime <:p" + i;
            param.put("p" + i, DateUtils.addDays(dFmt.parse(to), 1));
        }

        hql += hqlWhere;
        countHql += hqlWhere;

        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        hql += " order by " + defaultIfBlank(sidx, "h.operationTime") + " " + StringUtils.defaultString(sord);

        Collection<TranslationHistory> data = fillTransientLabel(retrieve(hql, param, countHql, param, requestMap));
        return toJSON(data, requestMap);
    }

    private Collection<TranslationHistory> fillTransientLabel(Collection<Object[]> data) {
        Collection<TranslationHistory> result = new ArrayList<TranslationHistory>();
        for (Object[] row : data) {
            Label l = (Label) row[0];
            TranslationHistory t = (TranslationHistory) row[1];
            t.setHistoryLabel(l);
            result.add(t);
        }
        return result;
    }

    @Override
    Class getEntityClass() {
        return TranslationHistory.class;
    }

}
