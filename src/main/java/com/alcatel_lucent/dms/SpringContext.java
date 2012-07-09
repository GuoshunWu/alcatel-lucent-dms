package com.alcatel_lucent.dms;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;


import java.util.Map;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class SpringContext {
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        instance = applicationContext;
    }

    private static ListableBeanFactory instance;
//    private static Log log = LogFactory.getLog(SpringContext.class);

    public static ListableBeanFactory getBeanFactory() {
        if (instance == null) {
            instance = new ClassPathXmlApplicationContext("classpath*:spring.xml");
        }
        return instance;
    }

    /**
     * Create domain bean.
     * @param type
     * @return
     */
    public static Object getBeanOfType(Class type) {
        Map map = getBeanFactory().getBeansOfType(type);
        if (map == null || map.size() == 0) {
            throw new SystemError("Cann't find definition of type [" + type.getName() + "] in spring configuration");
        } else if (map.size() == 1) {
            return map.values().toArray()[0];
        } else {
            String message = "Finding type [" + type.getName() + "] in spring configuration, expected single bean but found " + map.size() + ":";
            Object[] names = map.keySet().toArray();
            for (int i = 0; i < names.length; i++) {
                message += (" \n   [" + names[i] + "] ");
            }
            throw new SystemError(message);
        }
    }

    /**
     * Get service.
     * @param type
     * @return
     */
    public static Object getService(Class type) {
        return getBeanOfType(type);
    }
}
