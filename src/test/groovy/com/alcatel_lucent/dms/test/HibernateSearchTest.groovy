package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.model.Application
import com.alcatel_lucent.dms.model.Label
import com.alcatel_lucent.dms.model.Product
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
import org.apache.lucene.util.Version
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

//    @Test
    void testHibSearch() {
        //        total result size
        int pageNumber = 1  //http parameter page
        int pageSize = 500    //http parameter rows
        Pair<Integer, List> result = dao.hibSearchRetrieve(Label.class, [reference: 'starting', removed: false] as Map<String, Object>, (pageNumber - 1) * pageSize, pageSize, new Sort())
        println "Page number: ${pageNumber}, page size: ${pageSize}, total records: ${result.left}"

        List<Label> labels = result.right
        println 'Querying result: '
        labels.each { label ->
            println "${'*' * 100}\n${label.id}, ${label.key}, ${label.reference}, ${label.dictionary.base.name}"
        }
        println "Page ${labels.size()} record(s).".center(100, '-')
    }

//    @Test
    void testQueryParser() {

        String queryString = "\"(The AND Story AND Day)\"~4"
        QueryParser parser = new QueryParser(Version.LUCENE_31, "title", new StandardAnalyzer(Version.LUCENE_31))
//        parser = new ComplexPhraseQueryParser(Version.LUCENE_31, "title", new StandardAnalyzer(Version.LUCENE_31))
        Query query = parser.parse(queryString)

        Assert.assertEquals("title:story title:day", query.toString())
    }

    @Test
    void testLabelRest() {
        FullTextSession fullTextSession = Search.getFullTextSession(dao.getSession())
//        fullTextSession.createIndexer()
//                .startAndWait()
//        return

        QueryBuilder qb = fullTextSession.searchFactory.buildQueryBuilder().forEntity(Label.class).get()
        /*
        Lucene search syntax: +reference:starting + removed:false
        * */


        Query query = qb.bool()
                .must(
                qb.keyword().fuzzy().withThreshold(0.8).onField("reference").matching('starting test').createQuery()
        ).must(
                qb.keyword().fuzzy().withThreshold(0.8).onField("removed").matching('false').createQuery()
        ).createQuery()

        println "Query string: ${query.toString()}".center(100, '=')
        return
//        String searchString = "reference:starting"

        // wrap Lucene query in a org.hibernate.Query
        List list
        long start = System.nanoTime()

        FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Label.class)

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
        Map<String, Integer> fieldMap = null
        list.each { label ->
            println "${'*' * 100}\n${label.id}, ${label.key}, ${label.reference}, ${label.dictionary.base.name}"
        }
        println "Page ${list.size()} record(s).".center(100, '=')
    }

    private List<Label> filledLabels(List originalLabels) {
        List<Label> results = []

        Map<String, Integer> fieldMap = null
        originalLabels.each { obj ->
            Label label = obj[0]
            List<Field> fields = (obj[1] as Document).fields
            fieldMap = getFieldIndexMap(fields, 'dictionary.applications.id', 'dictionary.applications.products.id')

            fieldMap['dictionary.applications.id'].each { Field field ->
                Application app = dao.retrieve(Application.class, Long.parseLong(field.stringValue()))
                Label cloneLabel = label.clone()
                cloneLabel.setApp(app)
                results.add(cloneLabel)
            }


        }
        return results
    }

    private MultiValueMap<String, Field> getFieldIndexMap(List<Fieldable> fields, String... fieldNames) {
        MultiValueMap<String, Integer> fieldMap = new LinkedMultiValueMap<String, Integer>()
        fieldNames.each { String fieldName ->
            fields.each { Field field ->
                if (field.name().equals(fieldName)) {
                    fieldMap.add(fieldName, field)
                }
            }
        }
        return fieldMap
    }
}
