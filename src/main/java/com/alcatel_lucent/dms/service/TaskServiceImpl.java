package com.alcatel_lucent.dms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryHistory;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.model.TaskDetail;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.Util;

@Service("taskService")
public class TaskServiceImpl extends BaseServiceImpl implements TaskService {

    private static final String PROTECT_PASSWORD = "alcatel123";
    private static final String TASK_TEMPLATE_NAME = "task_template.xls";
    private static final Map<ExcelFileHeader, String> headerMap = new HashMap<ExcelFileHeader, String>();

    static {

        headerMap.put(ExcelFileHeader.LABEL, "Label");
        headerMap.put(ExcelFileHeader.CONTEXT, "Context");
        headerMap.put(ExcelFileHeader.MAX_LEN, "Max length");
        headerMap.put(ExcelFileHeader.DESCRIPTION, "Description");
        headerMap.put(ExcelFileHeader.REFERENCE, "Reference text");
        headerMap.put(ExcelFileHeader.TRANSLATION, "Translation");
        headerMap.put(ExcelFileHeader.REMARKS, "Remarks");
    }

    private static Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);
    @Autowired
    private LanguageService langService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TextService textService;

    @SuppressWarnings("unchecked")
    @Override
    public Task createTask(Long productId, Long appId, String name,
                           Collection<Long> dictIds, Collection<Long> languageIds) {
        Task task = new Task();
        task.setName(name);
        if (productId != null) {
            task.setProduct((Product) dao.retrieve(Product.class, productId));
        }
        if (appId != null) {
            task.setApplication((Application) dao.retrieve(Application.class, appId));
        }
        task.setCreateTime(new Date());
        task.setCreator(UserContext.getInstance().getUser());
        task.setStatus(Task.STATUS_OPEN);
        task = (Task) dao.create(task);

        log.info("Preparing translation task details...");
        String hql = "select distinct dl.language,l,ct " +
                "from Dictionary d join d.labels l join d.dictLanguages dl " +
                "join l.text.translations ct " +
                "where d.id in (:dictIds) and dl.language.id in (:langIds) and l.removed=false " +
                "and ct.language=dl.language and ct.status=:status " +
                "and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                "and l.context.name<>:exclusion ";
        Map param = new HashMap();
        param.put("dictIds", dictIds);
        param.put("langIds", languageIds);
        param.put("status", Translation.STATUS_UNTRANSLATED);
        param.put("exclusion", Context.EXCLUSION);
        Collection<Object[]> resultSet = dao.retrieve(hql, param);
        hql = "select distinct dl.language,l " +
                "from Dictionary d join d.labels l join d.dictLanguages dl " +
                "where d.id in (:dictIds) and dl.language.id in (:langIds) and l.removed=false " +
                "and not exists(select lt from LabelTranslation lt where lt.language=dl.language and lt.label=l and lt.needTranslation=false) " +
                "and l.context.name<>:exclusion " +
                "and not exists(select ct from Translation ct where ct.text=l.text and ct.language=dl.language) ";
        param = new HashMap();
        param.put("dictIds", dictIds);
        param.put("langIds", languageIds);
        param.put("exclusion", Context.EXCLUSION);
        resultSet.addAll(dao.retrieve(hql, param));
        ArrayList<Object[]> sortedDetails = new ArrayList<Object[]>(resultSet);

        // Sort task details by language,app,dict,label.sortNo
        // The order is important because of the way of generating files
        Collections.sort(sortedDetails, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                Language language1 = (Language) o1[0];
                Label label1 = (Label) o1[1];
                String key1 = language1.getId() + "_" + label1.getDictionary().getBase().getApplicationBase().getId() +
                        "_" + label1.getDictionary().getId() + "_" + label1.getSortNo();
                Language language2 = (Language) o2[0];
                Label label2 = (Label) o2[1];
                String key2 = language2.getId() + "_" + label2.getDictionary().getBase().getApplicationBase().getId() +
                        "_" + label2.getDictionary().getId() + "_" + label2.getSortNo();
                return key1.compareTo(key2);
            }

        });
        log.info("Creating " + sortedDetails.size() + " task details...");
        Collection<Translation> newTransList = new ArrayList<Translation>();
        HashSet<String> unique = new HashSet<String>();    // unique by language and text
        if (sortedDetails != null && sortedDetails.size() > 0) {
            for (Object[] row : sortedDetails) {
                Language language = (Language) row[0];
                Label label = (Label) row[1];
                Translation translation = (Translation) (row.length > 2 ? row[2] : null);
                Text text = label.getText();
                String key = language.getId() + "," + text.getId();
                if (unique.contains(key)) continue;
                unique.add(key);
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
                td.setLabel(label);
                td.setLabelKey(label.getKey());
                td.setMaxLength(label.getMaxLength());
                td.setDescription(label.getDescription());
                dao.create(td, false);
            }
        } else {
            throw new BusinessException(BusinessException.EMPTY_TASK);
        }
        for (Translation trans : newTransList) {
            dao.create(trans);
        }

        // create log
        historyService.logCreateTask(task);
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

        // create log
        historyService.logCloseTask(task);
    }

    @Override
    public void generateTaskFiles(String targetDir, Long taskId) {
        log.info("Generate task files to " + targetDir + ", id=" + taskId);
        Task task = (Task) dao.retrieve(Task.class, taskId);
        ArrayList<TaskDetail> currentDetails = new ArrayList<TaskDetail>();
        String currentLanguage = null;
        String currentApp = null;
        ArrayList<String> existingFilenames = new ArrayList<String>();
        int labelCount = 0, wordCount = 0;
        Map<String, Integer> labelCountMap = new TreeMap<String, Integer>();
        Map<String, Integer> wordCountMap = new TreeMap<String, Integer>();
        for (TaskDetail td : task.getDetails()) {
            String languageName = td.getLanguage().getName();
            String appName = td.getLabel().getDictionary().getBase().getApplicationBase().getName();
            if ((currentLanguage != null && !currentLanguage.equals(languageName)) ||
                    (currentApp != null && !currentApp.equals(appName))) {
                generateTaskFile(targetDir, currentLanguage, currentApp, currentDetails, existingFilenames);
                currentDetails.clear();
                if (currentLanguage != null && !currentLanguage.equals(languageName)) {
                    labelCountMap.put(currentLanguage, labelCount);
                    wordCountMap.put(currentLanguage, wordCount);
                    labelCount = wordCount = 0;
                    existingFilenames.clear();
                }
            }
            currentLanguage = languageName;
            currentApp = appName;
            currentDetails.add(td);
            labelCount++;
            wordCount += Util.countWords(td.getText().getReference());
        }
        if (currentDetails.size() > 0) {
            generateTaskFile(targetDir, currentLanguage, currentApp, currentDetails, existingFilenames);
            labelCountMap.put(currentLanguage, labelCount);
            wordCountMap.put(currentLanguage, wordCount);
        }

        // create statistics file
        generateStatisticsFile(targetDir, labelCountMap, wordCountMap);
    }

    private void generateTaskFile(String targetDir, String languageName,
                                  String appName, ArrayList<TaskDetail> taskDetails, ArrayList<String> existingFilenames) {
        File dir = new File(targetDir, languageName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // convert application name to target file name
        // if names conflict after the convention, add number suffix
        String filename = toFilename(appName);
        String tempName = filename;
        int i = 2;
        while (existingFilenames.contains(tempName)) {
            tempName = filename + "_" + i++;
        }
        existingFilenames.add(tempName);
        filename = tempName + ".xls";

        File targetFile = new File(dir, filename);
        Workbook wb = null;
        FileOutputStream fos = null;
        FileInputStream fin = null;
        try {
            fos = FileUtils.openOutputStream(targetFile);
            IOUtils.copy(getClass().getResourceAsStream(TASK_TEMPLATE_NAME), fos);
            fin = FileUtils.openInputStream(targetFile);
            wb = WorkbookFactory.create(fin);
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            throw new SystemError(e);
        } finally {
            IOUtils.closeQuietly(fin);
            IOUtils.closeQuietly(fos);
        }

        Font fontHead = wb.createFont();
        fontHead.setFontHeightInPoints((short) 10);
        fontHead.setFontName("Arial Unicode MS");
        fontHead.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle styleHead = wb.createCellStyle();
        styleHead.setFont(fontHead);
        Font fontBody = wb.createFont();
        fontBody.setFontHeightInPoints((short) 10);
        fontBody.setFontName("Arial Unicode MS");
        CellStyle styleBody = wb.createCellStyle();
        styleBody.setFont(fontBody);
        CellStyle styleUnlockedBody = wb.createCellStyle();
        styleUnlockedBody.setFont(fontBody);
        styleUnlockedBody.setLocked(false);

        Sheet sheet = wb.cloneSheet(0);
        wb.setSheetName(wb.getSheetIndex(sheet), languageName);

        sheet.createFreezePane(0, 1, 0, 1);
        Row row = sheet.createRow(0);
        createCell(row, 0, headerMap.get(ExcelFileHeader.LABEL), styleHead);
        createCell(row, 1, headerMap.get(ExcelFileHeader.CONTEXT), styleHead);
        createCell(row, 2, headerMap.get(ExcelFileHeader.MAX_LEN), styleHead);
        createCell(row, 3, headerMap.get(ExcelFileHeader.DESCRIPTION), styleHead);
        createCell(row, 4, headerMap.get(ExcelFileHeader.REFERENCE), styleHead);
        createCell(row, 5, headerMap.get(ExcelFileHeader.TRANSLATION), styleHead);
        createCell(row, 6, headerMap.get(ExcelFileHeader.REMARKS), styleHead);
        sheet.setColumnWidth(1, 0);
        sheet.setColumnWidth(4, 40 * 256);
        sheet.setColumnWidth(5, 40 * 256);
        sheet.setColumnWidth(6, 20 * 256);
        short rowNo = 1;
        for (TaskDetail td : taskDetails) {
            row = sheet.createRow(rowNo++);
            createCell(row, 0, td.getLabelKey(), styleBody);
            createCell(row, 1, td.getText().getContext().getKey(), styleBody);
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

        wb.removeSheetAt(0);
        try {
            fos = FileUtils.openOutputStream(targetFile);
            wb.write(fos);
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            throw new SystemError(e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    private void generateStatisticsFile(String targetDir,
                                        Map<String, Integer> labelCountMap,
                                        Map<String, Integer> wordCountMap) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("summary");
        Font fontHead = wb.createFont();
        fontHead.setFontHeightInPoints((short) 10);
        fontHead.setFontName("Arial Unicode MS");
        fontHead.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle styleHead = wb.createCellStyle();
        styleHead.setFont(fontHead);
        Row row = sheet.createRow(0);
//        CellUtil.createCell(row, 0, "Language", styleHead);
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
            log.error(e.toString());
            e.printStackTrace();
            throw new SystemError(e);
        } finally {
            if (fos != null) try {
                fos.close();
            } catch (Exception e) {
            }
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
     *
     * @param name contextName
     * @return
     */
    private String toFilename(String name) {
        int pos1 = name.lastIndexOf('/');
        int pos2 = name.lastIndexOf('\\');
        int pos = Math.max(pos1, pos2);
        if (pos != -1 && pos != name.length() - 1) {
            name = name.substring(pos + 1);
        }
        name = name.replace('\\', '_');
        name = name.replace('/', '_');
        name = name.replace(':', '_');
        name = name.replace('*', '_');
        name = name.replace('?', '_');
        name = name.replace('\"', '_');
        name = name.replace('<', '_');
        name = name.replace('>', '_');
        name = name.replace('|', '_');
        return name.trim();
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
                        for (String contextKey : transResult.keySet()) {
                            updateTaskDetails(task, language, contextKey, transResult.get(contextKey));
                        }
                    }
                }
            }
        }
        task.setLastUpdateTime(new Date());
        task.setLastUpdater(UserContext.getInstance().getUser());

        // create log
        historyService.logReceiveTask(task, taskDir);
        return task;
    }

    /**
     * Update task details.
     *
     * @param task           task
     * @param language       language
     * @param contextKey     context name
     * @param translationMap map of reference-translation pairs
     */
    private void updateTaskDetails(Task task, Language language, String contextKey,
                                   Map<String, String> translationMap) {
        String hql = "from TaskDetail where task.id=:taskId" +
                " and language.id=:languageId and text.context.key=:contextKey";
        Map param = new HashMap();
        param.put("taskId", task.getId());
        param.put("languageId", language.getId());
        param.put("contextKey", contextKey);
        Collection<TaskDetail> details = dao.retrieve(hql, param);
        for (TaskDetail td : details) {
            if (translationMap.containsKey(td.getText().getReference())) {
                td.setNewTranslation(translationMap.get(td.getText().getReference()));
            }
        }
    }

    /**
     * Read a task file.
     *
     * @param task     task
     * @param language language
     * @param taskFile task file
     * @return first level key is context key, second level key is reference text, value is translation
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
            log.error(e.toString());
            throw new SystemError(e);
        } finally {
            if (inp != null) try {
                inp.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Task applyTask(Long taskId, boolean markAllTranslated) throws BusinessException {
        log.info("Applying task: " + taskId);
        Task task = (Task) dao.retrieve(Task.class, taskId);
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

        // create log
        historyService.logImportTask(task);
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
     *
     * @param task    task
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
            textService.updateTranslations(context.getId(), textMap.values(), Constants.ImportingMode.TRANSLATION);
        }
        return count;
    }

    @Override
    public Map<Long, Map<Long, int[]>> getTaskSummary(Long taskId) {
        String hql = "select td.label.dictionary.base.applicationBase.id,td.language.id," +
                "sum(case when td.newTranslation<>td.text.reference then 1 else 0 end)," +
                "sum(case when td.newTranslation is null or td.newTranslation='' or td.newTranslation=td.text.reference then 1 else 0 end) " +
                "from TaskDetail td " +
                "where td.task.id=:taskId " +
                "group by td.label.dictionary.base.applicationBase.id,td.language.id";
        Map param = new HashMap();
        param.put("taskId", taskId);
        Collection<Object[]> resultSet = dao.retrieve(hql, param);
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
        for (Object[] row : resultSet) {
            Long appBaseId = ((Number) row[0]).longValue();
            Long languageId = ((Number) row[1]).longValue();
            int[] value = new int[2];
            value[0] = ((Number) row[2]).intValue();
            value[1] = ((Number) row[3]).intValue();
            Map<Long, int[]> langMap = result.get(appBaseId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(appBaseId, langMap);
            }
            langMap.put(languageId, value);
        }
        return result;
    }

    enum ExcelFileHeader {
        LABEL, CONTEXT, MAX_LEN, DESCRIPTION, REFERENCE, TRANSLATION, REMARKS
    }
}
