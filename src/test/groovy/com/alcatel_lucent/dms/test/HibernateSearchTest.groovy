package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.model.Application
import com.alcatel_lucent.dms.model.Label
import com.alcatel_lucent.dms.model.Product
import com.alcatel_lucent.dms.model.Translation
import com.alcatel_lucent.dms.service.DaoService
import org.apache.commons.lang3.tuple.Pair
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Fieldable
import org.apache.lucene.index.Term
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.FuzzyQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.Sort
import org.apache.lucene.search.SortField
import org.apache.lucene.util.Version
import org.apache.solr.handler.component.TermsComponent
import org.hibernate.search.FullTextQuery
import org.hibernate.search.FullTextSession
import org.hibernate.search.Search
import org.hibernate.search.query.dsl.QueryBuilder
import org.hibernate.search.query.dsl.impl.Helper
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

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

//    @Test
    void testM2MSearch() {
        long start = System.currentTimeMillis()
        List<com.alcatel_lucent.dms.model.Dictionary> dictionaries = dao.retrieve("from Dictionary as dict left join fetch dict.applications as app left join fetch app.products as prod")
        long end = System.currentTimeMillis()
        println "Total used ${end - start} ms."
        HashMap<Long, Dictionary> dictMap = new HashMap<>()
        // cache dictionaries in a map
        dictionaries.each { dict ->
            dictMap.put(dict.id, dict)
        }

        FullTextSession fullTextSession = Search.getFullTextSession(dao.getSession())
        QueryBuilder qb = fullTextSession.searchFactory.buildQueryBuilder().forEntity(Label.class).get()
        org.apache.lucene.search.Query query = qb
                .bool()
                .must(qb.keyword().onField('reference').matching('starting').createQuery())
                .must(qb.keyword().onField('removed').matching(false).createQuery())
                .must(qb.keyword().onField('dictionary.id').matching(104L).createQuery())
                .createQuery()
        FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Label.class)
        List<Label> result = []
        hibQuery.list().each { Label label ->
            List detailingLabels = detailingLabel(label, dictMap)
            result.addAll(detailingLabels)
        }

    }

    private List<Label> detailingLabel(Label label, HashMap<Long, com.alcatel_lucent.dms.model.Dictionary> dictMap) {
        List<Label> labels = []
        com.alcatel_lucent.dms.model.Dictionary dict = dictMap[label.dictionary.id]
        dict.applications.each { Application app ->
            app.products.each { Product product ->
                Label tmpLabel = label.clone()
                tmpLabel.dictionary = dict

                tmpLabel.app = app
                tmpLabel.prod = product
                labels.add(tmpLabel)
            }
        }
        return labels
    }

    @Test
    void testHibSearch() {
        //        total result size
        int pageNumber = 1  //http parameter page
        int pageSize = 200    //http parameter rows
        Pair<Integer, List> result = dao.hibSearchRetrieve(
                Translation.class,
                [status: 2, 'language.id': 21],
                ['text.reference': 'Patch No.'], 0.8f,
                (pageNumber - 1) * pageSize, pageSize,
                new Sort(new SortField(null, SortField.SCORE, true)),
                FullTextQuery.SCORE,
                FullTextQuery.THIS
        )
        println "Page number: ${pageNumber}, page size: ${pageSize}, total records: ${result.left}"

        List<Object[]> list = result.right
        println 'Querying result: '
        list.each {entry ->
            Float score = entry[0]
            Translation trans = entry[1]
            println "${'*' * 100}\n${score}, ${trans.id}, ${trans.text.reference}, ${trans.translation}"
        }
        println "Page ${list.size()} record(s).".center(100, '=')
    }

//    @Test
    void testQueryParser() {

        String queryString = "\"(The AND Story AND Day)\"~4"
        QueryParser parser = new QueryParser(Version.LUCENE_31, "title", new StandardAnalyzer(Version.LUCENE_31))
//        parser = new ComplexPhraseQueryParser(Version.LUCENE_31, "title", new StandardAnalyzer(Version.LUCENE_31))
        Query query = parser.parse(queryString)

        Assert.assertEquals("title:story title:day", query.toString())
    }

//    @Test
    void testLabelRest() {
        FullTextSession fullTextSession = Search.getFullTextSession(dao.getSession())
//        fullTextSession.createIndexer()
//                .startAndWait()
//        return

        QueryBuilder qb = fullTextSession.searchFactory.buildQueryBuilder().forEntity(Translation.class).get()
        /*
        Lucene search syntax: +_hibernate_class:com.alcatel_lucent.dms.model.Translation +text.reference:text~0.8 + status:2 + language.id:46
        * */


        Query query = qb.bool()
                .must(qb.keyword().fuzzy().withThreshold(0.8).onField("text.reference").matching('starting').createQuery()
        ).must(qb.keyword().onField("status").matching(2).createQuery()
        ).must(qb.keyword().onField("language.id").matching(46).createQuery()
        ).createQuery()
//        Query query = qb.phrase().onField("reference_forSort").sentence("This is a test.").createQuery()
        println "Query string: ${query.toString()}".center(100, '=')

//        return

        // wrap Lucene query in a org.hibernate.Query
        List list
        long start = System.nanoTime()

        FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Translation.class)

        hibQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS)
        hibQuery.sort = new Sort(new SortField(null, SortField.SCORE, true))
//        total result size
        int pageNumber = 1  //http parameter page
        int pageSize = 400    //http parameter rows
        println "Page number: ${pageNumber}, page size: ${pageSize}, total records: ${hibQuery.resultSize}"

        hibQuery.firstResult = (pageNumber - 1) * pageSize
        hibQuery.maxResults = pageSize

        list = hibQuery.list()

//        list = dao.retrieve('from Label where reference like :ref and removed=:removed and dictionary.id =:dictId', [ref: '%What%' , removed: false, dictId: 313L] as Map)
        long end = System.nanoTime()
        long duration = end - start

        long ms = duration / 1000000
        long us = duration % 1000000 / 1000

        println(ms + " ms, " + us + "us.")

//        DurationFormatUtils.formatPeriod(start, end, "SS 'minisecond(s)'")

        //execute search
        println 'Querying result: '
        list.each {entry ->
            Float score = entry[0]
            Translation trans = entry[1]
            println "${'*' * 100}\n${score}, ${trans.id}, ${trans.text.reference}, ${trans.translation}"
        }
        println "Page ${list.size()} record(s).".center(100, '=')
    }

}
