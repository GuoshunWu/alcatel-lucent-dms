package com.alcatel_lucent.dms.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.model.TaskDetail;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.Util;

@Service("taskService")
public class TaskServiceImpl extends BaseServiceImpl implements TaskService {
	
	private static Logger log = Logger.getLogger(TaskServiceImpl.class);
	
	private static final String PROTECT_PASSWORD = "alcatel123";

	@Autowired
	private LanguageService langService;
	 
	@Override
	public Task createTask(Long productId, String name,
			Collection<Long> dictIds, Collection<Long> languageIds) {
		Task task = new Task();
		task.setName(name);
		task.setProduct((Product) dao.retrieve(Product.class, productId));
		task.setCreateTime(new Date());
		task.setStatus(Task.STATUS_OPEN);
		task = (Task) dao.create(task);
		
		log.info("Preparing translation task details...");
		String hql = "select distinct dl.language,l.text,l.key,l.maxLength,l.description,ct " +
				"from Dictionary d join d.labels l join d.dictLanguages dl " +
				"left join l.origTranslations lt " +
				"left join l.text.translations ct " +
				"where lt.language=dl.language and ct.language=dl.language " +
				"and d.id in (:dictIds) and dl.language.id in (:langIds) " +
				"and (lt is null or lt.needTranslation=1) " +
				"and (ct is null or ct.status=:status) " +
				"order by dl.language.id,l.context.id,l.sortNo";
		Map param = new HashMap();
		param.put("dictIds", dictIds);
		param.put("langIds", languageIds);
		param.put("status", Translation.STATUS_UNTRANSLATED);
		Collection<Object[]> resultSet = dao.retrieve(hql, param);
		log.info("Creating " + resultSet.size() + " task details...");
		Collection<Translation> newTransList = new ArrayList<Translation>();
		HashSet<String> unique = new HashSet<String>();	// unique by language and text
		if (resultSet != null) {
			for (Object[] row : resultSet) {
				Language language = (Language) row[0];
				Text text = (Text) row[1];
				String key = language.getId() + "," + text.getId();
				if (unique.contains(key)) continue;
				unique.add(key);
				String labelKey = (String) row[2];
				String maxLength = (String) row[3];
				String description = (String) row[4];
				Translation translation = (Translation) row[5];
				if (translation == null) {
					translation = new Translation();
					translation.setText(text);
					translation.setLanguage(language);
					translation.setTranslation(text.getReference());
					translation.setStatus(Translation.STATUS_IN_PROGRESS);
					newTransList.add(translation);
				} else {
					translation.setStatus(Translation.STATUS_IN_PROGRESS);
				}
				TaskDetail td = new TaskDetail();
				td.setTask(task);
				td.setLanguage(language);
				td.setText(text);
				td.setOrigTranslation(translation.getTranslation());
				td.setLabelKey(labelKey);
				td.setMaxLength(maxLength);
				td.setDescription(description);
				dao.create(td, false);
			}
		}
		for (Translation trans : newTransList) {
			dao.create(trans);
		}

		return task;
	}

	@Override
	public void closeTask(Long taskId) throws BusinessException {
		Task task = (Task) dao.retrieve(Task.class, taskId);
		if (task.getStatus() == Task.STATUS_CLOSED) {
			throw new BusinessException(BusinessException.INVALID_TASK_STATUS);
		}
		task.setStatus(Task.STATUS_CLOSED);
		String hql = "select ct from Translation ct,Task t join t.details td " +
				"where td.text=ct.text and td.language=ct.language " +
				"and t.id=:taskId";
		Map param = new HashMap();
		param.put("taskId", taskId);
		Collection<Translation> transList = dao.retrieve(hql, param);
		for (Translation trans : transList) {
			if (trans.getStatus() == Translation.STATUS_IN_PROGRESS) {
				trans.setStatus(Translation.STATUS_UNTRANSLATED);
			}
		}
	}

	@Override
	public void generateTaskFiles(String targetDir, Long taskId) {
		Task task = (Task) dao.retrieve(Task.class, taskId);
		ArrayList<TaskDetail> currentDetails = new ArrayList<TaskDetail>();
		String currentLanguage = null;
		String currentContext = null;
		ArrayList<String> existingFilenames = new ArrayList<String>();
		int labelCount = 0, wordCount = 0;
		Map<String, Integer> labelCountMap = new TreeMap<String, Integer>();
		Map<String, Integer> wordCountMap = new TreeMap<String, Integer>();
		for (TaskDetail td : task.getDetails()) {
			String languageName = td.getLanguage().getName();
			String contextName = td.getText().getContext().getName();
			if ((currentLanguage != null && !currentLanguage.equals(languageName)) || 
					(currentContext != null && !currentContext.equals(contextName))) {
				generateTaskFile(targetDir, currentLanguage, currentContext, currentDetails, existingFilenames);
				currentDetails.clear();
				if (currentLanguage != null && !currentLanguage.equals(languageName)) {
					labelCountMap.put(currentLanguage, labelCount);
					wordCountMap.put(currentLanguage, wordCount);
					labelCount = wordCount = 0;
					existingFilenames.clear();
				}
			}
			currentLanguage = languageName;
			currentContext = contextName;
			currentDetails.add(td);
			labelCount++;
			wordCount += Util.countWords(td.getText().getReference());
		}
		if (currentDetails.size() > 0) {
			generateTaskFile(targetDir, currentLanguage, currentContext, currentDetails, existingFilenames);
		}
		
		// TODO create statistics file
	}

