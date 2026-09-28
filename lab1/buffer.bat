@echo off

echo 1 thread:
java -jar build/libs/lab1.jar buffer 1
echo.

echo 2 threads:
java -jar build/libs/lab1.jar buffer 2
echo.

echo 4 threads:
java -jar build/libs/lab1.jar buffer 4
echo.

echo 8 threads:
java -jar build/libs/lab1.jar buffer 8
echo.

echo 12 threads:
java -jar build/libs/lab1.jar buffer 12
echo.



echo Inconsistency test
java -jar build/libs/lab1.jar buffer 4 inconsistency
echo.
