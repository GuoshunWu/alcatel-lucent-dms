package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;

import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service("textService")
@SuppressWarnings("unchecked")
public class TextServiceImpl extends BaseServiceImpl implements TextService {

    private Logger log = LoggerFactory.getLogger(TextServiceImpl.class);

    @Autowired
    private GlossaryService glossaryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private DictionaryService dictionaryService;

    enum ExcelFileHeader {
        DICTIONARY, LABEL, MAX_LEN, REFERENCE, TRANSLATION
    }

    private static final Map<ExcelFileHeader, String> headerMap = new HashMap<ExcelFileHeader, String>();

    static {
        headerMap.put(ExcelFileHeader.DICTIONARY, "Dictionary");
        headerMap.put(ExcelFileHeader.LABEL, "Label");
        headerMap.put(ExcelFileHeader.MAX_LEN, "Max length");
        headerMap.put(ExcelFileHeader.REFERENCE, "Reference text");
        headerMap.put(ExcelFileHeader.TRANSLATION, "Translation");
    }

    public Context getContextByKey(String key) {
        return (Context) getDao().retrieveOne(
                "from Context where key=:key",
                JSONObject.fromObject(String.format("{'key':'%s'}",
                        key))
        );
    }


    public Text getText(Long ctxId, String reference) {
        Map params = new HashMap();
        params.put("reference", reference);
        params.put("contextid", ctxId);
        return (Text) dao.retrieveOne(
                "from Text where reference= :reference and context.id=:contextid",
                params);
    }

    public Text addText(Long ctxId, String reference) throws BusinessException {
        Context ctx = (Context) dao.retrieve(Context.class, ctxId);
        Text text = new Text();
        text.setContext(ctx);
        text.setReference(reference);
        text.setStatus(Text.STATUS_NOT_TRANSLATED);
        return (Text) dao.create(text, false);
    }

    public Translation getTranslation(Long ctxId, String reference, Long languageId) {
        Map param = new HashMap();
        param.put("ctxId", ctxId);
        param.put("reference", reference);
        param.put("languageId", languageId);
        return (Translation) dao.retrieveOne(
                "from translation " +
                        "where text.context.id=:ctxId " +
                        "and text.reference=:reference " +
                        "and language.id=:languageId", param
        );
    }

    public Text addTranslations(Long ctxId, String reference, Map<Long, String> translations) {
        Text text = getText(ctxId, reference);
        if (text == null) {
            text = addText(ctxId, reference);
        }
        return addTranslations(text.getId(), translations);
    }

    public Text addTranslations(Long textId, Map<Long, String> translations) {
        Text text = (Text) dao.retrieve(Text.class, textId);
        for (Long languageId : translations.keySet()) {
            Translation dbTrans = text.getTranslation(languageId);
            if (dbTrans == null) {
                Translation trans = new Translation();
                trans.setText(text);
                trans.setLanguage((Language) dao.retrieve(Language.class, languageId));
                trans.setTranslation(translations.get(languageId));
                dao.create(trans, false);
            } else {
                dbTrans.setTranslation(translations.get(languageId));
            }
        }
        dao.getSession().flush();
        return text;
    }

