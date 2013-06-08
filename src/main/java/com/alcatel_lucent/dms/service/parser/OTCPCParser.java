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
                readDictionaryInformation(sheet, dictionary, evaluator);
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_LANG)) {
                // no action.
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_CTX)) {
                // no action, it is processed in Translation sheet.
            } else if (sheet.getSheetName().equalsIgnoreCase(SHEET_REF)) {
                readRefSheet(i, dictionary, sheet);
            } else
                readTranslationsInSheet(sheet, i, dictionary, evaluator, warnings);
        }
        acceptedFiles.add(file);

        dictionary.setParseWarnings(warnings);
        return dictionary;
    }

    private void readRefSheet(int sortNo, Dictionary dictionary, Sheet sheet) {
        DictionaryLanguage dl = new DictionaryLanguage();
        dl.setLanguageCode(REFERENCE_LANG_CODE);
        dl.setSortNo(sortNo);

        dl.setLanguage(languageService.getLanguage(REFERENCE_LANG_CODE));
        dl.setCharset(new Charset(DEFAULT_ENCODING));

        dl.setDictionary(dictionary);
        dictionary.getDictLanguages().add(dl);

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
    }

    /**
     * Get the information from sheet to dictionary.
     */
    private void readDictionaryInformation(Sheet sheet, Dictionary dictionary, FormulaEvaluator evaluator) {
        StringBuilder sb = new StringBuilder();
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); ++i) {
            Row row = sheet.getRow(i);
            if (null != row) {
                for (Cell cell : row) {
                    if (null == cell) continue;
                    String cellValue = getStringCellValue(cell, evaluator);
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
     * read language translations from translation sheet
     */
    private void readTranslationsInSheet(Sheet sheet, int index, Dictionary dict, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings) {
        String langCode = sheet.getSheetName().trim();
        DictionaryLanguage dl = new DictionaryLanguage();
        dl.setLanguageCode(langCode);
        dl.setSortNo(index);

        dl.setLanguage(languageService.getLanguage(langCode));
        dl.setCharset(new Charset(DEFAULT_ENCODING));

        dl.setDictionary(dict);
        dict.getDictLanguages().add(dl);

        Map<String, Integer> colIndexes = getTitleMap(sheet);

        Collection checkCollection = Arrays.asList(TITLE_ID, TITLE_VALUE, TITLE_DEFAULT, TITLE_DISPLAY_CHECK);
        if (!(CollectionUtils.isSubCollection(checkCollection, colIndexes.keySet()))) {
            throw new BusinessException(BusinessException.INVALID_OTC_EXCEL_DICT_FILE);
        }

        Sheet ctxSheet = sheet.getWorkbook().getSheet(SHEET_CTX);
        Map<String, Integer> ctxColIndexes = getTitleMap(ctxSheet);
        checkCollection = Arrays.asList(SHEET_CTX_TITLE_DEFAULT, SHEET_CTX_TITLE_DESC);
        if (!(CollectionUtils.isSubCollection(checkCollection, ctxColIndexes.keySet()))) {
            throw new BusinessException(BusinessException.INVALID_OTC_EXCEL_DICT_FILE);
        }
        int displayCheckLen = Util.string2Map(dict.getAnnotation2()).get(OTCPCParser.SHEET_REF_TITLE_DISPLAY_CHECK).split(", ").length;

        for (Row row : sheet) {
            if (row.getRowNum() == sheet.getFirstRowNum()) continue;
            /**
             * Skip the row without label key
             * */
            Cell cell = row.getCell(colIndexes.get(TITLE_ID));
            if (null == cell || getStringCellValue(cell, evaluator).isEmpty()) continue;
            readLabelTrans(dict, row, colIndexes, evaluator, warnings, displayCheckLen);
        }

    }

    /**
     * Read a label and add it into the dictionary.
     *
     * @param dict        the dictionary to be filled
     * @param row         the label data row
     * @param colIndexMap the map include which index of the specific data belong to.
     */

    private Label readLabelTrans(Dictionary dict, Row row, Map<String, Integer> colIndexMap, FormulaEvaluator evaluator, Collection<BusinessWarning> warnings, int displayCheckLen) {
        Cell cell = row.getCell(colIndexMap.get(TITLE_ID));
        String labelKey = getStringCellValue(cell, evaluator).trim();

        if (null == cell || StringUtils.isBlank(labelKey)) {
            warnings.add(new BusinessWarning(BusinessWarning.LABEL_KEY_BLANK, row.getSheet().getSheetName(), TITLE_DEFAULT, row.getRowNum()));
        }

        Label label = dict.getLabel(labelKey);

        if (null == label) {
            cell = row.getCell(colIndexMap.get(TITLE_DEFAULT));

            String reference = getStringCellValue(cell, evaluator);

            label = createNewLabel(dict, row, labelKey, reference, colIndexMap, evaluator);
            dict.addLabel(label);
            // get description in context sheet
            Sheet ctxSheet = row.getSheet().getWorkbook().getSheet(SHEET_CTX);
            Map<String, Integer> ctxColIndexesMap = getTitleMap(ctxSheet);
            int colNumber = ctxColIndexesMap.get(SHEET_CTX_TITLE_DESC);
            Cell descCell = CellUtil.getRow(row.getRowNum(), ctxSheet).getCell(colNumber);
            if (null != descCell) label.setDescription(getStringCellValue(descCell, evaluator));
        }

        cell = row.getCell(colIndexMap.get(TITLE_VALUE));
        String translation = getStringCellValue(cell, evaluator);
        if (null == translation || translation.isEmpty()) {
            log.debug("Row {} column {} is blank.", row.getRowNum(), TITLE_VALUE);
            return label;
        }

        String langCode = row.getSheet().getSheetName().trim();
        LabelTranslation lbTranslation = new LabelTranslation();
        lbTranslation.setLabel(label);
        lbTranslation.setLanguageCode(langCode);

        DictionaryLanguage dl = dict.getDictLanguage(langCode);
        lbTranslation.setLanguage(null == dl ? null : dl.getLanguage());

        lbTranslation.setOrigTranslation(translation);
        label.getOrigTranslations().add(lbTranslation);

        return label;
    }

    private Label createNewLabel(Dictionary dict, Row row, String key, String reference, Map<String, Integer> colIndexMap, FormulaEvaluator evaluator) {
        Label label = new Label();
        label.setDictionary(dict);
        label.setSortNo(row.getRowNum());
        label.setKey(key);
        label.setReference(reference);
        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        Map<String, String> annotation = new HashMap<String, String>();

        // get display check column info and store it in label annotation1.
        Cell cell = row.getCell(colIndexMap.get(TITLE_DISPLAY_CHECK));
        if (null != cell) {
            CellRangeAddress cr = findCellRangeAddress(row.getSheet(), cell);
            int lblDisplayCheckMergeNum = cr.getLastColumn() - cr.getFirstColumn() + 1;
            annotation.put("displayCheckMergeNum", lblDisplayCheckMergeNum + "");
        }
        annotation.put("rowHeight", String.valueOf(row.getHeightInPoints()));


        Sheet refSheet = row.getSheet().getWorkbook().getSheet(SHEET_REF);
        Map<String, Integer> refColIndexMap = getTitleMap(refSheet);
        Row refRow = refSheet.getRow(row.getRowNum());

        if (null == refRow) {
            label.setAnnotation1(Util.map2String(annotation));
            return label;
        }
        cell = refRow.getCell(refColIndexMap.get(SHEET_REF_TITLE_DISPLAY_CHECK));
        if (null != cell) {
            CellStyle style = cell.getCellStyle();
            Font font = refSheet.getWorkbook().getFontAt(style.getFontIndex());
            label.setFontName(font.getFontName());
            label.setFontSize(font.getFontHeightInPoints() + "");
        }

        cell = refRow.getCell(refColIndexMap.get(SHEET_REF_TITLE_USER_INTERFACE));
        if (null != cell) {
            String userInterface = getStringCellValue(cell, evaluator);
            annotation.put("userInterface", userInterface);
        }

        cell = refRow.getCell(refColIndexMap.get(SHEET_REF_TITLE_USED));
        if (null != cell) {
            String used = getStringCellValue(cell, evaluator);
            annotation.put("used", used);
        }
        label.setAnnotation1(Util.map2String(annotation));
        return label;
    }

    /**
     * Convert the cell value to string
     *
     * @param cell
     * @return converted string
     */
    public String getStringCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (null == cell || Cell.CELL_TYPE_BLANK == cell.getCellType() || Cell.CELL_TYPE_ERROR == cell.getCellType())
            return StringUtils.EMPTY;
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            try {
                return formatter.formatCellValue(cell, evaluator);
            } catch (Exception e) {
                log.warn("Formula at row {} col {} of sheet {} evaluation failed({}).", new Object[]{cell.getRowIndex(), cell.getColumnIndex(), cell.getSheet().getSheetName(), e.toString()});
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
