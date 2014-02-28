package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.OTCPCParser;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OTCPCGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(OTCPCGenerator.class);


    @Autowired
    private DaoService dao;
    private static final int RESERVED_ROW_NUM = 100;

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.OTC_EXCEL;
    }

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
        File targetFile = new File(targetDir, dict.getName());

        OutputStream os = null;
        InputStream is = null;

        try {
            is = null;
//            os =FileUtils.openOutputStream(targetFile);
//            IOUtils.copy(is, os);
//            is = new AutoCloseInputStream(FileUtils.openInputStream(targetFile));
            Workbook wb = createWorkbook(dict, is);
            os = FileUtils.openOutputStream(targetFile);
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
        }

    }

    private void generateInfoSheet(Workbook wb, Dictionary dict, CellStyle boldStyle) {
        // generate "information" sheet
        Sheet infoSheet = wb.createSheet(OTCPCParser.SHEET_INFO);
        String info = dict.getAnnotation1();
        if (StringUtils.isBlank(info)) return;

        int rowNum = 0;
        try {
            boolean firstLine = true;
            for (String line : IOUtils.readLines(new StringReader(info))) {
                Row row = infoSheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                if (firstLine && !line.trim().isEmpty()) {
                    firstLine = false;
                    cell.setCellStyle(boldStyle);
                }

                cell.setCellValue(line);
            }
        } catch (IOException e) {
            log.warn("Dictionary {} annotation1 {}", dict.getName(), info);
        }
    }

    private Collection<CellReference> convertLabelKeyToCellRef(String formulaKeys, Sheet sheet, Integer idColumnNum, Integer valueColumnNum) {
        Collection<CellReference> cellReferences = new ArrayList<CellReference>();
        List<String> labelKeys = Arrays.asList(formulaKeys.split(","));
        for (String labelKey : labelKeys) {
            for (Row row : sheet) {
                Cell labelKeyCell = row.getCell(idColumnNum);
                if (labelKeyCell.getStringCellValue().trim().equals(labelKey)) {
                    cellReferences.add(new CellReference(row.getRowNum(), valueColumnNum));
                    break;
                }
            }
        }
        if (labelKeys.size() != cellReferences.size()) return null;
        return cellReferences;
    }


    /**
     * Update formula in case of Label update in DMS
     */
    private String updateFormula(String formula, CellReference[] params) {
        Pattern p = Pattern.compile("[a-zA-Z]{1,2}\\d{1,6}");
        // matcher
        Matcher m = p.matcher(formula);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (m.find()) {
            m.appendReplacement(sb, params[i++].formatAsString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Restore formula cell from formulaInfo
     *
     * @param formulaInfo    formula string info which from annotation
     * @param originalCell   original cell
     * @param value          default cell value if formula ref cell is not found
     * @param currentSheet   current sheet to be filled
     * @param labelKeyColumn column number of label key
     * @param valColumn      column number of value(reference or translation)
     * @param greyStyle      grey cell style
     */
    private Cell restoreFormulaCell(String formulaInfo, Cell originalCell, String value, Sheet currentSheet, int labelKeyColumn, int valColumn, CellStyle greyStyle) {
        if (StringUtils.isBlank(formulaInfo)) {
            originalCell.setCellValue(value);
            originalCell.setCellType(Cell.CELL_TYPE_STRING);
            return originalCell;
        }

        Map<String, String> params = Util.string2Map(formulaInfo);
        String formula = params.get("formula");
        String formulaKeys = params.get("formulaRefLabelKeys");
        Collection<CellReference> cellReferences = convertLabelKeyToCellRef(formulaKeys, currentSheet, labelKeyColumn, valColumn);
        if (null == cellReferences) {
            originalCell.setCellValue(value);
            originalCell.setCellType(Cell.CELL_TYPE_STRING);
            return originalCell;
        }

        formula = updateFormula(formula, cellReferences.toArray(new CellReference[0]));
        //convert keys to cell reference
        originalCell.setCellType(Cell.CELL_TYPE_FORMULA);
        originalCell.setCellFormula(formula);
        originalCell.setCellStyle(greyStyle);

        //log if evaluate correct.
        FormulaEvaluator evaluator = originalCell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        int cellType = evaluator.evaluateFormulaCell(originalCell);
        CellReference cr = new CellReference(originalCell.getSheet().getSheetName(), originalCell.getRowIndex(), originalCell.getColumnIndex(), false, false);
        log.info("Generated cell {} formula= {}, evaluated value= {}",
                new Object[]{cr.formatAsString(), formula, originalCell.getStringCellValue()});

        return originalCell;
    }

    private Sheet generateReferenceSheet(Workbook wb, Dictionary dict, int defaultColWidth, MultiKeyMap styleMap) {
        Sheet refSheet = wb.createSheet(OTCPCParser.SHEET_REF);

        CellStyle style = (CellStyle) styleMap.get(OTCExcelCellStyle.BOLD_LIGHT_BLUE, StringUtils.EMPTY);

        int rowNum = 0;
        Row row = refSheet.createRow(rowNum);
        int colIndex = 0;
        refSheet.setColumnWidth(colIndex, defaultColWidth);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_REF_TITLE_ID, style);

        refSheet.setColumnWidth(colIndex, defaultColWidth);
        int refColIndex = colIndex;
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_REF_TITLE_VALUE, style);

        String[] strWidths = Util.string2Map(dict.getAnnotation2()).get(OTCPCParser.SHEET_REF_TITLE_DISPLAY_CHECK).split(",");
        colIndex = drawDisplayCheckColumnHeader(row, strWidths, colIndex, style);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_REF_TITLE_USER_INTERFACE, style);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_REF_TITLE_USED, style);

        rowNum++;
        refSheet.createFreezePane(0, rowNum);

        Collection<Label> labels = dict.getAvailableLabels();

        CellStyle greyStyle = (CellStyle) styleMap.get(OTCExcelCellStyle.GREY, StringUtils.EMPTY);
        if (null == greyStyle) {
            greyStyle = wb.createCellStyle();
            greyStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            greyStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            styleMap.put(OTCExcelCellStyle.GREY, StringUtils.EMPTY, greyStyle);
        }

        CellStyle customBooleanStyle = (CellStyle) styleMap.get(OTCExcelCellStyle.USED, StringUtils.EMPTY);
        if (null == customBooleanStyle) {
            customBooleanStyle = wb.createCellStyle();
            DataFormat dFmt = wb.createDataFormat();
            customBooleanStyle.setDataFormat(dFmt.getFormat("\"True\";\"True\";\"False\""));
            styleMap.put(OTCExcelCellStyle.USED, StringUtils.EMPTY, customBooleanStyle);
        }

        int checkColumnLen = strWidths.length;

        for (Label label : labels) {
            colIndex = 0;
            row = refSheet.createRow(rowNum);
            //key
            CellUtil.createCell(row, colIndex++, label.getKey());
            //value
            Cell referenceCell = row.createCell(colIndex++);

            // restore cell formula
            String formulaInfo = label.getAnnotation2();
            restoreFormulaCell(formulaInfo, referenceCell, label.getReference(), refSheet, colIndex - 2, colIndex - 1, greyStyle);

            String annotation = label.getAnnotation1();
            if (StringUtils.isNotBlank(annotation)) {
                Map<String, String> annotationMap = Util.string2Map(label.getAnnotation1());
                String strMergeNum = annotationMap.get("displayCheckMergeNum");
                if (StringUtils.isNotBlank(strMergeNum)) {
                    Cell cell = drawDisplayCheckColumns(row, refColIndex, colIndex, Integer.parseInt(strMergeNum), checkColumnLen, greyStyle);
                    setDisplayCheckCellStyle(cell, label, styleMap);
                }

                String sRowHeight = Util.string2Map(label.getAnnotation1()).get("rowHeight");
                if (StringUtils.isNotBlank(sRowHeight)) {
                    row.setHeightInPoints(Float.parseFloat(sRowHeight));
                }
                colIndex += checkColumnLen;
                CellUtil.createCell(row, colIndex++, StringUtils.defaultString(annotationMap.get("userInterface")));
                Cell cell = row.createCell(colIndex++, Cell.CELL_TYPE_NUMERIC);
                String used = annotationMap.get("used");
                if (StringUtils.isNotBlank(used)) {
                    cell.setCellValue(Double.parseDouble(used));
                }
                cell.setCellStyle(customBooleanStyle);

            }

            rowNum++;
        }
        refSheet.setColumnWidth(2 + checkColumnLen, 5120);

