package com.alcatel_lucent.dms.config

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

/**
 * Created by Administrator on 2014/5/16 0016.
 */
class DMSWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        [AppConfig]
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        [WebConfig]
    }

    @Override
    protected String[] getServletMappings() {
//        ["*.ctrl", "*.sock"]
//        ["/test/webmvc/*"]
        ["/"]
    }
}
