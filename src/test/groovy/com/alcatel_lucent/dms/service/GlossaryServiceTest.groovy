package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.model.Glossary
import com.alcatel_lucent.dms.model.User
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

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
@Transactional //Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class GlossaryServiceTest {

    @Autowired
    private GlossaryService glossaryService

    @Autowired
    private DaoService daoService

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
    }

//    @Test
    void testCreateGlossary(){
        User user= daoService.retrieveOne("from User where loginName=:loginName", ['loginName': 'test'])
        Glossary glossary=new Glossary('Temp', user)
        glossary = daoService.create(glossary)
        println glossary.createTime
    }



//    @Test
    void testConsistentGlossaries() {
        glossaryService.consistentGlossaries();
    }

//    @Test
    void testGlossaryMatch(){
        GlossaryMatchObject gmo = new GlossaryMatchObject("HTTPS")
        String reference ="Https is a https://instance.opentouch.tv 1231 https://instance.opentouch.tv"
        Assert.assertEquals("HTTPS is a https://instance.opentouch.tv 1231 https://instance.opentouch.tv", pp.getProcessedString(reference))
//        Assert.assertEquals("HTTPS is a HTTPS://instance.opentouch.tv", pp.getProcessedString(reference))

        gmo = new GlossaryMatchObject("OpenTouch")
//        Assert.assertEquals("Https is a https://instance.OpenTouch.tv", pp.getProcessedString(reference))
        Assert.assertEquals("Https is a https://instance.opentouch.tv 1231 https://instance.opentouch.tv", pp.getProcessedString(reference))

    }
}
