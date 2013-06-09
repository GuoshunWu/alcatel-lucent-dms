package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

@Component
public class OTCPCParser extends DictionaryParser {

    // OTC excel sheet names(except those with language codes)
    public static final String SHEET_INFO = "Informations";
    //Column title in information sheet
    public static final String SHEET_INFO_TITLE_INFO = SHEET_INFO;

    public static final String SHEET_LANG = "Languages";
    //Column title in languages sheet
    public static final String SHEET_LANG_TITLE_LANG = SHEET_LANG;

    public static final String SHEET_CTX = "Context";
    //Column title in context sheet
    public static final String SHEET_CTX_TITLE_DEFAULT = "Value_default";
    public static final String SHEET_CTX_TITLE_DESC = "Context of use/display";

    public static final String SHEET_REF = "default";
    //Column title in default(reference) sheet
    public static final String SHEET_REF_TITLE_ID = "ID";
    public static final String SHEET_REF_TITLE_VALUE = "Value";
    public static final String SHEET_REF_TITLE_DISPLAY_CHECK = "Display check (approximatively)";
    public static final String SHEET_REF_TITLE_USER_INTERFACE = "User Interface";
    public static final String SHEET_REF_TITLE_USED = "Used";

    //Column title in language code sheet
    public static final String TITLE_ID = SHEET_REF_TITLE_ID;
    public static final String TITLE_DEFAULT = SHEET_CTX_TITLE_DEFAULT;
    public static final String TITLE_VALUE = SHEET_REF_TITLE_VALUE;
    public static final String TITLE_DISPLAY_CHECK = SHEET_REF_TITLE_DISPLAY_CHECK;

    public static final String REFERENCE_LANG_CODE = "en";
    public static final String DEFAULT_ENCODING = "UTF-16LE";

    public static final String[] extensions = new String[]{"xls", "xlsx"};
    @Autowired
    private LanguageService languageService;

    private static Logger log = LoggerFactory.getLogger(OTCPCParser.class);

    private static Map<String, Map<String, Integer>> sheetTitleColMap = new HashMap<String, Map<String, Integer>>();

    @Override
    public DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_EXCEL;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (null == file || !file.exists()) return deliveredDicts;

        Collection<File> OTCFiles = null;
        if (file.isDirectory()) {
            OTCFiles = FileUtils.listFiles(file, extensions, true);
        } else {
            OTCFiles = new ArrayList<File>();
            if (FilenameUtils.isExtension(file.getName().toLowerCase(), extensions)) {
                OTCFiles.add(file);
                rootDir = file.getParent();
            }
        }

