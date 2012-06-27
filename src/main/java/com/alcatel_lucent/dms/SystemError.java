package com.alcatel_lucent.dms;

import java.lang.reflect.InvocationTargetException;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class SystemError extends RuntimeException {
    private String eventID;

    public SystemError(String message) {
        super(message);
    }

    public SystemError(Throwable cause) {
        this(null, cause);
    }

    public SystemError(String message, Throwable cause) {
        super(message);
        while (cause != null && cause instanceof InvocationTargetException) {
            cause = cause.getCause();
        }
        if (cause != null) {
            super.initCause(cause);    //override method
        }
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
