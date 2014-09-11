package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.*;

/**
 * Label REST service.
 * URL: /rest/labels
 * Filter parameters:
 * prod		(optional) product id
 * app		(optional) application id
 * dict		(optional) dictionary id
 * text		(optional) search text (case insensitive)
 * NOTE: at least one of the parameter "dict" and "text" should be provided
 * <p/>
 * language	(optional) language id
 * If language is supplied, relative LabelTranslation and Translation object can be accessed by
 * adding "ot" or "ct" prefix to the property name, e.g. ot.needTranslation,ct.translation
 * Otherwise, only label properties can be accessed.
 * nodiff		(optional) default false, if true, return only labels of which translation is identical to reference text.
 * The option only works when "language" parameter is specified.
 * filters	(optional) jqGrid-style filter string, in json format, e.g.
 * {"groupOp":"AND","rules":[{"field":"status","op":"eq","data":"2"}]}
 * NOTE: only support filter "context.name", "ct.status" and "ct.translationType" for the moment
 * <p/>
 * Sort parameters:
 * sidx		(optional) sort by, default is "sortNo"
 * Translation-related properties can be specified by adding "ot" (LabelTranslation) or "ct" (Translation) prefix
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
@Path("labels")
@Component("labelREST")
public class LabelREST extends BaseREST {

    private static Logger log = LoggerFactory.getLogger(LabelREST.class);

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private TranslationService translationService;

    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
        return Label.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long prodId = requestMap.get("prod") == null ? null : Long.valueOf(requestMap.get("prod"));
        Long appId = requestMap.get("app") == null ? null : Long.valueOf(requestMap.get("app"));
        Long dictId = requestMap.get("dict") == null ? null : Long.valueOf(requestMap.get("dict"));
        String text = requestMap.get("text");
        // empty indicate that search exactly empty text
        String tOperator = null;
        String tValue = null;

        if (null != text) {
            if (text.isEmpty()) {
                tOperator = "=";
                tValue = text;
            } else {
                tOperator = "like";
                tValue = "%" + text.toUpperCase() + "%";
            }
        }


        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        if (sidx == null || sidx.trim().isEmpty()) {
            sidx = "sortNo";
        }
        if (sord == null) {
            sord = "ASC";
        }

        Long langId = requestMap.get("language") == null ? null : Long.valueOf(requestMap.get("language"));
        Collection<Label> labels;
        if (langId == null) {
            String hql, countHql;
            Map param = new HashMap();
            Map countParam = new HashMap();
            if (dictId != null) {
                hql = "select obj from Label obj where obj.dictionary.id=:dictId and obj.removed=false";
                countHql = "select count(*) from Label obj where obj.dictionary.id=:dictId and obj.removed=false";
                param.put("dictId", dictId);
                countParam.put("dictId", dictId);
            } else if (appId != null) {
                hql = "select obj from Application a join a.dictionaries d join d.labels obj where a.id=:appId and obj.removed=false";
                countHql = "select count(*) from Application a join a.dictionaries d join d.labels obj where a.id=:appId and obj.removed=false";
                param.put("appId", appId);
                countParam.put("appId", appId);
            } else if (prodId != null) {
                hql = "select obj from Product p join p.applications a join a.dictionaries d join d.labels obj where p.id=:prodId and obj.removed=false";
                countHql = "select count(*) from Product p join p.applications a join a.dictionaries d join d.labels obj where p.id=:prodId and obj.removed=false";
                param.put("prodId", prodId);
                countParam.put("prodId", prodId);
            } else {
                hql = "select obj from Label obj where obj.removed=false";
                countHql = "select count(*) from Label obj where obj.removed=false";
            }

            if (null != text) {
                hql += " and upper(obj.reference) " + tOperator + " :text";
                countHql += " and upper(obj.reference) " + tOperator + " :text";
                param.put("text", tValue);
                countParam.put("text", tValue);
            }

            Map<String, String> filters = getGridFilters(requestMap);
            if (filters != null) {    // filter by status
                String contextFilter = filters.get("context.name");
                if (contextFilter != null && !contextFilter.isEmpty()) {
                    if (contextFilter.equalsIgnoreCase("other")) {
                        String[] systemContexts = new String[]{
                                Context.DEFAULT, Context.EXCLUSION, Context.DICT, Context.APP, Context.PROD};
                        hql += " and obj.context.name not in (:systemContexts)";
                        countHql += " and obj.context.name not in (:systemContexts)";
                        param.put("systemContexts", Arrays.asList(systemContexts));
                        countParam.put("systemContexts", Arrays.asList(systemContexts));
                    } else {
                        hql += " and obj.context.name=:contextName";
                        countHql += " and obj.context.name=:contextName";
                        param.put("contextName", contextFilter);
                        countParam.put("contextName", contextFilter);
                    }
                }
            }
            ComparatorChain comparator = null;
            if (sidx.indexOf(",") == -1 && !sidx.equals("t") && !sidx.equals("n") && !sidx.equals("i") &&
                    !sidx.startsWith("app.") && !sidx.startsWith("prod.")) {
                hql += " order by obj." + sidx + " " + sord;
                labels = retrieve(hql, param, countHql, countParam, requestMap);
            } else {    // sort and page the results out of hql
                String[] orders = sidx.split(",");
                comparator = new ComparatorChain();
                for (String order : orders) {
                    order = order.trim();
                    if (order.isEmpty()) continue;
                    String[] idxOrd = order.split("\\s");
                    sidx = idxOrd[0];
                    sord = idxOrd.length < 2 ? sord : idxOrd[1];
                    comparator.addComparator(new ObjectComparator<Label>(sidx, sord));
                }
                labels = dao.retrieve(hql, param, null);
                requestMap.put("records", "" + labels.size());
            }

            // add T/N/I information if no language was specified
            if (text == null && dictId != null) {
                Map<Long, int[]> summary = translationService.getLabelTranslationSummary(dictId);
                for (Label label : labels) {
                    int[] tni = summary.get(label.getId());
                    label.setT(tni[0]);
                    label.setN(tni[1]);
                    label.setI(tni[2]);
                }
            } else if (text != null) {
                for (Label label : labels) {
                    int[] tni = translationService.getLabelTranslationSummaryByLabel(label.getId());
                    label.setT(tni[0]);
                    label.setN(tni[1]);
                    label.setI(tni[2]);
                }
            }

            // sort labels by comparator
            if (comparator != null) {
                Collections.sort((ArrayList<Label>) labels, comparator);
                labels = pageFilter(labels, requestMap);
            }

            return toJSON(labels, requestMap);

        }


        // add ot and ct information if a specific language was specified
        if (text == null && dictId != null) {
            labels = new ArrayList<Label>(translationService.getLabelsWithTranslation(dictId, langId));
        } else {
            labels = new ArrayList<Label>(translationService.searchLabelsWithTranslation(prodId, appId, dictId, langId, null == text ? "" : text));
        }
        ComparatorChain comparator = new ComparatorChain();
        String[] orders = sidx.split(",");
        for (String order : orders) {
            order = order.trim();
            if (order.isEmpty()) continue;
            String[] idxOrd = order.split("\\s");
            sidx = idxOrd[0];
            sord = idxOrd.length < 2 ? sord : idxOrd[1];
            comparator.addComparator(new ObjectComparator<Label>(sidx, sord));
        }
        Collections.sort((ArrayList<Label>) labels, comparator);
        Map<String, String> filters = getGridFilters(requestMap);
        if (filters != null) {    // filter by status
            String statusParam = filters.get("ct.status");
            if (statusParam != null && !statusParam.isEmpty()) {
                int statusFilter = Integer.parseInt(statusParam);
                // apply status filter
                Iterator<Label> iter = labels.iterator();
                while (iter.hasNext()) {
                    Label label = iter.next();
                    if (statusFilter != label.getCt().getStatus()) {
                        iter.remove();
                    }
                }
            }
            String typeParam = filters.get("ct.translationType");
            if (typeParam != null && !typeParam.isEmpty()) {
                int typeFilter = Integer.parseInt(typeParam);
                // apply type filter
                Iterator<Label> iter = labels.iterator();
                while (iter.hasNext()) {
                    Label label = iter.next();
                    if (label.getCt().getTranslationType() == null || typeFilter != label.getCt().getTranslationType()) {
                        iter.remove();
                    }
                }
            }
        }

        // filter by nodiff flag
        String nodiffStr = requestMap.get("nodiff");
        boolean nodiff = nodiffStr != null && nodiffStr.equalsIgnoreCase("true");
        if (nodiff) {
            Iterator<Label> iter = labels.iterator();
            while (iter.hasNext()) {
                Label label = iter.next();
                if (!label.getReference().equals(label.getCt().getTranslation())) {
                    iter.remove();
                }
            }
        }
        requestMap.put("records", "" + labels.size());
        // filter by page
        labels = pageFilter(labels, requestMap);

        return toJSON(labels, requestMap);
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
