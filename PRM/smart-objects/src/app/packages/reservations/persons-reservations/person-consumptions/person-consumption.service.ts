import { Injectable } from '@angular/core';
import { PersonConsumption } from './person-consumption';
import { Http } from '@angular/http';
import { BaseService } from '../../../../utils/base-service.service';
import { PersonReservationService } from '../person-reservation.service';

@Injectable()
export class PersonConsumptionService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static personConsumptionUrlPosfix = 'person-consumptions/';

    static getPersonConsumptionsUrl(idClient: any, idPackage: any, idReservation: any, idPersonReservation: any)
    {
        var urlPersonReservation = PersonReservationService.getPersonReservationUrl(idClient, idPackage, idReservation,
                                                                                    idPersonReservation);
        return `${urlPersonReservation}${PersonConsumptionService.personConsumptionUrlPosfix}`;
    }

    static getPersonConsumptionUrl(idClient: any, idPackage: any, idReservation: any, idPersonReservation: any, id: any)
    {
        var urlPersonConsumption = PersonConsumptionService.getPersonConsumptionsUrl(idClient, idPackage, idReservation,
                                                                                     idPersonReservation);
        return BaseService.getItemUrl(urlPersonConsumption, id);
    }

    getPersonConsumptions(idClient: any, idPackage: any, idReservation: any, idPersonReservation: any): Promise<PersonConsumption[]>
    {
        return this.getCollection(PersonConsumptionService.getPersonConsumptionsUrl(idClient, idPackage, idReservation,
                                                                                    idPersonReservation));
    }

    getPersonConsumption(idClient: any, idPackage: any, idReservation: any, idPersonReservation: any, id: number): Promise<PersonConsumption>
    {
        return this.getItem(PersonConsumptionService.getPersonConsumptionsUrl(idClient, idPackage, idReservation,
                                                                              idPersonReservation), id);
    }

    savePersonConsumption(idClient: any, idPackage: any, idReservation: any, idPersonReservation: any, personConsumption: PersonConsumption): Promise<PersonConsumption>
    {
        return this.saveItem(PersonConsumptionService.getPersonConsumptionsUrl(idClient, idPackage, idReservation,
                                                                               idPersonReservation), personConsumption);

    }
}

export const PERSON_CONSUMPTIONS_SERVICES : any[] = [PersonConsumptionService];