    public Map<String, Text> updateTranslations(Long ctxId, Collection<Text> texts, Constants.ImportingMode mode, int operationType) {
        Map<String, Text> result = new HashMap<String, Text>();
        Map<String, Text> dbTextMap = ctxId != null ? getTextsAsMap(ctxId, texts) : getTextsAsMapForLabelContext(texts);
        Context context = ctxId != null ? (Context) dao.retrieve(Context.class, ctxId) : null;
        // In delivery mode, try to translate labels in context "DEFAULT" and "DICT" automatically
        // by searching existing translation from other contexts
        if (ctxId != null) {
            log.info("Updating translations in context " + context.getName());
        }
    	if (mode == Constants.ImportingMode.DELIVERY &&
            (ctxId == null || context.getName().equals(Context.DEFAULT) || context.getName().equals(Context.DICT) ||
                    context.getName().equals(Context.LABEL))) {
    		suggestTranslations(texts, dbTextMap, ctxId == null);
    	}
        for (Text text : texts) {
            if (ctxId != null && result.containsKey(text.getReference())) {
                // ignore same reference for single context
                continue;
            }
            if (ctxId == null) {	// for LABEL context, context and dbTextMap is populated for each context
            	context = text.getContext();
            }
            
            Text dbText = dbTextMap.get(ctxId != null ? text.getReference() : context.getKey());
//            Text dbText = getText(ctxId, text.getReference());
            if (dbText == null) {
                dbText = addText(context.getId(), text.getReference());
            }
            HashSet<Long> langSet = new HashSet<Long>();
            if (text.getTranslations() != null) {
                for (Translation trans : text.getTranslations()) {
                    if (langSet.contains(trans.getLanguage().getId())) {
                        // ignore translation of same language
                        continue;
                    }
                    Translation dbTrans = dbText.getTranslation(trans.getLanguage().getId());
                    if (dbTrans == null) {
                        if (trans.getTranslationType() == null) {    // translation type is DICT by default
                            trans.setTranslationType(Translation.TYPE_DICT);
                        }
                        trans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                        dbTrans = addTranslation(dbText, trans);
                        // count diff translation
                        if (!trans.getTranslation().equals(text.getReference())) {
                        	dbText.addDiff(1);
                        }
                        dbText.addTranslation(dbTrans);        // the dbText will be used in next invoke, so add translations in-memory
                        historyService.addTranslationHistory(
                                dbTrans,
                                trans.getTranslationType() == Translation.TYPE_AUTO ? null : text.getRefLabel(),
                                trans.getTranslationType() == Translation.TYPE_AUTO ? TranslationHistory.TRANS_OPER_SUGGEST : operationType,
                                null);
                    } else if (mode == Constants.ImportingMode.TRANSLATION) { // update translations in TRANSLATION_MODE
                        if (trans.getTranslation() != null) {
                            // count diff translation
                        	if (!trans.getTranslation().equals(dbTrans.getTranslation())) {
                        		dbText.addDiff(1);
                        	}
                            dbTrans.setTranslation(trans.getTranslation());
                        }
                        dbTrans.setStatus(trans.getStatus());
                        // do not change translation src in case of capitalization
                        if (operationType != TranslationHistory.TRANS_OPER_CAPITALIZE) {
                            dbTrans.setTranslationType(trans.getTranslationType() != null ?
                                    trans.getTranslationType() : Translation.TYPE_TASK);
                        }
                        dbTrans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                        historyService.addTranslationHistory(dbTrans, text.getRefLabel(), operationType, null);
                    } else if (mode == Constants.ImportingMode.DELIVERY) {
                        // in DELIVERY_MODE, set status to UNTRANSLATED if translation is explicitly requested
//	                	if (dbTrans.getTranslation().equals(trans.getTranslation()) && 
//	                			!dbTrans.getTranslation().equals(text.getReference()) && 
//	                			trans.getStatus() == Translation.STATUS_UNTRANSLATED) {
//	                		dbTrans.setStatus(Translation.STATUS_UNTRANSLATED);
//	                	}
                        // update translation if got translated in delivered dict
                        if (trans.getStatus() == Translation.STATUS_TRANSLATED) {
                            // count diff translation
                        	if (!trans.getTranslation().equals(dbTrans.getTranslation())) {
                        		dbText.addDiff(1);
                        	}
                            int translationType = trans.getTranslationType() == null ? Translation.TYPE_DICT : trans.getTranslationType();
                            dbTrans.setTranslation(trans.getTranslation());
                            dbTrans.setStatus(Translation.STATUS_TRANSLATED);
                            dbTrans.setTranslationType(translationType);
                            dbTrans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                            historyService.addTranslationHistory(
                                    dbTrans,
                                    translationType == Translation.TYPE_AUTO ? null : text.getRefLabel(),
                                    translationType == Translation.TYPE_AUTO ? TranslationHistory.TRANS_OPER_SUGGEST : operationType,
                                    null);
                        }
                    } else {    // supplement mode
                        if (dbTrans.getStatus() != Translation.STATUS_TRANSLATED &&
                                trans.getStatus() == Translation.STATUS_TRANSLATED &&
                                !StringUtils.isBlank(trans.getTranslation())) {
                            // count diff translation
                        	if (!trans.getTranslation().equals(dbTrans.getTranslation())) {
                        		dbText.addDiff(1);
                        	}
                            dbTrans.setTranslation(trans.getTranslation());
                            dbTrans.setStatus(Translation.STATUS_TRANSLATED);
                            dbTrans.setTranslationType(trans.getTranslationType() != null ?
                                    trans.getTranslationType() : Translation.TYPE_DICT);
                            dbTrans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                            historyService.addTranslationHistory(dbTrans, null, TranslationHistory.TRANS_OPER_SUGGEST, null);
                        }
                    }
                    langSet.add(trans.getLanguage().getId());
                }
            }
            if (ctxId != null) {	// for single context, take reference as index
            	result.put(text.getReference(), dbText);
            } else {	// for multiple LABEL context, take context key as index
            	result.put(context.getKey(), dbText);
            }
        }
        historyService.flushHistoryQueue();
        return result;
    }

