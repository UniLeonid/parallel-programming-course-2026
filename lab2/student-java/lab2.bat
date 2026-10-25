@echo off
set "ROOT=%~dp0..\.."
call "%ROOT%\gradlew.bat" -q -p "%ROOT%" :lab2:student-java:compileJava || exit /b 1
java -cp "%~dp0build\classes\java\main" lab2.Main %*
