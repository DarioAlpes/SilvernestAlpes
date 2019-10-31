import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Reservation } from '../reservation';
import { ReservationService } from '../reservation.service';
import { Person } from '../../../persons/person';
import { PersonService } from '../../../persons/person.service';
import { PersonReservation } from './person-reservation';
import { PersonReservationService } from './person-reservation.service';
@Component({
  selector: 'person-reservation-detail',
  templateUrl: 'app/templates/packages/reservations/persons-reservations/person-reservation-details.html'
})
export class PersonReservationDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    personReservation: PersonReservation;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    idPackage: number;
    idReservation: number;
    persons: Person[];
    reservations: Reservation[];
    constructor(private personReservationService: PersonReservationService, private reservationService: ReservationService,
                private personService: PersonService, private route: ActivatedRoute, private router: Router)
    {
        this.personReservation = new PersonReservation();
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id_package'] !== undefined)
                    {
                        this.idPackage = +params['id_package'];
                        if((params['id_reservation'] !== undefined) && (params['id'] !== undefined))
                        {
                            this.navigated = true;
                            this.idReservation = +params['id_reservation'];
                            let id = +params['id'];
                            this.personReservationService.getPersonReservation(this.idClient, this.idPackage,
                                                                               this.idReservation, id)
                                .then(personReservation => this.personReservation = personReservation)
                                .catch(error => this.error = error);
                        }
                        else
                        {
                            this.navigated = false;
                            this.personReservation = new PersonReservation();
                        }
                        this.reservationService.getReservations(this.idClient, this.idPackage)
                            .then(reservations => this.reservations = reservations)
                            .catch(error => this.error = error);
                        this.personService.getPersons(this.idClient)
                            .then(persons => this.persons = persons.filter(person => !person['is-phantom']))
                            .catch(error => this.error = error);
                    }
                    else
                    {
                        this.router.navigate(['/clients', this.idClient, '/packages']);
                    }
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    savePersonReservation()
    {
        this.personReservationService.savePersonReservation(this.idClient, this.idPackage, this.personReservation)
            .then
                (
                    personReservation =>
                    {
                        this.personReservation = personReservation;
                        this.goBack(personReservation);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedPersonReservation: PersonReservation = null)
    {
        this.close.emit(savedPersonReservation);
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
