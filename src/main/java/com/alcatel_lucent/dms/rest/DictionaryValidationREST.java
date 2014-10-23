package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.ValidationInfo;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Dictionary error REST service.
 * URL: /rest/dictValidationResult
 * Filter parameters:
 * dict		dictionary id
 * type     errors or warnings
 * <p>
 * filters	(optional) jqGrid-style filter string, in json format, e.g.
 * {"groupOp":"AND","rules":[{"field":"status","op":"eq","data":"2"}]}
 * NOTE: only support filter "context.name", "ct.status" and "ct.translationType" for the moment
 * </p>
 * <p/>
 * <p>
 * Sort parameters:
 * sidx		(optional) sort by, default is "sortNo"
 * Translation-related properties can be specified by adding "ot" (LabelTranslation) or "ct" (Translation) prefix
 * sord		(optional) order, default is "ASC"
 * </p>
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
@Path("dictValidation")
@Component("dictValidationREST")
public class DictionaryValidationREST extends BaseREST {

    private static Logger log = LoggerFactory.getLogger(DictionaryValidationREST.class);

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private TranslationService translationService;

    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
        return ValidationInfo.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
        Long dictId = requestMap.get("dict") == null ? null : Long.valueOf(requestMap.get("dict"));
        List validationResult = new ArrayList();
        if (null != dictId) {
            Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
            dict.validate();
            validationResult = new ArrayList(
                    requestMap.get("type") == null || requestMap.get("type").equals("warnings")
                            ? dict.getDictWarnings() : dict.getDictErrors());
        }

        Map param = new HashMap();
        Map<String, String> filters = getGridFilters(requestMap);

        Util.filterCollection(validationResult, filters, true);

        String sidx = StringUtils.defaultIfBlank(requestMap.get("sidx"), "code");
        String sord = StringUtils.defaultString(requestMap.get("sord"), "asc");

        String[] orders = sidx.split("\\s*,\\s*");

        // sort and pager in memory
        Collections.sort((List<ValidationInfo>) validationResult, orders2Comparator(orders, sord));
        validationResult = (List<ValidationInfo>) pageFilter(validationResult, requestMap);

        return toJSON(validationResult, requestMap);
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