//        refSheet.setAutoFilter(new CellRangeAddress(0, rowNum - 1, 0, 3 + checkColumnLen));
        refSheet.setAutoFilter(new CellRangeAddress(0, 0, 2 + checkColumnLen, 3 + checkColumnLen));

        // group column
        refSheet.groupColumn(0, 0);
        refSheet.groupColumn(2, checkColumnLen);
        refSheet.groupColumn(checkColumnLen + 2, checkColumnLen + 2);

        return refSheet;
    }

    public static void setDisplayCheckCellStyle(Cell cell, Label label, MultiKeyMap styleMap) {
        Workbook wb = cell.getSheet().getWorkbook();
        if (StringUtils.isBlank(label.getFontName())) return;
        CellStyle cellStyle = (CellStyle) styleMap.get(label.getFontName(), StringUtils.defaultString(label.getFontSize()));
        if (null == cellStyle) {
            Font font = wb.createFont();
            font.setFontName(label.getFontName());
            if (StringUtils.isNotBlank(label.getFontSize())) {
                font.setFontHeightInPoints(Short.parseShort(label.getFontSize()));
            }

            cellStyle = wb.createCellStyle();
            cellStyle.setFont(font);
            styleMap.put(label.getFontName(), label.getFontSize(), cellStyle);
        }
        cell.setCellStyle(cellStyle);

    }

    public static int drawDisplayCheckColumnHeader(Row row, String[] strWidths, int colIndex, CellStyle style) {
        Sheet sheet = row.getSheet();

        int firstColumn = colIndex;
        int lastColumn = colIndex + strWidths.length - 1;

        int rowNum = row.getRowNum();

        Cell cell = null;
        for (String strWidth : strWidths) {
            int width = Integer.parseInt(strWidth);
            sheet.setColumnWidth(colIndex, width);
            if (null == cell) {
                cell = CellUtil.createCell(row, colIndex, OTCPCParser.SHEET_REF_TITLE_DISPLAY_CHECK, style);
            }
            colIndex++;
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstColumn, lastColumn));
        return colIndex;
    }

    /**
     * Draw display check on specific row
     *
     * @param row                   the row to draw.
     * @param refColIndex           the reference column number
     * @param colIndex              the column start index
     * @param mergeNum              the merged cell number
     * @param displayCheckColumnNum the total display check column number
     * @param style                 cell style
     */
    public static Cell drawDisplayCheckColumns(Row row, int refColIndex, int colIndex, int mergeNum, int displayCheckColumnNum, CellStyle style) {
        Cell cell = row.createCell(colIndex, Cell.CELL_TYPE_FORMULA);
        cell.setCellFormula(new CellReference(cell.getRowIndex(), refColIndex).formatAsString());

        int rowNum = row.getRowNum();
        row.getSheet().addMergedRegion(new CellRangeAddress(rowNum, rowNum, colIndex, colIndex + mergeNum - 1));

        colIndex++;
        //set the rest of the grid style
        int endGrey = colIndex + displayCheckColumnNum - 1;

        while (colIndex < endGrey) {
            CellUtil.createCell(row, colIndex, "", style);
            colIndex++;
        }
        return cell;
    }

    private void generateLanguageSheet(Workbook wb, Collection<DictionaryLanguage> dictionaryLanguages, CellStyle style) {
        Sheet langSheet = wb.createSheet(OTCPCParser.SHEET_LANG);
        int rowNum = 0;
        int colIndex = 0;

        Row row = langSheet.createRow(rowNum++);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_LANG_TITLE_LANG, style);

        //add reference language first
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
            row = langSheet.createRow(rowNum++);
            colIndex = 0;
            String langCode = dictionaryLanguage.getLanguageCode();
            if (langCode.equals(OTCPCParser.REFERENCE_LANG_CODE)) {
                langCode = OTCPCParser.SHEET_REF;
            }
            CellUtil.createCell(row, colIndex++, langCode);
            CellUtil.createCell(row, colIndex++, dictionaryLanguage.getLanguage().getName());
        }
    }

    private void generateContextSheet(Workbook wb, Sheet refSheet, Collection<Label> labels, CellStyle style) {
        Sheet ctxSheet = wb.createSheet(OTCPCParser.SHEET_CTX);
        int rowNum = 0;
        int colIndex = 1;

        Row row = ctxSheet.createRow(rowNum++);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_CTX_TITLE_DEFAULT, style);
        CellUtil.createCell(row, colIndex++, OTCPCParser.SHEET_CTX_TITLE_DESC, style);

        ctxSheet.createFreezePane(0, rowNum);

        Map<String, Integer> colMap = OTCPCParser.getTitleMap(refSheet);
        int defCellColIndex = colMap.get(OTCPCParser.SHEET_REF_TITLE_VALUE);

        Cell cell = null;
        int startRowNum = rowNum;
        for (Label label : labels) {
            row = ctxSheet.createRow(rowNum);
            colIndex = 0;

            cell = row.createCell(colIndex++, Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(rowNum);

            row.createCell(colIndex++, Cell.CELL_TYPE_FORMULA);

            CellUtil.createCell(row, colIndex++, StringUtils.defaultString(label.getDescription()));
            rowNum++;
        }

        ctxSheet.setColumnWidth(1, 19200);
        ctxSheet.setColumnWidth(2, 19200);

        // set array formula
        rowNum += (RESERVED_ROW_NUM - 1);

        CellRangeAddress refCellRef = new CellRangeAddress(startRowNum, rowNum, defCellColIndex, defCellColIndex);

        int valDefaultColIndex = 1;

        CellRangeAddress ctxCellRef = new CellRangeAddress(startRowNum, rowNum, valDefaultColIndex, valDefaultColIndex);
        String refSheetName = refSheet.getSheetName();
        String refRangeString = refCellRef.formatAsString();
        String formula = String.format("IF(%s!%s<>0,%s!%s,\"\")", refSheetName, refRangeString, refSheetName, refRangeString);
        wb.setSheetOrder(ctxSheet.getSheetName(), 2);
        ctxSheet.setArrayFormula(formula, ctxCellRef);
    }

    private void generateTranslationSheet(Workbook wb, Sheet refSheet, Collection<Label> labels, DictionaryLanguage dictionaryLanguage, int defaultColWidth, MultiKeyMap styleMap) {
        Sheet langSheet = wb.createSheet(dictionaryLanguage.getLanguageCode());
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        CellStyle style = (CellStyle) styleMap.get(OTCExcelCellStyle.BOLD_LIGHT_BLUE, StringUtils.EMPTY);
        CellStyle greyStyle = (CellStyle) styleMap.get(OTCExcelCellStyle.GREY, StringUtils.EMPTY);
        if (null == greyStyle) {
            greyStyle = wb.createCellStyle();
            greyStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            greyStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        }

        //Title row
        int rowNum = 0;
        Row row = langSheet.createRow(rowNum++);

        int colIndex = 0;
        langSheet.setColumnWidth(colIndex, defaultColWidth);
        CellUtil.createCell(row, colIndex++, OTCPCParser.TITLE_ID, style);

        langSheet.setColumnWidth(colIndex, defaultColWidth);
        CellUtil.createCell(row, colIndex++, OTCPCParser.TITLE_DEFAULT, style);

        langSheet.setColumnWidth(colIndex, defaultColWidth);
        int valueIndex = colIndex;
        CellUtil.createCell(row, colIndex++, OTCPCParser.TITLE_VALUE, style);

        Dictionary dict = dictionaryLanguage.getDictionary();
        String[] strWidths = Util.string2Map(dict.getAnnotation2()).get(OTCPCParser.SHEET_REF_TITLE_DISPLAY_CHECK).split(",");
        colIndex = drawDisplayCheckColumnHeader(row, strWidths, colIndex, style);

        langSheet.createFreezePane(0, rowNum);

        int checkColumnLen = strWidths.length;


        //Translation row
        Cell cell = null;

        int startRowNum = rowNum;
        for (Label label : labels) {
            row = langSheet.createRow(rowNum);
            colIndex = 0;

            row.createCell(colIndex++, Cell.CELL_TYPE_FORMULA);
            row.createCell(colIndex++, Cell.CELL_TYPE_FORMULA);
            // generate translation cell
            Cell tranCell = row.createCell(colIndex++);
            LabelTranslation lt = label.getOrigTranslation(dictionaryLanguage.getLanguageCode());
            // if not translated, generate an empty cell
            Translation to = label.getTranslationObject(dictionaryLanguage.getLanguageCode());
            String translation = to.getStatus() == Translation.STATUS_TRANSLATED ? to.getTranslation() : "";
            if (null != lt) { // may use formula
                restoreFormulaCell(lt.getAnnotation2(), tranCell,
                        translation, refSheet, 0, 2, greyStyle);
            }  else{
                tranCell.setCellValue(translation);
            }

            String annotation = label.getAnnotation1();
            if (StringUtils.isNotBlank(annotation)) {
                Map<String, String> annotationMap = Util.string2Map(label.getAnnotation1());
                String strMergeNum = annotationMap.get("displayCheckMergeNum");
                if (StringUtils.isNotBlank(strMergeNum)) {
                    cell = drawDisplayCheckColumns(row, valueIndex, colIndex, Integer.parseInt(strMergeNum), strWidths.length, greyStyle);
                    setDisplayCheckCellStyle(cell, label, styleMap);
                }

                String sRowHeight = Util.string2Map(label.getAnnotation1()).get("rowHeight");
                if (StringUtils.isNotBlank(sRowHeight)) {
                    row.setHeightInPoints(Float.parseFloat(sRowHeight));
                }
            }

            rowNum++;
        }

        rowNum += (RESERVED_ROW_NUM - 1);

        Map<String, Integer> colMap = OTCPCParser.getTitleMap(refSheet);
        int refColIndex = colMap.get(OTCPCParser.SHEET_REF_TITLE_ID);
        CellRangeAddress refCellRef = new CellRangeAddress(startRowNum, rowNum, refColIndex, refColIndex);

        colIndex = 0;
        CellRangeAddress ctxCellRef = new CellRangeAddress(startRowNum, rowNum, colIndex, colIndex);
        String refSheetName = refSheet.getSheetName();
        String refRangeString = refCellRef.formatAsString();
        String formula = String.format("IF(%s!%s<>0,%s!%s,\"\")", refSheetName, refRangeString, refSheetName, refRangeString);
//        log.info("Sheet {} array formula {}, ctx range: {}", new Object[]{langSheet.getSheetName(), formula, ctxCellRef});
        langSheet.setArrayFormula(formula, ctxCellRef);

        colIndex++;

        refColIndex = colMap.get(OTCPCParser.SHEET_REF_TITLE_VALUE);
        refCellRef = new CellRangeAddress(startRowNum, rowNum, refColIndex, refColIndex);
        ctxCellRef = new CellRangeAddress(startRowNum, rowNum, colIndex, colIndex);
        refRangeString = refCellRef.formatAsString();
        formula = String.format("IF(%s!%s<>0,%s!%s,\"\")", refSheetName, refRangeString, refSheetName, refRangeString);
//        log.info("Sheet {} array formula {}, ctx range: {}", new Object[]{langSheet.getSheetName(), formula, ctxCellRef});
        langSheet.setArrayFormula(formula, ctxCellRef);

        // group column
        langSheet.groupColumn(0, 0);
        langSheet.groupColumn(3, checkColumnLen + 1);

        langSheet.setForceFormulaRecalculation(true);
    }

    /**
     * Create workbook according to dict
     */
    private Workbook createWorkbook(Dictionary dict, InputStream template) throws IOException {

        HSSFWorkbook wb = null != template ? new HSSFWorkbook(template) : new HSSFWorkbook();

        int defaultColWidth = 256 * 40;
        MultiKeyMap styleMap = new MultiKeyMap();
        // create the workbook used styles

        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        CellStyle style = wb.createCellStyle();
        style = wb.createCellStyle();
        style.setFont(font);
        styleMap.put(OTCExcelCellStyle.BOLD, StringUtils.EMPTY, style);

        style = wb.createCellStyle();

        //creating a custom palette for the workbook
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex((short) 9, (byte) 204, (byte) 255, (byte) 255);
        style.setFillForegroundColor((short) 9);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(font);
        styleMap.put(OTCExcelCellStyle.BOLD_LIGHT_BLUE, StringUtils.EMPTY, style);

        generateInfoSheet(wb, dict, (CellStyle) styleMap.get(OTCExcelCellStyle.BOLD, StringUtils.EMPTY));

        Collection<DictionaryLanguage> dictionaryLanguageCollection = dict.getDictLanguages();
        generateLanguageSheet(wb, dictionaryLanguageCollection, (CellStyle) styleMap.get(OTCExcelCellStyle.BOLD, StringUtils.EMPTY));


        // generate "default" sheet
        Sheet refSheet = generateReferenceSheet(wb, dict, defaultColWidth, styleMap);


        Collection<Label> labels = dict.getAvailableLabels();

        // generate context sheet.
        generateContextSheet(wb, refSheet, labels, (CellStyle) styleMap.get(OTCExcelCellStyle.BOLD_LIGHT_BLUE, StringUtils.EMPTY));


        // generate a sheet for each language
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguageCollection) {
            if (dictionaryLanguage.getLanguageCode().equals(OTCPCParser.REFERENCE_LANG_CODE)) continue;
            generateTranslationSheet(wb, refSheet, labels, dictionaryLanguage, defaultColWidth, styleMap);
        }

        return wb;
    }
}
