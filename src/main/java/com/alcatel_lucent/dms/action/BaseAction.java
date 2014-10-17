package com.alcatel_lucent.dms.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

@ParentPackage("default")
@SuppressWarnings("serial")
public class BaseAction extends ActionSupport {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private HttpServletRequest request = ServletActionContext.getRequest();

    protected Collection<Long> toIdList(String idStr) {
        String[] ids = idStr.split(",");
        Collection<Long> result = new ArrayList<Long>();
        for (String id : ids) {
            if (!id.trim().isEmpty()) {
                result.add(Long.valueOf(id));
            }
        }
        return result;
    }

    protected String buildQueryString() {
        String queryString = StringUtils.defaultString(request.getQueryString());
        if (queryString.isEmpty()) {
            return "";
        }
        // remove naviTo parameter in QueryString
        String[] tokens = queryString.split("&");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (token.startsWith("naviTo")) continue;
            sb.append(token);
        }
        return sb.toString();
    }


    protected String buildNavigateURL(String protocol, String port, String targetPage) {
        String partURL = "";
        String navigateTo = "";

        if (null != protocol && port != null) {
            partURL = protocol + "://" + request.getServerName() + ":" + port + request.getContextPath() + "/entry.action?";
            navigateTo = "naviTo=" + targetPage;
        }
        String queryString = buildQueryString();

        if (queryString.isEmpty()) {
            return partURL + navigateTo;
        }
        return partURL + queryString + "&" + navigateTo;

    }

}
