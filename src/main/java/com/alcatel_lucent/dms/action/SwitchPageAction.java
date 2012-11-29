package com.alcatel_lucent.dms.action;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.AuthenticationService;
import com.alcatel_lucent.dms.service.DaoService;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.ResultPath;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@SuppressWarnings("serial")
@ResultPath("/")
public class SwitchPageAction extends ActionSupport implements SessionAware {

    private static Logger log = Logger.getLogger(SwitchPageAction.class);
    private Map<String, Object> session;

    @Autowired
    private DaoService dao;

    public void setDao(DaoService dao) {
        this.dao = dao;
    }

    //    current product id from client
    private Long pId;
    //    current product base id from client
    private Long pbId;

    private String naviTo;


    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
    }

    public Long getPbId() {
        return pbId;
    }

    public void setPbId(Long pbId) {
        this.pbId = pbId;
    }

    public String getNaviTo() {
        return naviTo;
    }

    public void setNaviTo(String naviTo) {
        this.naviTo = naviTo;
    }


    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    @Action(value = "entry", results = {
            @Result(location = "${naviTo}"),
            @Result(name = INPUT, location = "/login.jsp")
    })
    public String execute() throws Exception {
        log.debug("Switch to " + getNaviTo());
        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getNaviPages() {
        StringBuilder strPages = new StringBuilder("{");
        boolean isFirst = true;
        for (String pagePrefix : Arrays.asList("app", "trans", "task")) {
            if (!isFirst) strPages.append(", ");
            strPages.append(String.format("'%s':'%s'", pagePrefix + "mng.jsp", getText(pagePrefix + "mng.title")));
            isFirst = false;
        }
        strPages.append("}");
        Map<String, String> pagesMap = JSONObject.fromObject(strPages);
        return pagesMap;
    }

    @SuppressWarnings("unchecked")
    public ProductBase getProductBase() {
        if (null != pbId) {
            return (ProductBase) dao.retrieve(ProductBase.class, pbId);
        }
        return null;
    }
}
