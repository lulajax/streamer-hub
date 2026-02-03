# MCA 团播工具 (Multi-Caster Assistant)

一款专为 TikTok 直播间团播设计的辅助工具，支持贴纸舞、攻守擂(PK)、自由三种团播模式，提供实时礼物数据采集、主播榜单统计、用户绑定分配等功能。

## 功能特性

### 用户系统
- 📧 **邮箱注册** - 支持邮箱注册和验证
- 🔐 **安全登录** - JWT Token 身份验证
- 💎 **激活码激活** - 通过激活码解锁高级权益 (BASIC/PRO/ENTERPRISE)
- 👤 **用户管理** - 个人资料、订阅管理

### TikTok 直播集成
- 🔗 **TikTok-Live-Connector** - 使用开源项目实现直播数据采集
- 📺 **开播检测** - 自动检测主播是否在线
- 🎁 **礼物数据** - 实时获取直播间礼物信息
- 👥 **用户互动** - 接收评论、点赞、关注等事件
- 📊 **房间信息** - 获取直播间标题、观看人数等

### 游戏模式
- 🎭 **贴纸舞模式** - 普通/计票模式、倒计时、衰减机制、自动翻页
- ⚔️ **攻守擂(PK)模式** - 攻守双方配置、实时进度条、冻结特效
- 🎯 **自由模式** - 主播轮流出场、达标分数设定

### 数据管理
- 📈 **实时榜单** - 主播分数排行
- 📑 **礼物记录** - 详细的收礼记录
- 📊 **数据报表** - Excel 导出、统计分析
- 🔗 **OBS 挂件** - 直播间挂件链接生成

### 多语言支持
- 🇨🇳 简体中文
- 🇺🇸 English
- 🇻🇳 Tiếng Việt
- 🇹🇭 ไทย

## 技术栈

### 前端
- **框架**: Electron 28 + React 18 + TypeScript
- **构建**: Vite 5
- **样式**: Tailwind CSS + shadcn/ui
- **状态**: Zustand
- **多语言**: i18next
- **图表**: Recharts

### 后端
- **框架**: Spring Boot 3.2 + Java 21
- **数据库**: PostgreSQL 15+
- **缓存**: Redis 7+
- **实时通信**: WebSocket
- **邮件服务**: Spring Mail
- **安全**: Spring Security + JWT

