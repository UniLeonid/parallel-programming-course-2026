@echo off

echo 1 thread:
java -jar build/libs/lab1.jar naive-empty 1
echo.

echo 2 threads:
java -jar build/libs/lab1.jar naive-empty 2
echo.

echo 4 threads:
java -jar build/libs/lab1.jar naive-empty 4
echo.

echo 8 threads:
java -jar build/libs/lab1.jar naive-empty 8
echo.

echo 12 threads:
java -jar build/libs/lab1.jar naive-empty 12
echo.
