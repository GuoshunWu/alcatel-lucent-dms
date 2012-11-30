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
public class EntryAction extends ActionSupport {

    private static Logger log = Logger.getLogger(EntryAction.class);
    private Map<String, Object> session;

    @Autowired
    private DaoService dao;
    //    The page navigate to
    private String naviTo;
    //    build number for deploy
    @Value("${buildNumber}")
    private String buildNumber;

    private Long curProductId = -1L;
    private Long curProductBaseId = -1L;

    public Long getCurProductId() {
        return curProductId;
    }

    public void setCurProductId(Long curProductId) {
        this.curProductId = curProductId;
    }

    public Long getCurProductBaseId() {
        return curProductBaseId;
    }

    public void setCurProductBaseId(Long curProductBaseId) {
        this.curProductBaseId = curProductBaseId;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setDao(DaoService dao) {
        this.dao = dao;
    }

    public String getNaviTo() {
        return naviTo;
    }

    public void setNaviTo(String naviTo) {
        this.naviTo = naviTo;
    }

    @Action(results = {
            @Result(location = "${naviTo}"),
            @Result(name = INPUT, location = "/login.jsp")
    })
    public String execute() throws Exception {
        log.debug("Switch to " + getNaviTo());
        log.debug(String.format("Product id=%d, Product base id=%d", curProductId, curProductBaseId));
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
        Map<String, String> pagesMap = JSONObject.fromObject(strPages.toString());
        return pagesMap;
    }

    @SuppressWarnings("unchecked")
    public List<ProductBase> getProductBases() {
        return dao.retrieve("from ProductBase");
    }

    @SuppressWarnings("unchecked")
    public List<Product> getProducts() {
        if (null != curProductBaseId) {
            Map<String, Long> param = new HashMap<String, Long>();
            param.put("baseId", curProductBaseId);
            return dao.retrieve("from Product where base.id = :baseId", param);
        }
        return new ArrayList<Product>();
    }

    /**
     * For client parameters
     */
    public Map<String, String> getClientParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("locale", getLocale().toString());
        return params;
    }
}
