import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Event } from './event';
import { EventService } from './event.service';
@Component({
  selector: 'event-detail',
  templateUrl: 'app/templates/packages/events/event-details.html'
})

export class EventDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    event: Event;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;

    constructor(private eventService: EventService, private route: ActivatedRoute, private router: Router)
    {
        this.event = new Event();
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id'] !== undefined)
                    {
                        this.navigated = true;
                        let id = +params['id'];
                        this.eventService.getEvent(this.idClient, id)
                            .then(event => this.event = event)
                            .catch(error => this.error = error);
                    }
                    else
                    {
                        this.navigated = false;
                        this.event = new Event();
                    }
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    saveEvent()
    {
        this.eventService.saveEvent(this.idClient, this.event)
            .then
                (
                    event =>
                    {
                        this.event = event;
                        this.goBack(event);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedEvent: Event = null)
    {
        this.close.emit(savedEvent);
        if (this.navigated)
        {
            window.history.back();
        }
    }

    ngOnDestroy()
    {
        this.subscriptions.unsubscribe();
    }
}
