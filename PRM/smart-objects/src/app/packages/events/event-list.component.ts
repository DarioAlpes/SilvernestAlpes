import { Component, OnInit } from '@angular/core';
import { Event } from './event';
import { EventService } from './event.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'event-list',
  templateUrl: 'app/templates/packages/events/event-list.html'
})
export class EventListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private eventService: EventService)
    { }

    selectedEvent: Event;

    events: Event[];

    showEvent = false;

    error: any;

    idClient: number;

    subscriptions: any;

    onSelect(event: Event)
    {
        let link = ['/clients', this.idClient, '/events', event.id];
        this.router.navigate(link);
    }

    getEvents()
    {
        this.eventService
        .getEvents(this.idClient)
        .then(events => this.events = events)
        .catch(error => this.error = error);
    }

    addEvent()
    {
        this.showEvent = true;
        this.selectedEvent = new Event();
    }

    close(savedEvent: Event)
    {
        this.showEvent = false;
        if (savedEvent)
        {
            this.getEvents();
        }
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    this.getEvents();
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    goBack()
    {
        window.history.back();
    }
}
