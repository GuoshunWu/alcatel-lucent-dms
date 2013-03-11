package com.alcatel_lucent.dms.service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Translation;

@Service("translationService")
public class TranslationServiceImpl extends BaseServiceImpl implements
		TranslationService {
	
	private static Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);
	
	@Autowired
	private LanguageService languageService;

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
    public Map<Long, Map<Long, int[]>> getDictTranslationSummaryByProdHQL(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	
    	// count labels for each dictionary
    	String hql = "select d.id,dl.language.id,count(distinct dl.languageCode),count(*)" +
    			" from Product p join p.applications a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
    			" where p.id=:prodId and dl.language.id<>1" +
    			" group by d.id,dl.language.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
    	for (Object[] row : qr) {
    		Long dictId = ((Number) row[0]).longValue();
    		Long langId = ((Number) row[1]).longValue();
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		// initial empty langMap
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {0, 0, 0});
    		labelCount.put(dictId, ((Number)row[3]).intValue() / factor);
    	}
    	
    	// count untranslated and in progress translations for each dictionary
    	hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
    			",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl" +
    			" join d.labels l join l.text.translations t" +
    			" where p.id=:prodId and t.language=dl.language and dl.language.id<>1" +
    			" and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
    			" and l.context.name<>:exclusion" +
    			" group by d.id,dl.language.id";
    	param.put("exclusion", Context.EXCLUSION);
    	qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {0, ((Number)row[3]).intValue() / factor, ((Number)row[4]).intValue() / factor});
    	}
    	
    	// in case of no Translation object associated
    	// count as untranslated
    	hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
    			",count(*) " +
    			" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
    			" where p.id=:prodId and dl.language.id<>1" +
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
     * @param prodId
     * @return
     */
    public Map<Long, Map<Long, int[]>> getDictTranslationSummaryByProd(final Long prodId) {
    	final Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	dao.getSession().doWork(new Work() {
    		public void execute(Connection connection)throws SQLException{
    			String sql = 
    					"SELECT D.ID,LANG.ID,COUNT(DISTINCT DL.LANGUAGE_CODE)," +
    					" SUM(CASE WHEN LT.NEED_TRANSLATION=false OR CTX.NAME='[EXCLUSION]' OR CT.STATUS=" + Translation.STATUS_TRANSLATED + " THEN 1 ELSE 0 END) T," +
    					" SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.ID IS NULL OR CT.STATUS=" + Translation.STATUS_UNTRANSLATED + ") THEN 1 ELSE 0 END) N," +
    					" SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.STATUS=" + Translation.STATUS_IN_PROGRESS + ") THEN 1 ELSE 0 END) I" +
    					" FROM dms.PRODUCT_APPLICATION PA" +
    					" JOIN dms.APPLICATION APP ON PA.APPLICATION_ID=APP.ID" +
    					" JOIN dms.APPLICATION_DICTIONARY AD ON AD.APPLICATION_ID=APP.ID" +
    					" JOIN dms.DICTIONARY D ON AD.DICTIONARY_ID=D.ID" +
    					" JOIN dms.DICTIONARY_LANGUAGE DL ON DL.DICTIONARY_ID = D.ID AND DL.LANGUAGE_ID <> 1" +
    					" JOIN dms.LANGUAGE LANG ON LANG.ID = DL.LANGUAGE_ID" +
    					" JOIN dms.LABEL L ON L.DICTIONARY_ID = D.ID" +
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
    				map.put(langId, new int[] {t, n, i});
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
    			" where a.id=:appId and dl.language.id<>1" +
    			" group by d.id,dl.language.id";
    	Map param = new HashMap();
    	param.put("appId", appId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
    	for (Object[] row : qr) {
    		Long dictId = ((Number) row[0]).longValue();
    		Long langId = ((Number) row[1]).longValue();
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		// initial empty langMap
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {0, 0, 0});
    		labelCount.put(dictId, ((Number)row[3]).intValue() / factor);
    	}
    	
    	// count untranslated and in progress translations for each dictionary
    	hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
    			",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Application a join a.dictionaries d join d.dictLanguages dl" +
    			" join d.labels l join l.text.translations t" +
    			" where a.id=:appId and t.language=dl.language and dl.language.id<>1" +
    			" and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
    			" and l.context.name<>:exclusion" +
    			" group by d.id,dl.language.id";
    	param.put("exclusion", Context.EXCLUSION);
    	qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long dictId = (Long) row[0];
    		Long langId = (Long) row[1];
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		Map<Long, int[]> langMap = result.get(dictId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(dictId, langMap);
    		}
    		langMap.put(langId, new int[] {0, ((Number)row[3]).intValue() / factor, ((Number)row[4]).intValue() / factor});
    	}
    	
    	// in case of no Translation object associated
    	// count as untranslated
    	hql = "select d.id,dl.language.id,count(distinct dl.languageCode)" +
    			",count(*) " +
    			" from Application a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
    			" where a.id=:appId and dl.language.id<>1" +
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
    			" where a.id=:appId and dl.language.id<>1" +
    			" group by dl.language.id";
    	Map param = new HashMap();
    	param.put("appId", appId);
    	Collection<Number> qr1 = dao.retrieve(hql, param);
    	for (Number langId : qr1) {
    		langMap.put(langId.longValue(), new int[] {0, 0, 0});
    	}
    	
    	// count untranslated and in progress translations for each app
    	hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
    			",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Application a join a.dictionaries d join d.dictLanguages dl" +
    			" join d.labels l join l.text.translations t" +
    			" where a.id=:appId and t.language=dl.language and dl.language.id<>1" +
    			" and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
    			" and l.context.name<>:exclusion" +
    			" group by a.id,dl.language.id";
    	param.put("exclusion", Context.EXCLUSION);
        Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
//    		Long appId = (Long) row[0];
    		Long langId = (Long) row[1];
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		langMap.put(langId, new int[] {0, ((Number)row[3]).intValue() / factor, ((Number)row[4]).intValue() / factor});
    	}
    	
    	// in case of no Translation object associated
    	// count as untranslated
    	hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
    			",count(*) " +
    			" from Application a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
    			" where a.id=:appId and dl.language.id<>1" +
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
    			" where p.id=:prodId" +
    			" group by a.id";
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	Map<Long, Integer> labelCount = new HashMap<Long, Integer>();
    	for (Object[] row : qr) {
    		Long appId = ((Number) row[0]).longValue();
    		labelCount.put(appId, ((Number)row[1]).intValue());
    	}
    	
		// initial empty langMap
    	hql = "select a.id,dl.language.id" +
    			" from Product p join p.applications a join a.dictionaries d join d.labels l join d.dictLanguages dl" +
    			" where p.id=:prodId and dl.language.id<>1" +
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
    		langMap.put(langId, new int[] {0, 0, 0});
    	}
    	
    	// count untranslated and in progress translations for each app
    	hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
    			",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl" +
    			" join d.labels l join l.text.translations t" +
    			" where p.id=:prodId and t.language=dl.language and dl.language.id<>1" +
    			" and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
    			" and l.context.name<>:exclusion" +
    			" group by a.id,dl.language.id";
    	param.put("exclusion", Context.EXCLUSION);
    	qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long appId = (Long) row[0];
    		Long langId = (Long) row[1];
    		int factor = ((Number) row[2]).intValue();	// number of lang codes for the same language, needed for division 
    		Map<Long, int[]> langMap = result.get(appId);
    		if (langMap == null) {
    			langMap = new HashMap<Long, int[]>();
    			result.put(appId, langMap);
    		}
    		langMap.put(langId, new int[] {0, ((Number)row[3]).intValue() / factor, ((Number)row[4]).intValue() / factor});
    	}
    	
    	// in case of no Translation object associated
    	// count as untranslated
    	hql = "select a.id,dl.language.id,count(distinct dl.languageCode)" +
    			",count(*) " +
    			" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join d.labels l" +
    			" where p.id=:prodId and dl.language.id<>1" +
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
     * @param prodId
     * @return
     */
    public Map<Long, Map<Long, int[]>> getAppTranslationSummaryJDBC(final Long prodId) {
    	final Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	dao.getSession().doWork(new Work() {
    		public void execute(Connection connection)throws SQLException{
    			String sql = 
    					"SELECT APP.ID,LANG.ID,COUNT(DISTINCT DL.LANGUAGE_CODE)," +
    					" SUM(CASE WHEN LT.NEED_TRANSLATION=false OR CTX.NAME='[EXCLUSION]' OR CT.STATUS=" + Translation.STATUS_TRANSLATED + " THEN 1 ELSE 0 END) T," +
    					" SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.ID IS NULL OR CT.STATUS=" + Translation.STATUS_UNTRANSLATED + ") THEN 1 ELSE 0 END) N," +
    					" SUM(CASE WHEN (LT.ID IS NULL OR LT.NEED_TRANSLATION=true) AND CTX.NAME<>'[EXCLUSION]' AND (CT.STATUS=" + Translation.STATUS_IN_PROGRESS + ") THEN 1 ELSE 0 END) I" +
    					" FROM dms.PRODUCT_APPLICATION PA" +
    					" JOIN dms.APPLICATION APP ON PA.APPLICATION_ID=APP.ID" +
    					" JOIN dms.APPLICATION_DICTIONARY AD ON AD.APPLICATION_ID=APP.ID" +
    					" JOIN dms.DICTIONARY D ON AD.DICTIONARY_ID=D.ID" +
    					" JOIN dms.DICTIONARY_LANGUAGE DL ON DL.DICTIONARY_ID = D.ID AND DL.LANGUAGE_ID <> 1" +
    					" JOIN dms.LANGUAGE LANG ON LANG.ID = DL.LANGUAGE_ID" +
    					" JOIN dms.LABEL L ON L.DICTIONARY_ID = D.ID" +
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
    				map.put(langId, new int[] {t, n, i});
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
    		result.put(label.getId(), new int[] {0, 0, 0});
    	}
    	
    	// count untranslated and in progress translations for each label
    	String hql = "select l.id" +
    			",sum(case when t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Dictionary d join d.dictLanguages dl" +
    			" join d.labels l join l.text.translations t" +
    			" where d.id=:dictId and t.language=dl.language and dl.language.id<>1" +
    			" and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false)" +
    			" and l.context.name<>:exclusion" +
    			" group by l.id";
    	Map param = new HashMap();
    	param.put("dictId", dictId);
    	param.put("exclusion", Context.EXCLUSION);
    	Collection<Object[]> qr = dao.retrieve(hql, param);
    	for (Object[] row : qr) {
    		Long labelId = ((Number) row[0]).longValue();
    		int[] values = new int[] {0, ((Number) row[1]).intValue(), ((Number) row[2]).intValue()};
    		result.put(labelId, values);
    	}
    	
    	// in case of no Translation object associated
    	// count as untranslated
    	hql = "select l.id,count(*) " +
    			" from Dictionary d join d.dictLanguages dl join d.labels l" +
    			" where d.id=:dictId and dl.language.id<>1" +
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
	    		if (dl.getLanguage().getId() != 1L) {
	    			total++;
	    		}
	    	}
    	}
		for (int[] values : result.values()) {
			values[0] = total - values[1] - values[2];
		}
		return result;
    }

	@Override
	public void generateDictTranslationReportByProd(Long prodId, Collection<Long> langIds, OutputStream output) {
		Map<Long, Map<Long, int[]>> data = getDictTranslationSummaryByProd(prodId);
		Collection<Language> languages = languageService.getLanguagesInProduct(prodId);
		if (langIds != null) {
			HashSet<Long> langIdSet = new HashSet<Long>(langIds);
			for (Iterator<Language> iter = languages.iterator(); iter.hasNext();) {
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
			for (Iterator<Language> iter = languages.iterator(); iter.hasNext();) {
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
					values = new int[] {0, 0, 0};
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
			for (Iterator<Language> iter = languages.iterator(); iter.hasNext();) {
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
			for (Iterator<Language> iter = languages.iterator(); iter.hasNext();) {
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
					values = new int[] {0, 0, 0};
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
		for (Long langId : langIds) {
			Language language = (Language) dao.retrieve(Language.class, langId);
			Sheet sheet = wb.createSheet(language.getName());
			Row headRow = sheet.createRow(0);
			createCell(headRow, 0, "Dictionary", null);
			createCell(headRow, 1, "Label", null);
			createCell(headRow, 2, "Context", null);
			createCell(headRow, 3, "Max Length", null);
			createCell(headRow, 4, "Reference", null);
			createCell(headRow, 5, "Translation", null);
			createCell(headRow, 6, "Description", null);
			int r = 1;
			for (Long dictId : dictIds) {
				Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
				Collection<Label> labels = getLabelsWithTranslation(dictId, langId);
				for (Label label : labels) {
					Row row = sheet.createRow(r++);
					createCell(row, 0, dict.getName(), null);
					createCell(row, 1, label.getKey(), null);
					createCell(row, 2, label.getContext().getName(), null);
					createCell(row, 3, label.getMaxLength(), null);
					createCell(row, 4, label.getReference(), null);
					createCell(row, 5, label.getCt().getTranslation(), null);
					createCell(row, 6, label.getDescription(), null);
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
	
	public Collection<Label> getLabelsWithTranslation(Long dictId, Long langId) {
		String hql = "select obj from Label obj where obj.dictionary.id=:dictId order by obj.sortNo";
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
				ct.setId(-(label.getId() * 1000 + langId));	// virtual tid < 0, indicating a non-existing ct object
				ct.setTranslation(label.getOt().getOrigTranslation());
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
				if (dl.getLanguage().getId() == 1) continue;	// skip reference language
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
				lt.setTranslation(label.getTranslation(dl.getLanguageCode()));
				lt.setStatus(label.getTranslationStatus(dl.getLanguageCode()));
				if (status == null || lt.getStatus().intValue() == status.intValue()) {
					result.add(lt);
				}
			}
		}
		return result;
	}

}
