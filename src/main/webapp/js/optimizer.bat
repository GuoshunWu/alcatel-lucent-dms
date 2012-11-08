@echo on
rem with node
rem r -o app.build.js
rem with java
@set WORKDIR=D:/360CloudyDisk/Programing/dmslib/jslibs/example-multipage-shim/tools
set CLASSPATH=%WORKDIR%/js.jar;%WORKDIR%/compiler.jar
java -Xss20m org.mozilla.javascript.tools.shell.Main ../tools/r.js -o app.build.js