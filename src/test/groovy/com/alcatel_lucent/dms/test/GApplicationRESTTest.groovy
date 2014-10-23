package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.config.AppConfig
import com.alcatel_lucent.dms.rest.AppTranslationHistoryREST
import com.alcatel_lucent.dms.service.DaoService
import org.intellij.lang.annotations.Language
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.annotation.Transactional

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-31
 * Time: 下午10:03
 * To change this template use File | Settings | File Templates.
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = [AppConfig])

@Transactional //Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class RESTTest {


    @Autowired
    private AppTranslationHistoryREST appTransHistoryREST
    
    @Autowired DaoService dao

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    void testTranslationHistories(){
        Long appId = 1L
        @Language("HQL") String baseSQL = "from Application as a join a.dictionaries as d join d.labels as l join l.text as labelText join labelText.translations as t join t.histories as h where a.id = :appId"
        @Language("HQL") String hql = "select distinct l,h " + baseSQL
        @Language("HQL") String countHql = "select count(*) from (" + hql +") as z"
        Map<String, Object> param = new HashMap()
        param.put("appId", appId)


        List data = dao.retrieve(hql, param)

        Number records = (Number) dao.retrieveOne(countHql, param);
        println "Data size= ${data.size()}, records=${records}"
    }
}
