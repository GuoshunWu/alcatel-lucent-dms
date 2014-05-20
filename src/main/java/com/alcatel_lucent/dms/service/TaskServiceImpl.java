package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.generator.OTCExcelCellStyle;
import com.alcatel_lucent.dms.service.generator.OTCPCGenerator;
import com.alcatel_lucent.dms.service.parser.OTCPCParser;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.intellij.lang.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

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
        headerMap.put(ExcelFileHeader.SECONDARY_REFERENCE_LANGUAGE, "Secondary reference text");
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

    @Autowired
    private GlossaryService glossaryService;

    @SuppressWarnings("unchecked")
    @Override
    public Task createTask(Long productId, Long appId, String name,
                           Collection<Long> dictIds, Collection<Long> languageIds, Long secondaryReferenceLanguageId) {
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
        if (null != secondaryReferenceLanguageId) {
            task.setSecondaryReferenceLanguage((Language) dao.retrieve(Language.class, secondaryReferenceLanguageId));
        }

        log.info("Preparing translation task details...");
        @org.intellij.lang.annotations.Language("HQL") String hql = "select distinct dl.language,l,ct " +
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
        log.info("Creating " + newTransList.size() + " new translation objects...");
        for (Translation trans : newTransList) {
            dao.create(trans, false);
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

    /**
     * Get OTC excel dictionary display check information in the task details.
     * Return null if there are conflict exists
     *
     * @param taskDetails
     */
    private String[] getOTCExcelDisplayCheckInfo(Collection<TaskDetail> taskDetails) {
        String[] strWidths = null;
        String[] previousStrWidths = null;
        boolean conflict = false;
        for (TaskDetail td : taskDetails) {
            Dictionary dict = td.getLabel().getDictionary();
            if (dict.getFormat().equals(Constants.DictionaryFormat.OTC_EXCEL.toString())) {
                strWidths = Util.string2Map(dict.getAnnotation2()).get(OTCPCParser.SHEET_REF_TITLE_DISPLAY_CHECK).split(",");

                if (null != previousStrWidths) {
                    // check if there's conflict
                    if (previousStrWidths.length != strWidths.length) return null;

                    for (int i = 0; i < previousStrWidths.length && !conflict; ++i) {
                        if (!previousStrWidths[i].trim().equals(strWidths[i].trim())) return null;
                    }
                }
                previousStrWidths = strWidths;
            }
        }
        if (null != strWidths) {
            log.info("OTC excel dictionary display check information: " + Arrays.asList(strWidths));
        }
        return strWidths;
    }

    @Override
    public void generateTaskFiles(String targetDir, Long taskId) {
        Task task = (Task) dao.retrieve(Task.class, taskId);
        log.info("Generate task files to {}, id={}, secondary reference language= {}", targetDir, taskId, task.getSecondaryReferenceLanguage());

        ArrayList<TaskDetail> currentDetails = new ArrayList<TaskDetail>();
        String currentLanguage = null;
        String currentApp = null;
        ArrayList<String> existingFilenames = new ArrayList<String>();
        int labelCount = 0, wordCount = 0;
        Map<String, Integer> labelCountMap = new TreeMap<String, Integer>();
        Map<String, Integer> wordCountMap = new TreeMap<String, Integer>();
        HashSet<String> defaultReferences = new HashSet<String>();

        String[] otcExcelDisplayInfo = getOTCExcelDisplayCheckInfo(task.getDetails());

        for (TaskDetail td : task.getDetails()) {
            String languageName = td.getLanguage().getName();

            Dictionary dict = td.getLabel().getDictionary();
            String appName = dict.getBase().getApplicationBase().getName();

            if ((currentLanguage != null && !currentLanguage.equals(languageName)) ||
                    (currentApp != null && !currentApp.equals(appName))) {    // change to next file
                if (currentDetails.size() > 0) {
                    generateTaskFile(targetDir, currentLanguage, currentApp, currentDetails, existingFilenames, task.getSecondaryReferenceLanguage(), otcExcelDisplayInfo);
                    currentDetails.clear();
                }
                if (currentLanguage != null && !currentLanguage.equals(languageName)) {    // change to next language
                    labelCountMap.put(currentLanguage, labelCount);
                    wordCountMap.put(currentLanguage, wordCount);
                    labelCount = wordCount = 0;
                    existingFilenames.clear();
                    defaultReferences.clear();
                }
            }
            currentLanguage = languageName;
            currentApp = appName;

            // skip same reference text in special context such as [DEFAULT] ,[DICT] etc.
            Context ctx = td.getText().getContext();
            boolean skip = false;

            if (ctx.isSpecial()) {
                if (defaultReferences.contains(td.getText().getReference())) {
                    skip = true;
                } else {
                    defaultReferences.add(td.getText().getReference());
                }
            }
            if (!skip) {
                currentDetails.add(td);
                labelCount++;
                wordCount += Util.countWords(td.getText().getReference());
            }
        }

        if (currentDetails.size() > 0) {
            generateTaskFile(targetDir, currentLanguage, currentApp, currentDetails, existingFilenames, task.getSecondaryReferenceLanguage(), otcExcelDisplayInfo);
            labelCountMap.put(currentLanguage, labelCount);
            wordCountMap.put(currentLanguage, wordCount);
        }

        // create statistics file
        generateStatisticsFile(targetDir, labelCountMap, wordCountMap);
    }

    private void generateTaskFile(String targetDir, String languageName,
                                  String appName, ArrayList<TaskDetail> taskDetails, ArrayList<String> existingFileNames, Language secondaryReferenceLanguage) {
        generateTaskFile(targetDir, languageName, appName, taskDetails, existingFileNames, secondaryReferenceLanguage, null);
    }

    private void generateTaskFile(String targetDir, String languageName,
                                  String appName, ArrayList<TaskDetail> taskDetails, ArrayList<String> existingFileNames, Language secondaryReferenceLanguage, String[] OTCExcelDisplayInfo) {
        File dir = new File(targetDir, languageName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean existSecondaryRef = secondaryReferenceLanguage != null;

        // convert application name to target file name
        // if names conflict after the convention, add number suffix
        String filename = toFilename(appName);
        String tempName = filename;
        int i = 2;
        while (existingFileNames.contains(tempName)) {
            tempName = filename + "_" + i++;
        }
        existingFileNames.add(tempName);
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
        int rowNum = 0;
        final int WIDTH_BASE = 256;
        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.LABEL), styleHead);

//        sheet.setColumnWidth(rowNum, 0);
        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.CONTEXT), styleHead);

        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.MAX_LEN), styleHead);

        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.DESCRIPTION), styleHead);

        sheet.setColumnWidth(rowNum, 40 * WIDTH_BASE);
        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.REFERENCE), styleHead);
        if (existSecondaryRef) {
            sheet.setColumnWidth(rowNum, 40 * WIDTH_BASE);
            createCell(row, rowNum++, headerMap.get(ExcelFileHeader.SECONDARY_REFERENCE_LANGUAGE), styleHead);
        }
        sheet.setColumnWidth(rowNum, 40 * WIDTH_BASE);
        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.TRANSLATION), styleHead);

        sheet.setColumnWidth(rowNum, 20 * WIDTH_BASE);
        createCell(row, rowNum++, headerMap.get(ExcelFileHeader.REMARKS), styleHead);
        if (null != OTCExcelDisplayInfo) {
            OTCPCGenerator.drawDisplayCheckColumnHeader(row, OTCExcelDisplayInfo, rowNum++, styleHead);
        }

        short rowNo = 1;

        // create style map
        MultiKeyMap styleMap = new MultiKeyMap();
        // add grey style
        CellStyle greyStyle = (CellStyle) styleMap.get(OTCExcelCellStyle.GREY, StringUtils.EMPTY);
        if (null == greyStyle) {
            greyStyle = wb.createCellStyle();
            greyStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            greyStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            styleMap.put(OTCExcelCellStyle.GREY, StringUtils.EMPTY, greyStyle);
        }


        for (TaskDetail td : taskDetails) {
            row = sheet.createRow(rowNo++);
            Label label = td.getLabel();
            String reference = td.getText().getReference();
            String secondaryReference = null;
            DictionaryLanguage dictionaryLanguage;
            if (existSecondaryRef && null != (dictionaryLanguage = label.getDictionary().getDictLanguage(secondaryReferenceLanguage.getId()))) {
                String translation  = label.getTranslation(dictionaryLanguage.getLanguageCode());
                secondaryReference = StringUtils.defaultString(translation);
                log.info("secondary reference ={}", secondaryReference);
            }
            String translation = td.getNewTranslation() == null ? td.getOrigTranslation() : td.getNewTranslation();

            // for VoiceApp dict, add "..." for punctuation when position=begin/middle/end
            if (label.getDictionary().getFormat().equals(Constants.DictionaryFormat.VOICE_APP.toString())) {
                Map<String, String> attributes = Util.string2Map(label.getAnnotation1());
                String position = attributes.get("position");
                if (position != null) {
                    if (position.equals("begin") || position.equals("middle")) {
                        if (!StringUtils.isBlank(reference)) {
                            reference = reference + "...";
                        }
                        if (!StringUtils.isBlank(translation)) {
                            translation += "...";
                        }
                    }
                    if (position.equals("end") || position.equals("middle")) {
                        if (!StringUtils.isBlank(reference)) {
                            reference = "..." + reference;
                        }
                        if (!StringUtils.isBlank(translation)) {
                            translation = "..." + translation;
                        }
                    }
                }
            }

            // find LabelTranslation
            LabelTranslation lt = null;
            if (label.getOrigTranslations() != null) {
                for (LabelTranslation ot : label.getOrigTranslations()) {
                    if (ot.getLanguage().getId().equals(td.getLanguage().getId())) {
                        lt = ot;
                        break;
                    }
                }
            }
            rowNum = 0;
            createCell(row, rowNum++, td.getLabelKey(), styleBody);
            Context context = td.getText().getContext();
            // display only special contexts
            String contextStr = context.isSpecial() ? "" : context.getKey();
            createCell(row, rowNum++, contextStr, styleBody);
            if (td.getMaxLength() != null) {
                createCell(row, rowNum, td.getMaxLength(), styleBody);
            }
            rowNum++;
            if (td.getDescription() != null) {
                createCell(row, rowNum, td.getDescription(), styleBody);
            }
            rowNum++;
            createCell(row, rowNum++, reference, styleBody);
            // extract secondary for this language and add column
            if (existSecondaryRef) {
	            if (secondaryReference != null) {
	                createCell(row, rowNum, secondaryReference, styleBody);
	            }
	            rowNum++;
            }
            createCell(row, rowNum++, translation, styleUnlockedBody);
            createCell(row, rowNum++, lt == null || lt.getComment() == null ? "" : lt.getComment(), styleUnlockedBody);
            if (label.getDictionary().getFormat().equals(Constants.DictionaryFormat.OTC_EXCEL.toString())) {
                String strMergeNum = Util.string2Map(label.getAnnotation1()).get("displayCheckMergeNum");
                if (StringUtils.isNotBlank(strMergeNum)) {
                    Cell cell = OTCPCGenerator.drawDisplayCheckColumns(row, rowNum, 7, Integer.parseInt(strMergeNum), OTCExcelDisplayInfo.length, greyStyle);
                    OTCPCGenerator.setDisplayCheckCellStyle(cell, label, styleMap);
                    rowNum += 7;
                }
                //set row height
                String sRowHeight = Util.string2Map(label.getAnnotation1()).get("rowHeight");
                if (StringUtils.isNotBlank(sRowHeight)) {
                    row.setHeightInPoints(Float.parseFloat(sRowHeight));
                }
            }
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
        Cell cell = CellUtil.createCell(row, column, value);
        if (style != null) {
            cell.setCellStyle(style);
        }
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
                    if (taskFile.isFile() && (
                            taskFile.getName().toLowerCase().endsWith(".xls") ||
                                    taskFile.getName().toLowerCase().endsWith(".xlsx"))) {
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
                " and language.id=:languageId";
        Map param = new HashMap();
        param.put("taskId", task.getId());
        param.put("languageId", language.getId());
        if (contextKey.isEmpty()) {
            hql += " and text.context.name in :specialCtxNames";
            param.put("specialCtxNames", Context.SPECIAL_CONTEXT_NAMES);
        } else {
            hql += " and text.context.key=:contextKey";
            param.put("contextKey", contextKey);
        }
        Collection<TaskDetail> details = dao.retrieve(hql, param);
        for (TaskDetail td : details) {
            Label label = td.getLabel();
            String reference = label.getText().getReference();

            // For VoiceApp dict, add "..." for punctuation when position=begin/middle/end
            String position = null;
            if (label.getDictionary().getFormat().equals(Constants.DictionaryFormat.VOICE_APP.toString())) {
                Map<String, String> attributes = Util.string2Map(label.getAnnotation1());
                position = attributes.get("position");
                if (position != null && !StringUtils.isBlank(reference)) {
                    if (position.equals("begin") || position.equals("middle")) {
                        reference += "...";
                    }
                    if (position.equals("end") || position.equals("middle")) {
                        reference = "..." + reference;
                    }
                }
            }
            if (translationMap.containsKey(reference)) {
                String translation = translationMap.get(reference);
                // For VoiceApp dict, remove "..." when position=begin/middle/end
                if (position != null && translation != null) {
                    if (position.equals("begin") || position.equals("middle")) {
                        if (translation.endsWith("...")) {
                            translation = translation.substring(0, translation.length() - 3);
                        } else if (translation.endsWith("…")) {
                            translation = translation.substring(0, translation.length() - 1);
                        }
                    }
                    if (position.equals("end") || position.equals("middle")) {
                        if (translation.startsWith("...")) {
                            translation = translation.substring(3);
                        } else if (translation.startsWith("…")) {
                            translation = translation.substring(1);
                        }
                    }
                }
                td.setNewTranslation(translation);
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
                if (reference == null) {
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

    @Override
    public Task applyTask(Long taskId, boolean markAllTranslated) throws BusinessException {
        log.info("Applying task: " + taskId);
        Task task = (Task) dao.retrieve(Task.class, taskId);
        if (task.getStatus() == Task.STATUS_CLOSED || task.getLastUpdateTime() == null) {
            throw new BusinessException(BusinessException.INVALID_TASK_STATUS);
        }
        glossaryService.consistentGlossariesInTask(task);
        Collection<Context> contexts = getTaskContexts(taskId);
        int count = 0;
        for (Context context : contexts) {
            count += applyTask(task, context, markAllTranslated);
        }
        task.setLastApplyTime(new Date());
        log.info("" + count + " translation results were applied.");

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
                text.setRefLabel(td.getLabel());
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
            textService.updateTranslations(context.getId(), textMap.values(), Constants.ImportingMode.TRANSLATION, TranslationHistory.TRANS_OPER_RECEIVE);
            // update DEFAULT context from each DICT context, so the DEFAULT context would be a union of all translations
            if (context.getName().equals(Context.DICT)) {
                Context defaultCtx = textService.getContextByExpression(Context.DEFAULT, (Dictionary) null);
                textService.updateTranslations(defaultCtx.getId(), textMap.values(), Constants.ImportingMode.SUPPLEMENT, TranslationHistory.TRANS_OPER_SUGGEST);
            }
        }
        return count;
    }

    @Override
    public Map<Long, Map<Long, int[]>> getTaskSummary(Long taskId) {
/*        String hql = "select td.label.dictionary.base.applicationBase.id,td.language.id," +
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
*/
        Map<Long, Map<Long, int[]>> result = new HashMap<Long, Map<Long, int[]>>();
        Task task = (Task) dao.retrieve(Task.class, taskId);
        HashSet<String> uniqueReferences = new HashSet<String>();
        Long currentLangId = null;
        for (TaskDetail td : task.getDetails()) {
            Long appBaseId = td.getLabel().getDictionary().getBase().getApplicationBase().getId();
            Long languageId = td.getLanguage().getId();
            if (currentLangId == null || !currentLangId.equals(languageId)) {
                currentLangId = languageId;
                uniqueReferences.clear();
            }
            // skip duplicate reference text of context DEFAULT and DICT and LABEL
            String contextName = td.getLabel().getText().getContext().getName();
            if (contextName.equals(Context.DEFAULT) || contextName.equals(Context.DICT) || contextName.equals(Context.LABEL)) {
                if (uniqueReferences.contains(td.getText().getReference())) {
                    continue;
                } else {
                    uniqueReferences.add(td.getText().getReference());
                }
            }
            // add count
            Map<Long, int[]> langMap = result.get(appBaseId);
            if (langMap == null) {
                langMap = new HashMap<Long, int[]>();
                result.put(appBaseId, langMap);
            }
            int[] value = langMap.get(languageId);
            if (value == null) {
                value = new int[]{0, 0};
                langMap.put(languageId, value);
            }
            if (td.getNewTranslation() == null || td.getNewTranslation().isEmpty() ||
                    td.getNewTranslation().equals(td.getText().getReference())) {
                value[1]++;
            } else {
                value[0]++;
            }
        }
        return result;
    }

    enum ExcelFileHeader {
        LABEL, CONTEXT, MAX_LEN, DESCRIPTION, REFERENCE, SECONDARY_REFERENCE_LANGUAGE, TRANSLATION, REMARKS
    }

    @Override
    public Collection<Task> findAllDictRelatedTasks(Collection<Long> dictIdList) {
        String hql = "select distinct task from Task task join task.details td" +
                " where td.label.dictionary.id in (:dictIds)" +
                " order by task.createTime";
        Map param = new HashMap();
        param.put("dictIds", dictIdList);
        return dao.retrieve(hql, param);
    }

    @Override
    public void deleteTask(Long taskId) {
        Task task = (Task) dao.retrieve(Task.class, taskId);
        if (task.getStatus() == Task.STATUS_OPEN) {    // close the task if it's open
            closeTask(taskId);
        }
        String hql = "select obj from DictionaryHistory obj where obj.task.id=:taskId";
        Map param = new HashMap();
        param.put("taskId", taskId);
        Collection<DictionaryHistory> histories = dao.retrieve(hql, param);
        for (DictionaryHistory history : histories) {
            dao.delete(history);
        }
        dao.delete(task);
    }
}
