import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Person } from './person';
import { Event } from './events/event';
import { PersonService } from './person.service';
import { EventService} from './events/event.service';
@Component({
  selector: 'person-timeline',
  templateUrl: 'app/templates/persons/person-timeline.html'
})
export class PersonTimelineComponent implements OnInit, OnDestroy
{
    person: Person;

    events: Event[];

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;

    documentTypes: any[] = [{ value: "CC", text: "Cédula de ciudadania" },
                     { value: "TI", text: "Tarjeta de identidad" },
                     { value: "CE", text: "Cédula de extranjería" }];

    genders: any[] = [{ value: "male", text: "Masculino" },
               { value: "female", text: "Femenino" }];

    constructor(private personService: PersonService, private eventService: EventService,
                private route: ActivatedRoute, private router: Router)
    {
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client']!==undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id'] !== undefined)
                    {
                        this.navigated = true;
                        let id = +params['id'];
                        this.personService.getPerson(this.idClient, id)
                            .then(person => {this.person = person;
                            this.person['image-source'] = 'https://smartobjectssas.appspot.com/clients/' + this.idClient + '/persons/' + id + '/image';
                            let stri =  this.person['birthdate'];
                            var ty = +stri.substr(0,4),
                            tm = +stri.substr(4,2) - 1,
                            td = +stri.substr(6,2),
                            th = +stri.substr(8,2),
                            tmi = +stri.substr(10,2),
                            ts = +stri.substr(12,2);
                            this.person['fbirthdate'] = new Date(ty,tm,td,th,tmi,ts,0);
                          })
                            .catch(error => this.error = error);
                        this.eventService.getEvents(this.idClient, id)
                            .then(events => {this.events = events;
                              for(var i=0; i < this.events.length; i++){
                                let str =  this.events[i]['initial-time'];
                                var y = +str.substr(0,4),
                                m = +str.substr(4,2) - 1,
                                d = +str.substr(6,2),
                                h = +str.substr(8,2),
                                mi = +str.substr(10,2),
                                s = +str.substr(12,2);
                                this.events[i]['timestamp'] = new Date(y,m,d,h,mi,s,0);
                              }
                            console.log(this.events);})
                            .catch(error => this.error = error);
                    }
                    else
                    {
                        this.navigated = false;
                        this.person = new Person();
                    }
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    goBack(savedPerson: Person = null)
    {
        this.close.emit(savedPerson);
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
