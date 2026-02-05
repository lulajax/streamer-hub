# TikTok Client 使用说明

本文档说明 `packages/tiktok-client` 的功能、结构与使用方法，供后续维护与集成参考（已作为本仓库私有包使用）。

## 1. 功能概览
- 连接 TikTok LIVE：获取 roomId、房间信息，建立 WebSocket，接收弹幕/礼物/关注等事件
- HTTP/Web 路由封装：对 TikTok Web/Webcast API 的请求封装
- LiveConnectionData：通过 `api/live-client/liveConnectionData` 获取 websocketUrl/websocketCookies
- protobuf 解码：解析 Webcast 二进制消息并映射为事件
- 运行方式：主进程托管独立 Node 子进程，渲染进程通过 IPC 使用（避免阻塞渲染进程）

## 2. 目录结构
```
packages/
  tiktok-client/
    src/
      bridge/worker.ts          # 子进程入口（IPC）
      lib/                      # 核心实现
      types/                    # 类型与 schema
      index.ts                  # 包入口
      version.ts
    dist/                       # tsc 输出（运行时使用）
    package.json
    tsconfig.json
```

## 3. 核心类与职责
### 3.1 TikTokLiveConnection（`client.ts`）
- 负责连接/断开，管理连接状态
- 拉取 roomId/roomInfo/giftInfo（可选）
- 获取 liveConnectionData 并建立 WS
- 解析消息并派发事件

### 3.2 TikTokWebClient（`web/index.ts`）
- 聚合 HTTP 路由
- TikTok 原生来源：HTML / API-Live / Webcast
- 提供 Euler 与自定义签名相关路由（可按配置选择/兜底）

### 3.3 TikTokWsClient（`ws/lib/ws-client.ts`）
- 连接 WS，解码消息
- 发送 ACK/心跳/进入房间消息

## 4. 主要流程（高层）
1) `connect()` -> 获取 liveConnectionData  
2) `fetchRoomInfo` / `fetchAvailableGifts`（可选）  
3) 使用 websocketUrl/websocketCookies 建立 WS  
4) 进入房间  
5) 解码消息并触发事件

## 5. 使用示例
### 5.1 Node 侧（主进程/子进程）
```ts
import { TikTokLiveConnection, WebcastEvent, ControlEvent } from '@local/tiktok-client';

const conn = new TikTokLiveConnection('some_user', {
  fetchRoomInfoOnConnect: true,
  processInitialData: true,
  signApiKey: 'your-key'
});

conn.on(ControlEvent.CONNECTED, (state) => {
  console.log('connected', state.roomId);
});

conn.on(WebcastEvent.CHAT, (msg) => {
  console.log('chat', msg.content);
});

conn.on(ControlEvent.ERROR, (e) => {
  console.error(e.info, e.exception);
});

await conn.connect();
```

### 5.2 渲染进程（IPC 桥接）
```ts
const tiktokWsBridge = window.tiktokWsBridge;
tiktokWsBridge?.connect({
  hostName: 'some_user',
  authorization: 'Bearer <token>',
  proxy: {
    url: 'http://127.0.0.1:7890'
  },
  options: {
    fetchRoomInfoOnConnect: true,
    processInitialData: true
  },
  debug: true
});

const removeStatus = tiktokWsBridge?.onStatus((status) => {
  console.log('status', status);
});

const removeEvent = tiktokWsBridge?.onEvent((message) => {
  console.log(message.event, message.payload);
});
```

authorization 会透传到 HTTP 请求头。proxy.url 用于指定代理（http/https），用于子进程 HTTP/WS 请求。

### 5.3 发送聊天
sendMessage 默认使用 Euler 服务，Euler 失败且配置自定义签名服务时才会使用自定义服务。

### 5.4 仅 HTTP 获取房间信息
```ts
import { TikTokWebClient } from '@local/tiktok-client';

const web = new TikTokWebClient({
  customHeaders: {},
  axiosOptions: {},
  clientParams: {}
});

const roomInfo = await web.fetchRoomInfo({ roomId: '1234567890' });
```

## 6. 构建与运行
- 首次使用或修改后需构建：`npm run build:tiktok-client`
- 开发阶段可选：`npm run build:tiktok-client:watch`
- 子进程入口：`packages/tiktok-client/dist/bridge/worker.js`

## 7. 关键配置与环境变量
- `VITE_API_SERVICE`：业务 API 服务地址（liveConnectionData 使用）
- `apiAuthorization`：TikTokLiveConnection 选项，用于 liveConnectionData 的 Authorization 头
- `WHITELIST_AUTHENTICATED_SESSION_ID_HOST`：Euler 已注释，不再需要
- `TIKTOK_CLIENT_TIMEOUT`：HTTP 请求超时（毫秒）
- `RANDOMIZE_TIKTOK_DEVICE` / `RANDOMIZE_TIKTOK_LOCATION` / `RANDOMIZE_TIKTOK_SCREEN`：随机化设备/地区/屏幕参数
- `SIGN_API_URL` / `SIGN_API_KEY`：Euler 签名服务地址与密钥（可选）
- `CUSTOM_SIGN_API_URL` / `CUSTOM_SIGN_API_KEY` / `CUSTOM_SIGN_HEADERS`：自定义签名服务（Euler 兼容）
 - `customSignConfig`：在创建 `TikTokLiveConnection` 时传入，按连接实例覆盖自定义签名的 basePath/apiKey/headers（适合多账号动态配置）

自定义签名服务启用后，连接流程会优先使用 Euler，再回退到自定义签名。

## 8. 常见错误与排查
- `UserOfflineError`：主播未开播
- `FetchIsLiveError`：HTML/API 均无法获取开播状态

## 9. 事件订阅提示
事件常用枚举在 `types/events.ts` 中：
- 控制事件：`ControlEvent.CONNECTED / DISCONNECTED / ERROR / WEBSOCKET_DATA`
- 业务事件：`WebcastEvent.CHAT / GIFT / LIKE / FOLLOW / SHARE / BARRAGE ...`

建议只订阅业务需要的事件，避免高频事件导致性能压力。
