package com.alcatel_lucent.dms.config

import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler

/**
 * Created by Administrator on 2014/5/22 0022.
 */

@Configuration
@EnableWebSocket

// groovy Logging ref: http://groovy.codehaus.org/Logging
@Slf4j
class WebSocketConfig implements WebSocketConfigurer {


    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                log.info("session.id=${session.id}, message=${message}")
                session.sendMessage(new TextMessage("Hello ${message.payload}."))
            }
        }, "/test/webmvc/ws")
    }
}
