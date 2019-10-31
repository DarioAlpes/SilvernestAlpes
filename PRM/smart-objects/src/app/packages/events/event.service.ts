import { Injectable } from '@angular/core';
import { Event } from './event';
import { Http } from '@angular/http';
import { BaseService } from '../../utils/base-service.service';
import { ClientService } from '../../clients/client.service';

@Injectable()
export class EventService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }
    private static eventsUrlPosfix = 'social-events/';

    static getEventsUrl(idClient: any)
    {
        var urlClient = ClientService.getClientUrl(idClient);
        return `${urlClient}${EventService.eventsUrlPosfix}`;
    }

    static getEventUrl(idClient: any, id: any)
    {
        return BaseService.getItemUrl(EventService.getEventsUrl(idClient), id);
    }

    getEvents(idClient: any): Promise<Event[]>
    {
        return this.getCollection(EventService.getEventsUrl(idClient));
    }

    getEvent(idClient: any, id: number): Promise<Event>
    {
        return this.getItem(EventService.getEventsUrl(idClient), id);
    }

    saveEvent(idClient: any, event: Event): Promise<Event>
    {
        return this.saveItem(EventService.getEventsUrl(idClient), event);
    }
}

export const EVENTS_SERVICES : any[] = [EventService];