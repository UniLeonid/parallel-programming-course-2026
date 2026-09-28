@echo off

echo 1 thread:
java -jar build/libs/lab1.jar local 1
echo.

echo 2 threads:
java -jar build/libs/lab1.jar local 2
echo.

echo 4 threads:
java -jar build/libs/lab1.jar local 4
echo.

echo 8 threads:
java -jar build/libs/lab1.jar local 8
echo.

echo 12 threads:
java -jar build/libs/lab1.jar local 12
echo.



echo Inconsistency test
java -jar build/libs/lab1.jar local 4 inconsistency
echo.
