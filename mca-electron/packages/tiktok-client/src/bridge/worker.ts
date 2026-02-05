import {ProxyAgent} from 'proxy-agent';
import {
  ControlEvent,
  TikTokLiveConnection,
  TikTokLiveConstructorConnectionOptions,
  WebcastEvent
} from '..';

type ConnectPayload = {
  hostName: string;
  authorization?: string;
  options?: Partial<TikTokLiveConstructorConnectionOptions>;
  proxy?: {
    url: string;
  };
  debug?: boolean;
};

type WorkerCommand =
  | { type: 'connect'; payload: ConnectPayload }
  | { type: 'disconnect' }
  | { type: 'shutdown' };

type WorkerMessage =
  | { type: 'status'; status: 'connected' | 'disconnected'; detail?: unknown; state?: unknown }
  | { type: 'event'; event: string; payload: unknown }
  | { type: 'error'; message: string; detail?: unknown }
  | { type: 'debug'; message: string };

const send = (message: WorkerMessage) => {
  if (process.send) {
    process.send(message);
  }
};

const serializeError = (error: unknown) => {
  if (error instanceof Error) {
    return { message: error.message, stack: error.stack };
  }
  return { message: String(error) };
};

let connection: TikTokLiveConnection | null = null;
let debugEnabled = false;

const sendDebug = (message: string) => {
  if (debugEnabled) {
    send({ type: 'debug', message });
  }
};

const disposeConnection = async () => {
  if (!connection) {
    return;
  }
  connection.removeAllListeners();
  try {
    await connection.disconnect();
  } catch (error) {
    sendDebug(`disconnect failed: ${serializeError(error).message}`);
  }
  connection = null;
};

const bindConnection = (conn: TikTokLiveConnection) => {
  conn.on(ControlEvent.CONNECTED, (state) => {
    send({ type: 'status', status: 'connected', state });
  });

  conn.on(ControlEvent.DISCONNECTED, (detail) => {
    send({ type: 'status', status: 'disconnected', detail });
  });

  conn.on(ControlEvent.ERROR, (err: any) => {
    const message = err?.info || 'tiktok client error';
    send({ type: 'error', message, detail: serializeError(err?.exception ?? err) });
  });

  conn.on(WebcastEvent.GIFT, (payload) => {
    send({ type: 'event', event: 'gift', payload });
  });

  conn.on(WebcastEvent.CHAT, (payload) => {
    send({ type: 'event', event: 'chat', payload });
  });

  conn.on(WebcastEvent.LIKE, (payload) => {
    send({ type: 'event', event: 'like', payload });
  });
};

const handleConnect = async (payload: ConnectPayload) => {
  const hostName = payload.hostName?.trim();
  if (!hostName) {
    send({ type: 'error', message: 'Missing hostName' });
    return;
  }

  debugEnabled = Boolean(payload.debug);

  await disposeConnection();

  const options: TikTokLiveConstructorConnectionOptions = {
    ...(payload.options ?? {})
  } as TikTokLiveConstructorConnectionOptions;

  const proxyUrl = payload.proxy?.url?.trim();
  const proxyAgent = proxyUrl
    ? new ProxyAgent({
        getProxyForUrl: () => proxyUrl
      })
    : undefined;

  if (payload.authorization) {
    options.webClientHeaders = {
      ...(options.webClientHeaders ?? {}),
      Authorization: payload.authorization
    };
  }

  if (proxyAgent) {
    options.webClientOptions = {
      ...(options.webClientOptions ?? {}),
      proxy: false,
      httpAgent: proxyAgent,
      httpsAgent: proxyAgent
    };
    options.wsClientOptions = {
      ...(options.wsClientOptions ?? {}),
      agent: proxyAgent
    };
  }

  const conn = new TikTokLiveConnection(hostName, options);
  connection = conn;
  bindConnection(conn);

  try {
    await conn.connect();
  } catch (error) {
    send({ type: 'error', message: 'connect failed', detail: serializeError(error) });
    await disposeConnection();
    send({ type: 'status', status: 'disconnected' });
  }
};

const handleCommand = async (command: WorkerCommand) => {
  switch (command.type) {
    case 'connect':
      await handleConnect(command.payload);
      return;
    case 'disconnect':
      await disposeConnection();
      send({ type: 'status', status: 'disconnected' });
      return;
    case 'shutdown':
      await disposeConnection();
      process.exit(0);
      return;
    default:
      return;
  }
};

process.on('message', (message: WorkerCommand) => {
  void handleCommand(message);
});

process.on('disconnect', () => {
  void disposeConnection().finally(() => process.exit(0));
});

process.on('uncaughtException', (error) => {
  send({ type: 'error', message: 'uncaughtException', detail: serializeError(error) });
});

process.on('unhandledRejection', (reason) => {
  send({ type: 'error', message: 'unhandledRejection', detail: serializeError(reason) });
});