	private void generateTaskFile(String targetDir, String languageName,
			String contextName, ArrayList<TaskDetail> taskDetails, ArrayList<String> existingFilenames) {
		File dir = new File(targetDir, languageName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// convert context name to target file name
		// if names conflict after the convertion, add number suffix
		String filename = toFilename(contextName);
		String tempname = filename;
		int i = 2;
		while (existingFilenames.contains(tempname)) {
			tempname = filename + "_" + i++;
		}
		existingFilenames.add(tempname);
		filename = tempname + ".xls";
		Workbook wb = new HSSFWorkbook();
		Font fontHead = wb.createFont();
		fontHead.setFontHeightInPoints((short)10);
		fontHead.setFontName("Arial Unicode MS");
		fontHead.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    CellStyle styleHead = wb.createCellStyle();
	    styleHead.setFont(fontHead);
		Font fontBody = wb.createFont();
		fontBody.setFontHeightInPoints((short)10);
		fontBody.setFontName("Arial Unicode MS");
	    CellStyle styleBody = wb.createCellStyle();
	    styleBody.setFont(fontBody);
	    CellStyle styleUnlockedBody = wb.createCellStyle();
	    styleUnlockedBody.setFont(fontBody);
	    styleUnlockedBody.setLocked(false);
		Sheet sheet = wb.createSheet(languageName);
		sheet.createFreezePane( 0, 1, 0, 1 );
		Row row = sheet.createRow(0);
		createCell(row, 0, "Label", styleHead);
		createCell(row, 1, "Context", styleHead);
		createCell(row, 2, "Max length", styleHead);
		createCell(row, 3, "Description", styleHead);
		createCell(row, 4, "Reference text", styleHead);
		createCell(row, 5, "Translation", styleHead);
		createCell(row, 6, "Remarks", styleHead);
		sheet.setColumnWidth(4, 40 * 256);
		sheet.setColumnWidth(5, 40 * 256);
		sheet.setColumnWidth(6, 20 * 256);
		short rowNo = 1;
		for (TaskDetail td : taskDetails) {
			row = sheet.createRow(rowNo++);
			createCell(row, 0, td.getLabelKey(), styleBody);
			createCell(row, 1, contextName, styleBody);
			if (td.getMaxLength() != null) {
				createCell(row, 2, td.getMaxLength(), styleBody);
			}
			if (td.getDescription() != null) {
				createCell(row, 3, td.getDescription(), styleBody);
			}
			createCell(row, 4, td.getText().getReference(), styleBody);
			createCell(row, 5, td.getNewTranslation() == null ? td.getOrigTranslation() : td.getNewTranslation(), styleUnlockedBody);
			createCell(row, 6, "", styleUnlockedBody);
		}
		sheet.protectSheet(PROTECT_PASSWORD);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dir, filename));
			wb.write(fos);
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			throw new SystemError(e);
		} finally {
			if (fos != null) try {fos.close();} catch (Exception e) {}
		}
		
	}

	private Cell createCell(Row row, int column, String value, CellStyle style) {
		Cell cell = row.createCell(column);
		cell.setCellStyle(style);
		cell.setCellValue(value);
		return cell;
	}

	/**
	 * Convert context name to a valid filename, by removing leading folders
	 * and converting \ / : * ? " < > | to _
	 * @param contextName
	 * @return
	 */
	private String toFilename(String contextName) {
		int pos1 = contextName.lastIndexOf('/');
		int pos2 = contextName.lastIndexOf('\\');
		int pos = Math.max(pos1, pos2);
		if (pos != -1 && pos != contextName.length() - 1) {
			contextName = contextName.substring(pos + 1);
		}
		contextName = contextName.replace('\\', '_');
		contextName = contextName.replace('/', '_');
		contextName = contextName.replace(':', '_');
		contextName = contextName.replace('*', '_');
		contextName = contextName.replace('?', '_');
		contextName = contextName.replace('\"', '_');
		contextName = contextName.replace('<', '_');
		contextName = contextName.replace('>', '_');
		contextName = contextName.replace('|', '_');
		return contextName;
	}
}