    /**
     * For new un-translated translation entries, try to match existing translation by searching text across all contexts
     * If matched, set status to T, translation type to "Auto" and translation text to the matched result
     *
     * @param texts
     * @param dbTextMap
     */
    private void suggestTranslations(Collection<Text> texts, Map<String, Text> dbTextMap, boolean labelContext) {
    	Collection<Translation> transNeedSuggestion = new ArrayList<Translation>();
    	HashSet<String> refNeedSuggestion = new HashSet<String>();
        for (Text text : texts) {
            if (org.springframework.util.CollectionUtils.isEmpty(text.getTranslations())) continue;

            Text dbText = dbTextMap.get(labelContext ? text.getContext().getKey() : text.getReference());
            for (Translation trans : text.getTranslations()) {
            	trans.setText(text);		// text is null in some case
                Translation dbTrans = dbText == null ? null : dbText.getTranslation(trans.getLanguage().getId());
                if (dbTrans == null || dbTrans.getStatus() == Translation.STATUS_UNTRANSLATED) {
                    if (trans.getLanguage().getId() != 0 && trans.getLanguage().getId() != 1L &&
                            trans.getStatus() == Translation.STATUS_UNTRANSLATED &&
                            (trans.getTranslation().equals(text.getReference()) || trans.getTranslation().trim().isEmpty())) {
                    	transNeedSuggestion.add(trans);
                    	refNeedSuggestion.add(text.getReference());
                    }
                }
            }
        }
        Map<String, Map<Long, String>> suggestion = getSuggestedTranslations(refNeedSuggestion);
        for (Translation trans : transNeedSuggestion) {
        	Map<Long, String> suggestionMap = suggestion.get(trans.getText().getReference());
        	if (suggestionMap != null) {
                String suggestedTranslation = suggestionMap.get(trans.getLanguage().getId());
                if (suggestedTranslation != null) {
                    log.info("Auto translate \"" + trans.getText().getReference() + "\" to \"" + suggestedTranslation + "\" in " + trans.getLanguage().getName() + ".");
                    trans.setTranslation(suggestedTranslation);
                    trans.setStatus(Translation.STATUS_TRANSLATED);
                    trans.setTranslationType(Translation.TYPE_AUTO);
                }
            }
        }
    }

    /**
     * Search text having translation by reference for all languages
     *
     * @param reference
     * @return
     */
    private Map<String, Map<Long, String>> getSuggestedTranslations(Collection<String> references) {
    	Map<String, Map<Long, String>> result = new HashMap<String, Map<Long, String>>();
        Collection<String> refs = new ArrayList<String>();
        for (Iterator<String> iter = references.iterator(); iter.hasNext(); ) {
            String reference = iter.next();
            refs.add(reference);
            if (refs.size() >= 100 || !iter.hasNext()) {    // execute query every 100 texts
                String hql = "from Translation where text.reference in(:reference) and status=:status order by text.context.id";
                Map param = new HashMap();
                param.put("status", Translation.STATUS_TRANSLATED);
                param.put("reference", refs);
                Collection<Translation> qr = dao.retrieve(hql, param);
                for (Translation trans : qr) {
                	if (StringUtils.isBlank(trans.getTranslation())) continue;	// don't match empty translation even if it's marked as T
                	Map<Long, String> suggestMap = result.get(trans.getText().getReference());
                	if (suggestMap == null) {
                		suggestMap = new HashMap<Long, String>();
                		result.put(trans.getText().getReference(), suggestMap);
                	}
                    if (!suggestMap.containsKey(trans.getLanguage().getId()) &&
                            !trans.getText().getContext().getName().equals(Context.EXCLUSION)) {
                    	suggestMap.put(trans.getLanguage().getId(), trans.getTranslation());
                    }
                }
                refs.clear();
            }
        }
        return result;
    }


    @Override
    public int receiveTranslation(String fileName, Long languageId) {
        //file is excel file
        FileInputStream inp = null;
        int rowCount = 0;
        try {
            inp = new FileInputStream(fileName);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            int columnCount = header.getLastCellNum();

            /**
             * the header in excel file need to be put into the  headerMap which is a member of this class.
             * */
            Map<String, Integer> cellIndexMap = new HashMap<String, Integer>();
            for (int i = 0; i < columnCount; ++i) {
                String value = header.getCell(i).getStringCellValue();
                cellIndexMap.put(value, i);
            }
            HashMap<String, Object> rowContainer = null;
            Row row;
            Set<String> headerTitles = cellIndexMap.keySet();
            for (int dataIndex = sheet.getFirstRowNum() + 1; (null != (row = sheet.getRow(dataIndex))); ++dataIndex) {
                rowContainer = new HashMap<String, Object>();
                for (String headerTitle : headerTitles) {
                    Cell cell = row.getCell(cellIndexMap.get(headerTitle));
                    if (null != cell) {
                        Object cellValue = cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? (int) (cell.getNumericCellValue()) : cell.getStringCellValue();
                        rowContainer.put(headerTitle, cellValue);
                    }
                }
                if (rowContainer.get(headerMap.get(ExcelFileHeader.DICTIONARY)) == null) {
                    break;
                }
                updateRow(rowContainer, languageId);
                rowCount++;
//                println(Util.jsonFormat(rowContainer.toString()));
            }
        } catch (FileNotFoundException e) {
            throw new BusinessException(BusinessException.FILE_NOT_FOUND, fileName);
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        } catch (InvalidFormatException e) {
            throw new BusinessException(BusinessException.INVALID_EXCEL_FILE, fileName);
        } finally {
            if (inp != null) try {
                inp.close();
            } catch (Exception e) {
            }
        }
        return rowCount;
    }

