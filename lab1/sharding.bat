@echo off

echo 1 thread:
java -jar build/libs/lab1.jar sharding 1
echo.

echo 2 threads:
java -jar build/libs/lab1.jar sharding 2
echo.

echo 4 threads:
java -jar build/libs/lab1.jar sharding 4
echo.

echo 8 threads:
java -jar build/libs/lab1.jar sharding 8
echo.

echo 12 threads:
java -jar build/libs/lab1.jar sharding 12
echo.



echo Inconsistency test
java -jar build/libs/lab1.jar sharding 4 inconsistency
echo.
