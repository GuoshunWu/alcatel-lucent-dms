package com.alcatel_lucent.dms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import com.alcatel_lucent.dms.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;

@Service("translationService")
public class TranslationServiceImpl extends BaseServiceImpl implements
        TranslationService {

    private static Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    @Autowired
    private LanguageService languageService;

    @Autowired
    private TextService textService;

    @Autowired
    private GlossaryService glossaryService;

    /*
    public Map<Long, int[]> getDictTranslationSummary(Long dictId) {
        Map<Long, int[]> result = new HashMap<Long, int[]>();
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        String hql = "select ot.language.id" +
                ",sum(case when ot.needTranslation=false or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
                ",sum(case when ot.needTranslation=true and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when ot.needTranslation=true and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Dictionary d join d.labels l join l.origTranslations ot join l.text.translations t" +
                " where d.id=:dictId and ot.language=t.language" +
                " group by ot.language.id";
        Map param = new HashMap();
        param.put("dictId", dictId);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            result.put((Long) row[0], new int[] {((Number)row[1]).intValue(), ((Number)row[2]).intValue(), ((Number)row[3]).intValue()});
        }
        return result;
    }
*/
/*
 * aborted due to HQL left join issue
    public Map<Long, Map<Long, int[]>> getDictTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select d.id,dl.language.id" +
    			",sum(case when ot.needTranslation=false or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when (ot.needTranslation is null or ot.needTranslation=true) and (t.status is null or t.status=" + Translation.STATUS_UNTRANSLATED + ") then 1 else 0 end) " +
    			",sum(case when (ot.needTranslation is null or ot.needTranslation=true) and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl" +
    			" join d.labels l left join l.origTranslations ot with language.id=dl.language.id left join l.text.translations t with language.id=dl.language.id" +
    			" where p.id=:prodId" +
    			" group by d.id,dl.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }

    public Map<Long, Map<Long, int[]>> getAppTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select a.id,ot.language.id" +
    			",sum(case when ot.needTranslation=false or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=true and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=true and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by a.id,ot.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {((Number)row[2]).intValue(), ((Number)row[3]).intValue(), ((Number)row[4]).intValue()});
    	}
    	return result;
    }

*/
    public Map<Long, Map<Long, int[]>> getDictTranslationSummaryByProd(Long prodId) {
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();

        // count labels for each dictionary
        String hql = "select d.id,dl.language.id,count(distinct dl.languageCode),count(*)" +
                " from Product p join p.applications a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
                " where p.id=:prodId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " group by d.id,dl.language.id";
        Map param = new HashMap();
        param.put("prodId", prodId);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
        for (Object[] row : qr) {
            Long dictId = ((Number) row[0]).longValue();
            Long langId = ((Number) row[1]).longValue();
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            // initial empty langMap
            Map<Long, int[]> langMap = result.get(dictId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(dictId, langMap);
            }
            langMap.put(langId, new int[]{0, 0, 0});
            labelCount.put(dictId, ((Number) row[3]).intValue() / factor);
        }

        // count untranslated and in progress translations for each dictionary
        hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
                ",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl" +
                " join d.labels l join l.text.translations t" +
                " where p.id=:prodId and t.language=dl.language and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
                " and l.context.name<>:exclusion" +
                " group by d.id,dl.language.id";
        param.put("exclusion", Context.EXCLUSION);
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long dictId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            Map<Long, int[]> langMap = result.get(dictId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(dictId, langMap);
            }
            langMap.put(langId, new int[]{0, ((Number) row[3]).intValue() / factor, ((Number) row[4]).intValue() / factor});
        }

        // in case of no Translation object associated
        // count as untranslated
        hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
                ",count(*) " +
                " from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
                " where p.id=:prodId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) " +
                " and l.context.name<>:exclusion" +
                " group by d.id,dl.language.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long dictId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();
            Map<Long, int[]> langMap = result.get(dictId);
            int[] values = langMap.get(langId);
            values[1] += ((Number) row[3]).intValue() / factor;
        }

        // set translated = total - untranslated - in process
        for (Long dictId : result.keySet()) {
            Map<Long, int[]> langMap = result.get(dictId);
            int total = labelCount.get(dictId);
            for (int[] values : langMap.values()) {
                values[0] = total - values[1] - values[2];
            }
        }

        return result;
    }

    /**
     * JDBC version, not fast, unused
     *
     * @param prodId
     * @return
     */
    public Map<Long, Map<Long, int[]>> getDictTranslationSummaryByProdJDBC(final Long prodId) {
        final Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
        dao.getSession().doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                String sql =
                        "SELECT D.ID,LANG.ID,COUNT(DISTINCT DL.LANGUAGE_CODE)," +
                                " SUM(CASE WHEN LT.NEED_TRANSLATION=false OR CTX.NAME='[EXCLUSION]' OR CT.STATUS=" + Translation.STATUS_TRANSLATED + " THEN 1 ELSE 0 END) T," +
                                " SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.ID IS NULL OR CT.STATUS=" + Translation.STATUS_UNTRANSLATED + ") THEN 1 ELSE 0 END) N," +
                                " SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.STATUS=" + Translation.STATUS_IN_PROGRESS + ") THEN 1 ELSE 0 END) I" +
                                " FROM dms.PRODUCT_APPLICATION PA" +
                                " JOIN dms.APPLICATION APP ON PA.APPLICATION_ID=APP.ID" +
                                " JOIN dms.APPLICATION_DICTIONARY AD ON AD.APPLICATION_ID=APP.ID" +
                                " JOIN dms.DICTIONARY D ON AD.DICTIONARY_ID=D.ID" +
                                " JOIN dms.DICTIONARY_LANGUAGE DL ON DL.DICTIONARY_ID = D.ID AND DL.LANGUAGE_CODE <> D.REFERENCE_LANGUAGE" +
                                " JOIN dms.LANGUAGE LANG ON LANG.ID = DL.LANGUAGE_ID" +
                                " JOIN dms.LABEL L ON L.DICTIONARY_ID = D.ID AND L.REMOVED=false" +
                                " JOIN dms.CONTEXT CTX ON L.CONTEXT_ID=CTX.ID" +
                                " LEFT JOIN dms.LABEL_TRANSLATION LT ON LT.LABEL_ID = L.ID AND LT.LANGUAGE_ID = LANG.ID" +
                                " LEFT JOIN dms.TRANSLATION CT ON CT.TEXT_ID = L.TEXT_ID AND CT.LANGUAGE_ID = LANG.ID" +
                                " WHERE PA.PRODUCT_ID=" + prodId +
                                " GROUP BY D.ID,LANG.ID";
                long ts1 = System.currentTimeMillis();
                log.info("[SQL] " + sql);
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    Long dictId = rs.getLong(1);
                    Long langId = rs.getLong(2);
                    int factor = rs.getInt(3);
                    int t = rs.getInt(4) / factor;
                    int n = rs.getInt(5) / factor;
                    int i = rs.getInt(6) / factor;
                    Map<Long, int[]> map = result.get(dictId);
                    if (map == null) {
                        map = new HashMap<Long, int[]>();
                        result.put(dictId, map);
                    }
                    map.put(langId, new int[]{t, n, i});
                }
                long ts2 = System.currentTimeMillis();
                log.info("Time used for query: " + (ts2 - ts1) + "ms");
            }
        });
        return result;
    }

    public Map<Long, Map<Long, int[]>> getDictTranslationSummaryByApp(Long appId) {
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();

        // count labels for each dictionary
        String hql = "select d.id,dl.language.id,count(distinct dl.languageCode),count(*)" +
                " from Application a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
                " where a.id=:appId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " group by d.id,dl.language.id";
        Map param = new HashMap();
        param.put("appId", appId);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
        for (Object[] row : qr) {
            Long dictId = ((Number) row[0]).longValue();
            Long langId = ((Number) row[1]).longValue();
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            // initial empty langMap
            Map<Long, int[]> langMap = result.get(dictId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(dictId, langMap);
            }
            langMap.put(langId, new int[]{0, 0, 0});
            labelCount.put(dictId, ((Number) row[3]).intValue() / factor);
        }

        // count untranslated and in progress translations for each dictionary
        hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
                ",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Application a join a.dictionaries d join d.dictLanguages dl" +
                " join d.labels l join l.text.translations t" +
                " where a.id=:appId and t.language=dl.language and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
                " and l.context.name<>:exclusion" +
                " group by d.id,dl.language.id";
        param.put("exclusion", Context.EXCLUSION);
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long dictId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            Map<Long, int[]> langMap = result.get(dictId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(dictId, langMap);
            }
            langMap.put(langId, new int[]{0, ((Number) row[3]).intValue() / factor, ((Number) row[4]).intValue() / factor});
        }

        // in case of no Translation object associated
        // count as untranslated
        hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
                ",count(*) " +
                " from Application a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
                " where a.id=:appId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) " +
                " and l.context.name<>:exclusion" +
                " group by d.id,dl.language.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long dictId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();
            Map<Long, int[]> langMap = result.get(dictId);
            int[] values = langMap.get(langId);
            values[1] += ((Number) row[3]).intValue() / factor;
        }

        // set translated = total - untranslated - in process
        for (Long dictId : result.keySet()) {
            Map<Long, int[]> langMap = result.get(dictId);
            int total = labelCount.get(dictId);
            for (int[] values : langMap.values()) {
                values[0] = total - values[1] - values[2];
            }
        }

        return result;
    }

    public Map<Long, Map<Long, int[]>> getAppTranslationSummaryByApp(Long appId) {
        Map<Long, int[]> langMap = new HashMap<Long, int[]>();

        // count labels
        Application application = (Application) dao.retrieve(Application.class, appId);
        int labelCount = application.getLabelNum();

        // init empty langMap
        String hql = "select dl.language.id" +
                " from Application a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
                " where a.id=:appId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " group by dl.language.id";
        Map param = new HashMap();
        param.put("appId", appId);
        Collection<Number> qr1 = dao.retrieve(hql, param);
        for (Number langId : qr1) {
            langMap.put(langId.longValue(), new int[]{0, 0, 0});
        }

        // count untranslated and in progress translations for each app
        hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
                ",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Application a join a.dictionaries d join d.dictLanguages dl" +
                " join d.labels l join l.text.translations t" +
                " where a.id=:appId and t.language=dl.language and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
                " and l.context.name<>:exclusion" +
                " group by a.id,dl.language.id";
        param.put("exclusion", Context.EXCLUSION);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
//    		Long appId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            langMap.put(langId, new int[]{0, ((Number) row[3]).intValue() / factor, ((Number) row[4]).intValue() / factor});
        }

        // in case of no Translation object associated
        // count as untranslated
        hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
                ",count(*) " +
                " from Application a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
                " where a.id=:appId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) " +
                " and l.context.name<>:exclusion" +
                " group by a.id,dl.language.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