    public void updateTranslationStatus(Collection<Long> transIds, int transStatus) {
        for (Long id : transIds) {
            Translation trans = null;
            if (id > 0) {
                trans = (Translation) dao.retrieve(Translation.class, id);
                trans.setStatus(transStatus);
            } else {    // virtual tid = - (label_id * 1000 + language_id)
                id = -id;
                Long labelId = id / 1000;
                Long langId = id % 1000;
                Label label = (Label) dao.retrieve(Label.class, labelId);
                trans = label.getText().getTranslation(langId);
                if (trans == null) {
                    trans = new Translation();
                    trans.setTranslation(label.getReference());
                    trans.setLanguage((Language) dao.retrieve(Language.class, langId));
                    trans.setStatus(transStatus);
                    trans.setText(label.getText());
                    trans = (Translation) dao.create(trans);
                } else {
                    trans.setStatus(transStatus);
                }
            }
            historyService.addTranslationHistory(trans, null, TranslationHistory.TRANS_OPER_STATUS, null);
        }
        historyService.flushHistoryQueue();
    }

    public void updateTranslationStatusByDict(Collection<Long> dictIds, Collection<Long> langIds, int transStatus) {
        if (langIds != null && langIds.isEmpty()) return;
        String hql = "select dl.language,l.text from Dictionary d join d.dictLanguages dl join d.labels l" +
                " where dl.languageCode<>d.referenceLanguage and l.removed=false" +
                " and l.context.name<>:exclusion" +
                " and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language)" +
                " and d.id=:dictId";
        Map param = new HashMap();
        param.put("exclusion", Context.EXCLUSION);
        if (langIds != null) {
            hql += " and dl.language.id in (:langIds)";
            param.put("langIds", langIds);
        }
        for (Long dictId : dictIds) {
            Collection<Translation> qr = findAllTranslationsByDictAndLanguage(dictId, langIds);
            for (Translation trans : qr) {
                trans.setStatus(transStatus);
                historyService.addTranslationHistory(trans, null, TranslationHistory.TRANS_OPER_STATUS, null);
            }
            // create missing Translation objects
            param.put("dictId", dictId);
            Collection<Object[]> rows = dao.retrieve(hql, param);
            for (Object[] row : rows) {
                Language language = (Language) row[0];
                Text text = (Text) row[1];
                Translation translation = new Translation();
                translation.setLanguage(language);
                translation.setText(text);
                translation.setTranslation(text.getReference());
                translation.setStatus(transStatus);
                translation = (Translation) dao.create(translation);
                historyService.addTranslationHistory(translation, null, TranslationHistory.TRANS_OPER_STATUS, null);
            }
        }
        historyService.flushHistoryQueue();
    }

    public void updateTranslationStatusByApp(Collection<Long> appIds, Collection<Long> langIds, int status) {
        for (Long appId : appIds) {
//    		Collection<Translation> qr = findAllTranslationsByApp(appId);
//    		for (Translation trans : qr) {
//    			trans.setStatus(status);
//    		}
            Application app = (Application) dao.retrieve(Application.class, appId);
            Collection<Long> dictIds = new ArrayList<Long>();
            for (Dictionary dict : app.getDictionaries()) {
                dictIds.add(dict.getId());
            }
            if (!dictIds.isEmpty()) {
                updateTranslationStatusByDict(dictIds, langIds, status);
            }
        }
    }

