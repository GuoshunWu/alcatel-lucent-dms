package com.alcatel_lucent.dms.test

import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import com.alcatel_lucent.dms.service.TextService
import org.springframework.beans.factory.annotation.Autowired
import org.junit.Test
import org.junit.BeforeClass
import com.alcatel_lucent.dms.model.Text

import static org.junit.Assert.*
import com.alcatel_lucent.dms.service.DaoService
import net.sf.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-28
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GTextServiceImplTest {

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Autowired
    private DaoService dao;

    @Autowired
    private TextService textService;



    @Test
    void testReceiveTranslation(){
        String dir='Z:/ICSR6.6_incorrect_character'
//        dir='dct_test_files'
        String langCode='KO'
        String fileName=new File(dir, "${langCode}.xls").absolutePath;
        //query languageID
        Long languageId =dao.retrieveOne('select language.id from ISOLanguageCode where code like lower(:code)',['code':'ko'])  as Long
        assertNotNull "code for $langCode Language Id not found", languageId
        int count=textService.receiveTranslation(fileName, languageId)
        println count

    }
}
