package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.annotations.OrderBy;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
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
 * NOTE: only support filter "ct.status" and "ct.translationType" for the moment
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
@Path("luceneLabels")
@Component
public class LabelLuceneREST extends BaseREST {

    private static Logger log = LoggerFactory.getLogger(LabelLuceneREST.class);

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

        Integer firstResult = null;
        Integer maxResult = requestMap.get("rows") == null ? null : Integer.valueOf(requestMap.get("rows"));
        if (null != requestMap.get("page")) {
            firstResult = (Integer.parseInt(requestMap.get("page")) - 1) * maxResult;
        }

        if (text != null) {
            text = text.trim();
            text = text.isEmpty()?null: text.toUpperCase();
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

            Map<String, Object> keywords = new HashMap<String, Object>();
            keywords.put("removed", false);

            if (dictId != null) {
                keywords.put("dictionary.id", dictId);
            } else if (appId != null) {
                keywords.put("dictionary.applications.id", appId);
            } else if (prodId != null) {
                keywords.put("dictionary.applications.products.id", prodId);
            }

            if (text != null) {
                keywords.put("reference", text);
            }

            String[] orders = sidx.split("\\s*,\\s*");
            boolean containComputeOrder = isContainComputeOrder(orders);
            Pair<Integer, List> result;
            if (!containComputeOrder) {
                List<SortField> sortFields = new ArrayList<SortField>();
                for (String order : orders) {
                    String[] idxOrder = order.split("\\s+");
                    sidx = idxOrder[0];
                    if ("reference".equals(sidx)) sidx += "_forSort";
                    String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
                    sortFields.add(new SortField(sidx, SortField.STRING, tmpOrd.equalsIgnoreCase("asc")));
                }
                Sort sort = new Sort(sortFields.toArray(new SortField[0]));
                result = dao.hibSearchRetrieve(Label.class, keywords, firstResult, maxResult, sort);
                labels = result.getRight();
            } else {
                // disable page
                firstResult = maxResult = null;

                Sort sort = null;
                result = dao.hibSearchRetrieve(Label.class, keywords, firstResult, maxResult, sort);
                labels = result.getRight();
            }
            int resultSize = result.getLeft();
            if (null != firstResult) {// paged
                requestMap.put("records", resultSize + "");
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

            if(containComputeOrder){
                //order fields with object comparators
                ComparatorChain comparator = new ComparatorChain();
                for (String order : orders) {
                    String[] idxOrder = order.split("\\s+");
                    sidx = idxOrder[0];
                    if ("reference".equals(sidx)) sidx += "_forSort";
                    String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
                    comparator.addComparator(new ObjectComparator(sidx, tmpOrd));
                }
                Collections.sort((ArrayList<Label>) labels, comparator);
                // filter by page
                labels = pageFilter(labels, requestMap);
            }


        } else {
            // for search text and translation in Translation view, not implemented yet.
            log.error("This function is not implement yet.");
            labels = new ArrayList();

        }

        return toJSON(labels, requestMap);
    }



    /**
     * return true if orders contained compute order
     * */
    private boolean isContainComputeOrder(String[] orders) {
        List<String> computeField = Arrays.asList("t", "n", "i");
        for (String order : orders) {
            if (computeField.contains(order.split("\\s+")[0])) return true;
        }
        return false;
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
