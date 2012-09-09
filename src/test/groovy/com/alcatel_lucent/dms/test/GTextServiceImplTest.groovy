package com.alcatel_lucent.dms.test

import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import com.alcatel_lucent.dms.service.TextService
import com.alcatel_lucent.dms.service.LanguageService
import com.alcatel_lucent.dms.model.Language
import org.springframework.beans.factory.annotation.Autowired
import org.junit.Test
import org.junit.BeforeClass
import com.alcatel_lucent.dms.model.Text

import static org.junit.Assert.*
import com.alcatel_lucent.dms.service.DaoService
import net.sf.json.JSONObject
import com.alcatel_lucent.dms.BusinessException

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
	
	@Autowired
	private LanguageService langService;



    @Test
    void testReceiveTranslation() {
        String dir = 'Z:/ICSR6.6_incorrect_character'
        //dir='dct_test_files'
        [  'AR', 'CA', 'CZ', 'DA', 'DE', 'ES', 'FI', 'HU','PL', 'KO' , 'NO', 'RU', 'ZH-CN', 'ZH-TW'
        ].each { langCode ->

            String fileName = new File(dir, "${langCode}.xls").absolutePath;
            //query languageID

            Long languageId = dao.retrieveOne('''select distinct language.id from ISOLanguageCode where
            code like :lowerCode or code like :upperCode''',
                    ['lowerCode': "%${langCode.toLowerCase()}%".toString(), 'upperCode': "%${langCode.toUpperCase()}%".toString()]) as Long
			Language language = langService.getLanguage(langCode == 'CZ' ? 'CS' : langCode);
            assertNotNull "code for $langCode Language Id not found", language
            int count = -1;
            try {
                count = textService.receiveTranslation(fileName, language.getId())
            } catch (BusinessException e) {
                e.printStackTrace()
            }
            if (-1 == count) {
                println "Import file '$fileName' failed"
                return
            }
            println "File '$fileName' has been merged, totally updated $count Translation. ".center(80, '=')
        }
    }
}
