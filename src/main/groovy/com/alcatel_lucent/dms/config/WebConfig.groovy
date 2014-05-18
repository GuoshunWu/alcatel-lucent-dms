package com.alcatel_lucent.dms.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.view.InternalResourceViewResolver
import org.springframework.web.servlet.view.JstlView
import org.springframework.web.servlet.view.ResourceBundleViewResolver
import org.springframework.web.servlet.view.velocity.VelocityConfigurer
import org.springframework.web.servlet.view.velocity.VelocityViewResolver
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler

/**
 * Created by guoshunw on 2014/5/16.
 */

@Configuration
@EnableWebSocket
@EnableWebMvc
@ComponentScan("com.alcatel_lucent.dms.controller")
//@ImportResource("classpath:dispatcher-servlet.xml")
class WebConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    static final log = LoggerFactory.getLogger(WebConfig)

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/test/webmvc/ws")
    }

    @Bean
    WebSocketHandler myHandler() {
        return new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                log.info("session.id=${session.id}, message=${message}")
                session.sendMessage(new TextMessage("Hello, world."))
            }
        }
    }


    @Bean
    ViewResolver viewResolver(){
        new ResourceBundleViewResolver(basename: "views")
    }

//    @Bean
//    ViewResolver JSTLViewResolver() {
////        new UrlBasedViewResolver(viewClass: JstlView, prefix: '/WEB-INF/jsp/', suffix: '.jsp')
//        new InternalResourceViewResolver(viewClass: JstlView, prefix: '/WEB-INF/jsp/', suffix: '.jsp')
//    }


    @Bean
    VelocityConfigurer velocityConfig(){
        new VelocityConfigurer(resourceLoaderPath: "/WEB-INF/velocity/")
    }

    @Bean
    ViewResolver velocityViewResolver() {
        new VelocityViewResolver(cache: true, prefix: "", suffix: ".vm")
    }
}