    public void updateTranslationStatusByLabel(Collection<Long> labelIds, int transStatus) {
        for (Long labelId : labelIds) {
            Label label = (Label) dao.retrieve(Label.class, labelId);
            if (label.getContext().getName().equals(Context.EXCLUSION)) {
                continue;
            }
            if (label.getDictionary().getDictLanguages() != null) {
                Text text = label.getText();
                for (DictionaryLanguage dl : label.getDictionary().getDictLanguages()) {
                    if (dl.isReference()) {
                        continue;
                    }
                    LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
                    if (lt != null && !lt.isNeedTranslation()) continue;
                    Translation trans = text.getTranslation(dl.getLanguage().getId());
                    if (trans == null) {
                        // create missing translation
                        trans = new Translation();
                        trans.setLanguage(dl.getLanguage());
                        trans.setText(text);
                        trans.setTranslation(text.getReference());
                        trans.setStatus(transStatus);
                        trans = (Translation) dao.create(trans);
                    } else {
                        trans.setStatus(transStatus);
                    }
                    historyService.addTranslationHistory(trans, null, TranslationHistory.TRANS_OPER_STATUS, null);
                }
                historyService.flushHistoryQueue();
            }

        }
    }

    private Collection<Translation> findAllTranslationsByDictAndLanguage(Long dictId, Collection<Long> langIds) {
        String hql = "select s from Label l join l.text t join t.translations s" +
                " where l.dictionary.id=:dictId and l.removed=false and l.context.name<>:exclusion";
        Map param = new HashMap();
        param.put("dictId", dictId);
        param.put("exclusion", Context.EXCLUSION);
        if (langIds != null) {
            hql += " and s.language.id in (:langIds)";
            param.put("langIds", langIds);
        }
        return dao.retrieve(hql, param);
    }

    private Collection<Translation> findAllTranslationsByApp(Long appId) {
        String hql = "select t.translations from Application a join a.dictionaries d join d.labels l join l.text t" +
                " where a.id=:appId and l.removed=false and l.context.name<>:exclusion";
        Map param = new HashMap();
        param.put("appId", appId);
        param.put("exclusion", Context.EXCLUSION);
        return dao.retrieve(hql, param);
    }


    private void updateRow(HashMap<String, Object> rowContainer, Long languageId) {

        Context ctx = (Context) dao.retrieveOne("from Context where name = :name",
                JSONObject.fromObject("{'name':'" + rowContainer.get(headerMap.get(ExcelFileHeader.DICTIONARY)) + "'}"));
        if (null == ctx) {
            throw new BusinessException(BusinessException.CONTEXT_NOT_FOUND, rowContainer.get(headerMap.get(ExcelFileHeader.DICTIONARY)));
        }

        Map params = new HashMap();
        params.put("contextId", ctx.getId());
        params.put("reference", rowContainer.get(headerMap.get(ExcelFileHeader.REFERENCE)));
        Text text = (Text) dao.retrieveOne("from Text where context.id = :contextId and reference=:reference", params);

        if (null == text) {
            throw new BusinessException(BusinessException.TEXT_NOT_FOUND, params.get("reference"), ctx.getName());
        }
        Translation trans = text.getTranslation(languageId);
        String transString = rowContainer.get(headerMap.get(ExcelFileHeader.TRANSLATION)).toString().trim();
        if (null == trans) {
            trans = new Translation();
            trans.setText(text);
            Language language = (Language) dao.retrieve(Language.class, languageId);
            trans.setLanguage(language);
            trans.setTranslation(transString);
            trans.setStatus(Translation.STATUS_TRANSLATED);
            dao.create(trans);
        } else {
            trans.setTranslation(transString);
            trans.setStatus(Translation.STATUS_TRANSLATED);
        }

        //set memo
        Object maxLen = rowContainer.get(headerMap.get(ExcelFileHeader.MAX_LEN));
        if (null == maxLen || maxLen instanceof String && ((String) maxLen).isEmpty()) {
            return;
        }
        Integer[] maxLengths = null;
        if (maxLen instanceof Number) {
            maxLengths = new Integer[1];
            maxLengths[0] = (Integer) maxLen;
        } else {
            String[] maxLensString = maxLen.toString().split("[;, ]");
            maxLengths = new Integer[maxLensString.length];
            for (int i = 0; i < maxLensString.length; ++i) {
                maxLengths[i] = Integer.parseInt(maxLensString[i]);
            }
        }
        String warnings = "";
        if (!trans.isValidText()) {
            warnings += BusinessWarning.INVALID_TEXT;
        }
        String[] transStrings = transString.split("\n");
        for (int i = 0; i < transStrings.length; ++i) {
            if (maxLengths.length > i && transStrings[i].length() > maxLengths[i]) {
                if (!warnings.isEmpty()) {
                    warnings += ";";
                }
                warnings += BusinessWarning.EXCEED_MAX_LENGTH;
            }
        }

        trans.setWarnings(warnings);

    }

