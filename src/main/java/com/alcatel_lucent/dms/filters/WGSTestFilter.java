package com.alcatel_lucent.dms.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by guoshunw on 2014/10/17.
 */

//@WebFilter(displayName = "WGS Test Filter", filterName = "wgsTestFilter", urlPatterns = {
//        "/*"
//})
public class WGSTestFilter implements Filter {

    protected Logger log = LoggerFactory.getLogger(WGSTestFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        log.debug("in wgs test filter session.id="+session.getId());
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {

    }
}
