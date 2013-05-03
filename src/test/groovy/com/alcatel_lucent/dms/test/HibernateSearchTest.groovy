package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.model.Application
import com.alcatel_lucent.dms.model.ApplicationBase
import com.alcatel_lucent.dms.model.ProductBase
import com.alcatel_lucent.dms.model.test.Book
import com.alcatel_lucent.dms.rest.ApplicationREST
import com.alcatel_lucent.dms.service.DaoService
import org.hibernate.search.FullTextSession
import org.hibernate.search.Search
import org.hibernate.search.query.dsl.QueryBuilder
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
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
@ContextConfiguration(locations = ["/spring.xml"])
@Transactional //Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class HibernateSearchTest {

    @Autowired
    private DaoService dao

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    void testExample() {
        FullTextSession fullTextSession = Search.getFullTextSession(dao.getSession())
//        fullTextSession.createIndexer().startAndWait()
        QueryBuilder qb = fullTextSession.searchFactory.buildQueryBuilder().forEntity(Book.class).get()
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onFields('title', 'subtitle', 'authors.name')
//        , 'publicationDate'
                .matching('search')
                .createQuery()

        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, Book.class)

        //execute search
        List result = hibQuery.list()

        print "${'*'* 100}\n${result}\n"

    }
}
