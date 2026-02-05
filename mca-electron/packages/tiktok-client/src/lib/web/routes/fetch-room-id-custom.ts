import { Route } from '../../../types/route';
import { WebcastRoomIdRouteResponse } from '@eulerstream/euler-api-sdk';
import { AxiosRequestConfig } from 'axios';

export type FetchRoomIdFromCustomRouteParams = { uniqueId: string, options?: AxiosRequestConfig };

export class FetchRoomIdFromCustomRoute extends Route<FetchRoomIdFromCustomRouteParams, WebcastRoomIdRouteResponse> {

    async call({ uniqueId, options }): Promise<WebcastRoomIdRouteResponse> {
        return this.webClient.withSigner('custom', async () => {
            if (!this.webClient.isCustomSignerEnabled) {
                throw new Error('Custom signer is not configured.');
            }
            const fetchResponse = await this.webClient.customSigner.webcast.retrieveRoomId(uniqueId, options);
            return fetchResponse.data;
        });
    }

}
