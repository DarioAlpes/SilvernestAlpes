import { Component, OnInit } from '@angular/core';
import { Consumption } from './consumption';
import { ConsumptionService } from './consumption.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'consumptions-list',
  templateUrl: 'app/templates/packages/consumptions/consumption-list.html'
})
export class ConsumptionListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private consumptionService: ConsumptionService)
    { }

    selectedConsumption: Consumption;

    consumptions: Consumption[];

    showConsumption = false;

    error: any;

    idClient: number;

    idPackage: number;

    subscriptions: any;

    onSelect(consumption: Consumption)
    {
        let link = ['/clients', this.idClient, '/packages', this.idPackage, '/consumptions', consumption.id];
        this.router.navigate(link);
    }

    getConsumptions()
    {
        this.consumptionService
        .getConsumptions(this.idClient, this.idPackage)
        .then(consumptions => this.consumptions = consumptions)
        .catch(error => this.error = error);
    }

    addConsumption()
    {
        this.showConsumption = true;
        this.selectedConsumption = new Consumption();
    }

    close(savedConsumption: Consumption)
    {
        this.showConsumption = false;
        if (savedConsumption)
        {
            this.getConsumptions();
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
                        this.getConsumptions();
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
