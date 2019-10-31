import { Injectable } from '@angular/core';
import { Reservation } from './reservation';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { PackageService } from '../package.service';
import { PERSON_RESERVATIONS_SERVICES } from './persons-reservations/person-reservation.service';

@Injectable()
export class ReservationService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static reservationsUrlPosfix = 'reservations/';

    static getReservationsUrl(idClient: any, idPackage: any)
    {
        var urlPackage = PackageService.getPackageUrl(idClient, idPackage);
        return `${urlPackage}${ReservationService.reservationsUrlPosfix}`;
    }

    static getReservationUrl(idClient: any, idPackage: any, id: any)
    {
        return BaseService.getItemUrl(ReservationService.getReservationsUrl(idClient, idPackage), id);
    }

    getReservations(idClient: any, idPackage: any): Promise<Reservation[]>
    {
        return this.getCollection(ReservationService.getReservationsUrl(idClient, idPackage));
    }

    getReservation(idClient: any, idPackage: any, id: number): Promise<Reservation>
    {
        return this.getItem(ReservationService.getReservationsUrl(idClient, idPackage), id);
    }

    saveReservation(idClient: any, idPackage: any, reservation: Reservation): Promise<Reservation>
    {
        return this.saveItem(ReservationService.getReservationsUrl(idClient, idPackage), reservation);
    }
}
const RESERVATIONS_SPECIFIC_SERVICES : any[] = [ReservationService];

export const RESERVATIONS_SERVICES : any[] = RESERVATIONS_SPECIFIC_SERVICES.concat(PERSON_RESERVATIONS_SERVICES);