//    		Long appId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();
            int[] values = langMap.get(langId);
            values[1] += ((Number) row[3]).intValue() / factor;
        }

        // set translated = total - untranslated - in process
        for (int[] values : langMap.values()) {
            values[0] = labelCount - values[1] - values[2];
        }
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
        result.put(appId, langMap);
        return result;
    }

    public Map<Long, Map<Long, int[]>> getAppTranslationSummaryByProd(Long prodId) {
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();

        // count labels for each app
        String hql = "select a.id,count(*)" +
                " from Product p join p.applications a join a.dictionaries d join d.labels l" +
                " where p.id=:prodId and l.removed=false" +
                " group by a.id";
        Map param = new HashMap();
        param.put("prodId", prodId);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
        for (Object[] row : qr) {
            Long appId = ((Number) row[0]).longValue();
            labelCount.put(appId, ((Number) row[1]).intValue());
        }

        // initial empty langMap
        hql = "select a.id,dl.language.id" +
                " from Product p join p.applications a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
                " where p.id=:prodId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " group by a.id,dl.language.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long appId = ((Number) row[0]).longValue();
            Long langId = ((Number) row[1]).longValue();
            Map<Long, int[]> langMap = result.get(appId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(appId, langMap);
            }
            langMap.put(langId, new int[]{0, 0, 0});
        }

        // count untranslated and in progress translations for each app
        hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
                ",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl" +
                " join d.labels l join l.text.translations t" +
                " where p.id=:prodId and t.language=dl.language and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
                " and l.context.name<>:exclusion" +
                " group by a.id,dl.language.id";
        param.put("exclusion", Context.EXCLUSION);
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long appId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();    // number of lang codes for the same language, needed for division
            Map<Long, int[]> langMap = result.get(appId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(appId, langMap);
            }
            langMap.put(langId, new int[]{0, ((Number) row[3]).intValue() / factor, ((Number) row[4]).intValue() / factor});
        }

        // in case of no Translation object associated
        // count as untranslated
        hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
                ",count(*) " +
                " from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
                " where p.id=:prodId and dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) " +
                " and l.context.name<>:exclusion" +
                " group by a.id,dl.language.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long appId = (Long) row[0];
            Long langId = (Long) row[1];
            int factor = ((Number) row[2]).intValue();
            Map<Long, int[]> langMap = result.get(appId);
            int[] values = langMap.get(langId);
            values[1] += ((Number) row[3]).intValue() / factor;
        }

        // set translated = total - untranslated - in process
        for (Long dictId : result.keySet()) {
            Map<Long, int[]> langMap = result.get(dictId);
            int total = labelCount.get(dictId);
            for (int[] values : langMap.values()) {
                values[0] = total - values[1] - values[2];
            }
        }

        return result;
    }

    /**
     * JDBC version, but not fast, unused
     *
     * @param prodId
     * @return
     */
    public Map<Long, Map<Long, int[]>> getAppTranslationSummaryJDBC(final Long prodId) {
        final Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
        dao.getSession().doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                String sql =
                        "SELECT APP.ID,LANG.ID,COUNT(DISTINCT DL.LANGUAGE_CODE)," +
                                " SUM(CASE WHEN LT.NEED_TRANSLATION=false OR CTX.NAME='[EXCLUSION]' OR CT.STATUS=" + Translation.STATUS_TRANSLATED + " THEN 1 ELSE 0 END) T," +
                                " SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.ID IS NULL OR CT.STATUS=" + Translation.STATUS_UNTRANSLATED + ") THEN 1 ELSE 0 END) N," +
                                " SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.STATUS=" + Translation.STATUS_IN_PROGRESS + ") THEN 1 ELSE 0 END) I" +
                                " FROM dms.PRODUCT_APPLICATION PA" +
                                " JOIN dms.APPLICATION APP ON PA.APPLICATION_ID=APP.ID" +
                                " JOIN dms.APPLICATION_DICTIONARY AD ON AD.APPLICATION_ID=APP.ID" +
                                " JOIN dms.DICTIONARY D ON AD.DICTIONARY_ID=D.ID" +
                                " JOIN dms.DICTIONARY_LANGUAGE DL ON DL.DICTIONARY_ID = D.ID AND DL.LANGUAGE_CODE <> D.REFERENCE_LANGUAGE" +
                                " JOIN dms.LANGUAGE LANG ON LANG.ID = DL.LANGUAGE_ID" +
                                " JOIN dms.LABEL L ON L.DICTIONARY_ID = D.ID AND L.REMOVED=FALSE" +
                                " JOIN dms.CONTEXT CTX ON L.CONTEXT_ID=CTX.ID" +
                                " LEFT JOIN dms.LABEL_TRANSLATION LT ON LT.LABEL_ID = L.ID AND LT.LANGUAGE_ID = LANG.ID" +
                                " LEFT JOIN dms.TRANSLATION CT ON CT.TEXT_ID = L.TEXT_ID AND CT.LANGUAGE_ID = LANG.ID" +
                                " WHERE PA.PRODUCT_ID=" + prodId +
                                " GROUP BY APP.ID,LANG.ID";
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    Long appId = rs.getLong(1);
                    Long langId = rs.getLong(2);
                    int factor = rs.getInt(3);
                    int t = rs.getInt(4) / factor;
                    int n = rs.getInt(5) / factor;
                    int i = rs.getInt(6) / factor;
                    Map<Long, int[]> map = result.get(appId);
                    if (map == null) {
                        map = new HashMap<Long, int[]>();
                        result.put(appId, map);
                    }
                    map.put(langId, new int[]{t, n, i});
                }
            }
        });
        return result;
    }


    @Override
    public Map<Long, int[]> getLabelTranslationSummary(Long dictId) {
        Map<Long, int[]> result = new HashMap<Long, int[]>();
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        if (dict.getLabels() == null) return result;
        for (Label label : dict.getLabels()) {
            result.put(label.getId(), new int[]{0, 0, 0});
        }

        // count untranslated and in progress translations for each label
        String hql = "select l.id" +
                ",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
                ",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
                " from Dictionary d join d.dictLanguages dl" +
                " join d.labels l join l.text.translations t" +
                " where d.id=:dictId and t.language=dl.language and dl.languageCode<>d.referenceLanguage" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
                " and l.context.name<>:exclusion" +
                " group by l.id";
        Map param = new HashMap();
        param.put("dictId", dictId);
        param.put("exclusion", Context.EXCLUSION);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long labelId = ((Number) row[0]).longValue();
            int[] values = new int[]{0, ((Number) row[1]).intValue(), ((Number) row[2]).intValue()};
            result.put(labelId, values);
        }

        // in case of no Translation object associated
        // count as untranslated
        hql = "select l.id,count(*) " +
                " from Dictionary d join d.dictLanguages dl join d.labels l" +
                " where d.id=:dictId and dl.languageCode<>d.referenceLanguage" +
                " and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) " +
                " and l.context.name<>:exclusion" +
                " group by l.id";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long labelId = (Long) row[0];
            int[] values = result.get(labelId);
            values[1] += ((Number) row[1]).intValue();
        }

        // set translated = total - untranslated - in process
        int total = 0;
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (!dl.isReference()) {
                    total++;
                }
            }
        }
        for (int[] values : result.values()) {
            values[0] = total - values[1] - values[2];
        }
        return result;
    }

    public int[] getLabelTranslationSummaryByLabel(Long labelId) {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        Dictionary dict = label.getDictionary();
        int total = 0;
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (!dl.isReference()) {
                    total++;
                }
            }
        }
        if (label.getContext().getName().equals(Context.EXCLUSION)) {
            return new int[]{total, 0, 0};
        }
        int countN = 0, countI = 0;

        // count untranslated and in progress translations
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (dl.isReference()) continue;
                LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
                if (lt != null && !lt.isNeedTranslation()) continue;
                Translation trans = label.getText().getTranslation(dl.getLanguage().getId());
                if (trans == null || trans.getStatus() == Translation.STATUS_UNTRANSLATED) {
                    countN++;
                } else if (trans.getStatus() == Translation.STATUS_IN_PROGRESS) {
                    countI++;
                }
            }
        }
        return new int[]{total - countN - countI, countN, countI};
    }


    @Override
    public void generateDictTranslationReportByProd(Long prodId, Collection<Long> langIds, OutputStream output) {
        Map<Long, Map<Long, int[]>> data = getDictTranslationSummaryByProd(prodId);
        Collection<Language> languages = languageService.getLanguagesInProduct(prodId);
        if (langIds != null) {
            HashSet<Long> langIdSet = new HashSet<Long>(langIds);
            for (Iterator<Language> iter = languages.iterator(); iter.hasNext(); ) {
                Language language = iter.next();
                if (!langIdSet.contains(language.getId())) {
                    iter.remove();
                }
            }
        }
        generateDictTranslationReport(data, languages, output);
    }

    @Override
    public void generateDictTranslationReportByApp(Long appId, Collection<Long> langIds, OutputStream output) {
        Map<Long, Map<Long, int[]>> data = getDictTranslationSummaryByApp(appId);
        Collection<Language> languages = languageService.getLanguagesInApplication(appId);
        if (langIds != null) {
            HashSet<Long> langIdSet = new HashSet<Long>(langIds);
            for (Iterator<Language> iter = languages.iterator(); iter.hasNext(); ) {
                Language language = iter.next();
                if (!langIdSet.contains(language.getId())) {
                    iter.remove();
                }
            }
        }
        generateDictTranslationReport(data, languages, output);
    }

    private void generateDictTranslationReport(Map<Long, Map<Long, int[]>> data, Collection<Language> languages, OutputStream output) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row headRow1 = sheet.createRow(0);
        Row headRow2 = sheet.createRow(1);
        int col = 0;
        headRow1.createCell(col).setCellValue("Application");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
