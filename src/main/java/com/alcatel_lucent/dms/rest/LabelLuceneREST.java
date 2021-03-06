package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import com.alcatel_lucent.dms.util.Util;
import com.google.common.base.Strings;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
        Boolean fuzzy = Boolean.valueOf(requestMap.get("fuzzy"));
        String text = requestMap.get("text");

        Integer firstResult = null;
        Integer maxResult = requestMap.get("rows") == null ? null : Integer.valueOf(requestMap.get("rows"));
        if (null != requestMap.get("page")) {
            firstResult = (Integer.parseInt(requestMap.get("page")) - 1) * maxResult;
        }

        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        if (sidx == null || sidx.trim().isEmpty()) {
            sidx = "sortNo";
        }
        if (sord == null) {
            sord = "ASC";
        }

        Long langId = Strings.isNullOrEmpty(requestMap.get("language")) ? null : Long.valueOf(requestMap.get("language"));
        Collection<Label> labels;
        String[] orders = sidx.split("\\s*,\\s*");

        if (langId == null) {
            Map<String, Object> keywords = new HashMap<String, Object>();
            Map<String, String> fuzzyKeywords = new HashMap<String, String>();
            keywords.put("removed", false);

            if (dictId != null) {
                keywords.put("dictionary.id", dictId);
            } else if (appId != null) {
                keywords.put("dictionary.applications.id", appId);
            } else if (prodId != null) {
                keywords.put("dictionary.applications.products.id", prodId);
            }
            float minimumSimilarity = fuzzy ? 0.8f : 0.99f;

            if (text != null) {
                text = text.trim();
            }

            if (StringUtils.isNotEmpty(text)) {
                fuzzyKeywords.put("reference", text.toUpperCase());
            }


            boolean containComputeOrder = isContainComputeOrder(orders);
            Pair<Integer, List> result;
            Sort sort = orders2Sort(orders, sord);
            if (containComputeOrder) {
                firstResult = maxResult = null;
                sord = null;
            }
            result = dao.hibSearchRetrieve(Label.class, keywords, fuzzyKeywords, minimumSimilarity, firstResult, maxResult, sort);
            labels = result.getRight();

            int resultSize = result.getLeft();
            if (null != firstResult) {// paged
                requestMap.put("records", resultSize + "");
            }
            // add T/N/I information if no language was specified
            fillTNI(text, dictId, labels);

            if (containComputeOrder) {
                //order fields with object comparators
                Collections.sort((ArrayList<Label>) labels, orders2Comparator(orders, sord));
                // filter by page
                labels = pageFilter(labels, requestMap);
            }
            return toJSON(labels, requestMap);
        }

        boolean exact = requestMap.get("exact") != null && Boolean.parseBoolean(requestMap.get("exact"));

//        langId is null
        // add ot and ct information if a specific language was specified
        labels = (text == null && dictId != null) ?
                new ArrayList<Label>(translationService.getLabelsWithTranslation(dictId, langId)) :
                new ArrayList<Label>(translationService.searchLabelsWithTranslation(prodId, appId, dictId, langId, (null == text ? "" : text), exact));
        Collections.sort((ArrayList<Label>) labels, orders2Comparator(orders, sord, false));

        Map<String, String> filters = getGridFilters(requestMap);
        labels = Util.filterCollection(labels, filters, true);

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


    /**
     * return true if orders contained compute order
     */

    private boolean isContainComputeOrder(String[] orders) {
        List<String> computeField = Arrays.asList("t", "n", "i");
        for (String order : orders) {
            if (computeField.contains(order.split("\\s+")[0])) return true;
        }
        return false;

    }

    private void fillTNI(String text, Long dictId, Collection<Label> labels) {
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
    }

    private Sort orders2Sort(String[] orders, String sord) {
        String sidx;
        List<SortField> sortFields = new ArrayList<SortField>();
        for (String order : orders) {
            String[] idxOrder = order.split("\\s+");
            sidx = idxOrder[0];
            if ("reference".equals(sidx)) sidx += "_forSort";
            String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
            sortFields.add(new SortField(sidx, SortField.STRING, tmpOrd.equalsIgnoreCase("asc")));
        }
        return new Sort(sortFields.toArray(new SortField[0]));
    }

    protected ComparatorChain orders2Comparator(String[] orders, String sord) {
        return orders2Comparator(orders, sord, true);
    }

    private ComparatorChain orders2Comparator(String[] orders, String sord, boolean isLuceneOrder) {
        ComparatorChain comparator = new ComparatorChain();
        String sidx;
        for (String order : orders) {
            String[] idxOrder = order.split("\\s+");
            sidx = idxOrder[0];
            if ("reference".equals(sidx) && isLuceneOrder) sidx += "_forSort";
            String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
            comparator.addComparator(new ObjectComparator(sidx, tmpOrd));
        }
        return comparator;
    }


    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
