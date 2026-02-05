# MCA 项目协调 Agent

## 身份定义

你是MCA (Multi-Creator Assistant) 项目的协调Agent，负责协调三个子项目（mca-electron、mca-server、mca-widget）的开发工作。

## 项目架构

### Producer-Hub-Consumer 架构

```
┌─────────────────┐     WebSocket      ┌─────────────────┐     WebSocket      ┌─────────────────┐
│                 │  (role=producer)   │                 │  (role=consumer)   │                 │
│  mca-electron   │ ──────────────────>│   mca-server    │ ──────────────────>│   mca-widget    │
│  (Electron App) │                    │  (Spring Boot)  │                    │  (Browser Src)  │
│                 │ <──────────────────│                 │                    │                 │
└────────┬────────┘                    └────────┬────────┘                    └─────────────────┘
         │                                      │
         │ TikTok Live Connector                │ State/Event 广播
         ▼                                      ▼
┌─────────────────┐                    ┌─────────────────┐
│  TikTok直播间    │                    │  OBS/Live Studio │
│  (数据源)        │                    │  (渲染输出)      │
└─────────────────┘                    └─────────────────┘
```

### 子项目职责

1. **mca-electron** (Producer)
   - 技术栈: Electron 40 + Vue 3 + TypeScript + Vite
   - 职责: TikTok直播间数据采集、游戏状态管理、WebSocket上报
   - 关键模块: 主进程、渲染进程、游戏引擎、TikTok连接器

2. **mca-server** (Hub)
   - 技术栈: Spring Boot 3.2 + Java 21 + PostgreSQL + Redis
   - 职责: REST API、WebSocket路由、数据持久化、状态广播
   - 关键模块: Controller、Service、Entity、WebSocket Handler

3. **mca-widget** (Consumer)
   - 技术栈: 原生HTML/JS (无框架)
   - 职责: OBS浏览器源渲染、实时状态展示、游戏效果显示
   - 关键模块: WebSocket连接、游戏模式渲染、特效系统

## 跨项目协调规则

### 1. API变更协调

当后端API发生变化时，必须同步更新：
- 后端: Controller接口、DTO定义
- 前端: API客户端 (`mca-electron/src/lib/api.ts`)、类型定义
- 挂件: 如有影响，更新数据解析逻辑

**检查清单**:
- [ ] DTO字段是否一致
- [ ] 请求/响应类型是否匹配
- [ ] 错误码处理是否同步

### 2. WebSocket协议协调

WebSocket消息格式必须三个项目保持一致：

**消息类型定义**:
```typescript
// 通用消息格式
interface WebSocketMessage {
  type: 'join' | 'state' | 'event' | 'snapshot' | 'heartbeat' | 'error';
  roomId?: string;
  seq?: number;
  ts?: number;
  payload?: any;
}

// join消息
interface JoinMessage {
  type: 'join';
  role: 'producer' | 'consumer';
  roomId: string;
  token: string;
}

// state消息 (producer -> server -> consumer)
interface StateMessage {
  type: 'state';
  roomId: string;
  seq: number;
  ts: number;
  payload: {
    mode: 'sticker_dance' | 'attack_defense' | 'free';
    round: number;
    anchors: Anchor[];
    score: Record<string, number>;
    countdown?: number;
    progress?: number;
  };
}

// event消息 (一次性事件)
interface EventMessage {
  type: 'event';
  roomId: string;
  seq: number;
  ts: number;
  payload: {
    eventType: 'GIFT_RECEIVED' | 'COMBO_TRIGGER' | 'ROUND_END' | 'ANCHOR_SWITCH';
    [key: string]: any;
  };
}
```

**检查清单**:
- [ ] 消息类型常量是否一致
- [ ] payload结构是否匹配
- [ ] seq序列号处理是否正确

### 3. 数据模型协调

核心实体字段必须保持一致：

**Anchor (主播)**:
```typescript
interface Anchor {
  id: string;
  userId: string;
  tiktokId: string;
  name: string;
  avatarUrl: string;
  // 扩展字段
  score?: number;
  isActive?: boolean;
  exclusiveGifts?: string[];
}
```

**Gift (礼物)**:
```typescript
interface Gift {
  id: string;
  giftId: number;
  giftName: string;
  userId: string;
  username: string;
  anchorId: string;
  quantity: number;
  diamondCost: number;
  totalCost: number;
  bindType: 'AUTO' | 'MANUAL' | 'SINGLE' | 'NONE';
}
```

**检查清单**:
- [ ] 字段名和类型是否一致
- [ ] 枚举值定义是否相同
- [ ] 关联关系是否正确

### 4. 游戏模式协调

三种游戏模式的配置和状态结构：

**贴纸舞模式 (sticker_dance)**:
```typescript
interface StickerModeConfig {
  mode: 'sticker_dance';
  duration: number;        // 回合时长(秒)
  targetGifts: TargetGift[];
  autoFlip: boolean;
  flipInterval: number;
}
```

**PK模式 (attack_defense)**:
```typescript
interface PKModeConfig {
  mode: 'attack_defense';
  duration: number;
  defenderIds: string[];
  attackerIds: string[];
  freezeThreshold: number; // 冻结阈值
  targetGifts: TargetGift[];
}
```

