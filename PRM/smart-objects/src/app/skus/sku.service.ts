import { Injectable } from '@angular/core';
import { Sku } from './sku';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import { ClientService } from '../clients/client.service';
import { SKU_CATEGORIES_SERVICES } from './categories/sku-category.service';
@Injectable()
export class SkuService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static skusUrlPosfix = 'skus/';

    static getSkusUrl(idClient: any)
    {
        var urlClient = ClientService.getClientUrl(idClient);
        return `${urlClient}${SkuService.skusUrlPosfix}`;
    }

    static getSkuUrl(idClient: any, id: any)
    {
        return BaseService.getItemUrl(SkuService.getSkusUrl(idClient), id);
    }

    getSkus(idClient: any): Promise<Sku[]>
    {
        return this.getCollection(SkuService.getSkusUrl(idClient));
    }

    getSku(idClient: any, id: number): Promise<Sku>
    {
        return this.getItem(SkuService.getSkusUrl(idClient), id);
    }

    saveSku(idClient: any, sku: Sku): Promise<Sku>
    {
        return this.saveItem(SkuService.getSkusUrl(idClient), sku);
    }
}

const SKU_SPECIFIC_SERVICE : any[] = [SkuService];

export const SKU_SERVICES : any[] = SKU_SPECIFIC_SERVICE.concat(SKU_CATEGORIES_SERVICES);