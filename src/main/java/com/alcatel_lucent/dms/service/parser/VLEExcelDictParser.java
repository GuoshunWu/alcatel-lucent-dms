package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@Component("VLEExcelDictParser")
@SuppressWarnings("unchecked")
public class VLEExcelDictParser extends DictionaryParser {

    public static final String LABEL = "LABELS";
    public static final String MAX_LENGTH = "Max Length";
    public static final String REF_LANG_CODE = "English";
    @Autowired
    private LanguageService languageService;

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        FileFilter excelFilter = new SuffixFileFilter(Arrays.asList(".xls", ".xlsx"));
        if (!file.exists()) return deliveredDicts;
        if (file.isFile()) {
            deliveredDicts.add(parseDictionary(rootDir, file, acceptedFiles));
        } else {
            File[] subFiles = file.listFiles(excelFilter);
            for (File subFile : subFiles) {
                deliveredDicts.add(parseDictionary(rootDir, subFile, acceptedFiles));
            }
        }
        return deliveredDicts;
    }

    public Dictionary parseDictionary(String rootDir, File file, Collection<File> acceptedFiles) {

        DictionaryBase dictBase = new DictionaryBase();
        Dictionary dictionary = null;
        try {
            dictBase.setName(file.getCanonicalPath().replace(rootDir, rootDir));
            dictBase.setPath(file.getCanonicalPath());
            dictBase.setEncoding("UTF-8");
            dictBase.setFormat(Constants.DICT_FORMAT_VLEExcel);

            dictionary = new Dictionary();
            dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
            dictionary.setLabels(new ArrayList<Label>());

            dictionary.setBase(dictBase);


            Workbook wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(file)));
            Sheet sheet = wb.getSheetAt(0);
            CreationHelper helper = wb.getCreationHelper();

            HashedMap colIndexes = null;
            for (Row row : sheet) {
                /**
                 * We suppose that the first row is the title, which determine how to correlate their content in the following rows
                 * */
                if (row.getRowNum() == sheet.getFirstRowNum()) {
                    colIndexes = readTitleRow(dictionary, row);
                    if (!(colIndexes.containsKey(LABEL) && colIndexes.containsKey(MAX_LENGTH))) {
                        throw new BusinessException(BusinessException.INVALID_VLE_DICT_FILE);
                    }
                    continue;
                }
                assert colIndexes != null;

                /**
                 * We skip the row without label key
                 * */
                if (null == row.getCell((Integer) colIndexes.get(LABEL))) continue;
                dictionary.getLabels().add(readLabelFromRow(dictionary, row, colIndexes, helper));
            }
            acceptedFiles.add(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            log.warn(new BusinessException(BusinessException.INVALID_EXCEL_FILE).getMessage());
        } catch (BusinessException e) {
            log.warn(e.getMessage());
        }

        return dictionary;
    }

    /**
     * Read a label and add it into the dictionary.
     *
     * @param dict     the dictionary to be filled
     * @param row      the label data row
     * @param colIndex the map include which index of the specific data belong to.
     * @param helper
     */

    private Label readLabelFromRow(Dictionary dict, Row row, HashedMap colIndex, CreationHelper helper) {
        /**
         * The contents row
         * */
        MapIterator itr = colIndex.mapIterator();
        Cell cell = null;
        Label label = null;

        label = new Label();
        label.setDictionary(dict);
        label.setSortNo(row.getRowNum());

        label.setOrigTranslations(new ArrayList<LabelTranslation>());

        while (itr.hasNext()) {
            String colName = (String) itr.next();
            Integer index = (Integer) itr.getValue();

            cell = row.getCell(index);
            if (null == cell) {
                log.warn("Row {} column {} has null value.", row.getRowNum(), colName);
                continue;
            }

            String cellContent = getStringCellValue(cell, helper);
//            CellReference cellRef = new CellReference(cell);
//            log.info("cell {} value=[{}].", cellRef.formatAsString(), cellContent);

            /**
             * We should put the translations into LabelTranslations or Translation in text object?
             * */

            if (colName.equalsIgnoreCase(LABEL)) {
                label.setKey(cellContent);
            } else if (colName.equalsIgnoreCase(MAX_LENGTH)) {
                label.setMaxLength(cellContent);
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
     * @param helper
     * @return converted string
     */
    private String getStringCellValue(Cell cell, CreationHelper helper) {
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        if (Cell.CELL_TYPE_FORMULA == cell.getCellType()) {
            return formatter.formatCellValue(cell, helper.createFormulaEvaluator());
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
            if (LABEL.equalsIgnoreCase(cellValue) || MAX_LENGTH.equalsIgnoreCase(cellValue)) continue;

            DictionaryLanguage dl = new DictionaryLanguage();
            dl.setLanguageCode(cellValue);
            dl.setSortNo(cell.getColumnIndex());

            dl.setLanguage(languageService.findLanguageByName(cellValue));
            dl.setCharset(new Charset("UTF-16LE"));

            dl.setDictionary(dict);
            dict.getDictLanguages().add(dl);
        }

        return colIndexes;
    }

}
