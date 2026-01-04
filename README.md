# Strove AI 写作助手

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

基于 Spring Boot 与 Vue 3 的轻量级 AI 写作辅助工具，支持用户使用自己的大模型 API 密钥进行智能写作辅助。

## 🌟 产品特色

- ✨ **多模型支持**: 兼容 OpenAI、通义千问、文心一言、Deepseek、Kimi 等主流 AI 服务
- 🔐 **隐私优先**: API 密钥本地加密存储，后端不持久化用户密钥
- 💬 **智能对话**: AI 驱动的写作建议、文本润色、灵感生成
- 📝 **Markdown 编辑**: 实时预览、自动保存的文档编辑器
- 🎯 **提示词模板**: 内置 5 种常用写作场景模板
- 🚀 **极简部署**: 前后端分离，Docker 一键启动

---

## 🎯 核心功能

### 1. 用户认证系统
- 用户注册/登录（基于 JWT）
- 安全的密码加密存储（BCrypt）
- Token 自动刷新机制

### 2. AI 写作辅助
- **AI 对话**: 输入写作需求，获取 AI 建议
- **文本润色**: 专业/生动/简洁多种风格
- **灵感生成**: 根据关键词生成 3-5 条写作思路
- **对话历史**: 云端保存，随时查看历史记录

### 3. 文档管理
- Markdown 格式文档编辑
- 实时预览与自动保存
- 字数统计与版本管理
- 文档列表与分类

### 4. 密钥管理
- 支持多家 AI 服务商配置
- 本地 AES-256 加密存储
- 密钥有效性验证
- 自定义 API 地址

---

## 🛠️ 技术栈

### 后端
- **核心框架**: Spring Boot 3.5.7
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Spring Data JPA
- **AI 集成**: 兼容 OpenAI 协议的通用接口
- **构建工具**: Maven 3.9+
- **运行环境**: Java 21

### 前端
- **框架**: Vue 3.5 + TypeScript
- **构建工具**: Vite 7.1
- **路由**: Vue Router 4.6
- **状态管理**: Pinia 3.0
- **UI 组件**: Element Plus 2.11
- **Markdown**: marked 17.0
- **HTTP 客户端**: Axios 1.13

---

## 📦 快速开始

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/strove.git
cd strove
```

### 2. 数据库初始化

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init_strove_db.sql
```

或者手动创建：

```sql
CREATE DATABASE strove_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置环境变量（可选）

```bash
# Linux/Mac
export DB_URL="jdbc:mysql://localhost:3306/strove_db?useSSL=false&serverTimezone=Asia/Shanghai"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export JWT_SECRET="your-very-secure-secret-key-at-least-32-chars"

# Windows PowerShell
$env:DB_URL="jdbc:mysql://localhost:3306/strove_db?useSSL=false&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-very-secure-secret-key-at-least-32-chars"
```

### 4. 启动后端

```bash
# 方式 1: Maven 命令行
mvn spring-boot:run

# 方式 2: IDE 运行
# 在 IntelliJ IDEA 中运行 StroveApplication.java

# 后端默认地址: http://localhost:8123
```

### 5. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端默认地址: http://localhost:5173
```

### 6. 访问系统

打开浏览器访问: **http://localhost:5173**

#### 内置测试账号

| 用户类型 | 邮箱 | 密码 |
|---------|------|------|
| 管理员 | admin@strove.ai | Admin123 |
| 测试用户 | user@strove.ai | User123 |

---

## 📂 项目结构

```
strove/
├── src/main/java/com/erokin/strove/     # 后端源码
│   ├── StroveApplication.java            # 主应用入口
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.java          # 安全配置
│   │   ├── WebConfig.java               # CORS 配置
│   │   └── DatabaseAutoFixer.java       # 数据库自动修复
│   ├── controller/                       # REST 控制器
│   │   ├── AuthController.java          # 认证接口
│   │   ├── AIController.java            # AI 功能接口
│   │   ├── DocumentController.java      # 文档管理接口
│   │   ├── HealthController.java        # 健康检查
│   │   └── ToolController.java          # 开发工具
│   ├── dto/                              # 数据传输对象
│   │   ├── ApiResponse.java             # 统一响应格式
│   │   ├── LoginRequest.java            # 登录请求
│   │   ├── RegisterRequest.java         # 注册请求
│   │   ├── ChatRequest.java             # AI 对话请求
│   │   └── ...
│   ├── entity/                           # JPA 实体类
│   │   ├── User.java                    # 用户实体
│   │   ├── Document.java                # 文档实体
│   │   └── Conversation.java            # 对话记录实体
│   ├── repository/                       # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── DocumentRepository.java
│   │   └── ConversationRepository.java
│   ├── security/                         # 安全模块
│   │   ├── JwtTokenProvider.java        # JWT 工具类
│   │   └── JwtAuthenticationFilter.java # JWT 过滤器
│   └── service/                          # 业务服务层
│       ├── AuthService.java             # 认证服务
│       ├── AIService.java               # AI 服务
│       └── DocumentService.java         # 文档服务
├── src/main/resources/
│   └── application.yml                   # 应用配置文件
├── frontend/                             # 前端源码
│   ├── src/
│   │   ├── views/                       # 页面组件
│   │   │   ├── Auth/                    # 认证页面
│   │   │   │   ├── Login.vue           # 登录页
│   │   │   │   └── Register.vue        # 注册页
│   │   │   ├── Dashboard/               # 主页
│   │   │   │   └── Home.vue            # 首页
│   │   │   ├── Chat/                    # AI 对话
│   │   │   │   └── ChatInterface.vue   # 对话界面
│   │   │   ├── Documents/               # 文档管理
│   │   │   │   ├── EditorView.vue      # 编辑器
│   │   │   │   └── DocumentList.vue    # 文档列表
│   │   │   └── Inspiration/             # 灵感生成
│   │   │       └── Generate.vue        # 灵感生成页
│   │   ├── components/                  # 公共组件
│   │   ├── api/                         # API 接口
│   │   │   ├── client.ts               # Axios 客户端
│   │   │   ├── auth.ts                 # 认证 API
│   │   │   └── ai.ts                   # AI API
│   │   ├── stores/                      # Pinia 状态管理
│   │   │   ├── auth.ts                 # 认证状态
│   │   │   └── settings.ts             # 设置状态
│   │   ├── router/                      # 路由配置
│   │   │   └── index.ts
│   │   ├── App.vue                      # 根组件
│   │   ├── main.ts                      # 入口文件
│   │   └── style.css                    # 全局样式
│   ├── package.json                     # 依赖配置
│   └── vite.config.ts                   # Vite 配置
├── sql/                                  # SQL 脚本
│   └── init_strove_db.sql               # 数据库初始化
├── pom.xml                               # Maven 配置
├── README.md                             # 项目说明
├── 后端接口文档.md                        # API 文档
├── 2. 软件需求规格说明书.md               # 需求文档
├── 需求规格.md                           # 需求规格
└── 开发指南.md                           # 开发指南
```

