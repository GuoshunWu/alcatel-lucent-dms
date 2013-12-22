package com.alcatel_lucent.dms;

import com.alcatel_lucent.dms.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by Guoshun.Wu on 13-12-22.
 */
public class Global {
    private static final HashSet<User> onLineUsers = new LinkedHashSet<User>();

    private static Logger log = LoggerFactory.getLogger(Global.class);

    public static synchronized  HashSet<User> login(User user) throws InterruptedException {
        onLineUsers.add(user);
        return onLineUsers;
    }

    public static  synchronized HashSet<User> logout(User user) throws InterruptedException {
        onLineUsers.remove(user);
        return onLineUsers;
    }

    public static boolean isOnLine(User user) {
        log.info("onLineUsers {}, user {}", onLineUsers, user);
        return UserContext.getInstance().getUser().equals(user) || onLineUsers.contains(user);
    }

}
