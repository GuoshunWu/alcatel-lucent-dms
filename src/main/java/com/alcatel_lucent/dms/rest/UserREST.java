package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.alcatel_lucent.dms.service.LDAPService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.User;

/**
 * User REST service.
 * URL: /rest/users
 * <p/>
 * Sort parameters:
 * sidx		(optional) sort by, default is "loginName"
 * sord		(optional) order, default is "ASC"
 * <p/>
 * Format parameters:
 * format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 * prop		(required) properties to be retrieved
 * for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested,
 * e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 * for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 * for tree: prop=<property_name_for_id>,<property_name_for_name>
 * idprop		(optional) property name for id, for grid only
 * The result is not paged, that means "rows" and "page" parameter will not be supported.
 *
 * @author allany
 */
@Path("users")
@Component("userREST")
@SuppressWarnings("unchecked")
public class UserREST extends BaseREST {

    @Autowired
    private LDAPService ldapService;

    @Override
    String doGetOrPost(Map<String, String> requestMap) throws Exception {
        String hql = "from User";
        String countHql = "select count(*) from User";
        String sidx = requestMap.get("sidx");
        String sord = requestMap.get("sord");
        if (sidx == null || sidx.trim().isEmpty()) {
            sidx = "loginName";
        }
        if (sord == null) {
            sord = "ASC";
        }
        hql += " order by " + sidx + " " + sord;
        Collection<User> result = retrieve(hql, null, countHql, null, requestMap);
        requestMap.put("idprop", "loginName");
        return toJSON(result, requestMap);
    }

    @Override
    Class getEntityClass() {
        return User.class;
    }

    @GET
    @Path("ldapUser/{user.loginName}")
    public String getUser(@PathParam("user.loginName") String loginName) throws Exception {
        if (StringUtils.isEmpty(loginName)) {
            return StringUtils.EMPTY;
        }
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("prop", "loginName,name,email");
        User user = ldapService.findUserByCIL(loginName);
        return toJSON(user, requestMap);
    }

}
