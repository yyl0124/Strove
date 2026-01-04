#!/bin/bash

# Strove AI 系统健康检查脚本
# 使用方法: chmod +x check-system.sh && ./check-system.sh

echo "======================================"
echo "  Strove AI 系统健康检查"
echo "======================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查计数
PASSED=0
FAILED=0

# 检查函数
check() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $1"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $1"
        ((FAILED++))
    fi
}

# 1. 检查Node.js
echo "1. 检查 Node.js..."
node --version > /dev/null 2>&1
check "Node.js 已安装 ($(node --version 2>/dev/null || echo 'Not found'))"

# 2. 检查npm
echo "2. 检查 npm..."
npm --version > /dev/null 2>&1
check "npm 已安装 ($(npm --version 2>/dev/null || echo 'Not found'))"

# 3. 检查Java
echo "3. 检查 Java..."
java --version > /dev/null 2>&1
check "Java 已安装 ($(java --version 2>&1 | head -n 1 || echo 'Not found'))"

# 4. 检查Maven
echo "4. 检查 Maven..."
mvn --version > /dev/null 2>&1
check "Maven 已安装 ($(mvn --version 2>&1 | head -n 1 | grep -o 'Apache Maven [0-9.]*' || echo 'Not found'))"

# 5. 检查MySQL
echo "5. 检查 MySQL..."
mysql --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    check "MySQL 已安装 ($(mysql --version))"
else
    echo -e "${YELLOW}⚠${NC} MySQL 未检测到 - 请确保MySQL正在运行"
    ((FAILED++))
fi

# 6. 检查后端服务
echo ""
echo "6. 检查后端服务..."
BACKEND_RESPONSE=$(curl -s http://localhost:8123/api/health 2>/dev/null)
if echo "$BACKEND_RESPONSE" | grep -q "success"; then
    echo -e "${GREEN}✓${NC} 后端服务运行正常 (http://localhost:8123)"
    echo "   响应: $BACKEND_RESPONSE"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} 后端服务未运行"
    echo "   提示: 运行 'mvn spring-boot:run' 启动后端"
    ((FAILED++))
fi

# 7. 检查前端服务
echo ""
echo "7. 检查前端服务..."
FRONTEND_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 2>/dev/null)
if [ "$FRONTEND_RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓${NC} 前端服务运行正常 (http://localhost:5173)"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} 前端服务未运行 (HTTP状态: $FRONTEND_RESPONSE)"
    echo "   提示: cd frontend && npm run dev"
    ((FAILED++))
fi

# 8. 检查前端配置文件
echo ""
echo "8. 检查前端配置..."
if [ -f "frontend/.env.development" ]; then
    BASE_URL=$(grep VITE_API_BASE_URL frontend/.env.development | cut -d '=' -f2)
    if [ "$BASE_URL" = "http://localhost:8123" ]; then
        echo -e "${GREEN}✓${NC} API Base URL 配置正确: $BASE_URL"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} API Base URL 配置错误: $BASE_URL"
        echo "   应该是: http://localhost:8123"
        echo "   当前是: $BASE_URL"
        ((FAILED++))
    fi
else
    echo -e "${RED}✗${NC} 配置文件不存在: frontend/.env.development"
    ((FAILED++))
fi

# 9. 检查数据库连接
echo ""
echo "9. 检查数据库..."
mysql -u root -p123456 -e "USE strove_db; SELECT 'Connected' as status;" 2>/dev/null | grep -q "Connected"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 数据库 strove_db 存在且可连接"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠${NC} 无法连接到数据库 strove_db"
    echo "   提示: 检查MySQL密码或创建数据库"
    echo "   SQL: CREATE DATABASE strove_db;"
    ((FAILED++))
fi

# 总结
echo ""
echo "======================================"
echo "检查完成!"
echo "======================================"
echo -e "通过测试: ${GREEN}${PASSED}${NC}"
echo -e "失败测试: ${RED}${FAILED}${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 系统状态良好，可以开始使用！${NC}"
    echo ""
    echo "快速访问:"
    echo "  前端: http://localhost:5173"
    echo "  后端: http://localhost:8123/api/health"
    exit 0
else
    echo -e "${YELLOW}⚠️  发现 $FAILED 个问题，请根据上述提示修复${NC}"
    echo ""
    echo "常见解决方案:"
    echo "  1. 启动MySQL: sudo service mysql start"
    echo "  2. 启动后端: mvn spring-boot:run"
    echo "  3. 启动前端: cd frontend && npm run dev"
    exit 1
fi