    /**
     * Create a persistent Translation object.
     *
     * @param text  persistent Text object
     * @param trans translation text
     * @return persistent Translation object
     */
    private Translation addTranslation(Text text, Translation trans) {
        Translation dbTrans = new Translation();
        dbTrans.setText(text);
        dbTrans.setLanguage((Language) dao.retrieve(Language.class, trans.getLanguage().getId()));
        dbTrans.setTranslation(trans.getTranslation());
        dbTrans.setStatus(trans.getStatus());
        dbTrans.setTranslationType(trans.getTranslationType());
        dbTrans.setLastUpdateTime(trans.getLastUpdateTime());
        dbTrans.setVerifyStatus(trans.getVerifyStatus());
        return (Translation) dao.create(dbTrans, false);
    }

    /**
     * Get all text objects in a context as map, indexed by reference.
     *
     * @param ctxId context id
     * @return text map with reference as key
     */
    public Map<String, Text> getTextsAsMap(Long ctxId) {
        return getTextAsMapForDict(ctxId, null);
    }

    private Map<String, Text> textCollectionToMap(Collection<Text> texts) {
        Map<String, Text> result = new HashMap<String, Text>();
        if (org.springframework.util.CollectionUtils.isEmpty(texts)) return result;
        for (Text text : texts) {
            result.put(text.getReference(), text);
        }
        return result;
    }

    public Map<String, Text> getTextAsMapForDict(Long ctxId, Dictionary dict) {
        String hql = "from Text where context.id=:ctxId";
        Map param = new HashMap();
        param.put("ctxId", ctxId);
        if (null == dict) {
            return textCollectionToMap(dao.retrieve(hql, param));
        }

        // dict is not null
        Map<String, Text> result = new HashMap<String, Text>();
        hql += " and reference in :references";
        int subListSize = 200;
        List<String> dictReferences = new ArrayList<String>(new HashSet<String>(CollectionUtils.collect(dict.getLabels(),
                TransformerUtils.invokerTransformer("getText"))));
        for (int fromIndex = 0; fromIndex < dictReferences.size(); fromIndex += subListSize) {
            int toIndex = fromIndex + subListSize;
            if (toIndex > dictReferences.size()) toIndex = dictReferences.size();
            param.put("references", dictReferences.subList(fromIndex, toIndex));
            result.putAll(textCollectionToMap(dao.retrieve(hql, param)));
        }
        return result;
    }

    /**
     * Get all text objects of specified texts in a context as map, indexed by reference.
     *
     * @param ctxId context id
     * @param texts transient text objects
     * @return
     */
    public Map<String, Text> getTextsAsMap(Long ctxId, Collection<Text> texts) {
        Map<String, Text> result = new HashMap<String, Text>();
        Collection<String> refs = new ArrayList<String>();
        for (Iterator<Text> iter = texts.iterator(); iter.hasNext(); ) {
            Text text = iter.next();
            refs.add(text.getReference());
            if (refs.size() >= 100 || !iter.hasNext()) {    // execute query every 100 texts
                String hql = "from Text where context.id=:ctxId and reference in (:refs) order by id desc";
                Map param = new HashMap();
                param.put("ctxId", ctxId);
                param.put("refs", refs);
                Collection<Text> qr = dao.retrieve(hql, param);
                for (Text t : qr) {
                    result.put(t.getReference(), t);
                }
                refs.clear();
            }
        }
        return result;
    }
    
    /**
     * Get all text objects of specified transient texts in LABEL context as a map, indexed by context key
     * @param texts
     * @return
     */
    private Map<String, Text> getTextsAsMapForLabelContext(Collection<Text> texts) {
    	Map<String, Text> result = new HashMap<String, Text>();
    	Collection<Long> ctxIds = new ArrayList<Long>();
    	for (Iterator<Text> iter = texts.iterator(); iter.hasNext();) {
    		Text text = iter.next();
    		ctxIds.add(text.getContext().getId());
    		if (ctxIds.size() >= 100 || !iter.hasNext()) {
    			String hql = "from Text where context.id in(:ctxIds) order by id desc";
    			Map param = new HashMap();
    			param.put("ctxIds", ctxIds);
    			Collection<Text> qr = dao.retrieve(hql, param);
    			for (Text t : qr) {
    				result.put(text.getContext().getKey(), t);
    			}
    			ctxIds.clear();
    		}
    	}
    	return result;
    }

    @Override
    public String getContextKeyByExpressionForLabel(String contextExp, Long dictId) {
        int index = contextExp.indexOf("[") + 1;
        return new StringBuffer(contextExp).insert(index, "DICT-" + dictId + "-").toString();
    }

    @Override
    public Context getContextByExpressionForLabel(String contextExp, Long dictId) {
        String contextKey = getContextKeyByExpressionForLabel(contextExp, dictId);
        Context context = getContextByKey(contextKey);
        if (context == null) {
            context = new Context();
            context.setKey(contextKey);
            context.setName(Context.LABEL);
            context = (Context) dao.create(context, false);
        }
        return context;
    }

