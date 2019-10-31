import { Injectable } from '@angular/core';
import { Package } from './package';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import { ClientService } from '../clients/client.service';
import { ACCESSES_SERVICES } from './accesses/access.service';
import { CONSUMPTIONS_SERVICES } from './consumptions/consumption.service';
import { EVENTS_SERVICES } from './events/event.service';
import { RESERVATIONS_SERVICES } from './reservations/reservation.service';

@Injectable()
export class PackageService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static packagesUrlPosfix = 'packages/';

    static getPackagesUrl(idClient: any)
    {
        var urlClient = ClientService.getClientUrl(idClient);
        return `${urlClient}${PackageService.packagesUrlPosfix}`;
    }

    static getPackageUrl(idClient: any, id: any)
    {
        return BaseService.getItemUrl(PackageService.getPackagesUrl(idClient), id);
    }

    getPackages(idClient: any): Promise<Package[]>
    {
        return this.getCollection(PackageService.getPackagesUrl(idClient));
    }

    getPackage(idClient: any, id: number)
    {
        return this.getItem(PackageService.getPackagesUrl(idClient), id);
    }

    savePackage(idClient: any, pack: Package): Promise<Package>
    {
        return this.saveItem(PackageService.getPackagesUrl(idClient), pack);
    }
}

const PACKAGE_SPECIFIC_SERVICE : any[] = [PackageService];

export const PACKAGES_SERVICES : any[] = PACKAGE_SPECIFIC_SERVICE.concat(ACCESSES_SERVICES, EVENTS_SERVICES,
                                                                         CONSUMPTIONS_SERVICES, RESERVATIONS_SERVICES);