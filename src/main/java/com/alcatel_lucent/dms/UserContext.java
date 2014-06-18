package com.alcatel_lucent.dms;

import com.alcatel_lucent.dms.model.User;
import com.opensymphony.xwork2.ActionContext;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class UserContext implements Serializable {

    private static final long serialVersionUID = 7061256090839337985L;

    public static final String SESSION_USER_CONTEXT = "user_context";
    private static final ThreadLocal<UserContext> instance = new ThreadLocal<UserContext>();

    private Locale locale;
    private User user;
    private TimeZone timeZone;

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public UserContext() {
    }

    public UserContext(Locale locale, User user, TimeZone timeZone) {
        this.locale = locale;
        this.user = user;
        this.timeZone = timeZone;
    }

    public static UserContext getInstance() {
        return instance.get();
    }
    
    public static void setUserContext(UserContext uc) {
        instance.set(uc);
    }
    
    public static void removeUserContext() {
    	instance.remove();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
