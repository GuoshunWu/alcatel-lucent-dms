package com.alcatel_lucent.dms.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.web.servlet.config.annotation.EnableWebMvc

/**
 * Created by Administrator on 2014/5/17 0017.
 */
@Configuration
@ImportResource("classpath:spring.xml")
@ComponentScan("com.alcatel_lucent.dms")
class WebConfig {

}
