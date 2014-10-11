package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.BaseEntity;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Sort;
import org.hibernate.*;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchException;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */

@Repository("daoService")
public class DaoServiceImpl implements DaoService {
    private static Logger log = LoggerFactory.getLogger(DaoServiceImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

/*
    public DaoServiceImpl(SessionFactory sessionFactory) {
        this.setSessionFactory(sessionFactory);
    }
*/

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
//        return sessionFactory.getCurrentSession();
        return SessionFactoryUtils.getSession(sessionFactory, false);
    }

    public Object retrieve(Class clazz, Serializable id) {
        return retrieve(clazz, id, null);
    }

    public Object retrieve(Class clazz, Serializable id, String[] initProps) {
        log.debug("Retrieve entity " + clazz + "#" + id);
        Object obj = getSession().get(clazz, id);
        if (initProps != null) {
            for (String prop : initProps) {
                try {
                    Object propValue = PropertyUtils.getProperty(obj, prop);
                    if (propValue != null) {
                        Hibernate.initialize(propValue);
                    }
                } catch (Exception e) {
                    log.error("Error while get property " + prop + " of " + obj, e);
                    throw new SystemError(e);
                }
            }
        }

        return obj;
    }

    public List retrieve(String hql) {
        return retrieve(hql, null, null);
    }

    public List retrieve(String hql, String[] initProps) {
        return retrieve(hql, null, initProps);
    }

    public List retrieve(String hql, Map paramMap) {
        return retrieve(hql, paramMap, null);
    }

    public List retrieve(String hql, Map paramMap, String[] initProps) {
        return retrieve(hql, paramMap, 0, -1, initProps);
    }

    private List<String> getAllTermsFromText(String fieldName, String localText, Analyzer analyzer) throws IOException {
        List<String> terms = new ArrayList<String>();

        // Can't deal with null at this point. Likely returned by some FieldBridge not recognizing the type.
        if (localText == null) {
            throw new SearchException("Search parameter on field " + fieldName + " could not be converted. " +
                    "Are the parameter and the field of the same type?" +
                    "Alternatively, apply the ignoreFieldBridge() option to " +
                    "pass String parameters");
        }
        Reader reader = new StringReader(localText);
        TokenStream stream = analyzer.reusableTokenStream(fieldName, reader);

        CharTermAttribute attribute = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        while (stream.incrementToken()) {
            if (attribute.length() > 0) {
                terms.add(attribute.toString());
            }
        }
        stream.end();
        stream.close();
        return terms;
    }

    @Override
    public Pair<Integer, List> hibSearchRetrieve(Class cls, Map<String, Object> keywords,
                                                 Map<String, String> fuzzyKeywords, float minimumSimilarity,
                                                 int firstResult, int maxResults,
                                                 Sort sort,
                                                 String... projection) {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(cls).get();

        Analyzer analyzer = fullTextSession.getSearchFactory().getAnalyzer(cls);

        /*
        * Lucene search syntax: +reference: what + removed: false +dictionary.id:110
        * */
        BooleanJunction bj = qb.bool();
        Set<Map.Entry<String, Object>> keywordEntries = keywords.entrySet();
        for (Map.Entry<String, Object> entry : keywordEntries) {
            bj = bj.must(qb.keyword().onField(entry.getKey()).matching(entry.getValue()).createQuery());
        }

        BooleanQuery bQuery = new BooleanQuery();
        if (null != fuzzyKeywords && !fuzzyKeywords.isEmpty()) {
            Set<Map.Entry<String, String>> fuzzyKeywordEntries = fuzzyKeywords.entrySet();
            for (Map.Entry<String, String> fuzzyKeywordEntry : fuzzyKeywordEntries) {
                List<String> terms = null;
                try {
                    terms = getAllTermsFromText(fuzzyKeywordEntry.getKey(), fuzzyKeywordEntry.getValue(), analyzer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!terms.isEmpty()) {
                    for (String term : terms) {
                        bQuery.add(new FuzzyQuery(new Term(fuzzyKeywordEntry.getKey(), term), minimumSimilarity), BooleanClause.Occur.MUST);
                    }

                }
            }
        }
        bj.must(bQuery);
        org.apache.lucene.search.Query query = bj.createQuery();

        log.info("Lucene search string: \"{}\", firstResult={}, maxResult={}", new Object[]{query, firstResult, maxResults});

        FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, cls);
        int resultSize = hibQuery.getResultSize();
        if (firstResult != 0) {
            hibQuery.setFirstResult(firstResult);
        }
        if (maxResults >= 0) {
            hibQuery.setMaxResults(maxResults);
        }

        if (null != sort) {
            log.info("sort={}", sort.toString());
            hibQuery.setSort(sort);
        }
        if (projection.length > 0) {
            hibQuery.setProjection(projection);
        }
        return Pair.of(resultSize, hibQuery.list());
    }

    public Object merge(Object obj) {
        return getSession().merge(obj);
    }

    public Object retrieveOne(String hql) {
        return retrieveOne(hql, null, null);
    }

    public Object retrieveOne(String hql, String[] initProps) {
        return retrieveOne(hql, null, initProps);
    }

    public Object retrieveOne(String hql, Map paramMap) {
        return retrieveOne(hql, paramMap, null);
    }

    public Object retrieveOne(String hql, Map paramMap, String[] initProps) {
        List qr = retrieve(hql, paramMap, initProps);
        if (!qr.isEmpty()) {
            return qr.iterator().next();
        } else {
            return null;
        }
    }

    public List retrieve(String hql, Map paramMap, int firstResult, int maxResults) {
        return retrieve(hql, paramMap, firstResult, maxResults, null);
    }