//		headRow1.createCell(++col).setCellValue("App version");
//		sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Dictionary");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Dict version");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Encoding");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Format");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Num of string");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        col++;
        for (Language lang : languages) {
            headRow1.createCell(col).setCellValue(lang.getName());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 2));
            headRow2.createCell(col).setCellValue("T");
            headRow2.createCell(col + 1).setCellValue("N");
            headRow2.createCell(col + 2).setCellValue("I");
            col += 3;
        }
        int rowNo = 2;
        for (Long dictId : data.keySet()) {
            Row row = sheet.createRow(rowNo++);
            Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
            col = 0;
            createCell(row, col++, dict.getBase().getApplicationBase().getName(), null);
            createCell(row, col++, dict.getName(), null);
            createCell(row, col++, dict.getVersion(), null);
            createCell(row, col++, dict.getEncoding(), null);
            createCell(row, col++, dict.getFormat(), null);
            createCell(row, col++, dict.getLabelNum(), null);
            for (Language lang : languages) {
                int[] values = data.get(dictId).get(lang.getId());
                if (values == null) {
                    values = new int[]{0, 0, 0};
                }
                for (int i = 0; i < 3; i++) {
                    createCell(row, col++, values[i], null);
                }
            }
        }
        try {
            wb.write(output);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new SystemError(e);
        }
    }

    @Override
    public void generateAppTranslationReportByProd(Long prodId, Collection<Long> langIds, OutputStream output) {
        Map<Long, Map<Long, int[]>> data = getAppTranslationSummaryByProd(prodId);
        Collection<Language> languages = languageService.getLanguagesInProduct(prodId);
        if (langIds != null) {
            HashSet<Long> langIdSet = new HashSet<Long>(langIds);
            for (Iterator<Language> iter = languages.iterator(); iter.hasNext(); ) {
                Language language = iter.next();
                if (!langIdSet.contains(language.getId())) {
                    iter.remove();
                }
            }
        }
        generateAppTranslationReport(data, languages, output);
    }

    @Override
    public void generateAppTranslationReportByApp(Long appId, Collection<Long> langIds, OutputStream output) {
        Map<Long, Map<Long, int[]>> data = getAppTranslationSummaryByApp(appId);
        Collection<Language> languages = languageService.getLanguagesInApplication(appId);
        if (langIds != null) {
            HashSet<Long> langIdSet = new HashSet<Long>(langIds);
            for (Iterator<Language> iter = languages.iterator(); iter.hasNext(); ) {
                Language language = iter.next();
                if (!langIdSet.contains(language.getId())) {
                    iter.remove();
                }
            }
        }
        generateAppTranslationReport(data, languages, output);
    }

    private void generateAppTranslationReport(Map<Long, Map<Long, int[]>> data, Collection<Language> languages, OutputStream output) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row headRow1 = sheet.createRow(0);
        Row headRow2 = sheet.createRow(1);
        int col = 0;
        headRow1.createCell(col).setCellValue("Application");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("App version");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        headRow1.createCell(++col).setCellValue("Num of string");
        sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
        col++;
        for (Language lang : languages) {
            headRow1.createCell(col).setCellValue(lang.getName());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 2));
            headRow2.createCell(col).setCellValue("T");
            headRow2.createCell(col + 1).setCellValue("N");
            headRow2.createCell(col + 2).setCellValue("I");
            col += 3;
        }
        int rowNo = 2;
        for (Long appId : data.keySet()) {
            Row row = sheet.createRow(rowNo++);
            Application app = (Application) dao.retrieve(Application.class, appId);
            col = 0;
            createCell(row, col++, app.getName(), null);
            createCell(row, col++, app.getVersion(), null);
            createCell(row, col++, app.getLabelNum(), null);
            for (Language lang : languages) {
                int[] values = data.get(appId).get(lang.getId());
                if (values == null) {
                    values = new int[]{0, 0, 0};
                }
                for (int i = 0; i < 3; i++) {
                    createCell(row, col++, values[i], null);
                }
            }
        }
        try {
            wb.write(output);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new SystemError(e);
        }
    }

    public void exportTranslations(Collection<Long> dictIds, Collection<Long> langIds, OutputStream output) {
        Workbook wb = new HSSFWorkbook();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Long langId : langIds) {
            Language language = (Language) dao.retrieve(Language.class, langId);
            Sheet sheet = wb.createSheet(language.getName());
            Row headRow = sheet.createRow(0);
            createCell(headRow, 0, "Dictionary", null);
            createCell(headRow, 1, "Label", null);
            createCell(headRow, 2, "Context Key", null);
            createCell(headRow, 3, "Context", null);
            createCell(headRow, 4, "Max Length", null);
            createCell(headRow, 5, "Reference", null);
            createCell(headRow, 6, "Original Translation", null);
            createCell(headRow, 7, "Translation", null);
            createCell(headRow, 8, "Description", null);
            createCell(headRow, 9, "Translation Source", null);
            createCell(headRow, 10, "Last Updated", null);
            createCell(headRow, 11, "Label ID", null);
            sheet.setColumnWidth(2, 0);
            sheet.setColumnWidth(6, 0);
            sheet.setColumnWidth(11,  0);
            int r = 1;
            for (Long dictId : dictIds) {
                Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
                Collection<Label> labels = getLabelsWithTranslation(dictId, langId);
                for (Label label : labels) {
                    Row row = sheet.createRow(r++);
                    createCell(row, 0, dict.getName(), null);
                    createCell(row, 1, label.getKey(), null);
                    createCell(row, 2, label.getContext().getKey(), null);
                    createCell(row, 3, label.getContext().getName(), null);
                    createCell(row, 4, label.getMaxLength(), null);
                    createCell(row, 5, label.getReference(), null);
                    createCell(row, 6, label.getCt().getTranslation(), null);
                    createCell(row, 7, label.getCt().getTranslation(), null);
                    createCell(row, 8, label.getDescription(), null);
                    if (label.getCt().getTranslationType() != null) {
                        createCell(row, 9, getTranslationTypeLabel(label.getCt().getTranslationType()), null);
                    }
                    if (label.getCt().getLastUpdateTime() != null) {
                        createCell(row, 10, sdf.format(label.getCt().getLastUpdateTime()), null);
                    }
                    createCell(row, 11, label.getId(), null);
                }
            }
        }
        try {
            wb.write(output);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new SystemError(e);
        }
    }

    public int importTranslations(File file) throws BusinessException {
        FileInputStream inp = null;
        try {
            Map<String, Map<String, Text>> contextMap = new HashMap<String, Map<String, Text>>();
            int count = 0;
            inp = new FileInputStream(file);
            Workbook wb = WorkbookFactory.create(inp);
            HSSFDataFormatter formatter = new HSSFDataFormatter();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                String languageName = sheet.getSheetName();
                Language lang = languageService.findLanguageByName(languageName);
                if (lang == null) {
                    throw new BusinessException(BusinessException.UNKNOWN_LANG_NAME, languageName);
                }
                checkTranslationSheet(sheet);
                Row row;

                for (int dataIndex = sheet.getFirstRowNum() + 1; (null != (row = sheet.getRow(dataIndex))); ++dataIndex) {
                    String contextKey = formatter.formatCellValue(row.getCell(2));
                    String reference = formatter.formatCellValue(row.getCell(5));
                    String origTranslation = formatter.formatCellValue(row.getCell(6));
                    String newTranslation = formatter.formatCellValue(row.getCell(7));
                    Long refLabelId = null;
                    try {
                    	refLabelId = (long) row.getCell(11).getNumericCellValue();
                    } catch (Exception e) {
                    	log.warn("Error reading Column 'Label ID', maybe an excel of previous version.");
                    }


                    if (!origTranslation.equals(newTranslation)) {
                        log.info(languageName + " translation of \"" + reference + "\" was changed, update it into DMS.");
                        //consistent glossaries

                        Collection<GlossaryMatchObject> GlossaryMatchObjects = glossaryService.getGlossaryPatterns();
                        for (GlossaryMatchObject gmo : GlossaryMatchObjects) {
                            gmo.getProcessedString(reference);
                            if (!gmo.isReplaced()) continue;
                            newTranslation = gmo.getProcessedString(newTranslation);
                        }

                        Map<String, Text> textMap = contextMap.get(contextKey);
                        if (textMap == null) {
                            textMap = new HashMap<String, Text>();
                            contextMap.put(contextKey, textMap);
                        }
                        Text text = textMap.get(reference);
                        if (text == null) {
                        	Label label = refLabelId == null ? null : (Label) dao.retrieve(Label.class, refLabelId);
                            text = new Text();
                            text.setReference(reference);
                            text.setRefLabel(label);
                            textMap.put(reference, text);
                        }
                        Translation trans = new Translation();
                        trans.setLanguage(lang);
                        trans.setTranslation(newTranslation);
                        trans.setStatus(Translation.STATUS_TRANSLATED);
                        trans.setTranslationType(Translation.TYPE_MANUAL);
                        text.addTranslation(trans);
                        count++;
                    }
                }
            }
            for (String contextKey : contextMap.keySet()) {
                Context context = textService.getContextByKey(contextKey);
                if (context == null) {
                    throw new BusinessException(BusinessException.INVALID_CONTEXT_KEY, contextKey);
                }
                Map<String, Text> textMap = contextMap.get(contextKey);
                log.info("### context key:" + contextKey + " text num: " + textMap.size());
                textService.updateTranslations(context.getId(), textMap.values(), Constants.ImportingMode.TRANSLATION, TranslationHistory.TRANS_OPER_INPUT);
            }
            return count;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new SystemError(e);
        } finally {
            if (inp != null) try {
                inp.close();
            } catch (Exception e) {
            }
        }
    }

    private void checkTranslationSheet(Sheet sheet) throws BusinessException {
        Row row = sheet.getRow(0);
        if (row != null) {
            if (row.getCell(2).getStringCellValue().equals("Context Key") &&
                    row.getCell(5).getStringCellValue().equals("Reference") &&
                    row.getCell(6).getStringCellValue().equals("Original Translation") &&
                    row.getCell(7).getStringCellValue().equals("Translation")) {
                return;
            }
        }
        throw new BusinessException(BusinessException.INVALID_TRANSLATION_FILE);
    }

    private String getTranslationTypeLabel(int translationType) {
        switch (translationType) {
            case Translation.TYPE_DICT:
                return "From dict";
            case Translation.TYPE_TASK:
                return "From task";
            case Translation.TYPE_MANUAL:
                return "Manual";
            case Translation.TYPE_AUTO:
                return "Auto";
        }
        return null;
    }

    public Collection<Label> getLabelsWithTranslation(Long dictId, Long langId) {
        String hql = "select obj from Label obj where obj.dictionary.id=:dictId and obj.removed=false order by obj.sortNo";
        Map param = new HashMap();
        param.put("dictId", dictId);
        Collection<Label> labels = dao.retrieve(hql, param);
        Map<Long, Label> labelMap = new HashMap<Long, Label>();
        for (Label label : labels) {
            labelMap.put(label.getId(), label);
        }
        hql = "select l.id,ot" +
                " from Label l join l.origTranslations ot" +
                " where l.dictionary.id=:dictId and ot.language.id=:langId";
        param.put("langId", langId);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long labelId = ((Number) row[0]).longValue();
            LabelTranslation ot = (LabelTranslation) row[1];
            Label label = labelMap.get(labelId);
            if (label != null) {
                label.setOt(ot);
            }
        }
        hql = "select l.id,ct" +
                " from Label l join l.text.translations ct" +
                " where l.dictionary.id=:dictId and ct.language.id=:langId";
        qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long labelId = ((Number) row[0]).longValue();
            Translation ct = (Translation) row[1];
            Label label = labelMap.get(labelId);
            if (label != null) {
                label.setCt(ct);
            }
        }

        // populate default ct and ot values
        Iterator<Label> iter = labels.iterator();
        while (iter.hasNext()) {
            Label label = iter.next();
            if (label.getOt() == null) {
                LabelTranslation ot = new LabelTranslation();
                ot.setOrigTranslation(label.getReference());
                ot.setNeedTranslation(true);
                label.setOt(ot);
            }
            if (label.getCt() == null) {
                Translation ct = new Translation();
                ct.setId(-(label.getId() * 1000 + langId));    // virtual tid < 0, indicating a non-existing ct object
                ct.setTranslation(label.getReference());
                ct.setStatus(Translation.STATUS_UNTRANSLATED);
                label.setCt(ct);
            }
            // set status to Translated if no translation needed
            if (!label.getOt().isNeedTranslation() || label.getContext().getName().equals(Context.EXCLUSION)) {
                // duplicate an in-memory object to avoid database update
                Translation ct = new Translation();
                ct.setId(label.getCt().getId());
                ct.setTranslation(label.getOt().getOrigTranslation());
                ct.setStatus(Translation.STATUS_TRANSLATED);
                label.setCt(ct);
            }
        }
        return labels;
    }

    public Collection<Label> searchLabelsWithTranslation(Long prodId,
                                                         Long appId, Long dictId, Long langId, String text) {
        text = text.toUpperCase();
        String hql;
        Map param = new HashMap();

        // search text in reference
        if (dictId != null) {
            hql = "select obj,0,0 from Label obj where obj.dictionary.id=:dictId and obj.removed=false";
            param.put("dictId", dictId);
        } else if (appId != null) {
            hql = "select obj,0,a from Application a join a.dictionaries d join d.labels obj where a.id=:appId and obj.removed=false";
            param.put("appId", appId);
        } else if (prodId != null) {
            hql = "select obj,0,a from Product p join p.applications a join a.dictionaries d join d.labels obj where p.id=:prodId and obj.removed=false";
            param.put("prodId", prodId);
        } else {
            hql = "select obj,0,a from Application a join a.dictionaries d join d.labels obj where obj.removed=false";
        }
        hql += " and upper(obj.reference) like :text";
        param.put("text", "%" + text + "%");
        Collection<Object[]> result1 = dao.retrieve(hql, param);

        // search text in original translation
        param = new HashMap();
        param.put("languageId", langId);
        param.put("exclusion", Context.EXCLUSION);
        if (dictId != null) {
            hql = "select obj,lt,0 from Label obj join obj.origTranslations lt where obj.dictionary.id=:dictId and lt.language.id=:languageId and lt.needTranslation=false and obj.removed=false and obj.context.name<>:exclusion";
            param.put("dictId", dictId);
        } else if (appId != null) {
            hql = "select obj,lt,a from Application a join a.dictionaries d join d.labels obj join obj.origTranslations lt where a.id=:appId and lt.language.id=:languageId and lt.needTranslation=false and obj.removed=false and obj.context.name<>:exclusion";
            param.put("appId", appId);
        } else if (prodId != null) {
            hql = "select obj,lt,a from Product p join p.applications a join a.dictionaries d join d.labels obj join obj.origTranslations lt where p.id=:prodId and lt.language.id=:languageId and lt.needTranslation=false and obj.removed=false and obj.context.name<>:exclusion";
            param.put("prodId", prodId);
        } else {
            hql = "select obj,lt,a from Application a join a.dictionaries d join d.labels obj join obj.origTranslation lt where lt.language.id=:languageId and lt.needTranslation=false and obj.removed=false and obj.context.name<>:exclusion";
        }
        hql += " and lt.needTranslation=true and upper(lt.origTranslation) like :text";
        param.put("text", "%" + text + "%");
        Collection<Object[]> result2 = dao.retrieve(hql, param);


        // search text in translation
        param = new HashMap();
        param.put("languageId", langId);
        param.put("exclusion", Context.EXCLUSION);
        if (dictId != null) {
            hql = "select obj,t,0 from Label obj,Translation t where obj.dictionary.id=:dictId and obj.text=t.text and t.language.id=:languageId and obj.removed=false and obj.context.name<>:exclusion";
            param.put("dictId", dictId);
        } else if (appId != null) {
            hql = "select obj,t,a from Application a join a.dictionaries d join d.labels obj,Translation t where a.id=:appId and obj.text=t.text and t.language.id=:languageId and obj.removed=false and obj.context.name<>:exclusion";
            param.put("appId", appId);
        } else if (prodId != null) {
            hql = "select obj,t,a from Product p join p.applications a join a.dictionaries d join d.labels obj,Translation t where p.id=:prodId and obj.text=t.text and t.language.id=:languageId and obj.removed=false and obj.context.name<>:exclusion";
            param.put("prodId", prodId);
        } else {
            hql = "select obj,t,a from Application a join a.dictionaries d join d.labels obj,Translation t where obj.text=t.text and t.language.id=:languageId and obj.removed=false and obj.context.name<>:exclusion";
        }
        hql += " and upper(t.translation) like :text";
        param.put("text", "%" + text + "%");
        Collection<Object[]> result3 = dao.retrieve(hql, param);
        // filter out those needTranslation==false
        for (Iterator<Object[]> iter = result3.iterator(); iter.hasNext(); ) {
            Object[] row = iter.next();
            Label label = (Label) row[0];
            boolean needTranslation = false;
            boolean foundLt = false;
            if (label.getOrigTranslations() != null) {
                for (LabelTranslation lt : label.getOrigTranslations()) {
                    if (lt.getLanguage().getId().equals(langId)) {
                        foundLt = true;
                        if (lt.isNeedTranslation()) {
                            needTranslation = true;
                        }
                    }
                }
            }
            if (foundLt && !needTranslation) {    // all found label translation don't need translation
                iter.remove();
            }
        }

        // merge results
        TreeMap<String, Label> sortMap = new TreeMap<String, Label>();
        Collection<Object[]> results = new ArrayList<Object[]>();
        results.addAll(result2);
        results.addAll(result3);
        results.addAll(result1);
        for (Object[] row : results) {
            Label label = (Label) row[0];
            LabelTranslation ot = row[1] instanceof LabelTranslation ? (LabelTranslation) row[1] : null;
            Translation ct = row[1] instanceof Translation ? (Translation) row[1] : null;
            Application app = row[2] instanceof Application ? (Application) row[2] : null;
            String sortKey = (app == null ? "" : app.getName() + " " + app.getVersion())
                    + " " + label.getDictionary().getName() + " " + label.getDictionary().getVersion() + " "
                    + label.getSortNo() + " " + label.getId();
            Long virtualId = (-(label.getId() * 1000 + langId));    // virtual tid < 0, indicating a non-existing ct object
            if (ot != null) {    // found in origTranslation which doesn't need translation
                ct = new Translation();
                ct.setId(virtualId);
                ct.setTranslation(ot.getOrigTranslation());
                ct.setStatus(Translation.STATUS_TRANSLATED);
                label.setCt(ct);
            } else if (ct != null) {    // found in translation
                label.setCt(ct);
            } else {    // found in reference
                ct = new Translation();
                ct.setId(virtualId);
                if (label.getContext().getName().equals(Context.EXCLUSION)) {
                    ct.setTranslation(label.getReference());
                    ct.setStatus(Translation.STATUS_TRANSLATED);
                } else {
                    if (label.getOrigTranslations() != null) {
                        for (LabelTranslation lt : label.getOrigTranslations()) {
                            if (lt.getLanguage().getId().equals(langId)) {
                                ot = lt;
                                break;
                            }
                        }
                    }
                    if (ot != null && !ot.isNeedTranslation()) {
                        ct.setTranslation(label.getReference());
                        ct.setStatus(Translation.STATUS_TRANSLATED);
                    } else {
                        Translation trans = label.getText().getTranslation(langId);
                        if (trans != null) {
                            ct.setId(trans.getId());
                            ct.setTranslation(trans.getTranslation());
                            ct.setStatus(trans.getStatus());
                            ct.setTranslationType(trans.getTranslationType());
                            ct.setLastUpdateTime(trans.getLastUpdateTime());
                        } else {
                            ct.setTranslation(label.getReference());
                            ct.setStatus(Translation.STATUS_UNTRANSLATED);
                        }
                    }
                }
                label.setCt(ct);
            }
            label.setApp(app);
            if (!sortMap.containsKey(sortKey)) {
                sortMap.put(sortKey, label);
            }
        }
        return sortMap.values();
    }

    private Cell createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    @Override
    public List<LabelTranslation> getLabelTranslations(Long labelId,
                                                       Integer status) {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        Dictionary dict = label.getDictionary();
        List<LabelTranslation> result = new ArrayList<LabelTranslation>();
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                if (dl.isReference()) continue;  // skip reference language
                LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
                if (lt == null) {
                    lt = new LabelTranslation();
                    lt.setLabel(label);
                    lt.setOrigTranslation(label.getReference());
                    lt.setNeedTranslation(true);
                    lt.setSortNo(dl.getSortNo());
                    lt.setLanguageCode(dl.getLanguageCode());
                    lt.setLanguage(dl.getLanguage());
                }
                lt.setCt(label.getTranslationObject(dl));
                if (status == null || lt.getCt().getStatus() == status.intValue()) {
                    result.add(lt);
                }
            }
        }
        return result;
    }

}
