package com.alcatel_lucent.dms.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.util.Util;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

@Service("textService")
@SuppressWarnings("unchecked")
public class TextServiceImpl extends BaseServiceImpl implements TextService {

    enum ExcelFileHeader{
        DICTIONARY,LABEL, MAX_LEN,REFERENCE,TRANSLATION
    }
    private static final Map<ExcelFileHeader,String> headerMap=new HashMap<ExcelFileHeader, String>();
    static{
        headerMap.put(ExcelFileHeader.DICTIONARY,"Dictionary");
        headerMap.put(ExcelFileHeader.LABEL,"Label");
        headerMap.put(ExcelFileHeader.MAX_LEN,"Max length");
        headerMap.put(ExcelFileHeader.REFERENCE,"Reference text");
        headerMap.put(ExcelFileHeader.TRANSLATION,"Translation");
    }
    
    public Context getContextByName(String name) {
        return (Context) getDao().retrieveOne(
                "from Context where name=:name",
                JSONObject.fromObject(String.format("{'name':'%s'}",
                        name)));
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
                        "and language.id=:languageId", param);
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

    public Map<String, Text> updateTranslations(Long ctxId, Collection<Text> texts, int mode) {
        Map<String, Text> result = new HashMap<String, Text>();
        Map<String, Text> dbTextMap = getTextsAsMap(ctxId);
        for (Text text : texts) {
            if (result.containsKey(text.getReference())) {
                // ignore same reference
                continue;
            }
            Text dbText = dbTextMap.get(text.getReference());
            if (dbText == null) {
                dbText = addText(ctxId, text.getReference());
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
						dbTrans = addTranslation(dbText, trans);
	                } else if (mode == Constants.TRANSLATION_MODE) { // update translations only in TRANSLATION_MODE
	                    dbTrans.setTranslation(trans.getTranslation());
						dbTrans.setStatus(trans.getStatus());
	                }
	                langSet.add(trans.getLanguage().getId());
	            }
            }
            result.put(text.getReference(), dbText);
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
            JSONObject rowContainer = null;
            Row row;
            Set<String> headerTitles = cellIndexMap.keySet();
            for (int dataIndex = sheet.getFirstRowNum() + 1; (null != (row = sheet.getRow(dataIndex))); ++dataIndex) {
                rowContainer = new JSONObject();
                for (String headerTitle : headerTitles) {
                    Cell cell = row.getCell(cellIndexMap.get(headerTitle));
                    if (null != cell) {
                        Object cellValue = cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? (int)(cell.getNumericCellValue()) : cell.getStringCellValue();
                        rowContainer.put(headerTitle, cellValue);
                    }
                }
                updateRow(rowContainer,languageId);
                rowCount++;
//                println(Util.jsonFormat(rowContainer.toString()));
            }
            inp.close();
        } catch (FileNotFoundException e) {
            throw new BusinessException(BusinessException.FILE_NOT_FOUND, fileName);
        } catch (IOException e) {
            throw new SystemError(e.getMessage());
        } catch (InvalidFormatException e) {
            throw new BusinessException(BusinessException.INVALID_EXCEL_FILE, fileName);
        }
        return rowCount;
    }

    private void updateRow(JSONObject rowContainer, Long languageId) {

        Context ctx= (Context) dao.retrieveOne("from Context where name = :name",
                JSONObject.fromObject("{'name':'" + rowContainer.get(headerMap.get(ExcelFileHeader.DICTIONARY)) + "'}"));
        if (null == ctx) {
            throw new BusinessException(BusinessException.CONTEXT_NOT_FOUND, rowContainer.get(headerMap.get(ExcelFileHeader.DICTIONARY)));
        }

        Map params=new HashMap();
        params.put("contextId", ctx.getId());
        params.put("reference", rowContainer.get(headerMap.get(ExcelFileHeader.REFERENCE)));
        Text text=(Text)dao.retrieveOne("from Text where context.id = :contextId and reference=:reference",params);

        if(null == text){
            throw new BusinessException(BusinessException.TEXT_NOT_FOUND, params.get("reference"),ctx.getName());
        }
        Translation trans=text.getTranslation(languageId);
        String transString=rowContainer.get(headerMap.get(ExcelFileHeader.TRANSLATION)).toString().trim();
        if(null==trans){
            trans = new Translation();
            trans.setText(text);
            Language language= (Language) dao.retrieve(Language.class, languageId);
            trans.setLanguage(language);
            trans.setTranslation(transString);
        }else{
            trans.setTranslation(transString);
        }

        //set memo
        Object maxLen=rowContainer.get(headerMap.get(ExcelFileHeader.MAX_LEN));
        if(null==maxLen || maxLen instanceof String &&((String)maxLen).isEmpty()){
            return;
        }
        Integer[] maxLengths=null;
        if(maxLen instanceof Number){
            maxLengths=new Integer[1];
            maxLengths[0]= (Integer) maxLen;
        } else{
            String[] maxLensString=maxLen.toString().split("[;, ]");
            maxLengths = new Integer[maxLensString.length];
            for(int i=0;i<maxLensString.length; ++i) {
                maxLengths[i]=Integer.parseInt(maxLensString[i]);
            }
        }
        String warnings="";
        if(!trans.isValidText()){
        	warnings += BusinessWarning.INVALID_TEXT;
        }
        String[] transStrings=transString.split("\n");
        for(int i=0;i<transStrings.length; ++i){
            if(transStrings[i].length()>maxLengths[i]){
                if(!warnings.isEmpty()){
                	warnings+=";";
                }
                warnings+=BusinessWarning.EXCEED_MAX_LENGTH;
            }
        }

        trans.setWarnings(warnings);
        
    }

    /**
     * Create a persistent Translation object.
     *
     * @param text            persistent Text object
     * @param languageId      language id
     * @param translationText translation text
     * @param memo translation memo
     * @return persistent Translation object
     */
	private Translation addTranslation(Text text, Translation trans) {
        trans.setText(text);
		trans.setLanguage((Language) dao.retrieve(Language.class, trans.getLanguage().getId()));
        return (Translation) dao.create(trans, false);
    }

    /**
     * Get all text objects in a context as map, indexed by reference.
     *
     * @param ctxId context id
     * @return text map with reference as key
     */
    private Map<String, Text> getTextsAsMap(Long ctxId) {
        String hql = "from Text where context.id=:ctxId";
        Map param = new HashMap();
        param.put("ctxId", ctxId);
        Collection<Text> texts = dao.retrieve(hql, param);
        Map<String, Text> result = new HashMap<String, Text>();
        for (Text text : texts) {
            result.put(text.getReference(), text);
        }
        return result;
    }

    private void println(Object obj) {
        System.out.println(obj);
    }

}
