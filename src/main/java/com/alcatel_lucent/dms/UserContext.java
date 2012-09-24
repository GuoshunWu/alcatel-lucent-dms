package com.alcatel_lucent.dms;

import java.io.Serializable;
import java.util.Locale;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class UserContext implements Serializable {

    public static String SESSION_USER_CONTEXT = "user_context";

    private Locale locale;
    //private User user;

    public static UserContext getInstance() {
    	/*
        Map session = ActionContext.getContext().getSession();
        if (session != null) {
            return (UserContext) session.get(SESSION_USER_CONTEXT);
        }
        return null;
        */
    	/*@Todo to extracted from HTTP SESSION in future*/
    	UserContext context = new UserContext();
    	context.setLocale(Locale.ENGLISH);
    	return context;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
/*
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
*/
}
