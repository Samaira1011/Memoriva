@echo off
setlocal

set JUNIT_JAR=C:\Users\Samaaira\.gradle\caches\modules-2\files-2.1\junit\junit\4.13.2\8ac9e16dd933b6fb43bc7f576336b8f4d7eb5ba12\junit-4.13.2.jar
set HAMCREST_JAR=C:\Users\Samaaira\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.3\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar
set SRC_FILE=C:\Memoriva\app\src\test\java\com\example\memoriva\BuildConfigPreservationTest.java
set OUT_DIR=C:\Memoriva\app\build\test-classes-standalone

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo Compiling BuildConfigPreservationTest...
javac -cp "%JUNIT_JAR%;%HAMCREST_JAR%" -d "%OUT_DIR%" "%SRC_FILE%"
if %ERRORLEVEL% neq 0 (
    echo COMPILATION FAILED
    exit /b 1
)
echo Compilation succeeded.

echo Running tests...
java -cp "%OUT_DIR%;%JUNIT_JAR%;%HAMCREST_JAR%" org.junit.runner.JUnitCore com.example.memoriva.BuildConfigPreservationTest
exit /b %ERRORLEVEL%
