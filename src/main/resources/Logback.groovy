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
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.*

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

String TARGET_DIR = "target/DMSLOG"

appender("FILE", RollingFileAppender) {
    append = true
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${TARGET_DIR}/logFile.%d{yyyy-MM-dd}.log"
        maxHistory = 30
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}

//logger("com.alcatel_lucent.dms.filters",INFO)
//logger("org.hibernate.type", TRACE)
//logger("org.hibernate.SQL", DEBUG)

root(INFO, ["STDOUT", "FILE"])


