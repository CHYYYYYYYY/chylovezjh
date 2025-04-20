@echo off
echo 正在编译Java文件...
javac *.java
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    exit /b %errorlevel%
)
echo 编译成功.
echo.
echo 正在运行道路旅行规划器...
java RoadTripPlanner
pause 