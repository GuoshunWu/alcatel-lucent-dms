@if "%DEBUG%" == "" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%" == "Windows_NT" setlocal
set destinationDir=D:\tmp\implementTest
set NODE_EXECUTE=node-debug
@if "%DEBUG%" == "" set NODE_EXECUTE=node

%NODE_EXECUTE% %NODE_PATH%\requirejs\bin\r.js -o src/main/webapp/js/app.build.js dir=%destinationDir%
@rem End local scope for the variables with windows NT shell
if "%OS%" == "Windows_NT" endlocal
 goto :EOF
:environmentVariableError
 echo.
 echo ERROR: Environment variable %1 has not been set.
 echo Attempting to find %1 from PATH also failed.
