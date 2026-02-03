# MCA 团播工具 - 开发文档

## 项目结构

```
mca-replica/
├── mca-electron/          # Electron 前端应用
│   ├── src/
│   │   ├── main/          # Electron 主进程
│   │   │   └── index.ts   # 主进程入口
│   │   ├── preload/       # 预加载脚本
│   │   │   └── index.ts   # 预加载脚本入口
│   │   └── renderer/      # React 渲染进程
│   │       ├── components/# UI 组件
│   │       │   ├── ui/    # shadcn/ui 组件
│   │       │   ├── ActivationDialog.tsx
│   │       │   ├── RoomConnection.tsx
│   │       │   └── MainLayout.tsx
│   │       ├── pages/     # 页面组件
│   │       │   ├── Dashboard.tsx
│   │       │   ├── StickerMode.tsx
│   │       │   ├── PKMode.tsx
│   │       │   ├── FreeMode.tsx
│   │       │   ├── Widgets.tsx
│   │       │   ├── Reports.tsx
│   │       │   └── Settings.tsx
│   │       ├── stores/    # 状态管理
│   │       │   └── useStore.ts
│   │       ├── types/     # TypeScript 类型
│   │       │   └── index.ts
│   │       ├── i18n/      # 多语言
│   │       │   └── index.ts
│   │       ├── hooks/     # 自定义 Hooks
│   │       ├── lib/       # 工具函数
│   │       ├── styles/    # 样式文件
│   │       ├── App.tsx    # 应用入口
│   │       ├── main.tsx   # 渲染进程入口
│   │       └── index.html # HTML 模板
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── tailwind.config.js
│
├── mca-server/            # Spring Boot 后端服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/mca/server/
│   │       │   ├── McaServerApplication.java
│   │       │   ├── config/          # 配置类
│   │       │   │   ├── WebSocketConfig.java
│   │       │   │   └── CorsConfig.java
│   │       │   ├── controller/      # API 控制器
│   │       │   │   ├── ActivationController.java
│   │       │   │   ├── GiftController.java
│   │       │   │   ├── SessionController.java
│   │       │   │   └── ReportController.java
│   │       │   ├── service/         # 业务逻辑
│   │       │   │   ├── ActivationService.java
│   │       │   │   ├── GiftService.java
│   │       │   │   ├── SessionService.java
│   │       │   │   └── ReportService.java
│   │       │   ├── repository/      # 数据访问
│   │       │   │   ├── ActivationRepository.java
│   │       │   │   ├── RoomRepository.java
│   │       │   │   ├── AnchorRepository.java
│   │       │   │   ├── GiftRecordRepository.java
│   │       │   │   ├── PresetRepository.java
│   │       │   │   └── SessionRepository.java
│   │       │   ├── entity/          # 实体类
│   │       │   │   ├── Activation.java
│   │       │   │   ├── Room.java
│   │       │   │   ├── Anchor.java
│   │       │   │   ├── GiftRecord.java
│   │       │   │   ├── Preset.java
│   │       │   │   ├── Session.java
│   │       │   │   └── Round.java
│   │       │   ├── dto/             # 数据传输对象
│   │       │   │   ├── ApiResponse.java
│   │       │   │   ├── ActivationRequest.java
│   │       │   │   ├── ActivationResponse.java
│   │       │   │   ├── GiftRecordDTO.java
│   │       │   │   ├── AnchorDTO.java
│   │       │   │   ├── PresetDTO.java
│   │       │   │   └── SessionDTO.java
│   │       │   ├── websocket/       # WebSocket 处理
│   │       │   │   └── GiftWebSocketHandler.java
│   │       │   └── exception/       # 异常处理
│   │       │       └── GlobalExceptionHandler.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── mca-widget/            # 直播间挂件 (可选)
│   └── src/
│
├── README.md              # 项目说明
└── DEVELOPMENT.md         # 开发文档
```

## 技术栈

### 前端 (Electron + React)

| 技术 | 版本 | 用途 |
|------|------|------|
| Electron | 28.x | 桌面应用框架 |
| React | 18.x | UI 框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Tailwind CSS | 3.x | 样式框架 |
| shadcn/ui | - | UI 组件库 |
| Zustand | 4.x | 状态管理 |
| Socket.io-client | 4.x | WebSocket 客户端 |
| i18next | 23.x | 多语言支持 |
| Recharts | 2.x | 图表库 |

### 后端 (Spring Boot)

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.x | 应用框架 |
| Java | 21 | 编程语言 |
| PostgreSQL | 15+ | 主数据库 |
| Redis | 7+ | 缓存 |
| WebSocket (STOMP) | - | 实时通信 |
| JWT | 0.12.x | 身份验证 |
| Apache POI | 5.x | Excel 导出 |

## 快速开始

### 环境要求

- Node.js 20+
- Java 21
- PostgreSQL 15+
- Redis 7+

### 前端开发

```bash
# 进入前端目录
cd mca-electron

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建
npm run build

# 打包应用
npm run build:win    # Windows
npm run build:mac    # macOS
npm run build:linux  # Linux
```

