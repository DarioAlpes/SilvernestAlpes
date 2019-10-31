import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Package } from './package';
import { LocationService } from '../locations/location.service';
import { Location } from '../locations/location';
import { PackageService } from './package.service';
import { EventService } from './events/event.service';
import { Event } from './events/event';
import { Validators } from '@angular/forms';

@Component({
  selector: 'package-detail',
  templateUrl: 'app/templates/packages/package-details.html'
})
export class PackageDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    package: Package;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    locations: Location[];
    events: Event[];
    options:any;
    public boundCallBack: Function;
    public fields:Array<any>;

    constructor(private packageService: PackageService, private locationService: LocationService,
                private eventService: EventService, private route: ActivatedRoute, private router: Router)
    {
        this.package = new Package();
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client']!==undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id'] !== undefined && params['id'] !== 'new')
                    {
                        this.navigated = true;
                        let id = +params['id'];
                        this.idClient = +params['id_client'];
                        this.packageService.getPackage(this.idClient, id)
                            .then(pack => this.package = pack)
                            .catch(error => this.error = error);
                    }
                    else
                    {
                        this.navigated = false;
                        this.package = new Package();
                    }
                    if(params['id'] == 'new' )
                    {
                      this.navigated = true;
                      this.package = new Package();
                    }
                    else
                    {
                      this.navigated = false;
                    }
                    this.locationService.getLocations(this.idClient)
                        .then(locations => this.locations = locations)
                    .then(() =>
                          {
                            this.options = [];
                            for(var item of this.locations)
                            {
                                this.options.push({label: item.name, value: item.id});
                            }
                            this.fields = [
                                {id: 'id',label: 'Id', type:'noedit'},
                                {id: 'historic-id', label: 'ID Historico', fieldtype:'number', type:'input', validations: [Validators.required] },
                                {id: 'name', label: 'Nombre', type:'input', validations: [Validators.required] },
                                {id: 'description', label: 'Descripción', type:'input', validations: [Validators.required] },
                                {id: 'price', label: 'Precio', type:'mask', fieldtype:'number', alias:'currency', validations: [Validators.required] },
                                {id: 'id-location', label: 'Ubicación', options: this.options, type:'dropdown'},
                                {id: '"restricted-consumption"', label: 'Consumo restringido', type:'checkbox', validations: [Validators.required] },
                                {id: 'valid-from', label: 'Válido Desde', type:'date', validations: [Validators.required] },
                                {id: 'valid-through', label: 'Válido Hasta', type:'date', validations: [Validators.required] },
                                {id: 'duration', label: 'Duración', fieldtype:'number', type:'input', validations: [Validators.required] }
                            ];
                            })
                        .catch(error => this.error = error);
                    this.eventService.getEvents(this.idClient)
                        .then(events => this.events = events)
                        .catch(error => this.error = error);
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
        this.boundCallBack = this.savePackage.bind(this);
    }

    savePackage()
    {
        this.packageService.savePackage(this.idClient, this.package)
            .then
                (
                    pack =>
                    {
                        this.package = pack;
                        this.goBack(pack);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedPackage: Package = null)
    {
        this.close.emit(savedPackage);
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
