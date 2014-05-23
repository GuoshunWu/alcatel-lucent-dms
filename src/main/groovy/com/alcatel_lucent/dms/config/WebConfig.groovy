package com.alcatel_lucent.dms.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.view.ResourceBundleViewResolver
import org.springframework.web.servlet.view.velocity.VelocityConfigurer
import org.springframework.web.servlet.view.velocity.VelocityViewResolver

/**
 * Created by guoshunw on 2014/5/16.
 */

@Configuration
@EnableWebMvc
@ComponentScan("com.alcatel_lucent.dms.controller")
//@Import(WebSocketConfig)
class WebConfig extends WebMvcConfigurerAdapter {

    static final log = LoggerFactory.getLogger(WebConfig)

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
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
