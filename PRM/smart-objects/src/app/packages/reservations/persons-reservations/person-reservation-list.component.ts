import { Component, OnInit } from '@angular/core';
import { PersonReservation } from './person-reservation';
import { PersonReservationService } from './person-reservation.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'person-reservation-list',
  templateUrl: 'app/templates/packages/reservations/persons-reservations/person-reservation-list.html'
})
export class PersonReservationListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute,
                private personReservationService: PersonReservationService)
    { }

    selectedPersonReservation: PersonReservation;

    personReservations: PersonReservation[];

    showPersonReservation = false;

    error: any;

    idClient: number;

    idPackage: number;

    subscriptions: any;

    onSelect(personReservation: PersonReservation)
    {
        let link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations',
                    personReservation['id-reservation'], '/persons-reservations', personReservation.id];
        this.router.navigate(link);
    }

    activate(personReservation: PersonReservation)
    {
        this.personReservationService
        .activatePersonReservations(this.idClient, this.idPackage, personReservation, true)
        .then(personReservation => this.getPersonReservations())
        .catch(error => this.error = error);
    }

    deactivate(personReservation: PersonReservation)
    {
        this.personReservationService
        .activatePersonReservations(this.idClient, this.idPackage, personReservation, false)
        .then(personReservation => this.getPersonReservations())
        .catch(error => this.error = error);
    }

    getPersonReservations()
    {
        this.personReservationService
        .getPersonReservations(this.idClient, this.idPackage)
        .then(personReservations => this.personReservations = personReservations)
        .catch(error => this.error = error);
    }

    addPersonReservation()
    {
        this.showPersonReservation = true;
        this.selectedPersonReservation = new PersonReservation();
    }

    close(savedPersonReservation: PersonReservation)
    {
        this.showPersonReservation = false;
        if (savedPersonReservation)
        {
            this.getPersonReservations();
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
                    if(params['id_package'] !== undefined)
                    {
                        this.idPackage = +params['id_package'];
                        this.getPersonReservations();
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

    goBack()
    {
        window.history.back();
    }
}
