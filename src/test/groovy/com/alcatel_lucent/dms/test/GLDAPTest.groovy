package com.alcatel_lucent.dms.test

import javax.naming.Context
import javax.naming.NamingEnumeration
import org.junit.BeforeClass
import org.junit.Test
import javax.naming.directory.*
import org.junit.Ignore

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-28
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
@Ignore
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GLDAPTest {

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

//    @Test
    public void testAlcatelLDAP() throws Exception {
        String INITCTX = "com.sun.jndi.ldap.LdapCtxFactory"
        String ldapUrl = "ldap://ldap.sxb.bsf.alcatel.fr"
        String LDAP_DN = "o=alcatel"

        String username = "guoshunw"
        String password = ""

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = new InitialDirContext(env)
        def filter = "(&(objectclass=person)(cslx500=$username))"
//        filter = "(&(objectclass=person))"
        NamingEnumeration<SearchResult> results = ctx.search(LDAP_DN,
                filter,
                new SearchControls(
                        returningObjFlag: true,
                        searchScope: SearchControls.SUBTREE_SCOPE,
//                        attributesToReturn: ["cn", "cslx500", "mail"]
                )
        )
        Attributes attrs = null
        results.toList().each {result ->
            attrs = result.attributes
            attrs.all.toList().each {attr ->
                println attr
            }
//            println attrs.cn.get()
//            println attrs.cslx500.get()
//            println attrs.mail.get()

            println '=' * 80
        }
        ctx.close()
    }
//    @Test
    public void testLDAPSettings() throws Exception {
        String name = "admin"
        String uid = "cbuckley"
        String authDN = "uid=${name},ou=system"
        String password = "secret"

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
        String baseDN = "ou=system"
        String filter = "(&(objectClass=person))"
//        filter = "(&(objectClass=inetOrgPerson)(uid={0}))";

        NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, [uid].toArray(), constraints)
        results.toList().each {result ->
            result.attributes.all.toList().each {attr ->
                println attr
            }

            println '=' * 80
        }
        results.close()
        ctx.close()
    }

    @Test
    void tempTest() {
        println org.apache.commons.lang3.StringUtils.removeEnd('abc', 'c')
    }

}
