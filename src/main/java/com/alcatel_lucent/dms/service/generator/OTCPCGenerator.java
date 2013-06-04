package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.OTCPCParser;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Component
public class OTCPCGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(OTCPCGenerator.class);
    @Autowired
    private DaoService dao;
    
    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_EXCEL;
    }

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        File targetFile = new File(targetDir, dict.getName());

        Workbook wb = createWorkbook(dict);
        try {
            wb.write(FileUtils.openOutputStream(targetFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create workbook according to dict
     */
    private Workbook createWorkbook(Dictionary dict) {
        Workbook wb = new HSSFWorkbook();
        Collection<DictionaryLanguage> dictionaryLanguageCollection = dict.getDictLanguages();
        int defaultColWidth = 256 * 40;
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        
        // generate "default" sheet
        int rowNum = 0;
        Sheet refSheet = wb.createSheet(OTCPCParser.SHEET_REF);
        Row row = refSheet.createRow(rowNum++);
        refSheet.setColumnWidth(0, defaultColWidth);
        Cell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
        cell.setCellValue(OTCPCParser.TITLE_ID);
        cell.setCellStyle(style);

        refSheet.setColumnWidth(1, defaultColWidth);
        cell = row.createCell(1, Cell.CELL_TYPE_STRING);
        cell.setCellValue(OTCPCParser.TITLE_VALUE);
        cell.setCellStyle(style);
        
        Collection<Label> labels = dict.getAvailableLabels();
        for (Label label : labels) {
            row = refSheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(label.getKey());
            cell = row.createCell(1);
            cell.setCellValue(label.getReference());
        }
        
        // generate a sheet for each language
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguageCollection) {
            Sheet langSheet = wb.createSheet(dictionaryLanguage.getLanguageCode());

            int colIndex = 0;
            rowNum = 0;
            //Title row
            row = langSheet.createRow(rowNum++);

            langSheet.setColumnWidth(colIndex, defaultColWidth);
            cell = row.createCell(colIndex++, Cell.CELL_TYPE_STRING);
            cell.setCellValue(OTCPCParser.TITLE_ID);
            cell.setCellStyle(style);

            langSheet.setColumnWidth(colIndex, defaultColWidth);
            cell = row.createCell(colIndex++, Cell.CELL_TYPE_STRING);
            cell.setCellValue(OTCPCParser.TITLE_DEFAULT);
            cell.setCellStyle(style);

            langSheet.setColumnWidth(colIndex, defaultColWidth);
            cell = row.createCell(colIndex++, Cell.CELL_TYPE_STRING);
            cell.setCellValue(OTCPCParser.TITLE_VALUE);
            cell.setCellStyle(style);

            //Translation row
            for (Label label : labels) {
                row = langSheet.createRow(rowNum++);
                colIndex = 0;
                cell = row.createCell(colIndex++);
                cell.setCellValue(label.getKey());

                cell = row.createCell(colIndex++);
                cell.setCellValue(label.getReference());

                cell = row.createCell(colIndex++);
                cell.setCellValue(label.getTranslation(dictionaryLanguage.getLanguageCode()));

            }
        }

        return wb;
    }

}
