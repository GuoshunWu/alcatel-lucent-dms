/*

http://logback.qos.ch/manual/introduction.html

Built on Thu Nov 07 15:11:57 CET 2013 by logback-translator
For more information on configuration files in Groovy
please see http://logback.qos.ch/manual/groovy.html

For assistance related to this tool or configuration files
in general, please contact the logback user mailing list at
    http://qos.ch/mailman/listinfo/logback-user

For professional support please see
   http://www.qos.ch/shop/products/professionalSupport
*/



import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.TRACE

context = new LevelChangePropagator()
context.resetJUL = true

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy/MM/dd-HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
//        pattern = "%d{yyyy/MM/dd-HH:mm:ss} [%thread] %-5level %logger{36} %caller{1} - %msg%n"
    }
    filter(ThresholdFilter){
        level = ${logger.filter}
    }
}

String TARGET_DIR = new File(this.class.getResource('/').path +"../../../../logs", "dms").canonicalPath
println String.format("Log root dir=%s", TARGET_DIR).center(100, '=')
appender("FILE", RollingFileAppender) {
    append = true
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${TARGET_DIR}/DMSlog.%d{yyyy-MM-dd}.log"
        maxHistory = 100
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}

//logger("com.alcatel_lucent.dms.filters",DEBUG)
//logger("org.jasig.cas.client",TRACE)


//logger("org.hibernate.type", TRACE)
//logger("org.hibernate.SQL", DEBUG)

root(INFO, ["STDOUT","FILE"])


