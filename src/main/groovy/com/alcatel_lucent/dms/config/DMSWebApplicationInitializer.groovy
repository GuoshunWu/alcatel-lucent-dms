package com.alcatel_lucent.dms.config

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

/**
 * Created by Administrator on 2014/5/16 0016.
 */
class DMSWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return [WebConfig]
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return [WebSocketConfig]
    }

    @Override
    protected String[] getServletMappings() {
        return ["/test/myHandler"]
    }
}
