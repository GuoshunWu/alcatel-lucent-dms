package com.alcatel_lucent.dms.filters;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

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

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(req, resp);
        log.debug("I am a filter log");
        System.out.println("I am a filter");
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
