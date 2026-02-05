import WebcastHttpClient from './lib/http-client';
import {
    FetchRoomIdFromCustomRoute,
    FetchRoomInfoFromCustomRoute,
    FetchRoomInfoRoute,
    FetchSignedWebSocketFromCustomRoute,
    SendRoomChatFromCustomRoute,
    SendRoomChatFromEulerRoute
} from './routes';
import { FetchRoomInfoFromHtmlRoute } from './routes/fetch-room-info-html';
import { FetchSignedWebSocketFromEulerRoute } from './routes/fetch-signed-websocket-euler';
import { FetchRoomIdFromEulerRoute } from './routes/fetch-room-id-euler';
import { FetchRoomInfoFromEulerRoute } from './routes/fetch-room-info-euler';
import { FetchRoomInfoFromApiLiveRoute } from './routes/fetch-room-info-api-live';

// Export all types and classes
export * from './routes';
export * from './lib';

// Export a wrapper that brings it all together
export class TikTokWebClient extends WebcastHttpClient {

    // TikTok-based routes
    public readonly fetchRoomInfo: FetchRoomInfoRoute;
    public readonly fetchRoomInfoFromApiLive: FetchRoomInfoFromApiLiveRoute;
    public readonly fetchRoomInfoFromHtml: FetchRoomInfoFromHtmlRoute;

    // Custom signer routes
    public readonly fetchSignedWebSocketFromCustom: FetchSignedWebSocketFromCustomRoute;
    public readonly fetchRoomIdFromCustom: FetchRoomIdFromCustomRoute;
    public readonly fetchRoomInfoFromCustom: FetchRoomInfoFromCustomRoute;

    // Euler-based routes
    public readonly fetchSignedWebSocketFromEuler: FetchSignedWebSocketFromEulerRoute;
    public readonly fetchRoomIdFromEuler: FetchRoomIdFromEulerRoute;
    public readonly fetchRoomInfoFromEuler: FetchRoomInfoFromEulerRoute;
    public readonly sendRoomChatFromEuler: SendRoomChatFromEulerRoute;
    public readonly sendRoomChatFromCustom: SendRoomChatFromCustomRoute;

    constructor(...params: ConstructorParameters<typeof WebcastHttpClient>) {
        super(...params);

        this.fetchRoomInfo = new FetchRoomInfoRoute(this);
        this.fetchRoomInfoFromHtml = new FetchRoomInfoFromHtmlRoute(this);
        this.fetchRoomInfoFromApiLive = new FetchRoomInfoFromApiLiveRoute(this);

        this.fetchSignedWebSocketFromCustom = new FetchSignedWebSocketFromCustomRoute(this);
        this.fetchRoomIdFromCustom = new FetchRoomIdFromCustomRoute(this);
        this.fetchRoomInfoFromCustom = new FetchRoomInfoFromCustomRoute(this);

        this.fetchSignedWebSocketFromEuler = new FetchSignedWebSocketFromEulerRoute(this);
        this.fetchRoomIdFromEuler = new FetchRoomIdFromEulerRoute(this);
        this.fetchRoomInfoFromEuler = new FetchRoomInfoFromEulerRoute(this);
        this.sendRoomChatFromEuler = new SendRoomChatFromEulerRoute(this);
        this.sendRoomChatFromCustom = new SendRoomChatFromCustomRoute(this);
    }

}


