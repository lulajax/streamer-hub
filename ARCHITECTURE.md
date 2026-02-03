# MCA 挂件系统架构文档

## 系统概述

MCA 挂件系统采用 **Producer-Hub-Consumer** 架构模式：

```
┌─────────────────┐     WebSocket      ┌─────────────────┐     WebSocket      ┌─────────────────┐
│                 │  (role=producer)   │                 │  (role=consumer)   │                 │
│  Electron 客户端 │ ──────────────────>│  服务端 WebSocket │ ──────────────────>│  挂件 (浏览器源) │
│  (Producer)     │                    │  (Hub/Router)   │                    │  (Consumer)    │
│                 │ <──────────────────│                 │                    │                 │
└─────────────────┘                    └─────────────────┘                    └─────────────────┘
       │                                        │
       │ TikTok Live Connector                  │ State/Event 广播
       ▼                                        ▼
┌─────────────────┐                    ┌─────────────────┐
│  TikTok 直播间   │                    │  OBS/Live Studio │
│  (数据源)        │                    │  (渲染输出)      │
└─────────────────┘                    └─────────────────┘
```

## 核心概念

### 1. roomId
- 字符串，全局唯一
- 表示一个「直播间的一次运行实例」
- Electron 与挂件必须使用同一个 roomId

### 2. 连接角色 (role)
- `producer`: Electron 客户端（每个 room 只允许 1 个）
- `consumer`: 挂件（浏览器源，允许多个）

### 3. 消息类型

#### state (状态快照)
```json
{
  "type": "state",
  "roomId": "r_xxx",
  "seq": 1024,
  "ts": 1730000000000,
  "payload": {
    "mode": "attack_defense | sticker_dance | free",
    "round": 1,
    "score": { "a": 10, "b": 8 },
    "anchors": [...],
    "combo": 3,
    "progress": 0.62,
    "countdown": 60
  }
}
```

#### event (一次性事件)
```json
{
  "type": "event",
  "roomId": "r_xxx",
  "seq": 1025,
  "ts": 1730000000500,
  "payload": {
    "eventType": "GIFT_RECEIVED",
    "giftId": 5655,
    "user": "tom",
    "effect": "boom"
  }
}
```

#### snapshot (服务端 → consumer)
```json
{
  "type": "snapshot",
  "roomId": "r_xxx",
  "seq": 2048,
  "payload": { ...latestState }
}
```

## 服务端实现 (Spring Boot)

### WebSocket 配置
```java
registry.addHandler(roomWebSocketHandler, "/ws/room")
        .setAllowedOrigins("*");
```

### 连接管理
```java
Map<String, WebSocketSession> roomProducerMap;      // roomId -> Producer
Map<String, Set<WebSocketSession>> roomConsumerMap; // roomId -> Consumers
Map<String, JsonNode> latestStateMap;               // roomId -> Latest State
Map<String, AtomicLong> seqCounterMap;              // roomId -> Sequence Counter
```

### 消息处理流程

1. **join 消息**
   - 校验 token
   - 校验 roomId
   - producer: 检查是否已有 producer
   - consumer: 直接加入，发送 snapshot

2. **state 消息 (producer → server)**
   - 更新 latestStateMap
   - 广播给所有 consumer

3. **event 消息 (producer → server)**
   - 直接广播给 consumer

4. **断线处理**
   - producer 断线: 标记 room 为「无生产者」
   - consumer 断线: 直接移除

## Electron 客户端实现

### WebSocket Producer Hook
```typescript
const { connect, sendState, sendEvent, status } = useRoomWebSocket()

// 连接
connect(roomId, token, 'producer')

// 发送状态
sendState({ mode: 'sticker_dance', round: 1, score: { a: 10 } })

// 发送事件
sendEvent({ eventType: 'GIFT_RECEIVED', giftId: 5655 })
```

### Game Engine
- 贴纸舞模式: `StickerDanceEngine`
- 攻守擂模式: `AttackDefenseEngine`
- 自由模式: `FreeModeEngine`

### 游戏管理器
```typescript
const {
  initializeGame,
  connectProducer,
  startGame,
  pauseGame,
  processGift,
  getWidgetUrl,
} = useGameManager()
```

## 挂件实现 (HTML + JS)

### URL 格式
```
http://localhost:3000/index.html?roomId=r_xxx&token=overlay_token
```

### 启动流程
1. 解析 URL 参数
2. 建立 WebSocket
3. 发送 `join(role=consumer)`
4. 等待 snapshot / state

### 渲染规则
- **snapshot**: 直接全量刷新 UI
- **state**: 若 seq <= lastSeq → 忽略，否则更新 UI
- **event**: 播放动画，不影响主状态

### 游戏模式渲染

#### 贴纸舞模式
- 主播头像网格
- 分数显示
- 倒计时
- Combo 显示

#### 攻守擂模式
- 双方分数
- 进度条
- 冻结特效

#### 自由模式
- 当前主播
- 进度条
- 排行榜
- 倒计时

## 通信协议

### WebSocket URL
```
ws://localhost:8080/ws/room
```

### 连接后第一条消息 (join)
```json
{
  "type": "join",
  "role": "producer | consumer",
  "roomId": "r_xxx",
  "token": "JWT_TOKEN"
}
```

### 心跳
```json
{ "type": "heartbeat", "ts": 1730000001000 }
```

## 安全与权限

### Token 验证
- 使用 JWT Token
- producer: 需要有效用户 token
- consumer: 使用 overlay token（短期有效）

### 权限控制
- 同 room 只允许 1 个 producer
- consumer 数量无限制
- token 过期后自动断开

## 同步与稳定性

### 重连机制
- consumer 断线重连后重新 join，等待 snapshot
- producer 只管持续上报
- 服务端永远保存 latestState

### 序列号 (seq)
- 单调递增
- consumer 丢弃 seq <= lastSeq 的消息
- 保证状态一致性

## 最小可运行验收标准 (MVP)

- [x] Electron 启动 → 服务端看到 producer online
- [x] 浏览器源打开 → 立即收到 snapshot
- [x] Electron 改变状态 → 挂件 < 200ms 内同步
- [x] 挂件刷新页面 → 状态不丢
- [x] Electron 断网 → 挂件显示等待
- [x] 重连后 → 自动恢复

## 部署指南

### 1. 启动后端
```bash
cd mca-server
./mvnw spring-boot:run
```

### 2. 启动挂件服务器
```bash
cd mca-widget
npm run serve
```

### 3. 启动 Electron
```bash
cd mca-electron
npm run dev
```

### 4. OBS 配置
1. 添加「浏览器源」
2. URL: `http://localhost:3000/index.html?roomId=YOUR_ROOM_ID&token=YOUR_TOKEN`
3. 宽度: 800, 高度: 600

## 扩展计划

- [ ] Redis / Kafka 多机扩展
- [ ] 云端回放
- [ ] 多挂件类型（榜单 / 特效 / HUD）
- [ ] SaaS 管理后台