---

## 🔌 API 接口

详细的接口文档请查看: **[后端接口文档.md](./后端接口文档.md)**

### 主要接口列表

| 分类 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **认证** | POST | `/api/auth/register` | 用户注册 |
| | POST | `/api/auth/login` | 用户登录 |
| **AI 功能** | POST | `/api/ai/chat` | AI 对话 |
| | POST | `/api/ai/test-connection` | 测试连接 |
| | POST | `/api/ai/inspiration` | 生成灵感 |
| | POST | `/api/ai/polish` | 润色文本 |
| | GET | `/api/ai/conversations` | 获取对话历史 |
| | DELETE | `/api/ai/conversations/{id}` | 删除对话记录 |
| **文档** | POST | `/api/documents` | 创建文档 |
| | PUT | `/api/documents/{id}` | 更新文档 |
| | GET | `/api/documents` | 获取文档列表 |
| | GET | `/api/documents/{id}` | 获取文档详情 |
| | DELETE | `/api/documents/{id}` | 删除文档 |
| **系统** | GET | `/api/health` | 健康检查 |

---

## 🎨 支持的 AI 服务商

| 服务商 | Base URL | 模型示例 |
|--------|----------|----------|
| **OpenAI** | `https://api.openai.com/v1` | gpt-3.5-turbo, gpt-4 |
| **通义千问** | `https://dashscope.aliyuncs.com/compatible-mode/v1` | qwen-turbo, qwen-plus |
| **Deepseek** | `https://api.deepseek.com` | deepseek-chat |
| **月之暗面 Kimi** | `https://api.moonshot.cn/v1` | moonshot-v1-8k |
| **智谱清言** | `https://open.bigmodel.cn/api/paas/v4` | glm-4, glm-3-turbo |

*所有服务商均需兼容 OpenAI API 协议*

---

## 🧪 测试

### 后端测试

```bash
# 编译检查
mvn clean compile

# 运行测试（如有）
mvn test

# 打包
mvn clean package -DskipTests
```

### 前端测试

```bash
cd frontend

# 类型检查
npm run build

# 开发预览
npm run preview
```

---

## 🚀 部署

### 使用 Docker（推荐）

```bash
# TODO: 添加 Docker Compose 配置
docker-compose up -d
```

### 手动部署

#### 后端部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/strove-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://your-db-host:3306/strove_db \
  --spring.datasource.username=your_user \
  --spring.datasource.password=your_password \
  --jwt.secret=your-jwt-secret
```

#### 前端部署

```bash
cd frontend

# 构建生产版本
npm run build

# 部署 dist 目录到 Nginx/Apache
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/strove/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8123;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/strove_db?useSSL=false&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: ${JWT_SECRET:your-secret-key-must-be-at-least-32-characters-long}
  expiration-ms: 86400000  # 24 hours

server:
  port: 8123
```

### 前端配置 (.env.development)

```bash
VITE_API_BASE_URL=http://localhost:8123
```

---

## 📖 文档

- [后端接口文档](./后端接口文档.md) - 完整的 API 接口说明
- [软件需求规格说明书](./2.%20软件需求规格说明书.md) - 详细需求文档
- [开发指南](./开发指南.md) - 开发快速上手
- [AI 集成方案](./AI集成方案.md) - AI 服务接入指南

---

## 🛣️ 路线图

### ✅ 已完成 (v1.0.0)
- [x] 用户认证系统（注册/登录/JWT）
- [x] API 密钥管理（多服务商支持）
- [x] AI 对话功能（文本生成、润色）
- [x] Markdown 文档编辑器
- [x] 写作灵感生成
- [x] 提示词模板系统
- [x] 对话历史管理
- [x] 响应式深色主题

### 🚧 计划中 (v1.1.0)
- [ ] 多文档云端同步
- [ ] 团队协作功能
- [ ] 更多 AI 模型支持
- [ ] 导出为 PDF/Word
- [ ] 写作统计与分析
- [ ] 浏览器插件版本

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👨‍💻 作者

**Erokin Team**

- 项目主页: [https://github.com/erokin/strove](https://github.com/erokin/strove)
- 联系邮箱: dev@strove.ai

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Element Plus](https://element-plus.org/) - UI 组件库
- [OpenAI](https://openai.com/) - AI 技术支持

---

## ⚠️ 免责声明

本项目仅供学习和研究使用。用户需自行承担使用第三方 AI 服务的费用和风险。请遵守各 AI 服务商的使用条款和法律法规。

---

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！**
