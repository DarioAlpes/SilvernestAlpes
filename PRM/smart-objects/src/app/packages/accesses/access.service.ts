import { Injectable } from '@angular/core';
import { Access } from './access';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { PackageService } from '../package.service';

@Injectable()
export class AccessService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static accessesUrlPosfix = 'accesses/';

    static getAccessesUrl(idClient: any, idPackage: any)
    {
        var urlPackage = PackageService.getPackageUrl(idClient, idPackage);
        return `${urlPackage}${AccessService.accessesUrlPosfix}`;
    }

    static getAccessUrl(idClient: any, idPackage: any, id: any)
    {
        return BaseService.getItemUrl(AccessService.getAccessesUrl(idClient, idPackage), id);
    }

    getAccesses(idClient: any, idPackage: any): Promise<Access[]>
    {
        return this.getCollection(AccessService.getAccessesUrl(idClient, idPackage));
    }

    getAccess(idClient: any, idPackage: any, id: number): Promise<Access>
    {
        return this.getItem(AccessService.getAccessesUrl(idClient, idPackage), id);
    }

    saveAccess(idClient: any, idPackage: any, access: Access): Promise<Access>
    {
        return this.saveItem(AccessService.getAccessesUrl(idClient, idPackage), access);
    }
}

export const ACCESSES_SERVICES : any[] = [AccessService];