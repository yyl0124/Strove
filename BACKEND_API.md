# Strove AI 写作助手 - 后端API文档

## 项目概述
Strove AI 写作助手是一个轻量级AI写作辅助工具，让用户使用自己的AI大模型密钥，快速获取写作灵感、润色文案、编辑文档。

## 技术栈
- **框架**: Spring Boot 3.5.7
- **数据库**: MySQL 8+
- **认证**: Spring Security + JWT
- **Java版本**: 21
- **构建工具**: Maven

## 快速开始

### 1. 环境要求
- JDK 21
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
```bash
# 执行数据库初始化脚本
mysql -u root -p < sql/init_strove_db.sql
```

### 3. 配置文件
修改 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/strove_db
    username: your_username
    password: your_password
```

### 4. 运行项目
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

服务启动后访问: http://localhost:8123

## API接口文档

### 认证接口

#### 1. 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGc...",
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

#### 2. 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

Response:
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGc...",
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### AI接口

所有AI接口需要携带JWT Token:
```
Authorization: Bearer {token}
```

#### 3. AI对话
```
POST /api/ai/chat
Content-Type: application/json
Authorization: Bearer {token}

{
  "message": "帮我写一篇工作总结",
  "apiKey": "your-openai-api-key",
  "provider": "openai",
  "model": "gpt-3.5-turbo"
}

Response:
{
  "success": true,
  "data": {
    "reply": "AI生成的回复内容...",
    "conversationId": 123
  }
}
```

#### 4. 生成灵感
```
POST /api/ai/inspiration
Content-Type: application/json

{
  "keyword": "春日游记",
  "apiKey": "your-openai-api-key",
  "provider": "openai"
}

Response:
{
  "success": true,
  "data": [
    "从清晨的第一缕阳光写起，描绘春日的宁静美好",
    "以樱花盛开为线索，串联起一路的惊喜和感动",
    "讲述与朋友踏青的趣事，展现春天的活力"
  ]
}
```

#### 5. 润色文本
```
POST /api/ai/polish
Content-Type: application/json
Authorization: Bearer {token}

{
  "text": "我们公司是一家专业的软件开发企业",
  "apiKey": "your-openai-api-key",
  "provider": "openai",
  "style": "专业"
}

Response:
{
  "success": true,
  "data": "润色后的文本内容..."
}
```

#### 6. 获取对话历史
```
GET /api/ai/conversations?limit=20
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": [
    {
      "id": 123,
      "userId": 1,
      "message": "用户消息",
      "response": "AI回复",
      "createdAt": "2025-12-25T10:00:00"
    }
  ]
}
```

#### 7. 删除对话记录
```
DELETE /api/ai/conversations/{id}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "删除成功"
}
```

### 文档接口

#### 8. 创建文档
```
POST /api/documents
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "我的文档",
  "content": "# 标题\n\n文档内容..."
}

Response:
{
  "success": true,
  "message": "创建成功",
  "data": {
    "id": 1,
    "userId": 1,
    "title": "我的文档",
    "content": "# 标题\n\n文档内容...",
    "wordCount": 100,
    "createdAt": "2025-12-25T10:00:00",
    "updatedAt": "2025-12-25T10:00:00"
  }
}
```

#### 9. 更新文档
```
PUT /api/documents/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "更新后的标题",
  "content": "更新后的内容..."
}
```

#### 10. 获取用户所有文档
```
GET /api/documents
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": [...]
}
```

#### 11. 获取文档详情
```
GET /api/documents/{id}
Authorization: Bearer {token}
```

#### 12. 删除文档
```
DELETE /api/documents/{id}
Authorization: Bearer {token}
```

### 健康检查

#### 13. 系统健康检查
```
GET /api/health

Response:
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2025-12-25T10:00:00",
    "service": "Strove AI Writing Assistant",
    "version": "1.0.0-MVP"
  }
}
```

## AI服务商支持

### OpenAI
- Provider: `openai`
- Models: `gpt-3.5-turbo`, `gpt-4`
- API Key: 从 https://platform.openai.com/ 获取

### 通义千问 (计划支持)
- Provider: `qianwen`
- Models: `qwen-turbo`, `qwen-plus`

### 文心一言 (计划支持)
- Provider: `wenxin`
- Models: `ernie-bot`

## 安全性
- 所有密码使用BCrypt加密
- JWT Token有效期24小时
- API密钥由前端管理，不存储在服务器
- 所有请求建议使用HTTPS

## 开发规范
1. 代码遵循阿里巴巴Java开发规范
2. 所有Service方法需要事务注解
3. 统一使用ApiResponse包装响应
4. 异常统一在GlobalExceptionHandler处理

## 项目结构
```
src/main/java/com/erokin/strove/
├── config/              # 配置类
├── controller/          # 控制器
├── dto/                 # 数据传输对象
├── entity/              # 实体类
├── exception/           # 异常处理
├── repository/          # 数据访问层
├── security/            # 安全相关
└── service/             # 业务逻辑层
```

## MVP功能清单

### P0 (已完成)
- [x] 用户注册/登录
- [x] JWT认证
- [x] AI对话功能
- [x] 文档CRUD
- [x] 对话历史管理

### P1 (待完善)
- [ ] 完整的通义千问集成
- [ ] 完整的文心一言集成
- [ ] 提示词模板接口
- [ ] 更多AI模型支持

### P2 (未来扩展)
- [ ] 文档导出功能
- [ ] 多文档管理
- [ ] 使用统计
- [ ] 团队协作

## 许可证
MIT License
