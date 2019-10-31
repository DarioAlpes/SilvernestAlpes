import { Component, OnInit } from '@angular/core';
import { Consumption } from '../consumptions/consumption';
import { ConsumptionService } from '../consumptions/consumption.service';
import { Access } from '../accesses/access';
import { AccessService } from '../accesses/access.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'package-navigation',
  templateUrl: 'app/templates/packages/navigation/navigation.html'
})
export class ConsumptionListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private consumptionService: ConsumptionService, private accessService: AccessService)
    { }


    consumptions: Consumption[];

    accesses: Access[];

    navbar: Array<any>;

    error: any;

    idClient: number;

    idPackage: number;

    subscriptions: any;


    getData()
    {
        this.consumptionService
        .getConsumptions(this.idClient, this.idPackage)
        .then(consumptions => {this.consumptions = consumptions;
        var consumptionCount = this.consumptions.length;
        this.navbar[0].total = consumptionCount;
        this.navbar[0].link = consumptionCount;})
        .catch(error => this.error = error);
        this.accessService
        .getAccesses(this.idClient, this.idPackage)
        .then(accesses => {this.accesses = accesses;
        var accessCount = this.accesses.length;
        this.navbar[1].total = accessCount;
        this.navbar[1].link = accessCount;})
        .catch(error => this.error = error);
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
                        this.getData();
                    }
                    else
                    {

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
