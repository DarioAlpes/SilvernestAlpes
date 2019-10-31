import { Component, OnInit } from '@angular/core';
import { PersonConsumption } from './person-consumption';
import { PersonConsumptionService } from './person-consumption.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'person-consumption-list',
  templateUrl: 'app/templates/packages/reservations/persons-reservations/person-consumptions/person-consumption-list.html'
})
export class PersonConsumptionListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute,
                private personConsumptionService: PersonConsumptionService)
    { }

    selectedPersonConsumption: PersonConsumption;

    personConsumptions: PersonConsumption[];

    showPersonConsumption = false;

    error: any;

    idClient: number;

    idPackage: number;

    idReservation: number;

    idPersonReservation: number;

    subscriptions: any;

    onSelect(personConsumption: PersonConsumption)
    {
        let link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations', this.idReservation,
                    '/persons-reservations', this.idPersonReservation, '/person-consumptions', personConsumption.id];
        this.router.navigate(link);
    }

    getPersonConsumptions()
    {
        this.personConsumptionService
        .getPersonConsumptions(this.idClient, this.idPackage, this.idReservation, this.idPersonReservation)
        .then(personConsumptions => this.personConsumptions = personConsumptions)
        .catch(error => this.error = error);
    }

    addPersonConsumption()
    {
        this.showPersonConsumption = true;
        this.selectedPersonConsumption = new PersonConsumption();
    }

    close(savedPersonConsumption: PersonConsumption)
    {
        this.showPersonConsumption = false;
        if (savedPersonConsumption)
        {
            this.getPersonConsumptions();
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
                        if((params['id_reservation'] !== undefined) && (params['id_person_reservation'] !== undefined))
                        {
                            this.idReservation = +params['id_reservation'];
                            this.idPersonReservation = +params['id_person_reservation'];
                            this.getPersonConsumptions();
                        }
                        else
                        {
                            this.router.navigate(['/clients', this.idClient, '/packages', this.idPackage, '/persons-reservations']);
                        }
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
