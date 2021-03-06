package com.alcatel_lucent.dms.service.generator

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.model.DictionaryBase
import com.alcatel_lucent.dms.service.parser.StandardExcelDictParser
import org.apache.commons.collections.MapIterator
import org.apache.commons.collections.map.HashedMap
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.junit.*
import org.junit.runner.RunWith
import org.logicalcobwebs.proxool.ProxoolFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

import static org.apache.commons.io.FileUtils.copyFile

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */


@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class VLEExcelDictGeneratorTest {

    @Autowired
    private StandardExcelGenerator VLEExcelGenerator;
    @Autowired
    private StandardExcelDictParser VLEExcelDictParser;

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    static void tearDownAfterClass() throws Exception {
    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {
        ProxoolFacade.shutdown(0);
    }

//    @Test
    void testPOIGenerator(){
        HSSFWorkbook wb = new HSSFWorkbook()
        Sheet sheet =wb.createSheet("Test1")
        Row row =sheet.createRow(0)
        CellUtil.createCell(row, 0, "Hello world.")
        row = sheet.createRow(1)
        Cell cell = row.createCell(0)
        cell.setCellType(Cell.CELL_TYPE_FORMULA)
        String refCell = "A1"
        String formula = refCell
        formula = String.format("CONCATENATE(%s,\" - %%s\")", refCell)
        formula = String.format "IF(isblank(%s),\" not blank a1\",CONCATENATE(\"Hello\", \" - %%s.\"))", refCell
        formula = String.format "IF(isblank(%s),\" not blank a1\",CONCATENATE(A1, \" - %%s.\"))", refCell
        formula = String.format "IF(isblank(%s),\" not blank a1\",CONCATENATE(text(a1, \"#\"), \" - %%s.\"))", refCell

        cell.setCellFormula(formula)
        wb.forceFormulaRecalculation = true
        wb.write new FileOutputStream( "d:/test/test.xls")


    }

//    @Test
    void testGenerateDict() throws Exception {
        println "=" * 100

        File file = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/VLEDict/test")
        ArrayList<Dictionary> dictionaries = VLEExcelDictParser.parse('D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/VLEDict/test', file, [] as Collection<File>)
        dictionaries.each { dict ->
            VLEExcelGenerator.generateDict(new File("E:/test"), dict, null)
        }

    }

//    @Test
    void testMapIterator() {
        HashedMap map = ["LABEL": 1, "Max Length": 2, "English": 3, "France": 4]
        MapIterator itr = map.mapIterator()
        println "FirstTime".center(100, '=')
        while (itr.hasNext()) {
            String key = itr.next()
            Integer value = itr.getValue()

            println "Key=$key, value= $value."
        }
        println "Second time".center(100, '=')
        itr = map.mapIterator()
        while (itr.hasNext()) {
            String key = itr.next()
            Integer value = itr.getValue()

            println "Key=$key, value= $value."
        }
    }
}
