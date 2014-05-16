package com.alcatel_lucent.dms.websocket

import com.alcatel_lucent.dms.service.DaoService
import org.slf4j.LoggerFactory

import javax.websocket.*
import javax.websocket.server.ServerEndpoint

/**
 * Created by guoshunw on 2014/5/16.
 */
@ServerEndpoint("/test/echo")
class EchoServer {
    static final log = LoggerFactory.getLogger(EchoServer)
    private DaoService daoService

    @OnOpen
    void onOpen(Session session, EndpointConfig config) {
        log.info("session id: ${session.id} open...")

        session.basicRemote.sendText("onOpen")
    }

    @OnMessage
    String onMessage(Session session, String message) {
        log.info("Got message: {}, dao={}", message, daoService)
        "Thanks for the message: ${message}"
    }

    @OnError
    void onError(Session session, Throwable t) {
        t.printStackTrace()
    }

    @OnClose
    void onClose(Session session, CloseReason reason) {
        log.info("session id: ${session.id} close...")
    }

}
