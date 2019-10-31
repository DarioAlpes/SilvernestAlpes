import { Injectable } from '@angular/core';
import { SkuCategory } from './sku-category';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { ClientService } from '../../clients/client.service';

@Injectable()
export class SkuCategoryService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static categoriesUrlPosfix = 'sku-categories/';

    static getSkuCategoriesUrl(idClient: any)
    {
        var urlClient = ClientService.getClientUrl(idClient);
        return `${urlClient}${SkuCategoryService.categoriesUrlPosfix}`;
    }

    static getSkuCategoryUrl(idClient: any, id: any)
    {
        return BaseService.getItemUrl(SkuCategoryService.getSkuCategoriesUrl(idClient), id);
    }

    getSkuCategories(idClient: any): Promise<SkuCategory[]>
    {
        return this.getCollection(SkuCategoryService.getSkuCategoriesUrl(idClient));
    }

    getSkuCategory(idClient: any, id: number): Promise<SkuCategory>
    {
        return this.getItem(SkuCategoryService.getSkuCategoriesUrl(idClient), id);
    }

    saveSkuCategory(idClient: any, category: SkuCategory): Promise<SkuCategory>
    {
        return this.saveItem(SkuCategoryService.getSkuCategoriesUrl(idClient), category);
    }
}

export const SKU_CATEGORIES_SERVICES : any[] = [SkuCategoryService];