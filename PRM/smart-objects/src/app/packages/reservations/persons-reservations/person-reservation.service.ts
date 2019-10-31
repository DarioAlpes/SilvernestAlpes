import { Injectable } from '@angular/core';
import { PersonReservation } from './person-reservation';
import { Http } from '@angular/http';
import { BaseService } from '../../../utils/base-service.service';
import { PackageService } from '../../package.service';
import { ReservationService } from '../reservation.service';
import { PERSON_CONSUMPTIONS_SERVICES } from './person-consumptions/person-consumption.service';

@Injectable()
export class PersonReservationService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static personReservationsUrlPosfix = 'persons-reservations/';

    static getPersonReservationsUrl(idClient: any, idPackage: any)
    {
        var urlPackage = PackageService.getPackageUrl(idClient, idPackage);
        return `${urlPackage}${PersonReservationService.personReservationsUrlPosfix}`;
    }

    static getPersonReservationsBaseUrl(idClient: any, idPackage: any, idReservation: any)
    {
       var urlReservation = ReservationService.getReservationUrl(idClient, idPackage, idReservation);
        return `${urlReservation}${PersonReservationService.personReservationsUrlPosfix}`;
    }

    static getPersonReservationUrl(idClient: any, idPackage: any, idReservation: any, id: any)
    {
        var urlReservation = PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation);
        return BaseService.getItemUrl(urlReservation, id);
    }

    getPersonReservations(idClient: any, idPackage: any): Promise<PersonReservation[]>
    {
        return this.getCollection(PersonReservationService.getPersonReservationsUrl(idClient, idPackage));
    }

    getPersonReservation(idClient: any, idPackage: any, idReservation: any, id: number): Promise<PersonReservation>
    {
        return this.getItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), id);
    }

    savePersonReservation(idClient: any, idPackage: any, personReservation: PersonReservation): Promise<PersonReservation>
    {
        if(personReservation.id)
        {
            var idReservation = personReservation['id-reservation'];
            return this.updateItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), personReservation);
        }
        else
        {
            return this.createItem(PersonReservationService.getPersonReservationsUrl(idClient, idPackage), personReservation);
        }
    }

    activatePersonReservations(idClient: any, idPackage: any, personReservation: PersonReservation, activate: boolean): Promise<PersonReservation>
    {
        var idReservation = personReservation['id-reservation'];
        var activationBody = {active: activate}
        return this.patchItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), activationBody, personReservation.id);
    }
}

const PERSON_RESERVATIONS_SPECIFIC_SERVICES : any[] = [PersonReservationService];
export const PERSON_RESERVATIONS_SERVICES : any[] = PERSON_RESERVATIONS_SPECIFIC_SERVICES.concat(PERSON_CONSUMPTIONS_SERVICES);