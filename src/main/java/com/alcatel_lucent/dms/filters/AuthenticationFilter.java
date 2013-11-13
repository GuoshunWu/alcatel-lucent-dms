package com.alcatel_lucent.dms.filters;

import com.alcatel_lucent.dms.UserContext;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */

//@WebFilter(filterName = "authenticationFilter", urlPatterns = {"/*"}, asyncSupported = true, initParams = {
//        @WebInitParam(description = "This parameter include the pattern list separated by comma, the uri in which will not be ignored by this filter.",
//                name = "excludePatterns",
//                value = "/entry\\.action\\?login\\.jsp,/login\\.action,\n" +
//                        "/login/forward-to-https,\n" +
//                        "/test/.*,/scripts/.*,/json/.*,/manual/.*,/release_notes.txt,.*js,.*map,.*coffee,.*css,.*images.*,.*ico"
//        ),
//        @WebInitParam(description = "This parameter include the pattern list separated by comma, the uri in which will send specific response to client",
//                name = "ajaxURIs",
//                value = "/test/.*,/rest/.*,/app/.*,/trans/.*,/task/.*,/admin/.*"
//        ),
//        @WebInitParam(name = "authURL",
//                value = "/login/forward-to-https",
//                description = "This parameter include the pattern list separated by comma, the uri in which will send specific response to client"
//        )}
//)
public class AuthenticationFilter implements Filter {
    protected Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private List<String> excludePatterns;
    private List<String> ajaxURIs;
    private String authURL = "/login/forward-to-https";




    public void destroy() {

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession();
        log.debug("session.id="+session.getId());

        String uri = request.getRequestURI();

        UserContext uc = (UserContext) session.getAttribute(UserContext.SESSION_USER_CONTEXT);
        UserContext.setUserContext(uc);
        try {
	        uri = uri.replace(request.getContextPath(), "");
	        if (uri.endsWith("entry.action")) uri += '?' + request.getParameter("naviTo");
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
	        response.sendRedirect(request.getContextPath() + authURL);
        } finally {
        	UserContext.removeUserContext();
        }
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