### TikTok 数据采集
- **TikTok-Live-Connector** - [GitHub](https://github.com/zerodytrash/TikTok-Live-Connector)
- 使用 Euler Stream API 进行签名验证
- WebSocket 实时接收直播事件

## 快速开始

### 环境要求

- Node.js 20+
- Java 21
- PostgreSQL 15+
- Redis 7+
- SMTP 邮件服务器 (用于发送验证邮件)

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/mca-replica.git
cd mca-replica
```

#### 2. 配置后端

```bash
cd mca-server

# 配置数据库
# 编辑 src/main/resources/application.yml

# 配置邮件服务
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

# 运行后端
./mvnw spring-boot:run
```

#### 3. 配置前端

```bash
cd mca-electron

# 安装依赖
npm install

# 开发模式
npm run dev

# 打包应用
npm run build:win    # Windows
npm run build:mac    # macOS
npm run build:linux  # Linux
```

### 数据库初始化

```sql
-- PostgreSQL
CREATE DATABASE mca_db;
CREATE USER mca_user WITH PASSWORD 'mca_password';
GRANT ALL PRIVILEGES ON DATABASE mca_db TO mca_user;
```

## 使用指南

### 1. 注册账号

1. 打开应用，点击"Register"
2. 输入邮箱、密码、昵称
3. 检查邮箱，点击验证链接

### 2. 激活权益

1. 登录后，输入激活码激活高级权益
2. 激活码格式: `BAS-XXXXXXXXXXXX`, `PRO-XXXXXXXXXXXX`, `ENT-XXXXXXXXXXXX`
3. 不同级别解锁不同功能

### 3. 连接直播间

1. 输入 TikTok 主播 ID (如: @username)
2. 点击"Connect"连接直播间
3. 系统会自动检测主播是否在线

### 4. 使用游戏模式

1. 选择游戏模式 (贴纸舞/PK/自由)
2. 配置主播和礼物
3. 点击"Start"开始游戏

### 5. OBS 集成

1. 进入"Widgets"页面
2. 复制需要的挂件链接
3. 在 OBS 中添加"浏览器源"
4. 粘贴链接并调整大小

## API 文档

### 认证相关

```
POST   /api/auth/register              # 注册
GET    /api/auth/verify-email          # 验证邮箱
POST   /api/auth/login                 # 登录
POST   /api/auth/activate              # 激活权益
GET    /api/auth/me                    # 获取当前用户
POST   /api/auth/forgot-password       # 忘记密码
POST   /api/auth/reset-password        # 重置密码
```

### TikTok 直播相关

```
GET    /api/tiktok/is-live/{uniqueId}  # 检查是否在线
GET    /api/tiktok/room-info/{uniqueId} # 获取房间信息
GET    /api/tiktok/room-id/{uniqueId}   # 获取房间ID
GET    /api/tiktok/gifts/{roomId}       # 获取可用礼物
GET    /api/tiktok/websocket-url        # 获取WebSocket连接
GET    /api/tiktok/connect-info/{uniqueId} # 获取完整连接信息
```

### 礼物相关

```
POST   /api/gifts/session/{sessionId}   # 记录礼物
GET    /api/gifts/session/{sessionId}   # 获取礼物记录
PUT    /api/gifts/{giftId}/bind         # 绑定礼物
```

### 会话相关

```
POST   /api/sessions                    # 创建会话
POST   /api/sessions/{id}/start         # 开始会话
POST   /api/sessions/{id}/pause         # 暂停会话
POST   /api/sessions/{id}/end           # 结束会话
```

## 配置文件

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mca_db
    username: mca_user
    password: mca_password
  
  redis:
    host: localhost
    port: 6379
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

jwt:
  secret: your-secret-key
  expiration: 86400000

mca:
  activation:
    default-expiry-days: 365
  tiktok:
    sign-api-key: ""  # Optional: Euler Stream API key
```

## 开发指南

### 项目结构

```
mca-replica/
├── mca-electron/          # Electron 前端
│   ├── src/
│   │   ├── components/    # UI 组件
│   │   ├── pages/         # 页面
│   │   ├── hooks/         # 自定义 Hooks
│   │   ├── stores/        # 状态管理
│   │   └── types/         # TypeScript 类型
│   └── package.json
│
├── mca-server/            # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/mca/server/
│   │       ├── controller/  # API 控制器
│   │       ├── service/     # 业务逻辑
│   │       ├── repository/  # 数据访问
│   │       ├── entity/      # 实体类
│   │       └── dto/         # 数据传输对象
│   └── pom.xml
│
└── README.md
```

### 开发模式运行

```bash
# 终端 1: 运行后端
cd mca-server
./mvnw spring-boot:run

# 终端 2: 运行前端
cd mca-electron
npm run dev
```

## 部署指南

### Docker 部署 (推荐)

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

### 手动部署

1. **部署后端**
   ```bash
   cd mca-server
   ./mvnw clean package -DskipTests
   java -jar target/mca-server-1.0.0.jar
   ```

2. **部署前端**
   ```bash
   cd mca-electron
   npm run build
   # 分发打包后的应用
   ```

## 常见问题

### Q: 邮件发送失败？
A: 检查邮件服务器配置，确保 SMTP 设置正确。对于 Gmail，需要使用应用专用密码。

### Q: TikTok 连接失败？
A: 
- 确保主播正在直播
- 检查网络连接
- 部分地区可能需要 VPN
- 检查 Euler Stream API 状态

### Q: OBS 挂件不显示？
A: 
- 确保 OBS 可以访问后端服务
- 检查挂件 URL 是否正确
- 尝试刷新浏览器源

### Q: 激活码无效？
A: 
- 检查激活码格式是否正确
- 确认激活码未被使用
- 联系管理员获取新激活码

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

MIT License

## 致谢

- [TikTok-Live-Connector](https://github.com/zerodytrash/TikTok-Live-Connector) - TikTok 直播数据采集
- [shadcn/ui](https://ui.shadcn.com/) - UI 组件库
- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架

## 联系方式

- 邮箱: support@mca-app.com
- 官网: https://mca-app.com