    @Override
    public Context createContext(String name, String key) {
        Context context = new Context();
        context.setKey(key);
        context.setName(name);
        context = (Context) dao.create(context, false);
        return context;
    }

    @Override
    public Context getContextByExpression(String contextExp, Text text) {
        String contextKey = populateContextKey(contextExp, text);
        Context context = getContextByKey(contextKey);
        if (context == null) context = createContext(contextExp, contextKey);
        return context;
    }

    @Override
    public Context getContextByExpression(String contextExp, Dictionary dict) {
        String contextKey = populateContextKey(contextExp, dict);
        Context context = getContextByKey(contextKey);
        if (context == null) context = createContext(contextExp, contextKey);
        return context;
    }

    @Override
    public String populateContextKey(String contextExp, Text text) {
        if (null == text) return contextExp;
        return replaceVar(contextExp, Context.UNIQUE, "[UNIQUE-" + text.getId() + "]");
    }

    @Override
    public String populateContextKey(String contextExp, Label label) {
        if (null == label) return contextExp;
        return replaceVar(contextExp, Context.LABEL, "[LABEL-" + label.getKey() + "]");
    }

    public String populateContextKey(String contextExp, Dictionary dict) {
        if (dict != null) {
            contextExp = replaceVar(contextExp, Context.DICT, "[DICT-" + dict.getBase().getId() + "]");
            contextExp = replaceVar(contextExp, Context.APP, "[APP-" + dict.getBase().getApplicationBase().getId() + "]");
            contextExp = replaceVar(contextExp, Context.PROD, "[PROD-" + dict.getBase().getApplicationBase().getProductBase().getId() + "]");
        }
        return contextExp;
    }

    public String replaceVar(String exp, String from, String to) {
        int pos;
        while ((pos = exp.indexOf(from)) != -1) {
            exp = exp.substring(0, pos) + to + exp.substring(pos + from.length());
        }
        return exp;
    }


    @Override
    public Collection<String> updateTranslation(Long labelId,
                                                Long translationId, String translation, Boolean confirmAll) {
        Label label = (Label) dao.retrieve(Label.class, labelId);
        if (label.getContext().getName().equals(Context.EXCLUSION)) {
            throw new BusinessException(BusinessException.CANNOT_UPDATE_EXCLUSION);
        }
        Translation trans;
        if (translationId < 0) {    // proceed virtual id, create translation if necessary
            translationId = -translationId;
            Long langId = translationId % 1000;
            trans = label.getText().getTranslation(langId);
            if (trans == null) {
                trans = new Translation();
                trans.setTranslation(label.getReference());
                trans.setLanguage((Language) dao.retrieve(Language.class, langId));
                trans.setStatus(Translation.STATUS_UNTRANSLATED);
                trans.setText(label.getText());
                trans = (Translation) dao.create(trans, true);
            }
        } else {
            trans = (Translation) dao.retrieve(Translation.class, translationId);
        }
        if (!label.getText().getId().equals(trans.getText().getId())) {
            throw new BusinessException(BusinessException.INCONSISTENT_DATA);
        }
        Long langId = trans.getLanguage().getId();
        Collection<String> result = new TreeSet<String>();

        // consistent glossaries
        Collection<GlossaryMatchObject> GlossaryMatchObjects = glossaryService.getNotDirtyGlossaryPatterns();
        String reference = trans.getText().getReference();
        for (GlossaryMatchObject gmo : GlossaryMatchObjects) {
            gmo.getProcessedString(reference);
            if (!gmo.isReplaced()) continue;
            translation = gmo.getProcessedString(translation);
        }

        if (confirmAll != null && confirmAll) {    // confirm to update translation for all reference
            trans.setTranslation(translation);
            trans.setTranslationType(Translation.TYPE_MANUAL);
            trans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
            historyService.addTranslationHistory(trans, label, TranslationHistory.TRANS_OPER_INPUT, null);
        } else if (confirmAll != null && !confirmAll) {
            // change context to [LABEL] first
            Context context = getContextByExpressionForLabel("[LABEL-" + label.getKey() + "]", label.getDictionary().getId());
            Collection<Label> labels = new ArrayList<Label>();
            labels.add(label);
            dictionaryService.updateLabelContext(context, labels);

            Text text = getText(context.getId(), label.getReference());
            Translation newTrans = text.getTranslation(trans.getLanguage().getId());
            if (newTrans == null) {
                newTrans = new Translation();
                newTrans.setLanguage(trans.getLanguage());
                newTrans.setTranslation(translation);
                newTrans.setStatus(trans.getStatus());
                newTrans.setTranslationType(Translation.TYPE_MANUAL);
                newTrans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                newTrans = addTranslation(text, newTrans);
            } else {
                newTrans.setTranslation(translation);
                newTrans.setTranslationType(Translation.TYPE_MANUAL);
                newTrans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
            }
            historyService.addTranslationHistory(newTrans, label, TranslationHistory.TRANS_OPER_INPUT, null);
        } else {    // no confirm
            Dictionary dict = label.getDictionary();
            String hql = "select distinct d from Dictionary d join d.labels l join d.dictLanguages dl" +
                    " where dl.language.id=:langId and l.text.id=:textId and l.context.name<>:exclusion and d.id<>:dictId";
            Map param = new HashMap();
            param.put("langId", trans.getLanguage().getId());
            param.put("textId", label.getText().getId());
            param.put("exclusion", Context.EXCLUSION);
            param.put("dictId", dict.getId());
            Collection<Dictionary> dictList = dao.retrieve(hql, param);
            for (Dictionary otherDict : dictList) {
                result.add(otherDict.getName());
            }
            if (result.isEmpty()) {    // no confirmation needed, update translation directly
                trans.setTranslation(translation);
                trans.setTranslationType(Translation.TYPE_MANUAL);
                trans.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                historyService.addTranslationHistory(trans, label, TranslationHistory.TRANS_OPER_INPUT, null);
            } else {
                return result;
            }
        }
        // set needTranslation to true if translation text is manually updated
        if (label.getOrigTranslations() != null) {
            for (LabelTranslation lt : label.getOrigTranslations()) {
                if (lt.getLanguage().getId().equals(langId)) {
                    lt.setNeedTranslation(true);
                }
            }
        }
        historyService.flushHistoryQueue();
        return result;
    }


