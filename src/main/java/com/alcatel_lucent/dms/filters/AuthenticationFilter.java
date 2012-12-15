package com.alcatel_lucent.dms.filters;

import com.alcatel_lucent.dms.UserContext;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.alcatel_lucent.dms.util.Util.anyMatch;

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
    private List<String> ajaxURIs;
    private String authURL = "/login/forward-to-https";

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession();
        String uri = request.getRequestURI();

        UserContext uc = (UserContext) session.getAttribute(UserContext.SESSION_USER_CONTEXT);
        UserContext.setUserContext(uc);

        uri = uri.replace(request.getContextPath(), "");
        if (uri.endsWith("entry.action")) {
            uri += '?' + request.getParameter("naviTo");
        }
        log.debug("uri=" + uri);
        if (anyMatch(uri, excludePatterns)) {
            log.debug("uri " + uri + " in the exclude pattern list, ignore.");
            chain.doFilter(req, resp);
            return;
        }

        if (null != uc) {
            chain.doFilter(req, resp);
            return;
        }

        if (anyMatch(uri, ajaxURIs)) {
            log.debug("uri " + uri + " in the ajax uri pattern list, response json.");
            ajaxResponse(response);
            return;
        }


//        Normal JSP response
//        forward will cause struts core filter error
//        request.getRequestDispatcher("/login.jsp").forward(req,resp);
        response.sendRedirect(request.getContextPath() + authURL);
    }

    public void init(FilterConfig config) throws ServletException {
        String strExcludePatterns = config.getInitParameter("excludePatterns");
        if (null != strExcludePatterns) {
            excludePatterns = Arrays.asList(strExcludePatterns.split("\\s*,\\s*"));
            log.debug("String exclude pattern in AuthenticationFilter=" + strExcludePatterns);
        }
        String strJsonURIs = config.getInitParameter("ajaxURIs");
        if (null != strJsonURIs) {
            ajaxURIs = Arrays.asList(strJsonURIs.split("\\s*,\\s*"));
            log.debug("String json uris in AuthenticationFilter=" + strJsonURIs);
        }

        String authURL = config.getInitParameter("authURL");
        if (null != authURL) {
            this.authURL = authURL;
        }
    }

    /**
     * Send ajax json response to client
     */
    private void ajaxResponse(HttpServletResponse response) throws IOException {
        //        Write json response to client
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION);
        response.getWriter().print(JSONObject.fromObject("{'status':203, 'message':'session time out.'}"));
        response.flushBuffer();
    }

}
