package com.alcatel_lucent.dms.listeners;

import com.alcatel_lucent.dms.Global;
import com.alcatel_lucent.dms.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Date;

/**
 * Created by guoshunw on 13-12-30.
 */
public class DMSHttpSessionListener implements HttpSessionListener {

    private static Logger log = LoggerFactory.getLogger(DMSHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        /*
        * clean session attribute in case user close web browser unexpectedly
        * */
        HttpSession session = se.getSession();
        UserContext userContext = (UserContext) session.getAttribute(UserContext.SESSION_USER_CONTEXT);
        if (null != userContext) {
            try {
                Global.logout(userContext.getUser());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            session.removeAttribute(UserContext.SESSION_USER_CONTEXT);
        }
        log.info("User {} logout at {}, session id: {}.", userContext != null ? userContext.getUser().getName() : "Unknown", new Date().toString(), session.getId());
    }
}
