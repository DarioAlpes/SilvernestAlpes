import { Injectable } from '@angular/core';
import { Location } from './location';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import { ClientService } from '../clients/client.service';

@Injectable()
export class LocationService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static locationsUrlPosfix = 'locations/';

    static getLocationsUrl(idClient: any)
    {
        var urlClient = ClientService.getClientUrl(idClient);
        return `${urlClient}${LocationService.locationsUrlPosfix}`;
    }

    static getLocationUrl(idClient: any, id: any)
    {
        return BaseService.getItemUrl(LocationService.getLocationsUrl(idClient), id);
    }

    getLocations(idClient: any): Promise<Location[]>
    {
        return this.getCollection(LocationService.getLocationsUrl(idClient));
    }

    getChildrenLocations(idClient: any, idLocation: any): Promise<Location[]>
    {
      return this.getCollection(LocationService.getLocationUrl(idClient, idLocation) + 'children/');
    }

    getLocation(idClient: any, id: number): Promise<Location>
    {
        return this.getItem(LocationService.getLocationsUrl(idClient), id);
    }

    saveLocation(idClient: any, location: Location): Promise<Location>
    {
        return this.saveItem(LocationService.getLocationsUrl(idClient), location);
    }
}

export const LOCATIONS_SERVICES : any[] = [LocationService];
