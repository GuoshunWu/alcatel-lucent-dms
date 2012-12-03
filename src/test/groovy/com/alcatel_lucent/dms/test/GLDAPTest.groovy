package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.BusinessException
import com.alcatel_lucent.dms.model.Language
import com.alcatel_lucent.dms.service.DaoService
import com.alcatel_lucent.dms.service.LanguageService
import com.alcatel_lucent.dms.service.TextService
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import static org.junit.Assert.assertNotNull
import javax.naming.Context
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.NamingEnumeration
import javax.naming.directory.SearchResult

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-28
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GLDAPTest {

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    public void testAlcatelLDAP() throws Exception {
        InitialDirContext ctx = new InitialDirContext(
                [
                        "$Context.INITIAL_CONTEXT_FACTORY": 'com.sun.jndi.ldap.LdapCtxFactory',
                        "$Context.PROVIDER_URL": 'ldap://ldap.sxb.bsf.alcatel.fr',
                        "$Context.SECURITY_AUTHENTICATION": 'simple',
                        "$Context.SECURITY_PRINCIPAL": 'dms',
                        "$Context.SECURITY_CREDENTIALS": ''
                ] as Hashtable
        )
        SearchControls constraints = new SearchControls(
                returningObjFlag: true,
                searchScope: SearchControls.SUBTREE_SCOPE,
                attributesToReturn: ["cn", "cslx500", "mail"]
        )
        String csl = "guoshunw"
        String baseDN = "o=alcatel"
        String filter = "(&(objectclass=person)(cslx500=$csl))"
        println "baseDN=$baseDN, filter=$filter"
        NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, constraints)
//        results.toList().each {result ->
//            result.attributes.all.toList().each {attr ->
//                println attr
//            }
//
//            println '=' * 80
//        }
    }
//    @Test
    public void testLDAPSettings() throws Exception {
        String name = "Cornelius Buckley"
        String uid = "cbuckley"
        String authDN = "cn=${name},ou=people,o=sevenSeas"
        String password = "pass"

//        name = "admin"
//        authDN = "uid=${name},ou=system"
//        password = "alcatel123"

        Hashtable<String, Object> env = new Hashtable<String, Object>()

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389")
        env.put(Context.SECURITY_AUTHENTICATION, "simple")
        env.put(Context.SECURITY_PRINCIPAL, authDN)
        env.put(Context.SECURITY_CREDENTIALS, password)

        InitialDirContext ctx = new InitialDirContext(env)
        SearchControls constraints = new SearchControls(returningObjFlag: true, searchScope: SearchControls.SUBTREE_SCOPE, attributesToReturn: ['uid', "commonName", "surname", "manager", "mail"])
//        String filter = "(&(objectclass=person)(cslx500=" + csl + "))";
        String baseDN = "ou=people,o=sevenSeas"
        String filter = "(&(objectClass=person))"
//        filter = "(&(objectClass=inetOrgPerson)(uid={0}))";

        NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, [uid].toArray(), constraints)
        results.toList().each {result ->
            result.attributes.all.toList().each {attr ->
                println attr
            }

            println '=' * 80
        }
    }

}