    @Override
    public void populateTranslationSummary(Collection<Text> texts) {
        for (Text text : texts) {
            text.populateTranslationSummary();
        }
    }


    @Override
    public void populateRefs(Collection<Text> texts) {
        if (texts.isEmpty()) return;
        Collection<Long> ids = new ArrayList<Long>();
        Map<Long, Text> textMap = new HashMap<Long, Text>();
        for (Text text : texts) {
            ids.add(text.getId());
            textMap.put(text.getId(), text);
        }
        String hql = "select text.id,count(*) from Label where text.id in (:ids) and removed=false group by text.id";
        Map param = new HashMap();
        param.put("ids", ids);
        Collection<Object[]> qr = dao.retrieve(hql, param);
        for (Object[] row : qr) {
            Long textId = ((Number) row[0]).longValue();
            int count = ((Number) row[1]).intValue();
            Text text = textMap.get(textId);
            text.setRefs(count);
        }
    }

    public void deleteText(Long textId) {
        String hql = "select count(*) from Label where text.id=:textId and removed=false";
        Map param = new HashMap();
        param.put("textId", textId);
        Number refs = (Number) dao.retrieveOne(hql, param);
        if (refs.intValue() > 0) {
            throw new BusinessException(BusinessException.TEXT_HAS_REFS);
        }
        dao.delete(Text.class, textId);
    }


	@Override
	public Collection<Text> getDiffTexts(Long textId) {
		Text text = (Text) dao.retrieve(Text.class, textId);
		String hql = "from Text where reference=:ref and context.id<>:contextId";
		Map param = new HashMap();
		param.put("ref", text.getReference());
		param.put("contextId", text.getContext().getId());
		Collection<Text> qr = dao.retrieve(hql, param);
		for (Text diffText : qr) {
			diffText.setDiff(countDiffTranslations(text, diffText));
		}
		return qr;
	}


	private int countDiffTranslations(Text text1, Text text2) {
		int count = 0;
		if (text1.getTranslations() != null) {
			for (Translation trans1 : text1.getTranslations()) {
				Translation trans2 = text2.getTranslation(trans1.getLanguage().getId());
				if (trans2 != null && (
						!trans1.getTranslation().equals(trans2.getTranslation()) 
						|| trans1.getStatus() != trans2.getStatus())) {
					count++;
				}
			}
		}
		return count;
	}


	@Override
	public Collection<Translation[]> findDiffTranslations(Long textId1, Long textId2) {
		Text text1 = (Text) dao.retrieve(Text.class, textId1);
		Text text2 = (Text) dao.retrieve(Text.class, textId2);
		Collection<Translation[]> result = new ArrayList<Translation[]>();
		if (text1.getTranslations() != null) {
			for (Translation trans1 : text1.getTranslations()) {
				Translation trans2 = text2.getTranslation(trans1.getLanguage().getId());
				if (trans2 != null && (
						!trans1.getTranslation().equals(trans2.getTranslation()) 
						|| trans1.getStatus() != trans2.getStatus())) {
					result.add(new Translation[] {trans1, trans2});
				}
			}
		}
		return result;
	}
}
