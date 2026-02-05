import { Route } from '../../../types/route';
import { WebcastRoomIdRouteResponse } from '@eulerstream/euler-api-sdk';
import { AxiosRequestConfig } from 'axios';

export type FetchRoomIdFromEulerRouteParams = { uniqueId: string, options?: AxiosRequestConfig };

export class FetchRoomIdFromEulerRoute extends Route<FetchRoomIdFromEulerRouteParams, WebcastRoomIdRouteResponse> {

    async call({ uniqueId, options }): Promise<WebcastRoomIdRouteResponse> {
        return this.webClient.withSigner('euler', async () => {
            const fetchResponse = await this.webClient.webSigner.webcast.retrieveRoomId(uniqueId, options);
            return fetchResponse.data;
        });
    }

}
