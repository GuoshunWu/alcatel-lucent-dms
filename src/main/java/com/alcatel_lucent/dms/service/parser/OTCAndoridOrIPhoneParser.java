package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.normalize;

//@Component
public class OTCAndoridOrIPhoneParser extends DictionaryParser {

    public static final String TITLE_ID = "ID";
    public static final String TITLE_DEFAULT = "Default";
    public static final String TITLE_VALUE = "Value";
    public static final String DEFAULT_ENCODING = "UTF-16LE";
    public static final String[] extensions = new String[]{"xls", "xlsx"};
    @Autowired
    private LanguageService languageService;

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (null == file || !file.exists()) return deliveredDicts;

        if (file.isFile()) {
            if (!FilenameUtils.isExtension(file.getName(), extensions)) {
                try {
                    deliveredDicts.add(parseDictionary(normalize(rootDir, true), file, acceptedFiles));
                } catch (BusinessException e) {
                    // Ignore INVALID_OTC_PC_DICT_FILE error because the file can be another type of excel dictionary.
                    if (e.getErrorCode() != BusinessException.INVALID_OTC_PC_DICT_FILE) {
                        throw e;
                    }
                }
            }
            return deliveredDicts;
        }

        // It is a directory
        Collection<File> OTCFiles = FileUtils.listFiles(file, extensions, true);
        for (File OTCFile : OTCFiles) {
            try {
                deliveredDicts.add(parseDictionary(normalize(rootDir, true), OTCFile, acceptedFiles));
            } catch (BusinessException e) {
                // Ignore INVALID_OTC_PC_DICT_FILE error because the file can be another type of excel dictionary.
                if (e.getErrorCode() != BusinessException.INVALID_OTC_PC_DICT_FILE) {
                    throw e;
                }
            }
        }
        return deliveredDicts;
    }

    public Dictionary parseDictionary(String rootDir, File file, Collection<File> acceptedFiles) {

        DictionaryBase dictBase = new DictionaryBase();
        Dictionary dictionary = null;


        String dictPath = normalize(file.getAbsolutePath(), true);
        String dictName = dictPath;
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }

        dictBase.setName(dictName);
        dictBase.setPath(dictPath);
        dictBase.setEncoding(DEFAULT_ENCODING);
        dictBase.setFormat(Constants.DictionaryFormat.OTC_PC.toString());

        dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);


        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(file)));
        } catch (Exception e) {
            throw new BusinessException(BusinessException.INVALID_EXCEL_FILE, file.getName());
        }

        int numOfSheet = wb.getNumberOfSheets();
        for (int i = 0; i < numOfSheet; ++i) {
            readTranslationsInSheet(wb.getSheetAt(i), i, dictionary);
        }
        acceptedFiles.add(file);


        return dictionary;
    }

    /**
     * Each sheet store a separate language translations
     */
    private void readTranslationsInSheet(Sheet sheet, int index, Dictionary dict) {
        List<String> otherSheets = Arrays.asList("Informations", "Languages", "Context", "default");
        if (otherSheets.contains(sheet.getSheetName().trim())) {
            return;
        }
        String langCode = sheet.getSheetName().trim();

        DictionaryLanguage dl = new DictionaryLanguage();
        dl.setLanguageCode(langCode);
        dl.setSortNo(index);

        dl.setLanguage(languageService.findLanguageByName(langCode));
        dl.setCharset(new Charset(DEFAULT_ENCODING));

        dl.setDictionary(dict);
        dict.getDictLanguages().add(dl);

        Map<String, Integer> colIndexes = null;
        for (Row row : sheet) {
            /**
             * We suppose that the first row is the title, which determine how to correlate their content in the following rows
             * */
            if (row.getRowNum() == sheet.getFirstRowNum()) {
                colIndexes = readTitleRow(row);
//                if (!(CollectionUtils.isSubCollection(Arrays.asList(TITLE_ID, TITLE_DEFAULT, TITLE_VALUE), colIndexes.keySet()))) {
//                    throw new BusinessException(BusinessException.INVALID_OTC_PC_DICT_FILE);
//                }
                continue;
            }

            /**
             * We skip the row without label key
             * */
            Cell cell = row.getCell(colIndexes.get(TITLE_ID));
            if (null == cell || getStringCellValue(cell).isEmpty()) continue;
            dict.getLabels().add(readLabelTrans(dict, row, colIndexes));
        }

    }

    /**
     * Read a label and add it into the dictionary.
     *
     * @param dict     the dictionary to be filled
     * @param row      the label data row
     * @param colIndex the map include which index of the specific data belong to.
     */

    private Label readLabelTrans(Dictionary dict, Row row, Map<String, Integer> colIndex) {
        Cell cell = row.getCell(colIndex.get(TITLE_ID));
        String cellContent = getStringCellValue(cell);

        Label label = dict.getLabel(cellContent);
        if (null == label) {
            cell = row.getCell(colIndex.get(TITLE_DEFAULT));
            if (null == cell) {
                log.debug("Row {} column {} has null value.", row.getRowNum(), TITLE_DEFAULT);
            }
            label = createNewLabel(dict,
                    row.getRowNum(),
                    cellContent,
                    getStringCellValue(cell)
            );
        }

        cell = row.getCell(colIndex.get(TITLE_VALUE));

        cellContent = getStringCellValue(cell);
        if (null == cellContent || cellContent.isEmpty()) {
            log.debug("Row {} column {} has null value.", row.getRowNum(), TITLE_VALUE);
            return label;
        }

        String langCode = row.getSheet().getSheetName().trim();
        LabelTranslation lbTranslation = new LabelTranslation();
        lbTranslation.setLabel(label);
        lbTranslation.setLanguageCode(langCode);

        DictionaryLanguage dl = label.getDictionary().getDictLanguage(langCode);
        lbTranslation.setLanguage(null == dl ? null : dl.getLanguage());
        lbTranslation.setOrigTranslation(cellContent);
        label.getOrigTranslations().add(lbTranslation);

        return label;
    }

    private Label createNewLabel(Dictionary dict, int num, String key, String reference) {
        Label label = new Label();
        label.setDictionary(dict);
        label.setSortNo(num);
        label.setKey(key);
        label.setReference(reference);
        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        return label;
    }

    /**
     * Convert the cell value to string
     *
     * @param cell
     * @return converted string
     */
    private String getStringCellValue(Cell cell) {
        if (null == cell) return StringUtils.EMPTY;
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        FormulaEvaluator evaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            return formatter.formatCellValue(cell, evaluator);
        }
        return formatter.formatCellValue(cell);
    }

    /**
     * Read the title row for the dictionary
     *
     * @param row the title data row
     */
    private Map<String, Integer> readTitleRow(Row row) {
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