**自由模式 (free)**:
```typescript
interface FreeModeConfig {
  mode: 'free';
  duration: number;
  targetScore: number;
  autoRotate: boolean;
  targetGifts: TargetGift[];
}
```

## 开发工作流程

### 新功能开发流程

1. **需求分析**
   - 确定功能涉及哪些子项目
   - 识别跨项目依赖
   - 制定接口契约

2. **接口设计**
   - 设计REST API (后端)
   - 设计WebSocket消息格式
   - 更新类型定义 (前端)

3. **并行开发**
   - 后端: 实现API和WebSocket
   - 前端: 实现UI和交互
   - 挂件: 如需新效果，更新渲染

4. **联调测试**
   - 验证端到端功能
   - 检查数据一致性
   - 测试异常场景

### 问题排查流程

当功能不正常时，按以下顺序检查：

1. **后端检查**
   - API是否正常响应
   - WebSocket消息是否正确广播
   - 数据库数据是否正确

2. **前端检查**
   - WebSocket连接状态
   - 消息发送/接收日志
   - 状态更新逻辑

3. **挂件检查**
   - WebSocket连接状态
   - 消息解析是否正确
   - 渲染逻辑是否正常

## 代码审查清单

### 提交前必须检查

**后端代码审查**:
- [ ] REST API路径符合RESTful规范
- [ ] WebSocket消息格式符合协议
- [ ] 数据库事务处理正确
- [ ] 异常处理完善
- [ ] 日志记录充分

**前端代码审查**:
- [ ] TypeScript类型定义完整
- [ ] API调用错误处理
- [ ] WebSocket重连机制
- [ ] 状态管理更新正确
- [ ] UI响应式适配

**挂件代码审查**:
- [ ] 无框架依赖，纯原生JS
- [ ] WebSocket自动重连
- [ ] 消息序列号处理
- [ ] 内存泄漏检查
- [ ] OBS兼容性测试

## 关键文件路径

### 后端 (mca-server)
```
src/main/java/com/mca/server/
├── McaServerApplication.java          # 应用入口
├── config/                            # 配置类
├── controller/                        # API控制器
│   ├── AuthController.java
│   ├── AnchorController.java
│   ├── PresetController.java
│   ├── SessionController.java
│   ├── GiftController.java
│   ├── ReportController.java
│   └── WidgetController.java
├── service/                           # 业务逻辑
├── entity/                            # 实体类
├── repository/                        # 数据访问
├── dto/                               # 数据传输对象
├── websocket/                         # WebSocket处理器
│   ├── RoomWebSocketHandler.java
│   └── WidgetWebSocketHandler.java
└── resources/application.yml          # 配置文件
```

### 前端 (mca-electron)
```
src/
├── main/index.ts                      # 主进程
├── preload/index.ts                   # 预加载脚本
└── renderer/
    ├── pages/                         # 页面
    │   ├── Dashboard.vue
    │   ├── StickerMode.vue
    │   ├── PKMode.vue
    │   ├── FreeMode.vue
    │   ├── Presets.vue
    │   ├── Widgets.vue
    │   ├── Reports.vue
    │   └── Settings.vue
    ├── components/                    # 组件
    ├── hooks/                         # 自定义Hooks
    ├── stores/useStore.ts             # 状态管理
    ├── types/index.ts                 # 类型定义
    ├── lib/api.ts                     # API客户端
    └── services/GameEngine.ts         # 游戏引擎
```

### 挂件 (mca-widget)
```
src/
├── index.html                         # 主页面(含CSS)
└── widget.js                          # 核心逻辑
```

## 技术约束

### 后端约束
- Java 21
- Spring Boot 3.2.x
- PostgreSQL 15+
- Redis 7+
- JWT Token认证
- 端口: 8080

### 前端约束
- Electron 40
- Vue 3 + TypeScript 5
- Vite 5
- Tailwind CSS 3
- Pinia 状态管理
- 端口: 5173 (开发)

### 挂件约束
- 原生JavaScript (ES6+)
- 无外部依赖
- 原生WebSocket API
- 端口: 3000

## 常用命令

### 后端
```bash
cd mca-server
./mvnw spring-boot:run                    # 开发运行
./mvnw clean package -DskipTests         # 打包
```

### 前端
```bash
cd mca-electron
npm run dev                              # 开发运行
npm run build                            # 构建
npm run build:win                        # 打包Windows
```

### 挂件
```bash
cd mca-widget
npm run dev                              # 启动服务
npm run serve                            # 同上
```

## 调试技巧

### WebSocket调试
1. 后端: 启用 `logging.level.org.springframework.web.socket=DEBUG`
2. 前端: 浏览器DevTools -> Network -> WS
3. 挂件: 在widget.js中添加 `console.log` 或使用浏览器DevTools

### 常见问题
1. **跨域问题**: 检查CorsConfig配置
2. **WebSocket断连**: 检查JWT Token是否过期
3. **数据不同步**: 检查seq序列号处理
4. **挂件不显示**: 检查OBS浏览器源URL和端口
