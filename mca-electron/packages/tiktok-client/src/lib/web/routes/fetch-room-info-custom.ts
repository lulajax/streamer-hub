import { Route } from '../../../types/route';
import { WebcastRoomInfoRouteResponse } from '@eulerstream/euler-api-sdk';
import { AxiosRequestConfig } from 'axios';

export type FetchRoomInfoFromCustomRouteParams = { uniqueId: string, options?: AxiosRequestConfig };

export class FetchRoomInfoFromCustomRoute extends Route<FetchRoomInfoFromCustomRouteParams, WebcastRoomInfoRouteResponse> {

    async call({ uniqueId, options }): Promise<WebcastRoomInfoRouteResponse> {
        return this.webClient.withSigner('custom', async () => {
            if (!this.webClient.isCustomSignerEnabled) {
                throw new Error('Custom signer is not configured.');
            }
            const fetchResponse = await this.webClient.customSigner.webcast.retrieveRoomInfo(uniqueId, options);
            return fetchResponse.data;
        });
    }

}
