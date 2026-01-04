@echo off
REM Strove AI 系统健康检查脚本 (Windows版本)
REM 使用方法: 双击运行或在命令行执行 check-system.bat

echo ======================================
echo   Strove AI 系统健康检查
echo ======================================
echo.

set PASSED=0
set FAILED=0

REM 1. 检查Node.js
echo 1. 检查 Node.js...
where node >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=*" %%i in ('node --version') do set NODE_VER=%%i
    echo [OK] Node.js 已安装 !NODE_VER!
    set /a PASSED+=1
) else (
    echo [FAIL] Node.js 未安装
    set /a FAILED+=1
)

REM 2. 检查npm
echo 2. 检查 npm...
where npm >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=*" %%i in ('npm --version') do set NPM_VER=%%i
    echo [OK] npm 已安装 !NPM_VER!
    set /a PASSED+=1
) else (
    echo [FAIL] npm 未安装
    set /a FAILED+=1
)

REM 3. 检查Java
echo 3. 检查 Java...
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java 已安装
    set /a PASSED+=1
) else (
    echo [FAIL] Java 未安装
    set /a FAILED+=1
)

REM 4. 检查Maven
echo 4. 检查 Maven...
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven 已安装
    set /a PASSED+=1
) else (
    echo [FAIL] Maven 未安装
    set /a FAILED+=1
)

REM 5. 检查MySQL
echo 5. 检查 MySQL...
where mysql >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] MySQL 已安装
    set /a PASSED+=1
) else (
    echo [WARN] MySQL 未检测到 - 请确保MySQL正在运行
    set /a FAILED+=1
)

REM 6. 检查后端服务
echo.
echo 6. 检查后端服务...
curl -s http://localhost:8123/api/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] 后端服务运行正常 (http://localhost:8123)
    set /a PASSED+=1
) else (
    echo [FAIL] 后端服务未运行
    echo    提示: 运行 'mvn spring-boot:run' 启动后端
    set /a FAILED+=1
)

REM 7. 检查前端服务
echo.
echo 7. 检查前端服务...
curl -s http://localhost:5173 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] 前端服务运行正常 (http://localhost:5173)
    set /a PASSED+=1
) else (
    echo [FAIL] 前端服务未运行
    echo    提示: cd frontend ^&^& npm run dev
    set /a FAILED+=1
)

REM 8. 检查前端配置
echo.
echo 8. 检查前端配置...
if exist "frontend\.env.development" (
    findstr "VITE_API_BASE_URL=http://localhost:8123" frontend\.env.development >nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] API Base URL 配置正确
        set /a PASSED+=1
    ) else (
        echo [FAIL] API Base URL 配置错误
        echo    应该是: VITE_API_BASE_URL=http://localhost:8123
        set /a FAILED+=1
    )
) else (
    echo [FAIL] 配置文件不存在: frontend\.env.development
    set /a FAILED+=1
)

REM 总结
echo.
echo ======================================
echo 检查完成!
echo ======================================
echo 通过测试: %PASSED%
echo 失败测试: %FAILED%
echo.

if %FAILED% EQU 0 (
    echo [SUCCESS] 系统状态良好，可以开始使用！
    echo.
    echo 快速访问:
    echo   前端: http://localhost:5173
    echo   后端: http://localhost:8123/api/health
) else (
    echo [WARNING] 发现 %FAILED% 个问题，请根据上述提示修复
    echo.
    echo 常见解决方案:
    echo   1. 启动MySQL服务（服务管理器中启动MySQL80）
    echo   2. 启动后端: mvn spring-boot:run
    echo   3. 启动前端: cd frontend ^&^& npm run dev
)

echo.
pause
