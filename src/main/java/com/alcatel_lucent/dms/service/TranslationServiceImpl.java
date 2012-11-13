package com.alcatel_lucent.dms.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Translation;

@Service("translationService")
public class TranslationServiceImpl extends BaseServiceImpl implements
		TranslationService {
	
	private static Logger log = Logger.getLogger(TranslationServiceImpl.class);
	
	@Autowired
	private LanguageService languageService;

	/*    
    public Map<Long, int[]> getDictTranslationSummary(Long dictId) {
    	Map<Long, int[]> result = new HashMap<Long, int[]>();
    	Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
    	String hql = "select ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
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
    
    public Map<Long, Map<Long, int[]>> getDictTranslationSummary(Long prodId) {
    	Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
    	String hql = "select d.id,ot.language.id" +
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
    			" from Product p join p.applications a join a.dictionaries d" +
    			" join d.labels l join l.origTranslations ot join l.text.translations t" +
    			" where p.id=:prodId and ot.language=t.language" +
    			" group by d.id,ot.language.id";
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
    			",sum(case when ot.needTranslation=0 or t.status=" + Translation.STATUS_TRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_UNTRANSLATED + " then 1 else 0 end) " +
    			",sum(case when ot.needTranslation=1 and t.status=" + Translation.STATUS_IN_PROGRESS + " then 1 else 0 end) " +
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

	@Override
	public void generateDictTranslationReport(Long prodId, OutputStream output) {
		Map<Long, Map<Long, int[]>> data = getDictTranslationSummary(prodId);
		Collection<Language> languages = languageService.getLanguagesInProduct(prodId);
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		Row headRow1 = sheet.createRow(0);
		Row headRow2 = sheet.createRow(1);
		int col = 0;
		headRow1.createCell(col).setCellValue("Application");
		sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
		headRow1.createCell(++col).setCellValue("App version");
		sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
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
			createCell(row, col++, dict.getApp().getName(), null);
			createCell(row, col++, dict.getApp().getVersion(), null);
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
			log.error(e);
			throw new SystemError(e);
		}
	}

	@Override
	public void generateAppTranslationReport(Long prodId, OutputStream output) {
		Map<Long, Map<Long, int[]>> data = getAppTranslationSummary(prodId);
		
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

}