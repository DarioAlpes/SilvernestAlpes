import { Injectable } from '@angular/core';
import { Event } from './event';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { PersonService } from '../person.service';
@Injectable()
export class EventService extends BaseService
{
  constructor(http: Http)
  {
    super(http);
  }
  private static eventUrlPosfix = 'events/';

  static getEventsUrl(idClient: any, idPerson: any)
  {
    var urlPerson = PersonService.getPersonUrl(idClient, idPerson);
    return `${urlPerson}${EventService.eventUrlPosfix}`;
  }

  static getEventUrl(idClient: any, idPerson: any, id: any)
  {
    return BaseService.getItemUrl(EventService.getEventsUrl(idClient, idPerson), id);
  }

  getEvents(idClient: any, idPerson: any): Promise<Event[]>
  {
    return this.getCollection(EventService.getEventsUrl(idClient, idPerson));
  }

  getEvent(idClient: any, idPerson: any, id: number): Promise<Event>
  {
    return this.getItem(EventService.getEventsUrl(idClient, idPerson), id);
  }

  saveEvent(idClient: any, idPerson: any, event: Event): Promise<Event>
  {
    return this.saveItem(EventService.getEventsUrl(idClient, idPerson), event);
  }

  deleteEvent(idClient: any, idPerson: any, event: Event): Promise<Event>
  {
    return this.deleteItem(EventService.getEventsUrl(idClient, idPerson), event);
  }
}


export const EVENTS_SERVICES : any[] = [EventService];
