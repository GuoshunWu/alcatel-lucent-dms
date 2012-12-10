package com.alcatel_lucent.dms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.Context;
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
	
	@Autowired
	private TextService textService;
	
	enum ExcelFileHeader{
        LABEL, CONTEXT, MAX_LEN, DESCRIPTION, REFERENCE,TRANSLATION, REMARKS
    }
	private static final Map<ExcelFileHeader,String> headerMap=new HashMap<ExcelFileHeader, String>();
    static{
    	
        headerMap.put(ExcelFileHeader.LABEL, "Label");
        headerMap.put(ExcelFileHeader.CONTEXT, "Context");
        headerMap.put(ExcelFileHeader.MAX_LEN, "Max length");
        headerMap.put(ExcelFileHeader.DESCRIPTION, "Description");
        headerMap.put(ExcelFileHeader.REFERENCE, "Reference text");
        headerMap.put(ExcelFileHeader.TRANSLATION, "Translation");
        headerMap.put(ExcelFileHeader.REMARKS, "Remarks");
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public Task createTask(Long productId, String name,
			Collection<Long> dictIds, Collection<Long> languageIds) {
		Task task = new Task();
		task.setName(name);
		task.setProduct((Product) dao.retrieve(Product.class, productId));
		task.setCreateTime(new Date());
		task.setCreator(UserContext.getInstance().getUser());
		task.setStatus(Task.STATUS_OPEN);
		task = (Task) dao.create(task);
		
		log.info("Preparing translation task details...");
		String hql = "select distinct dl.language,l.text,l.key,l.maxLength,l.description,ct,l.context,l.sortNo " +
				"from Dictionary d join d.labels l join d.dictLanguages dl " +
				"join l.text.translations ct " +
				"where d.id in (:dictIds) and dl.language.id in (:langIds) and ct.language=dl.language and ct.status=:status " +
				"and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
				"order by dl.language.id,l.context.id,l.sortNo";
		Map param = new HashMap();
		param.put("dictIds", dictIds);
		param.put("langIds", languageIds);
		param.put("status", Translation.STATUS_UNTRANSLATED);
		Collection<Object[]> resultSet = dao.retrieve(hql, param);
		hql = "select distinct dl.language,l.text,l.key,l.maxLength,l.description,l.context,l.sortNo " +
				"from Dictionary d join d.labels l join d.dictLanguages dl " +
				"where d.id in (:dictIds) and dl.language.id in (:langIds) " +
				"and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
				"and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) ";
		param = new HashMap();
		param.put("dictIds", dictIds);
		param.put("langIds", languageIds);
		resultSet.addAll(dao.retrieve(hql,param));
		ArrayList<Object[]> sortedDetails = new ArrayList<Object[]>(resultSet);
		
		// Sort task details by language,context,label.sortNo
		// The order is important because of the way of generating files
		Collections.sort(sortedDetails, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				Long langId1 = ((Language) o1[0]).getId();
				Long ctxId1 = ((Text) o1[1]).getContext().getId();
				Long langId2 = ((Language) o2[0]).getId();
				Long ctxId2 = ((Text) o2[1]).getContext().getId();
				if (langId1 != langId2) {
					return langId1 > langId2 ? 1 : -1;
				} else {
					if (ctxId1 != ctxId2) {
						return ctxId1 > ctxId2 ? 1 : -1;
					} else {
						return 0;
					}
				}
			}
			
		});
		log.info("Creating " + sortedDetails.size() + " task details...");
		Collection<Translation> newTransList = new ArrayList<Translation>();
		HashSet<String> unique = new HashSet<String>();	// unique by language and text
		if (sortedDetails != null && sortedDetails.size() > 0) {
			for (Object[] row : sortedDetails) {
				Language language = (Language) row[0];
				Text text = (Text) row[1];
				String key = language.getId() + "," + text.getId();
				if (unique.contains(key)) continue;
				unique.add(key);
				String labelKey = (String) row[2];
				String maxLength = (String) row[3];
				String description = (String) row[4];
				Translation translation = row[5] instanceof Translation ? (Translation) row[5] : null;
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
		} else {
			throw new BusinessException(BusinessException.EMPTY_TASK);
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
		task.setCloseTime(new Date());
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
		log.info("Generate task files to " + targetDir + ", id=" + taskId);
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
			labelCountMap.put(currentLanguage, labelCount);
			wordCountMap.put(currentLanguage, wordCount);
		}
		
		// create statistics file
		generateStatisticsFile(targetDir, labelCountMap, wordCountMap);
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
		createCell(row, 0, headerMap.get(ExcelFileHeader.LABEL), styleHead);
		createCell(row, 1, headerMap.get(ExcelFileHeader.CONTEXT), styleHead);
		createCell(row, 2, headerMap.get(ExcelFileHeader.MAX_LEN), styleHead);
		createCell(row, 3, headerMap.get(ExcelFileHeader.DESCRIPTION), styleHead);
		createCell(row, 4, headerMap.get(ExcelFileHeader.REFERENCE), styleHead);
		createCell(row, 5, headerMap.get(ExcelFileHeader.TRANSLATION), styleHead);
		createCell(row, 6, headerMap.get(ExcelFileHeader.REMARKS), styleHead);
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

	private void generateStatisticsFile(String targetDir,
			Map<String, Integer> labelCountMap,
			Map<String, Integer> wordCountMap) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("summary");
		Font fontHead = wb.createFont();
		fontHead.setFontHeightInPoints((short)10);
		fontHead.setFontName("Arial Unicode MS");
		fontHead.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    CellStyle styleHead = wb.createCellStyle();
	    styleHead.setFont(fontHead);
		Row row = sheet.createRow(0);
		createCell(row, 0, "Language", styleHead);
		createCell(row, 1, "Label count", styleHead);
		createCell(row, 2, "Word count", styleHead);
		sheet.setColumnWidth(0, 20 * 256);
		sheet.setColumnWidth(1, 20 * 256);
		sheet.setColumnWidth(2, 20 * 256);
		short r = 1;
		int totalLabel = 0, totalWord = 0;
		for (String langName : labelCountMap.keySet()) {
			row = sheet.createRow(r++);
			row.createCell(0).setCellValue(langName);
			row.createCell(1).setCellValue(labelCountMap.get(langName));
			row.createCell(2).setCellValue(wordCountMap.get(langName));
			totalLabel += labelCountMap.get(langName);
			totalWord += wordCountMap.get(langName);
		}
		row = sheet.createRow(r++);
		createCell(row, 0, "Total", styleHead);
		row.createCell(1).setCellValue(totalLabel);
		row.createCell(2).setCellValue(totalWord);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(targetDir, "summary.xls"));
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
		if (style != null) {
			cell.setCellStyle(style);
		}
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
	
	public Task receiveTaskFiles(Long taskId, String taskDir) throws BusinessException {
		log.info("Receive task files from " + taskDir + " ...");
		Task task = (Task) dao.retrieve(Task.class, taskId);
		if (task.getStatus() == Task.STATUS_CLOSED) {
			throw new BusinessException(BusinessException.INVALID_TASK_STATUS);
		}
		File rootDir = new File(taskDir);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			throw new SystemError("Failed to open task folder '" + taskDir + "'.");
		}
		File[] subDirs = rootDir.listFiles();
		for (File dir : subDirs) {
			if (dir.isDirectory()) {
				Language language = langService.findLanguageByName(dir.getName());
				if (language == null) {
					throw new BusinessException(BusinessException.UNKNOWN_LANG_NAME, dir.getName());
				}
				for (File taskFile : dir.listFiles()) {
					if (taskFile.isFile() && taskFile.getName().toLowerCase().endsWith(".xls")) {
						Map<String, Map<String, String>> transResult = receiveTaskFile(task, language, taskFile);
						for (String contextName : transResult.keySet()) {
							updateTaskDetails(task, language, contextName, transResult.get(contextName));
						}
					}
				}
			}
		}
		task.setLastUpdateTime(new Date());
		task.setLastUpdater(UserContext.getInstance().getUser());
		return task;
	}
	
	/**
	 * Update task details.
	 * @param task task
	 * @param language language
	 * @param contextName context name
	 * @param translationMap map of reference-translation pairs
	 */
	private void updateTaskDetails(Task task, Language language, String contextName,
			Map<String, String> translationMap) {
		String hql = "from TaskDetail where task.id=:taskId" +
				" and language.id=:languageId and text.context.name=:contextName";
		Map param = new HashMap();
		param.put("taskId", task.getId());
		param.put("languageId", language.getId());
		param.put("contextName", contextName);
		Collection<TaskDetail> details = dao.retrieve(hql, param);
		for (TaskDetail td : details) {
			td.setNewTranslation(translationMap.get(td.getText().getReference()));
		}
	}

	/**
	 * Read a task file.
	 * @param task task
	 * @param language language
	 * @param taskFile task file
	 * @return first level key is context name, second level key is reference text, value is translation
	 */
	private Map<String, Map<String, String>> receiveTaskFile(Task task, Language language, File taskFile) {
		log.info("Receiving task file " + taskFile + " ...");
        //file is excel file
        FileInputStream inp = null;
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        try {
            inp = new FileInputStream(taskFile);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            HSSFDataFormatter formatter = new HSSFDataFormatter();
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
            if (!cellIndexMap.containsKey(headerMap.get(ExcelFileHeader.CONTEXT)) ||
            		!cellIndexMap.containsKey(headerMap.get(ExcelFileHeader.REFERENCE)) ||
            		!cellIndexMap.containsKey(headerMap.get(ExcelFileHeader.TRANSLATION))) {
            	throw new BusinessException(BusinessException.INVALID_TASK_FILE, language.getName() + "/" + taskFile.getName());
            }
            Row row;
            for (int dataIndex = sheet.getFirstRowNum() + 1; (null != (row = sheet.getRow(dataIndex))); ++dataIndex) {
                String context = formatter.formatCellValue(row.getCell(cellIndexMap.get(headerMap.get(ExcelFileHeader.CONTEXT))));
                String reference = formatter.formatCellValue(row.getCell(cellIndexMap.get(headerMap.get(ExcelFileHeader.REFERENCE))));
                String translation = formatter.formatCellValue(row.getCell(cellIndexMap.get(headerMap.get(ExcelFileHeader.TRANSLATION))));
                if (context == null || context.isEmpty() || reference == null) {
                	throw new BusinessException(BusinessException.INVALID_TASK_FILE, language.getName() + "/" + taskFile.getName());
                }
                Map<String, String> transMap = result.get(context);
                if (transMap == null) {
                	transMap = new HashMap<String, String>();
                	result.put(context, transMap);
                }
                transMap.put(reference, translation);
            }
            return result;
        } catch (Exception e) {
        	e.printStackTrace();
        	log.error(e);
        	throw new SystemError(e);
        } finally {
        	if (inp != null) try {inp.close();} catch (Exception e) {}
        }
	}
	
	@Override
	public Task applyTask(Long taskId, boolean markAllTranslated) throws BusinessException {
		log.info("Applying task: " + taskId);
		Task task = (Task) dao.retrieve(Task.class,taskId);
		if (task.getStatus() == Task.STATUS_CLOSED || task.getLastUpdateTime() == null) {
			throw new BusinessException(BusinessException.INVALID_TASK_STATUS);
		}
		Collection<Context> contexts = getTaskContexts(taskId);
		int count = 0;
		for (Context context : contexts) {
			count += applyTask(task, context, markAllTranslated);
		}
		task.setLastApplyTime(new Date());
		log.info("" + count + " translation results were applied.");
		return task;
	}

	private Collection<Context> getTaskContexts(Long taskId) {
		String hql = "select distinct obj.text.context from TaskDetail obj where obj.task.id=:taskId";
		Map param = new HashMap();
		param.put("taskId", taskId);
		return dao.retrieve(hql, param);
	}

	/**
	 * Apply translation task result by context.
	 * @param task task
	 * @param context context
	 * @return number of translations applied
	 */
	private int applyTask(Task task, Context context, boolean markAllTranslated) {
		String hql = "from TaskDetail where task.id=:taskId and text.context.id=:contextId";
		Map param = new HashMap();
		param.put("taskId", task.getId());
		param.put("contextId", context.getId());
		Collection<TaskDetail> details = dao.retrieve(hql, param);
		Map<String, Text> textMap = new HashMap<String, Text>();
		int count = 0;
		for (TaskDetail td : details) {
			if (td.getNewTranslation() == null || td.getNewTranslation().trim().isEmpty()) {
				continue;
			}
			if (!markAllTranslated && td.getNewTranslation().equals(td.getText().getReference())) {
				continue;
			}
			Text text = textMap.get(td.getText().getReference());
			if (text == null) {
				text = new Text();
				text.setReference(td.getText().getReference());
				textMap.put(td.getText().getReference(), text);
			}
			Translation trans = new Translation();
			trans.setTranslation(td.getNewTranslation());
			trans.setLanguage(td.getLanguage());
			trans.setStatus(Translation.STATUS_TRANSLATED);
			text.addTranslation(trans);
			count++;
		}
		if (count > 0) {
			textService.updateTranslations(context.getId(), textMap.values(), Constants.TRANSLATION_MODE);
		}
		return count;
	}

	@Override
	public Map<Long, Map<Long, int[]>> getTaskSummary(Long taskId) {
		String hql = "select td.text.context.id,td.language.id," +
				"sum(case when td.newTranslation<>td.text.reference then 1 else 0 end)," +
				"sum(case when td.newTranslation is null or td.newTranslation='' or td.newTranslation=td.text.reference then 1 else 0 end) " +
				"from TaskDetail td " +
				"where td.task.id=:taskId " +
				"group by td.text.context.id,td.language.id";
		Map param = new HashMap();
		param.put("taskId", taskId);
		Collection<Object[]> resultSet = dao.retrieve(hql, param);
		Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
		for (Object[] row : resultSet) {
			Long contextId = ((Number) row[0]).longValue();
			Long languageId = ((Number) row[1]).longValue();
			int[] value = new int[2];
			value[0] = ((Number) row[2]).intValue();
			value[1] = ((Number) row[3]).intValue();
			Map<Long, int[]> langMap = result.get(contextId);
			if (langMap == null) {
				langMap = new HashMap<Long, int[]>();
				result.put(contextId, langMap);
			}
			langMap.put(languageId, value);
		}
		return result;
	}
}