    public List retrieve(String hql, Map paramMap, int firstResult, int maxResults, String[] initProps) {
        log.info("Retrieve [" + hql + "] with paramMap: " + paramMap);
        Query query = getSession().createQuery(hql);
        if (paramMap != null) {
            Set paramKey = paramMap.keySet();
            for (Object aParamKey : paramKey) {
                String paramName = (String) aParamKey;
                Object paramValue = paramMap.get(paramName);
                if (paramValue instanceof Collection) {
                    query.setParameterList(paramName, (Collection) paramValue);
                } else {
                    query.setParameter(paramName, paramValue);
                }
            }
        }

        if (firstResult != 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }

        long ts1 = System.currentTimeMillis();
        List results = query.list();
        long ts2 = System.currentTimeMillis();
        if (initProps != null) {
            for (Object obj : results) {
                for (String prop : initProps) {
                    try {
                        Hibernate.initialize(PropertyUtils.getProperty(obj, prop));
                    } catch (Exception e) {
                        log.error("Error while get property " + prop + " of " + obj, e);
                        throw new SystemError(e);
                    }
                }
            }
        }
        if (log.isInfoEnabled()) {
            log.info("result records count: " + results.size() + " time used: " + (ts2 - ts1) + "ms");
        }
        return results;
    }

    public Object create(Object entity) {
        return create(entity, true);
    }

    public Object create(Object entity, boolean flush) {
        if (log.isDebugEnabled()) {
            log.debug("Create entity [" + entity + "]");
        }
        Session session = getSession();
        try {
            session.save(entity);
            if (flush) {
                session.flush();
            }
            return entity;
        } catch (HibernateException e) {
            log.error("Error on creating entity " + entity, e);
            throw e;
        }
    }

    public Object[] createArray(Object[] entities) {
        Object[] returns = new Object[entities.length];
        Session session = getSession();
        for (int i = 0; i < entities.length; i++) {
            returns[i] = create(entities[i], false);
            if ((i + 1) % 100 == 0) {
                session.flush();
//                session.clear();
            }
        }
        session.flush();
//        session.clear();
        return returns;
    }

    public Object update(Object obj) {
        return update(obj, null);
    }

    public Object update(Object obj, String[] propertyNames) {
        Session session = getSession();
        try {
            if (propertyNames == null) {
                obj = session.merge(obj);
            } else {
                Object targetObj = retrieve(obj.getClass(), ((BaseEntity) obj).getId());
                for (String propertyName : propertyNames) {
                    Object value;
                    try {
                        value = PropertyUtils.getProperty(obj, propertyName);
                    } catch (Exception e) {
                        log.error("error while getting property '" + propertyName + "' of " + obj, e);
                        throw new SystemError(e);
                    }
                    if (value != null && value instanceof BaseEntity) {
                        try {
                            if (((BaseEntity) value).getId() != null) {
                                value = retrieve(value.getClass(), ((BaseEntity) value).getId());
                            } else {
                                value = null;
                            }
                        } catch (HibernateException e) {
                            log.warn("error while converse transient object '" + value.getClass() + "' to persistant (may be persistant already)");
                        }
                    }
                    try {
                        PropertyUtils.setProperty(targetObj, propertyName, value);
                    } catch (Exception e) {
                        log.error("error while setting property '" + propertyName + "' of " + targetObj + " with value " + value, e);
                        throw new SystemError(e);
                    }
                }
                obj = targetObj;
            }
            session.flush();
            if (log.isDebugEnabled()) {
                log.debug("Update object [" + obj + "]");
            }
            return obj;
        } catch (HibernateException ex) {
            throw ex;
        }
    }

    public int update(String hql, Map paramMap){
        return delete(hql, paramMap);
    }

    public Object createOrUpdate(Object obj) {
        Session session = getSession();
        try {
            Serializable id = session.getIdentifier(obj);
            if (id != null) {
                return update(obj);
            } else {
                return create(obj);
            }
        } catch (Exception e) {
            // not an obj retrieved by any session, maybe trasient object
            return create(obj);
        }
    }

    public void delete(Object obj) {
        delete(obj, true);
    }

    public void delete(Object obj, boolean flush) {
        Session session = getSession();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Delete entity [" + obj + "]");
            }
            session.delete(obj);
            if (flush) {
                session.flush();
            }
        } catch (HibernateException e) {
            log.error("Error on deleting entity " + obj, e);
            throw e;
        }
    }

    public void delete(String className, Serializable id) {
        try {
            Class c = Class.forName(className);
            Object obj = retrieve(c, id);
            delete(obj);
        } catch (ClassNotFoundException ex) {
            throw new SystemError(ex.getMessage(), ex);
        }
    }

    public void delete(Class c, Serializable id) {
        Object obj = retrieve(c, id);
        delete(obj);
    }

    public int delete(String deleteStr, Map paramMap) {
        if (log.isDebugEnabled()) {
            log.debug("Delete [" + deleteStr + "] with paramMap: " + paramMap);
        }
        Session sess = getSession();
        Query query = sess.createQuery(deleteStr);

        if (paramMap != null) {
            Set paramKey = paramMap.keySet();
            for (Object aParamKey : paramKey) {
                String paramName = (String) aParamKey;
                Object paramValue = paramMap.get(paramName);
                if (paramValue instanceof Collection) {
                    query.setParameterList(paramName, (Collection) paramValue);
                } else {
                    query.setParameter(paramName, paramValue);
                }
            }
        }
        long ts1 = System.currentTimeMillis();
        int result = query.executeUpdate();
        long ts2 = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info("result records count: " + result + " time used: " + (ts2 - ts1) + "ms");
        }
        return result;
    }
    
    public void setRollbackOnly() {
    	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

}
