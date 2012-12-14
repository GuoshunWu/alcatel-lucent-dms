package com.alcatel_lucent.dms.filters;

import com.alcatel_lucent.dms.UserContext;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession();
        String uri = request.getRequestURI();

        UserContext uc = (UserContext) session.getAttribute(UserContext.SESSION_USER_CONTEXT);
        UserContext.setUserContext(uc);

        uri=uri.replace(request.getContextPath(),"");
        if (uri.endsWith("entry.action")) {
//                process real uri
            uri += '?' + request.getParameter("naviTo");
        }
        log.debug("uri=" + uri);
        for (String pattern : excludePatterns) {

            if (uri.matches(pattern)) {
                log.debug("uri " + uri + " match pattern: " + pattern + ", ignore.");
                chain.doFilter(req, resp);
                return;
            }
        }

        if (null != uc) {
            chain.doFilter(req, resp);
            return;
        }

//        forward will cause struts core filter error
//        request.getRequestDispatcher("/login.jsp").forward(req,resp);
        response.sendRedirect(request.getContextPath() + "/login/forward-to-https");
    }

    public void init(FilterConfig config) throws ServletException {
        String strExcludePatterns = config.getInitParameter("excludePatterns");
        if (null != strExcludePatterns) {
            excludePatterns = Arrays.asList(strExcludePatterns.split(","));
        }
        log.debug("String Exclude pattern in AuthenticationFilter=" + strExcludePatterns);
    }

}