        for (File OTCFile : OTCFiles) {
            try {
                Pair<String, String> namePair = getDictNamePair(rootDir, OTCFile);
                deliveredDicts.add(parseDictionary(namePair.getLeft(), namePair.getRight(), OTCFile, acceptedFiles));
            } catch (BusinessException e) {
                // Ignore INVALID_OTC_PC_DICT_FILE error because the file can be another type of excel dictionary.
                if (e.getErrorCode() != BusinessException.INVALID_OTC_EXCEL_DICT_FILE) {
                    throw e;
                }
            }
        }
        return deliveredDicts;
    }

    public Dictionary parseDictionary(String dictName, String dictPath, File file, Collection<File> acceptedFiles) {

        DictionaryBase dictBase = new DictionaryBase();

        dictBase.setName(dictName);
        dictBase.setPath(dictPath);
        dictBase.setEncoding(DEFAULT_ENCODING);
        dictBase.setFormat(Constants.DictionaryFormat.OTC_EXCEL.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setReferenceLanguage(REFERENCE_LANG_CODE);
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);


        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(file)));
        } catch (Exception e) {
            throw new BusinessException(BusinessException.INVALID_EXCEL_FILE, file.getName());
        }

        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();

        int numOfSheet = wb.getNumberOfSheets();

        //   HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        for (int i = 0; i < numOfSheet; ++i) {
            Sheet sheet = wb.getSheetAt(i);
            if (sheet.getSheetName().equalsIgnoreCase(SHEET_INFO)) {
                readDictionaryInformation(sheet, dictionary, evaluator, warnings, dictName);
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_LANG)) {
                readDictLanguages(sheet, dictionary);
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_CTX)) {
                // no action, it is processed in reference sheet.
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_REF)) {
                readRefSheet(dictionary, sheet, evaluator, warnings, dictName);
            } else {
                //no action, all the translations have been proce
            }
        }
        acceptedFiles.add(file);

        dictionary.setParseWarnings(warnings);
        return dictionary;
    }

    private void readDictLanguages(Sheet langSheet, Dictionary dictionary) {
        Map<String, Integer> colIndexes = getTitleMap(langSheet);
        int langCodeColNum = colIndexes.get(SHEET_LANG_TITLE_LANG);
        int sortNo = 0;
        for (Row row : langSheet) {
            if (row.getRowNum() == langSheet.getFirstRowNum()) continue;
            Cell langCell = row.getCell(langCodeColNum);
            if (null == langCell) continue;
            String langCode = langCell.getStringCellValue().trim();
            if (StringUtils.isEmpty(langCode)) continue;

            if (langCode.equals(SHEET_REF)) {
                langCode = REFERENCE_LANG_CODE;
            }
            DictionaryLanguage dl = new DictionaryLanguage();
            dl.setLanguageCode(langCode);
            dl.setSortNo(sortNo);
            dl.setLanguage(languageService.getLanguage(langCode));
            dl.setCharset(new Charset(DEFAULT_ENCODING));
            dl.setDictionary(dictionary);
            dictionary.getDictLanguages().add(dl);

            ++sortNo;
        }
    }

    private void readRefSheet(Dictionary dictionary, Sheet sheet, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        //save the display check column info in reference sheet into dictionary annotation2
        Map<String, Integer> colIndexes = getTitleMap(sheet);

        Cell displayCheckTitleCell = CellUtil.getCell(sheet.getRow(sheet.getFirstRowNum()), colIndexes.get(SHEET_REF_TITLE_DISPLAY_CHECK));
        CellRangeAddress cellRangeAddress = findCellRangeAddress(sheet, displayCheckTitleCell);
        StringBuilder sb = new StringBuilder();

        for (int colIdx = cellRangeAddress.getFirstColumn(); colIdx <= cellRangeAddress.getLastColumn(); ++colIdx) {
            if (colIdx > cellRangeAddress.getFirstColumn()) {
                sb.append(",");
            }
            sb.append(sheet.getColumnWidth(colIdx));
        }
        String annotation2 = SHEET_REF_TITLE_DISPLAY_CHECK + "=" + sb.toString();
        dictionary.setAnnotation2(annotation2);
        //read and generate labels
        Label label;
        for (Row row : sheet) {
            if (row.getRowNum() == sheet.getFirstRowNum()) continue;
            label = getLabel(row, colIndexes, evaluator, warnings, dictName);
            label.setDictionary(dictionary);
            fillLabelTranslations(label, row, evaluator, warnings, dictName);
            dictionary.addLabel(label);
        }
    }

    private Label getLabel(Row row, Map<String, Integer> colIndexes, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        Cell cell = row.getCell(colIndexes.get(SHEET_REF_TITLE_ID));
        String labelKey = getStringCellValue(cell, evaluator, warnings, dictName).trim();

        if (null == cell || StringUtils.isBlank(labelKey)) {
            warnings.add(new BusinessWarning(BusinessWarning.LABEL_KEY_BLANK, row.getSheet().getSheetName(), TITLE_DEFAULT, row.getRowNum()));
        }

        cell = row.getCell(colIndexes.get(SHEET_REF_TITLE_VALUE));
        String reference = getStringCellValue(cell, evaluator, warnings, dictName);

        // get display check column info and store it in label annotation1.
        cell = row.getCell(colIndexes.get(SHEET_REF_TITLE_DISPLAY_CHECK));
        Integer lblDisplayCheckMergeNum = null;
        Font font = null;
        if (null != cell) {
            CellRangeAddress cr = findCellRangeAddress(row.getSheet(), cell);
            lblDisplayCheckMergeNum = cr.getLastColumn() - cr.getFirstColumn() + 1;
            font = cell.getSheet().getWorkbook().getFontAt(cell.getCellStyle().getFontIndex());
        }

        // get description in context sheet
        Sheet ctxSheet = row.getSheet().getWorkbook().getSheet(SHEET_CTX);
        Map<String, Integer> ctxColIndexesMap = getTitleMap(ctxSheet);
        int colNumber = ctxColIndexesMap.get(SHEET_CTX_TITLE_DESC);
        Cell descCell = CellUtil.getRow(row.getRowNum(), ctxSheet).getCell(colNumber);

        cell = row.getCell(colIndexes.get(SHEET_REF_TITLE_USER_INTERFACE));
        String userInterface = null == cell ? "" : getStringCellValue(cell, evaluator, warnings, dictName);

        cell = row.getCell(colIndexes.get(SHEET_REF_TITLE_USED));
        String used = null == cell ? "" : getStringCellValue(cell, evaluator, warnings, dictName);
        return createNewLabel(row.getRowNum(), row.getHeightInPoints(), labelKey, reference, lblDisplayCheckMergeNum, font, userInterface, used);
    }

    private Label createNewLabel(int sortNo, float rowHeightInPoints, String key, String ref, Integer displayCheckMergeNum, Font font, String userInterface, String used) {
        Label label = new Label();
        label.setSortNo(sortNo);
        label.setKey(key);
        label.setReference(ref);
        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        if (null != font) {
            label.setFontName(font.getFontName());
            label.setFontSize(font.getFontHeightInPoints() + "");
        }
        Map<String, String> annotation = new HashMap<String, String>();
        annotation.put("rowHeight", String.valueOf(rowHeightInPoints));

        if (null != displayCheckMergeNum) {
            annotation.put("displayCheckMergeNum", displayCheckMergeNum + "");
        }
        if (null != userInterface) {
            annotation.put("userInterface", userInterface);
        }
        if (null != used) {
            annotation.put("used", used);
        }

        label.setAnnotation1(Util.map2String(annotation));
        return label;
    }

    private void fillLabelTranslations(Label label, Row row, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        Collection<DictionaryLanguage> dictionaryLanguages = label.getDictionary().getDictLanguages();
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
            if (dictionaryLanguage.getLanguageCode().equals(REFERENCE_LANG_CODE)) continue;
            LabelTranslation lt = getLabelTranslation(dictionaryLanguage.getLanguageCode(), dictionaryLanguage.getLanguage(), row, evaluator, warnings, dictName);
            if (null != lt) {
                lt.setLabel(label);
                label.getOrigTranslations().add(lt);
            }
        }
    }

    private LabelTranslation getLabelTranslation(String langCode, Language language, Row row, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        Sheet transSheet = row.getSheet().getWorkbook().getSheet(langCode);
        if (null == transSheet) {
            log.warn("Language sheet \"{}\" not found.", langCode);
            return null;
        }
        Map<String, Integer> colIndexes = getTitleMap(transSheet);
        int transColNumber = colIndexes.get(TITLE_VALUE);
        Cell transCell = transSheet.getRow(row.getRowNum()).getCell(transColNumber);
        if (null == transCell) {
            return null;
        }
        String translation = getStringCellValue(transCell, evaluator, warnings, dictName);
        if (StringUtils.isBlank(translation)) return null;

        LabelTranslation lbTranslation = new LabelTranslation();
        lbTranslation.setLanguageCode(langCode);
        lbTranslation.setLanguage(language);
        lbTranslation.setSortNo(row.getRowNum());
        lbTranslation.setOrigTranslation(translation);
        return lbTranslation;
    }

    /**
     * Get the information from sheet to dictionary.
     */
    private void readDictionaryInformation(Sheet sheet, Dictionary dictionary, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        StringBuilder sb = new StringBuilder();
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); ++i) {
            Row row = sheet.getRow(i);
            if (null != row) {
                for (Cell cell : row) {
                    if (null == cell) continue;
                    String cellValue = getStringCellValue(cell, evaluator, warnings, dictName);
                    if (cellValue.isEmpty()) continue;
                    sb.append(cellValue);
                }
            }
            sb.append("\n");
        }
        dictionary.setAnnotation1(sb.toString());
    }

    /**
     * Get cached sheet title map
     * The first row is the title, which determine how to correlate their content in the following rows
     */
    public static synchronized Map<String, Integer> getTitleMap(Sheet sheet) {
        Map<String, Integer> titleMap = sheetTitleColMap.get(sheet.getSheetName());
        if (null == titleMap) {
            titleMap = readTitleRow(sheet.getRow(sheet.getFirstRowNum()));
            sheetTitleColMap.put(sheet.getSheetName(), titleMap);
        }
        return titleMap;
    }

    /**
     * Find the cell correlated cell range
     */
    private CellRangeAddress findCellRangeAddress(Sheet sheet, Cell cell) {
        CellRangeAddress cellRangeAddress = null;
        for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
            cellRangeAddress = sheet.getMergedRegion(i);
            boolean isInRange = cellRangeAddress.isInRange(cell.getRowIndex(), cell.getColumnIndex());
            if (isInRange) return cellRangeAddress;
        }
        return cellRangeAddress;
    }

    /**
     * Convert the cell value to string
     *
     * @param cell
     * @return converted string
     */
    public String getStringCellValue(Cell cell, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, String dictName) {
        if (null == cell || Cell.CELL_TYPE_BLANK == cell.getCellType() || Cell.CELL_TYPE_ERROR == cell.getCellType())
            return StringUtils.EMPTY;
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            try {
                return formatter.formatCellValue(cell, evaluator);
            } catch (Exception e) {
                BusinessWarning warning = new BusinessWarning(BusinessWarning.EXCEL_CELL_EVALUATION_FAIL, cell.getRowIndex(), cell.getColumnIndex(), cell.getSheet().getSheetName(),dictName, e.getMessage());
                warnings.add(warning);
                log.warn(warning.toString());
                return StringUtils.EMPTY;
            }
        }
        return formatter.formatCellValue(cell);
    }

    /**
     * Read the title row for the dictionary
     *
     * @param row the title data row
     */
    private static Map<String, Integer> readTitleRow(Row row) {
        Map<String, Integer> colIndexes = new HashMap<String, Integer>();
        for (Cell cell : row) {
            if (null == cell) continue;
            String cellValue = cell.getStringCellValue().trim();

            if (cellValue.isEmpty()) continue;
            colIndexes.put(cellValue, cell.getColumnIndex());
        }
        return colIndexes;
    }
}
