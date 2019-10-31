import { Injectable } from '@angular/core';
import { Person } from './person';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import { ClientService } from '../clients/client.service';
import {EVENTS_SERVICES} from "./events/event.service";
@Injectable()
export class PersonService extends BaseService
{
  constructor(http: Http)
  {
    super(http);
  }
  private static personsUrlPosfix = 'persons/';

  static getPersonsUrl(idClient: any)
  {
    var urlClient = ClientService.getClientUrl(idClient);
    return `${urlClient}${PersonService.personsUrlPosfix}`;
  }

  static getPersonUrl(idClient: any, id: any)
  {
    return BaseService.getItemUrl(PersonService.getPersonsUrl(idClient), id);
  }

  getPersons(idClient: any): Promise<Person[]>
  {
    return this.getCollection(PersonService.getPersonsUrl(idClient));
  }

  getPerson(idClient: any, id: number): Promise<Person>
  {
    return this.getItem(PersonService.getPersonsUrl(idClient), id);
  }

  savePerson(idClient: any, person: Person): Promise<Person>
  {
    return this.saveItem(PersonService.getPersonsUrl(idClient), person);
  }

  deletePerson(idClient: any, person: Person): Promise<Person>
  {
    return this.deleteItem(PersonService.getPersonsUrl(idClient), person);
  }

  getImagetUrl(idClient: number, person: Person)
  {
    return this.http.get(BaseService.getItemUrl(PersonService.getPersonsUrl(idClient), person.id)+'image-url/')
      .toPromise()
      .then(response => JSON.parse(response.json()))
      .catch(this.handleApiError);
  }
}


export const PERSON_SERVICES : any[] = [PersonService, ...EVENTS_SERVICES];
