import { Injectable } from '@angular/core';
import { Consumption } from './consumption';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { PackageService } from '../package.service';

@Injectable()
export class ConsumptionService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static consumptionsUrlPosfix = 'consumptions/';

    static getConsumptionsUrl(idClient: any, idPackage: any)
    {
        var urlPackage = PackageService.getPackageUrl(idClient, idPackage);
        return `${urlPackage}${ConsumptionService.consumptionsUrlPosfix}`;
    }

    static getConsumptionUrl(idClient: any, idPackage: any, id: any)
    {
        return BaseService.getItemUrl(ConsumptionService.getConsumptionsUrl(idClient, idPackage), id);
    }

    getConsumptions(idClient: any, idPackage: any): Promise<Consumption[]>
    {
        return this.getCollection(ConsumptionService.getConsumptionsUrl(idClient, idPackage));
    }

    getConsumption(idClient: any, idPackage: any, id: number): Promise<Consumption>
    {
        return this.getItem(ConsumptionService.getConsumptionsUrl(idClient, idPackage), id);
    }

    saveConsumption(idClient: any, idPackage: any, consumption: Consumption): Promise<Consumption>
    {
        return this.saveItem(ConsumptionService.getConsumptionsUrl(idClient, idPackage), consumption);
    }
}

export const CONSUMPTIONS_SERVICES : any[] = [ConsumptionService];