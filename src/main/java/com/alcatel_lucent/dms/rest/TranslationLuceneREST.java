package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import org.apache.commons.beanutils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.*;

/**
 * Translation hibernate search REST service.
 * URL: /rest/labels
 * Filter parameters:
 * transId	Translation id
 * app		(optional) application id
 * dict		(optional) dictionary id
 * text		(optional) search text (case insensitive)
 * NOTE: at least one of the parameter "dict" and "text" should be provided
 * <p/>
 * language	(optional) language id
 * If language is supplied, relative LabelTranslation and Translation object can be accessed by
 * adding "ot" or "ct" prefix to the property name, e.g. ot.needTranslation,ct.translation
 * Otherwise, only label properties can be accessed.
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
@Path("luceneTranslations")
@Component
public class TranslationLuceneREST extends BaseREST {

    private static Logger log = LoggerFactory.getLogger(TranslationLuceneREST.class);

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
        String text = requestMap.get("text");
        Boolean fuzzy = Boolean.valueOf(requestMap.get("fuzzy"));
        Long transId = Long.parseLong(requestMap.get("transId"));

        Integer firstResult = null;
        Integer maxResult = requestMap.get("rows") == null ? null : Integer.valueOf(requestMap.get("rows"));
        if (null != requestMap.get("page")) {
            firstResult = (Integer.parseInt(requestMap.get("page")) - 1) * maxResult;
        }

        if (text != null) {
            text = text.trim();
            text = text.isEmpty() ? null : text.toUpperCase();
        }

        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        if (sidx == null || sidx.trim().isEmpty()) {
            sidx = FullTextQuery.SCORE;
        }
        if (sord == null) {
            sord = "desc";
        }

        Long langId = requestMap.get("language") == null ? null : Long.valueOf(requestMap.get("language"));
        Collection<Label> labels;
        String[] orders = sidx.split("\\s*,\\s*");


        Map<String, Object> keywords = new HashMap<String, Object>();
        Map<String, String> fuzzyKeywords = new HashMap<String, String>();
        keywords.put("status", 2);
        // -id: 843415
//        keywords.put("id", transId);
        if (null != langId) {
            keywords.put("language.id", langId);
        }

        float minimumSimilarity = fuzzy ? 0.8f : 0.99f;
        if (StringUtils.isNotEmpty(text)) {
            fuzzyKeywords.put("text.reference", text);
        }

        Pair<Integer, List> result;
        Sort sort = orders2Sort(orders, sord);

        result = dao.hibSearchRetrieve(Translation.class, keywords, fuzzyKeywords, minimumSimilarity,
                firstResult, maxResult, sort,
                FullTextQuery.SCORE,
                FullTextQuery.THIS
        );

        int resultSize = result.getLeft();
        if (null != firstResult) {// paged
            requestMap.put("records", resultSize + "");
        }

        return toJSON(toDynaBeanList(result.getRight()), requestMap);
    }

    /**
     * Convert item in list which is a object array to dynamic bean.
     */
    private List<DynaBean> toDynaBeanList(List list) {
        final DynaClass dTranslation = new BasicDynaClass("Translation", null, new DynaProperty[]{
                new DynaProperty("id", Long.class),
                new DynaProperty("score", Float.class),
                new DynaProperty("reference", String.class),
                new DynaProperty("translation", String.class)
        });

        List<DynaBean> resultList = (List<DynaBean>) CollectionUtils.collect(list, new Transformer() {
            @Override
            public Object transform(Object o) {
                Object[] row = (Object[]) o;
                Float score = (Float) row[0];
                Translation trans = (Translation) row[1];

                DynaBean translation = new BasicDynaBean(dTranslation);
                translation.set("id", trans.getId());
                translation.set("score", score);
                translation.set("reference", trans.getText().getReference());
                translation.set("translation", trans.getTranslation());
                return translation;
            }
        });

        return resultList;
    }

    private Sort orders2Sort(String[] orders, String sord) {
        String sidx;
        List<SortField> sortFields = new ArrayList<SortField>();
        for (String order : orders) {
            String[] idxOrder = order.split("\\s+");
            sidx = idxOrder[0];
//            if (sidx.endsWith("reference")) sidx += "_forSort";
//            if (sidx.equals("score")) sidx = FullTextQuery.SCORE;
            String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
            boolean isAsc = tmpOrd.equalsIgnoreCase("asc");
            SortField sf = FullTextQuery.SCORE.equals(sidx) ?
                    new SortField(null, SortField.SCORE, isAsc) :
                    new SortField(sidx, SortField.STRING, isAsc);
            sortFields.add(sf);
        }
        return new Sort(sortFields.toArray(new SortField[0]));
    }


    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
