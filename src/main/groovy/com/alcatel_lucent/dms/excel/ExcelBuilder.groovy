package com.alcatel_lucent.dms.excel

import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.web.servlet.view.document.AbstractExcelView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Administrator on 2014/5/18 0018.
 */

class ExcelBuilder extends AbstractExcelView {

    /**
     * Subclasses must implement this method to create an Excel HSSFWorkbook document,
     * given the model.
     * @param model the model Map
     * @param workbook the Excel workbook to complete
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("model in build excel= ${model}")
        HSSFSheet sheet = workbook.createSheet("Spring")
        sheet.setDefaultColumnWidth(12)
        HSSFCell cell = getCell(sheet, 0, 0)
        setText(cell, "Spring-Excel test")

        (model.get("wordList") as List<String>).eachWithIndex { String word, Integer index ->
            setText(getCell(sheet, 2 + index, 0), word)
        }
    }
}
