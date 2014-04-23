package com.alcatel_lucent.dms.servlet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Created by guoshunw on 2014/4/23.
 */

@ServerEndpoint("/serverEnd")
public class WebSocketServerEndPoint {

    private static Logger log = LoggerFactory.getLogger(WebSocketServerEndPoint.class);

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected ... " + session.getId());
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        if (message.equals("quit")) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User left."));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return message;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }
}
