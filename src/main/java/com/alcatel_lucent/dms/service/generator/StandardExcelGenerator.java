package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static com.alcatel_lucent.dms.service.parser.StandardExcelDictParser.*;

@Component
public class StandardExcelGenerator extends DictionaryGenerator {

    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        generateDict(targetDir, dict);
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.STD_EXCEL;
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        File targetFile = new File(targetDir, dict.getName());

        OutputStream os=null;
        try {
            IOUtils.copy(getClass().getResourceAsStream("StandardExcelTemplate.xls"), FileUtils.openOutputStream(targetFile));
            Workbook wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(targetFile)));
            fillWorkbook(wb, dict);
            os =  FileUtils.openOutputStream(targetFile);
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void fillWorkbook(Workbook wb, Dictionary dict) {
        Sheet sheet = wb.getSheetAt(0);

        //Write the header of the file
        Collection<DictionaryLanguage> dctLanguages = dict.getDictLanguages();
        Row titleRow = sheet.getRow(0);
        Cell cell = null;
            /*
            index 0: LABELS
            index 1: Max length
            * */
        HashedMap colIndexes = new HashedMap();
        colIndexes.put(LABEL, 0);
        colIndexes.put(MAX_LENGTH, 1);
        colIndexes.put(CONTEXT, 2);
        colIndexes.put(DESCRIPTION, 3);
        int i = 4;
        String langCode;
        CellStyle style = titleRow.getCell(1).getCellStyle();

        for (DictionaryLanguage dctLanguage : dctLanguages) {
            cell = titleRow.createCell(i);
            langCode = dctLanguage.getLanguageCode();
            cell.setCellValue(langCode);
            cell.setCellStyle(style);

            colIndexes.put(langCode, i++);
        }
//          Write the labels
        i = 1;
        Collection<Label> labels = dict.getAvailableLabels();
        Row row = null;
        MapIterator itr = null;
        for (Label label : labels) {
            row = sheet.createRow(i++);
            itr = colIndexes.mapIterator();
            while (itr.hasNext()) {
                String colName = (String) itr.next();
                Integer index = (Integer) itr.getValue();
                cell = row.createCell(index);
                if (colName.equals(LABEL)) {
                    cell.setCellValue(label.getKey());
                } else if (colName.equals(MAX_LENGTH)) {
                    cell.setCellValue(label.getMaxLength());
                } else if (colName.equals(CONTEXT)) {
                    cell.setCellValue(label.getContext().getName());
                } else if (colName.equals(DESCRIPTION)) {
                    cell.setCellValue(label.getDescription());
                } else if (colName.equals(REF_LANG_CODE)) {
                    cell.setCellValue(label.getReference());
                } else { //All the other language translations
                    cell.setCellValue(label.getTranslation(colName));
                }
            }
        }
    }
}
