import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Access } from '../../../accesses/access';
import { AccessService } from '../../../accesses/access.service';
import { Consumption } from '../../../consumptions/consumption';
import { ConsumptionService } from '../../../consumptions/consumption.service';
import { Sku } from '../../../../skus/sku';
import { SkuService } from '../../../../skus/sku.service';
import { PersonConsumption } from './person-consumption';
import { PersonConsumptionService } from './person-consumption.service';
@Component({
  selector: 'person-consumption-detail',
  templateUrl: 'app/templates/packages/reservations/persons-reservations/person-consumptions/person-consumption-details.html'
})
export class PersonConsumptionDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    personConsumption: PersonConsumption;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    idPackage: number;
    idReservation: number;
    idPersonReservation: number;
    skus: Sku[];
    accesses: Access[];
    consumptions: Consumption[];
    constructor(private personConsumptionService: PersonConsumptionService, private skuService: SkuService,
                private accessService: AccessService, private consumptionService: ConsumptionService,
                private route: ActivatedRoute, private router: Router)
    {
        this.personConsumption = new PersonConsumption();
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
                            if(params['id'] !== undefined)
                            {
                                this.navigated = true;
                                let id = +params['id'];
                                this.personConsumptionService.getPersonConsumption(this.idClient, this.idPackage,
                                                                                   this.idReservation,
                                                                                   this.idPersonReservation, id)
                                    .then(personConsumption => this.personConsumption = personConsumption)
                                    .catch(error => this.error = error);
                            }
                            else
                            {
                                this.navigated = false;
                                this.personConsumption = new PersonConsumption();
                            }
                            this.accessService.getAccesses(this.idClient, this.idPackage)
                                .then(accesses => this.accesses = accesses)
                                .catch(error => this.error = error);
                            this.consumptionService.getConsumptions(this.idClient, this.idPackage)
                                .then(consumptions => this.consumptions = consumptions)
                                .catch(error => this.error = error);
                            this.skuService.getSkus(this.idClient)
                                .then(skus => this.skus = skus)
                                .catch(error => this.error = error);
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

    savePersonConsumption()
    {
        this.personConsumptionService.savePersonConsumption(this.idClient, this.idPackage,this.idReservation,
                                                            this.idPersonReservation, this.personConsumption)
            .then
                (
                    personConsumption =>
                    {
                        this.personConsumption = personConsumption;
                        this.goBack(personConsumption);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedPersonConsumption: PersonConsumption = null)
    {
        this.close.emit(savedPersonConsumption);
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
