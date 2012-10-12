package com.alcatel_lucent.dms;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-22
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class provides an application-wide access to the
 * Spring ApplicationContext! The ApplicationContext is
 * injected in a static method of the class "AppContext".
 * Use AppContext.getApplicationContext() to get access
 * to all Spring Beans.
 */
@Component("springContext")
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context=applicationContext;
    }

    public static ApplicationContext getContext(){
        return context;
    }

    public static Object getBean(String name) {
        return context.getBean(name);
    }
    
}
