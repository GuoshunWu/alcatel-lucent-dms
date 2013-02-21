package com.alcatel_lucent.dms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.User;

import javax.naming.directory.*;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;

@Service("ldapService")
public class LDAPServiceImpl implements LDAPService {
    private static Logger log = LoggerFactory.getLogger(LDAPServiceImpl.class);
    
    @Value("${ldap.url}")
	private String ldapUrl;


    public static String INITCTX = "com.sun.jndi.ldap.LdapCtxFactory";
    public static String LDAP_DN = "o=alcatel";

    public boolean login(String username, String password) {
        return loginLDAP(username, password) != null;
    }
    
    private DirContext loginLDAP(String username, String password) {
        log.info("Connecting to LDAP server with " + username + "...");
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            return new InitialDirContext(env);
        } catch (AuthenticationException e) {
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SystemError(e);
        }
    }

    private NamingEnumeration search(DirContext ctx, String baseDN, String filter, String[] attrNames) {
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attrNames);
            NamingEnumeration ne = ctx.search(baseDN, filter, constraints);
            if (ne.hasMoreElements()) {
                SearchResult sr = (SearchResult) ne.next();
                return sr.getAttributes().getAll();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SystemError(e);
        }
    }

    public User findUserByCSL(String csl) {
        DirContext ctx = null;
        try {
            ctx = loginLDAP("dms", "");
            if (ctx == null) return null;
            log.info("Search CSL: " + csl );
            String filter = "(&(objectclass=person)(cslx500=" + csl + "))";
            NamingEnumeration enu = search(ctx, LDAP_DN, filter, new String[] {"cn", "cslx500", "mail"});
            if (enu != null) {
                User user = new User();
                while (enu.hasMore()) {
                    Attribute attr = (Attribute) enu.next();
                    NamingEnumeration values = attr.getAll();
                    String value = (String) (values != null && values.hasMore() ? values.next() : null);
                    if (attr.getID().equalsIgnoreCase("cn")) {
                        user.setName(value);
                    } else if (attr.getID().equalsIgnoreCase("cslx500")) {
                        user.setLoginName(value);
                    } else if (attr.getID().equalsIgnoreCase("mail")) {
                        user.setEmail(value);
                    }
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (ctx != null) try {ctx.close();} catch (Exception e) {}
        }
    }

    public User findUserByCIL(String cil) {
        DirContext ctx = null;
        try {
            ctx = loginLDAP("dms", "");
            if (ctx == null) return null;
            String filter = "(&(objectclass=person)(cn=" + cil + "))";
            log.info("Search CIL: " + cil);
            NamingEnumeration enu = search(ctx, LDAP_DN, filter, new String[] {"cn", "cslx500", "mail"});
            if (enu != null) {
                User user = new User();
                while (enu.hasMore()) {
                    Attribute attr = (Attribute) enu.next();
                    NamingEnumeration values = attr.getAll();
                    String value = (String) (values != null && values.hasMore() ? values.next() : null);
                    if (attr.getID().equalsIgnoreCase("cn")) {
                        user.setName(value);
                    } else if (attr.getID().equalsIgnoreCase("cslx500")) {
                        user.setLoginName(value);
                    } else if (attr.getID().equalsIgnoreCase("mail")) {
                        user.setEmail(value);
                    }
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (ctx != null) try {ctx.close();} catch (Exception e) {}
        }
    }

}
