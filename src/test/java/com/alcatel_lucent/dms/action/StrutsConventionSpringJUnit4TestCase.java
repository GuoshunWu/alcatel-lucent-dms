package com.alcatel_lucent.dms.action;

import java.util.HashMap;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapper;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.views.JspSupportServlet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;

/**
 * Improved the sample from originally
 * http://depressedprogrammer.wordpress.com/2007/06/18/unit-testing
 * -struts-2-actions-spring-junit/
 * <p/>
 * - Added support for Struts Convention plugin annotations, also made it run
 * with JUnit 4.
 *
 * @author Zarar Siddiqi
 * @author ZapJava.com
 */
public class StrutsConventionSpringJUnit4TestCase {
    /**
     * Where are the Spring configurations?
     */
    private static final String CONFIG_LOCATIONS = "applicationContext-test.xml,"
            + "applicationContext-test-security.xml";

    private static ApplicationContext applicationContext;
    protected Dispatcher dispatcher;
    protected ActionProxy proxy;
    protected static MockServletContext servletContext;
    protected static MockServletConfig servletConfig;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    /**
     * Mimic the getActionProxy method the usual StrutsSpringJuni4TestCase uses.
     *
     * @param clazz     Class for which to create Action
     * @param namespace Namespace of action
     * @param name      Action name
     * @return Action class
     * @throws Exception Catch-all exception
     */
    @SuppressWarnings("unchecked")
    protected ActionProxy getActionProxy(String uri) {
        request.setRequestURI(uri);
        ActionMapping mapping = Dispatcher
                .getInstance()
                .getContainer()
                .getInstance(ActionMapper.class)
                .getMapping(request, Dispatcher.getInstance().getConfigurationManager());
        String namespace = mapping.getNamespace();
        String name = mapping.getName();
        String method = mapping.getMethod();

        // create a proxy class which is just a wrapper around the action call.
        // The proxy is created by checking the namespace and name against the
        // struts.xml configuration
        proxy = dispatcher
                .getContainer()
                .getInstance(ActionProxyFactory.class)
                .createActionProxy(namespace, name, method,
                        new HashMap<String, Object>(), true, false);

        // Add all request parameters to our action
        ActionContext invocationContext = proxy.getInvocation()
                .getInvocationContext();
        invocationContext.setParameters(new HashMap<String, Object>(request
                .getParameterMap()));

        // set the action context to the one used by the proxy
        ActionContext.setContext(invocationContext);

        // By default, have a clean session.
        proxy.getInvocation().getInvocationContext()
                .setSession(new HashMap<String, Object>());

        // Important - unit testing JSP's is just not very helpful.
        // do not execute the result after executing the action.
        proxy.setExecuteResult(false);

        // set the actions context to the one which the proxy is using
        ServletActionContext.setContext(proxy.getInvocation()
                .getInvocationContext());
        ServletActionContext.setRequest(request);
        ServletActionContext.setResponse(response);
        ServletActionContext.setServletContext(servletContext);
        return proxy;
    }

    /**
     * Create a new dispatcher before every test.
     */
    @Before
    public void initDispatcher() {
        // Dispatcher is the guy that actually handles all requests. Pass in
        // an empty. Map as the parameters but if you want to change stuff like
        // what config files to read, you need to specify them here. Here's how to
        // scan packages for actions. Thanks to Hardy Ferentschik, for action
        // packages ideas. (see Dispatcher's source code)
        dispatcher = new Dispatcher(servletContext, new HashMap<String, String>());
        dispatcher.init();
        Dispatcher.setInstance(dispatcher);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * Initialize Spring context before the unit tests.
     *
     * @throws Exception When something bad happens.
     */
    @BeforeClass
    public static void setUpContext() throws Exception {
        // Only initialize Spring context once - speeds up the tests a lot.
        if (applicationContext == null) {
            // this is the first time so initialize Spring context
            servletContext = new MockServletContext();
            servletContext.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM,
                    CONFIG_LOCATIONS);
            applicationContext = (new ContextLoader())
                    .initWebApplicationContext(servletContext);

            // Struts JSP support servlet (for Freemarker)
            new JspSupportServlet().init(new MockServletConfig(servletContext));
        }
    }
}
