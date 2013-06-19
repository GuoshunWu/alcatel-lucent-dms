package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.model.TranslationMatch;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import org.apache.commons.collections.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
 * URL: /rest/luceneTranslations
 * Filter parameters:
 * transId	Translation id
 * text	 search text (case insensitive)
 * <p/>
 * language	language id
 * filters	(optional) jqGrid-style filter string, in json format, e.g.
 * {"groupOp":"AND","rules":[{"field":"status","op":"eq","data":"2"}]}
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

    @Override
    public Class<Label> getEntityClass() {
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
        String[] orders = sidx.split("\\s*,\\s*");


        Map<String, Object> keywords = new HashMap<String, Object>();
        Map<String, String> fuzzyKeywords = new HashMap<String, String>();
        keywords.put("status", 2);

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

        return toJSON(toTranslationMatchList(result.getRight(), transId), requestMap);
    }

    /**
     * find original translation match in result list.
     */
    private TranslationMatch findOriginalTranslation(final List<TranslationMatch> resultList, final Long transId) {
        return (TranslationMatch) CollectionUtils.find(resultList, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                TranslationMatch filteredTm = (TranslationMatch) object;
                return filteredTm.getId().equals(transId);
            }
        });
    }

    /**
     * Convert item in list which is a object array to dynamic bean.
     */
    private List<TranslationMatch> toTranslationMatchList(List list, final Long transId) {

        List<TranslationMatch> resultList = (List<TranslationMatch>) CollectionUtils.collect(list, new Transformer() {
            @Override
            public Object transform(Object o) {
                Object[] row = (Object[]) o;
                Float score = (Float) row[0];
                Translation trans = (Translation) row[1];
                return new TranslationMatch(trans.getId(), score, trans.getTranslation(), trans.getText().getReference());
            }
        });
        final TranslationMatch originalTransMatch = findOriginalTranslation(resultList, transId);
//        log.info("original transMatch: {}", originalTransMatch);
//        log.info("resultList size {},resultList=\n{}", resultList.size(), StringUtils.join(resultList, "\n"));

        //distinct
        List<TranslationMatch> filteredList = new ArrayList<TranslationMatch>();
        for (final TranslationMatch tm : resultList) {
            if (!tm.getId().equals(originalTransMatch.getId())
                    && !tm.getTranslation().equals(originalTransMatch.getTranslation())
                    && !CollectionUtils.exists(filteredList, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    TranslationMatch filteredTm = (TranslationMatch) object;
                    boolean transEqual = filteredTm.getTranslation().equals(tm.getTranslation());
                    if (transEqual) {
//                        log.info("tm.id={},tm.trans={}; filteredTm.id={}, filteredTm.trans={}, should be rejected",
//                                new Object[]{tm.getId(), tm.getTranslation(), filteredTm.getId(), filteredTm.getTranslation()});
                        return true;
                    } else {
                        return false;
                    }
                }
            })) {
//                log.info("added tm id: {}, translation {} to filtered list.", tm.getId(), tm.getTranslation());
                filteredList.add(tm);
            }
        }

//        log.info("filterList size: {}, filteredList =\n{}", filteredList.size(), StringUtils.join(filteredList, "\n"));


        return filteredList;
    }

    private Sort orders2Sort(String[] orders, String sord) {
        String sidx;
        List<SortField> sortFields = new ArrayList<SortField>();
        for (String order : orders) {
            String[] idxOrder = order.split("\\s+");
            sidx = idxOrder[0];
            String tmpOrd = idxOrder.length > 1 ? idxOrder[1] : sord;
            boolean isAsc = tmpOrd.equalsIgnoreCase("asc");
            SortField sf = FullTextQuery.SCORE.equals(sidx) ?
                    new SortField(null, SortField.SCORE, isAsc) :
                    new SortField(sidx, SortField.STRING, isAsc);
            sortFields.add(sf);
        }
        return new Sort(sortFields.toArray(new SortField[0]));
    }
}