### 后端开发

```bash
# 进入后端目录
cd mca-server

# 使用 Maven Wrapper 运行
./mvnw spring-boot:run

# 或者打包后运行
./mvnw clean package -DskipTests
java -jar target/mca-server-1.0.0.jar
```

### 数据库初始化

```sql
-- PostgreSQL
CREATE DATABASE mca_db;
CREATE USER mca_user WITH PASSWORD 'mca_password';
GRANT ALL PRIVILEGES ON DATABASE mca_db TO mca_user;

-- Redis (默认端口 6379)
redis-server
```

## 核心功能模块

### 1. 激活系统
- 16位激活码验证
- 设备绑定
- 有效期管理

### 2. 直播间连接
- TikTok ID 输入验证
- 内置浏览器监控窗口
- 礼物数据采集

### 3. 贴纸舞模式
- 普通模式：显示专属礼物贴纸
- 计票模式：实时显示礼物数量
- 倒计时/衰减机制
- 自动翻页功能

### 4. 攻守擂(PK)模式
- 攻守双方配置
- 实时 PK 进度条
- 劣势冻结特效
- 多种计分方式

### 5. 自由模式
- 主播轮流出场
- 达标分数设定
- 排行榜实时更新

### 6. 礼物绑定系统
- 专属礼物自动绑定
- 手动绑定/单次分配
- 绑定关系持久化

### 7. 挂件系统
- 主挂件（贴纸舞/PK/自由模式）
- 榜单挂件（横版/竖版）
- 头像框挂件
- OBS/LiveStudio 集成

### 8. 数据报表
- Excel 导出
- 60天数据保留
- 多维度统计分析

## API 文档

### 激活相关

```
POST   /api/activation              # 激活设备
GET    /api/activation/check/{deviceId}  # 检查激活状态
POST   /api/activation/deactivate/{deviceId}  # 取消激活
POST   /api/activation/generate     # 生成激活码
```

### 礼物相关

```
POST   /api/gifts/session/{sessionId}  # 记录礼物
GET    /api/gifts/session/{sessionId}  # 获取礼物记录
GET    /api/gifts/session/{sessionId}/type/{bindType}  # 按类型筛选
PUT    /api/gifts/{giftId}/bind      # 更新礼物绑定
GET    /api/gifts/session/{sessionId}/top  # 获取热门礼物
```

### 会话相关

```
POST   /api/sessions                 # 创建会话
POST   /api/sessions/{sessionId}/start    # 开始会话
POST   /api/sessions/{sessionId}/pause    # 暂停会话
POST   /api/sessions/{sessionId}/end      # 结束会话
POST   /api/sessions/{sessionId}/next-round  # 下一轮
GET    /api/sessions/{sessionId}     # 获取会话详情
GET    /api/sessions/room/{roomId}   # 获取房间会话列表
```

### 报表相关

```
GET    /api/reports/session/{sessionId}/export  # 导出会话报表
```

### WebSocket

```
WS     /ws/gifts/{roomId}           # 礼物实时推送
```

## 配置文件

### 前端 (.env)

```
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
```

### 后端 (application.yml)

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

jwt:
  secret: your-secret-key
  expiration: 86400000
```

## 部署指南

### 生产环境部署

1. **服务器准备**
   - 安装 Java 21
   - 安装 PostgreSQL
   - 安装 Redis
   - 配置防火墙

2. **后端部署**
   ```bash
   ./mvnw clean package -DskipTests
   nohup java -jar target/mca-server-1.0.0.jar > app.log 2>&1 &
   ```

3. **前端打包**
   ```bash
   npm run build
   npm run build:win  # 或其他平台
   ```

4. **Nginx 配置（可选）**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location /api {
           proxy_pass http://localhost:8080/api;
       }
       
       location /ws {
           proxy_pass http://localhost:8080/ws;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection "upgrade";
       }
   }
   ```

## 开发注意事项

1. **代码规范**
   - 使用 ESLint 和 Prettier 进行代码格式化
   - 遵循 Java 代码规范
   - 提交前运行测试

2. **Git 工作流**
   - main: 生产分支
   - develop: 开发分支
   - feature/*: 功能分支
   - hotfix/*: 热修复分支

3. **测试**
   - 单元测试：Jest (前端), JUnit (后端)
   - 集成测试：Cypress (前端)
   - E2E 测试：Playwright

4. **安全**
   - 使用 HTTPS
   - JWT Token 定期刷新
   - 输入验证和 XSS 防护
   - SQL 注入防护

## 常见问题

### Q: 监控窗口无法显示 TikTok 页面？
A: 检查网络连接，确保可以访问 TikTok。部分地区可能需要 VPN。

### Q: 礼物数据无法采集？
A: 确保监控窗口保持打开且未最小化。检查 WebSocket 连接状态。

### Q: OBS 挂件不显示？
A: 检查挂件 URL 是否正确，确保 OBS 可以访问后端服务。

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License
