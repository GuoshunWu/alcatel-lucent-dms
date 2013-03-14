package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import static com.alcatel_lucent.dms.service.parser.VLEExcelDictParser.LABEL;
import static com.alcatel_lucent.dms.service.parser.VLEExcelDictParser.MAX_LENGTH;
import static com.alcatel_lucent.dms.service.parser.VLEExcelDictParser.REF_LANG_CODE;

@Component("VLEExcelGenerator")
public class VLEExcelGenerator extends DictionaryGenerator {

    private Logger log = LoggerFactory.getLogger(VLEExcelGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        generateDict(targetDir, dict);
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        File targetFile = new File(targetDir, dict.getName());
        try {
            IOUtils.copy(getClass().getResourceAsStream("VLEDictTemplate.xls"), FileUtils.openOutputStream(targetFile));

            Workbook wb = WorkbookFactory.create(new AutoCloseInputStream(new FileInputStream(targetFile)));
            Sheet sheet = wb.getSheetAt(0);
            CreationHelper helper = wb.getCreationHelper();

//            Write the header of the file
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
            int i = 2;
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
                        cell.setCellValue(Integer.parseInt(label.getMaxLength()));
                    } else if (colName.equals(REF_LANG_CODE)) {
                        cell.setCellValue(label.getReference());
                    } else { //All the other language translations
                        cell.setCellValue(label.getTranslation(colName));
                    }
                }
            }

            wb.write(FileUtils.openOutputStream(targetFile));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}
