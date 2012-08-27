@echo off
set REBEL_HOME=D:\360CloudyDisk\Software\JRebelCracked
set MAVEN_OPTS=-javaagent:%REBEL_HOME%\jrebel.jar -noverify -Dfile.encoding=utf8

mvn jetty:run 