@echo off
echo 正在编译排序算法评估程序...
javac SortingAlgorithmEvaluation.java
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    exit /b %errorlevel%
)
echo 编译成功.
echo.
echo 正在运行排序算法评估...
java SortingAlgorithmEvaluation > sorting_results.txt
echo 结果已保存到 sorting_results.txt 文件中.
pause 