package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.action.StrutsConventionSpringJUnit4TestCase;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionProxy;
import org.apache.struts2.StrutsSpringTestCase;
import org.apache.struts2.StrutsTestCase;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-12
 * Time: 下午3:06
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class LoginActionTest extends StrutsSpringTestCase {
    /*
        If you use Spring as the object factory, the StrutsSpringTestCase class can be used to write your JUnits.
        This class extends StrutsTestCase and has a applicationContext field of type ApplicationContext.
    */
//    @Test
//    public void testExecute() throws Exception {
//
//    }

    @Test
    public void testGetActionMapping() throws Exception {
        ActionMapping mapping = getActionMapping("/login/login.action");
        assertNotNull(mapping);
        assertEquals("/login", mapping.getNamespace());
        assertEquals("login", mapping.getName());
    }

    @Test
    public void testGetActionProxy() throws Exception {
//        set parameters before calling getActionProxy
        request.setParameter("username", "WGS");
        request.setParameter("password", "123456");

        ActionProxy proxy = getActionProxy("/login/login.action");
        assertNotNull(proxy);

        LoginAction action = (LoginAction) proxy.getAction();
        assertNotNull(action);

        String result = proxy.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("WGS", action.getUsername());
        assertEquals("123456", action.getPassword());
    }

    @Test
    public void testExecuteAction() throws ServletException, UnsupportedEncodingException {
        String output = executeAction("/login/login");
        System.out.println("output=" + output);
    }

    public void testGetValueFromStack() throws ServletException, UnsupportedEncodingException {
        request.setParameter("username", "WGS");
        request.setParameter("password", "123456");

        executeAction("/login/login");

        String name = (String) findValueAfterExecute("username");
        assertEquals("WGS", name);
    }
}
