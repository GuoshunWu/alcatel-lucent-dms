package com.alcatel_lucent.dms.filters;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-12
 * Time: 下午2:23
 * To change this template use File | Settings | File Templates.
 */
//@WebFilter(filterName = "authenticationFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {
    protected Logger log = Logger.getLogger(AuthenticationFilter.class);
    private List<String> excludePatterns;

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(req, resp);

    }

    public void init(FilterConfig config) throws ServletException {
        String strExcludePatterns = config.getInitParameter("excludePatterns");
        if (null != strExcludePatterns) {
            excludePatterns = Arrays.asList(strExcludePatterns.split(","));
        }
        log.info("String Exclude pattern=" + strExcludePatterns);
    }

}
