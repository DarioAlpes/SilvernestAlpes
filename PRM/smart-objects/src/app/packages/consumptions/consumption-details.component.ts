import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Consumption } from './consumption';
import { ConsumptionService } from './consumption.service';
import { LocationService } from '../../locations/location.service';
import { Location } from '../../locations/location';
import { SkuService } from '../../skus/sku.service';
import { Sku } from '../../skus/sku';
@Component({
  selector: 'consumption-detail',
  templateUrl: 'app/templates/packages/consumptions/consumption-details.html'
})
export class ConsumptionDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    consumption: Consumption;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    idPackage: number;
    locations: Location[];
    skus: Sku[];

    constructor(private consumptionService: ConsumptionService, private locationService: LocationService,
                private skuService: SkuService, private route: ActivatedRoute, private router: Router)
    {
        this.consumption = new Consumption();
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
                        if(params['id'] !== undefined)
                        {
                            this.navigated = true;
                            let id = +params['id'];
                            this.consumptionService.getConsumption(this.idClient, this.idPackage, id)
                                .then(consumption => this.consumption = consumption)
                                .catch(error => this.error = error);
                        }
                        else
                        {
                            this.navigated = false;
                            this.consumption = new Consumption();
                        }
                        this.locationService.getLocations(this.idClient)
                            .then(locations => this.locations = locations)
                            .catch(error => this.error = error);
                        this.skuService.getSkus(this.idClient)
                            .then(skus => this.skus = skus)
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

    setSelectedSkus(selectedSkus)
    {
        var selectedSkuIds = Array.prototype.filter.call(selectedSkus, skuItem => skuItem.selected === true)
                                                   .map(skuItem => +skuItem.value );
        this.consumption['id-skus'] = selectedSkuIds;
    }

    setSelectedLocations(selectedLocations)
    {
        var selectedLocationIds =  Array.prototype.filter.call(selectedLocations, locationItem => locationItem.selected === true)
                                                           .map(locationItem => +locationItem.value );
        this.consumption['id-locations'] = selectedLocationIds;
    }

    saveConsumption()
    {
        this.consumptionService.saveConsumption(this.idClient, this.idPackage, this.consumption)
            .then
                (
                    consumption =>
                    {
                        this.consumption = consumption;
                        this.goBack(consumption);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedConsumption: Consumption = null)
    {
        this.close.emit(savedConsumption);
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
