@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  prompter-backend startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and PROMPTER_BACKEND_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\prompter-backend-0.1.0.jar;%APP_HOME%\lib\integraciones-0.1.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.3.11.jar;%APP_HOME%\lib\red-0.0.1.jar;%APP_HOME%\lib\jackson-module-kotlin-2.9.7.jar;%APP_HOME%\lib\persistencia-0.0.1.jar;%APP_HOME%\lib\entidades-0.0.1.jar;%APP_HOME%\lib\configuracion-persistencia-postgresql-0.0.1.jar;%APP_HOME%\lib\configuracion-persistencia-h2-0.0.1.jar;%APP_HOME%\lib\jersey-hk2-2.27.jar;%APP_HOME%\lib\jersey-test-framework-provider-grizzly2-2.27.jar;%APP_HOME%\lib\jersey-container-grizzly2-servlet-2.27.jar;%APP_HOME%\lib\jersey-container-grizzly2-http-2.27.jar;%APP_HOME%\lib\shiro-jersey-0.2.0.jar;%APP_HOME%\lib\shiro-redis-3.1.0.jar;%APP_HOME%\lib\shiro-web-1.2.3.jar;%APP_HOME%\lib\shiro-core-1.2.6.jar;%APP_HOME%\lib\checkstyle-8.3.jar;%APP_HOME%\lib\commons-beanutils-1.9.3.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\metrics-core-3.2.3.jar;%APP_HOME%\lib\jackson-jaxrs-json-provider-2.9.7.jar;%APP_HOME%\lib\jackson-datatype-jaxrs-2.9.7.jar;%APP_HOME%\lib\jackson-module-afterburner-2.9.7.jar;%APP_HOME%\lib\kotlinpoet-0.7.0.jar;%APP_HOME%\lib\configuracion-persistencia-0.0.1.jar;%APP_HOME%\lib\ormlite-jdbc.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.3.11.jar;%APP_HOME%\lib\kotlin-reflect-1.3.11.jar;%APP_HOME%\lib\kotlinx-coroutines-core-1.0.0.jar;%APP_HOME%\lib\kotlin-stdlib-jre7-1.2.21.jar;%APP_HOME%\lib\kotlin-stdlib-1.3.11.jar;%APP_HOME%\lib\threetenbp-1.3.6.jar;%APP_HOME%\lib\jackson-module-paranamer-2.9.7.jar;%APP_HOME%\lib\postgresql-42.2.4.jar;%APP_HOME%\lib\HikariCP-3.2.0.jar;%APP_HOME%\lib\h2-1.4.196.jar;%APP_HOME%\lib\jersey-container-servlet-2.27.jar;%APP_HOME%\lib\jersey-test-framework-core-2.27.jar;%APP_HOME%\lib\jersey-container-servlet-core-2.27.jar;%APP_HOME%\lib\jersey-server-2.27.jar;%APP_HOME%\lib\jersey-client-2.27.jar;%APP_HOME%\lib\jersey-media-jaxb-2.27.jar;%APP_HOME%\lib\jersey-common-2.27.jar;%APP_HOME%\lib\hk2-locator-2.5.0-b42.jar;%APP_HOME%\lib\javax.inject-2.5.0-b42.jar;%APP_HOME%\lib\grizzly-http-servlet-2.4.0.jar;%APP_HOME%\lib\grizzly-http-server-2.4.0.jar;%APP_HOME%\lib\javax.ws.rs-api-2.1.jar;%APP_HOME%\lib\javax.servlet-api-4.0.0.jar;%APP_HOME%\lib\junit-4.12.jar;%APP_HOME%\lib\jedis-2.9.0.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\jackson-jaxrs-base-2.9.7.jar;%APP_HOME%\lib\jackson-module-jaxb-annotations-2.9.7.jar;%APP_HOME%\lib\jackson-databind-2.9.7.jar;%APP_HOME%\lib\jackson-core-2.9.7.jar;%APP_HOME%\lib\kotlinx-coroutines-core-common-1.0.0.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.3.11.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\paranamer-2.8.jar;%APP_HOME%\lib\ormlite-core-5.1.jar;%APP_HOME%\lib\hk2-api-2.5.0-b42.jar;%APP_HOME%\lib\hk2-utils-2.5.0-b42.jar;%APP_HOME%\lib\javax.annotation-api-1.2.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.1.jar;%APP_HOME%\lib\aopalliance-repackaged-2.5.0-b42.jar;%APP_HOME%\lib\javassist-3.22.0-CR2.jar;%APP_HOME%\lib\grizzly-http-2.4.0.jar;%APP_HOME%\lib\validation-api-1.1.0.Final.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\commons-pool2-2.4.2.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\antlr4-runtime-4.7.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\guava-23.0.jar;%APP_HOME%\lib\Saxon-HE-9.8.0-4.jar;%APP_HOME%\lib\jackson-annotations-2.9.0.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\grizzly-framework-2.4.0.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\jsr305-1.3.9.jar;%APP_HOME%\lib\error_prone_annotations-2.0.18.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.14.jar

@rem Execute prompter-backend
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PROMPTER_BACKEND_OPTS%  -classpath "%CLASSPATH%" co.smartobjects.prompterbackend.PrompterBackend %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable PROMPTER_BACKEND_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%PROMPTER_BACKEND_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
