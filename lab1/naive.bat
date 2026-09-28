@echo off

echo 1 thread:
java -jar build/libs/lab1.jar naive 1
echo.

echo 2 threads:
java -jar build/libs/lab1.jar naive 2
echo.

echo 4 threads:
java -jar build/libs/lab1.jar naive 4
echo.

echo 8 threads:
java -jar build/libs/lab1.jar naive 8
echo.

echo 12 threads:
java -jar build/libs/lab1.jar naive 12
echo.
