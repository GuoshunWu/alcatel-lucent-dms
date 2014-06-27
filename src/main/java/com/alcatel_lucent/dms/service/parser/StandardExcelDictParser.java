package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

@Component("StandardExcelDictParser")
public class StandardExcelDictParser extends DictionaryParser {

    public static final String LABEL = "Labels";
    public static final String MAX_LENGTH = "Max Length";
    public static final String CONTEXT = "Context";
    public static final String DESCRIPTION = "Description";
    public static final String REF_LANG_CODE = "English";
    public static final String DEFAULT_ENCODING = "UTF-16LE";
    public static final List<String> EXTENSION = Arrays.asList("xls", "xlsx");
    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.STD_EXCEL;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        FileFilter fileFilter = new OrFileFilter(new SuffixFileFilter(Arrays.asList(".xls", ".xlsx")), DirectoryFileFilter.INSTANCE);
        if (!file.exists()) return deliveredDicts;
        if (file.isFile()) {
            if (FilenameUtils.isExtension(file.getName(), EXTENSION)) {
                try {
                    deliveredDicts.add(parseDictionary(rootDir, file, acceptedFiles));
                } catch (BusinessException e) {
                    // Ignore INVALID_OTC_PC_DICT_FILE error because the file can be another type of excel dictionary.
                    if (e.getErrorCode() != BusinessException.INVALID_VLE_DICT_FILE) {
                        throw e;
                    }
                }
            }
        } else {
            File[] subFiles = file.listFiles(fileFilter);
            for (File subFile : subFiles) {

                try {
                    deliveredDicts.addAll(parse(normalize(rootDir, true), subFile, acceptedFiles));
                } catch (BusinessException e) {
                    // Ignore INVALID_OTC_PC_DICT_FILE error because the file can be another type of excel dictionary.
                    if (e.getErrorCode() != BusinessException.INVALID_VLE_DICT_FILE) {
                        throw e;
                    }
                }
            }
        }
        return deliveredDicts;
    }

    public Dictionary parseDictionary(String rootDir, File file, Collection<File> acceptedFiles) {
        log.info("Parsing excel file " + file.getName());
        DictionaryBase dictBase = new DictionaryBase();
        Dictionary dictionary = null;

        String dictPath = FilenameUtils.normalize(file.getAbsolutePath(), true);
        String dictName = dictPath;
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }

        dictBase.setName(dictName);
        dictBase.setPath(dictPath);
        dictBase.setEncoding(DEFAULT_ENCODING);
        dictBase.setFormat(Constants.DictionaryFormat.STD_EXCEL.toString());

        dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setReferenceLanguage(REF_LANG_CODE);
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);
        dictionary.setParseWarnings(new ArrayList<BusinessWarning>());


        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(file)));
        } catch (Exception e1) {
            BusinessException exception = new BusinessException(BusinessException.INVALID_EXCEL_FILE);
            log.error(exception.getMessage());
            throw exception;
        }

        Sheet sheet = wb.getSheetAt(0);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        HashedMap colIndexes = null;
        Set<String> labelKeys = new HashSet<String>();
        for (Row row : sheet) {
            /**
             * Suppose that the first row is the title, which determine how to correlate their content in the following rows
             * */
            if (row.getRowNum() == sheet.getFirstRowNum()) {
                colIndexes = readTitleRow(dictionary, row);
                if (!(colIndexes.containsKey(LABEL) && colIndexes.containsKey(MAX_LENGTH) &&
                        colIndexes.containsKey(CONTEXT) && colIndexes.containsKey(DESCRIPTION))) {
                    log.info(file.getName() + " is not a DMS standard excel dictionary.");
                    throw new BusinessException(BusinessException.INVALID_VLE_DICT_FILE, file.getName());
                }
                continue;
            }

            /**
             * Skip the row without label key
             * */
            Cell keyCell = row.getCell((Integer) colIndexes.get(LABEL));
            if (null == keyCell) continue;
//            String labelKey = getStringCellValue(keyCell, evaluator);
//            if (labelKeys.contains(labelKey)) {
//                dictionary.getParseWarnings().add(new BusinessWarning(BusinessWarning.DUPLICATE_LABEL_KEY, row.getRowNum(), labelKey));
//                continue;
//            }
//            labelKeys.add(getStringCellValue(keyCell, evaluator));
            dictionary.getLabels().add(readLabelFromRow(dictionary, row, colIndexes, evaluator));
        }
        acceptedFiles.add(file);
        log.info("Accepted excel file " + file.getName());
        return dictionary;
    }

    /**
     * Read a label and add it into the dictionary.
     *
     * @param dict     the dictionary to be filled
     * @param row      the label data row
     * @param colIndex the map include which index of the specific data belong to.
     */

    private Label readLabelFromRow(Dictionary dict, Row row, HashedMap colIndex, FormulaEvaluator evaluator) {
        /**
         * The contents row
         * */
        MapIterator itr = colIndex.mapIterator();
        Cell cell = null;
        Label label = null;

        label = new Label();
        label.setReference("");
        label.setDictionary(dict);
        label.setSortNo(row.getRowNum());

        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        while (itr.hasNext()) {
            String colName = (String) itr.next();
            Integer index = (Integer) itr.getValue();

            cell = row.getCell(index);
            if (null == cell) {
                log.debug("Row {} column {} has null value.", row.getRowNum(), colName);
                continue;
            }

            String cellContent = getStringCellValue(cell, evaluator);
//            CellReference cellRef = new CellReference(cell);
//            log.info("cell {} value=[{}].", cellRef.formatAsString(), cellContent);

            /**
             * We should put the translations into LabelTranslations or Translation in text object?
             * */

            if (colName.equalsIgnoreCase(LABEL)) {
                label.setKey(cellContent);
            } else if (colName.equalsIgnoreCase(MAX_LENGTH)) {
                label.setMaxLength(cellContent);
            } else if (colName.equalsIgnoreCase(CONTEXT)) {
                if (!cellContent.trim().isEmpty()) {
                    Context ctx = new Context(cellContent);
                    label.setContext(ctx);
                }
            } else if (colName.equalsIgnoreCase(DESCRIPTION)) {
                label.setDescription(cellContent);
            } else {//Language translations
                if (colName.equals(REF_LANG_CODE)) {
                    label.setReference(cellContent);
                } else {
                    LabelTranslation lbTranslation = new LabelTranslation();
                    lbTranslation.setLabel(label);
                    lbTranslation.setLanguageCode(colName);

                    DictionaryLanguage dl = label.getDictionary().getDictLanguage(colName);
                    lbTranslation.setLanguage(null == dl ? null : dl.getLanguage());
                    lbTranslation.setOrigTranslation(cellContent);
                    label.getOrigTranslations().add(lbTranslation);
                }
            }

        }

        return label;
    }

    /**
     * Convert the cell value to string
     *
     * @param cell
     * @return converted string
     */
    private String getStringCellValue(Cell cell, FormulaEvaluator evaluator) {
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            return formatter.formatCellValue(cell, evaluator);
        }
        return formatter.formatCellValue(cell);
    }

    /**
     * Read the title row for the dictionary
     *
     * @param dict the dictionary to be filled
     * @param row  the title data row
     */
    private HashedMap readTitleRow(Dictionary dict, Row row) {
        HashedMap colIndexes = new HashedMap();
        for (Cell cell : row) {
            if (null == cell) continue;
            String cellValue = cell.getStringCellValue().trim();
            if (cellValue.isEmpty()) continue;
            colIndexes.put(cellValue, cell.getColumnIndex());
            if (LABEL.equalsIgnoreCase(cellValue) || MAX_LENGTH.equalsIgnoreCase(cellValue) ||
                    CONTEXT.equalsIgnoreCase(cellValue) || DESCRIPTION.equalsIgnoreCase(cellValue)) continue;

            DictionaryLanguage dl = new DictionaryLanguage();
            dl.setLanguageCode(cellValue);
            dl.setSortNo(cell.getColumnIndex());

            dl.setLanguage(languageService.findLanguageByName(cellValue));
            dl.setCharset(new Charset(DEFAULT_ENCODING));

            dl.setDictionary(dict);
            dict.getDictLanguages().add(dl);
        }

        return colIndexes;
    }

}
