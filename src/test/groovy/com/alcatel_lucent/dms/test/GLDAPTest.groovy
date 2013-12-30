package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.service.LDAPService
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import java.security.Provider
import java.security.Security

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-28
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

class GLDAPTest {
    @Autowired
    LDAPService ldapService

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    void testExample() throws Exception {
        Provider[] providers = Security.providers

        providers.each { Provider provider ->
            println "${provider.getName()}:${provider.getVersion()}: ${provider.getInfo()}".center(150, '=')
            provider.getServices().each {Provider.Service service->
                print "    $service"
            }
        }
    }
//    @Test
    void testLDAPSettings() throws Exception {
        String name = "reader.Web_SHA"
        String authDN = "uid=${name},dc=Web_SHA,dc=apps,dc=root"
        String password = "Pass_123"

        Hashtable<String, Object> env = new Hashtable<String, Object>()

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        env.put(Context.PROVIDER_URL, "ldap://ldap-emea.app.alcatel-lucent.com:2791/dc=internal,dc=users,dc=root")

        env.put(Context.SECURITY_AUTHENTICATION, "simple")
        env.put(Context.SECURITY_PRINCIPAL, authDN)
        env.put(Context.SECURITY_CREDENTIALS, password)

        InitialDirContext ctx = new InitialDirContext(env)
        SearchControls constraints = new SearchControls(returningObjFlag: true, searchScope: SearchControls.SUBTREE_SCOPE, attributesToReturn: ['uid', "commonName", "surname", "manager", "mail"])
        String filter = "(&(objectClass=person)(uid=guoshunw))"

        NamingEnumeration<SearchResult> results = ctx.search("", filter, constraints)
        results.toList().each { result ->
            result.attributes.all.toList().each { attr ->
                println attr
            }

            println '=' * 80
        }
        results.close()
        ctx.close()
    }

//    @Test
    void testLDAPService() throws Exception {
        println ldapService.findUserByCSLOrCIL("james zhang")

    }
}
