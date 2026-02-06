@echo off
REM =============================
REM CONFIGURATION
REM =============================
set "TOMCAT_DIR=C:\tomcat\apache-tomcat-10.1.28"
set "APP_NAME=location"
set "TEST_DIR=D:\L3\workflow_git\lv-bo"

REM =============================
REM BUILD TEST PROJECT
REM =============================
echo Building test project...
cd /d "%TEST_DIR%"
call mvn clean compile -q
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test project compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Test project compiled

REM =============================
REM PREPARE WEB-INF/classes
REM =============================
set "WEB_CLASSES=%TEST_DIR%\src\main\webapp\WEB-INF\classes"
echo Preparing WEB-INF/classes...

if exist "%WEB_CLASSES%" rmdir /s /q "%WEB_CLASSES%"
mkdir "%WEB_CLASSES%"


REM Copy test classes
xcopy /E /I /Y "%TEST_DIR%\target\classes\com\test" "%WEB_CLASSES%\com\test" > nul

REM Copy application.properties
mkdir "%WEB_CLASSES%\resources" 2>nul
xcopy /Y "%TEST_DIR%\src\main\resources\resources\application.properties" ^
         "%WEB_CLASSES%\resources\" > nul

echo Classes prepared

REM =============================
REM PREPARE WEB-INF/lib
REM =============================
set "WEB_LIB=%TEST_DIR%\src\main\webapp\WEB-INF\lib"
echo Preparing WEB-INF/lib...

if exist "%WEB_LIB%" rmdir /s /q "%WEB_LIB%"
mkdir "%WEB_LIB%"

REM Copy Jackson JARs (use Maven dependency plugin to copy dependencies)
cd /d "%TEST_DIR%"
call mvn dependency:copy-dependencies -DoutputDirectory="%WEB_LIB%" -DincludeScope=runtime -q
IF %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Could not copy all dependencies
)

echo Dependencies prepared

REM =============================
REM DEPLOY TO TOMCAT
REM =============================
echo Deploying to Tomcat...

if exist "%TOMCAT_DIR%\webapps\%APP_NAME%" (
    echo Removing old app...
    rmdir /s /q "%TOMCAT_DIR%\webapps\%APP_NAME%"
)

xcopy /E /I /Y "%TEST_DIR%\src\main\webapp" "%TOMCAT_DIR%\webapps\%APP_NAME%" > nul
echo App deployed

REM =============================
REM START TOMCAT
REM =============================
echo Starting Tomcat...
set "CATALINA_HOME=%TOMCAT_DIR%"
call "%TOMCAT_DIR%\bin\startup.bat"

timeout /t 6 > nul
echo.
echo =============================================
echo DEPLOYMENT SUCCESSFUL!
echo Open: http://localhost:8080/%APP_NAME%/popo
echo Open: http://localhost:8080/%APP_NAME%/mimi
echo =============================================
echo.
pause