import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Access } from './access';
import { AccessService } from './access.service';
import { LocationService } from '../../locations/location.service';
import { Location } from '../../locations/location';
@Component({
  selector: 'access-detail',
  templateUrl: 'app/templates/packages/accesses/access-details.html'
})
export class AccessDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    access: Access;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    idPackage: number;
    locations: Location[];

    constructor(private accessService: AccessService, private locationService: LocationService,
                private route: ActivatedRoute, private router: Router)
    {
        this.access = new Access();
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
                            this.accessService.getAccess(this.idClient, this.idPackage, id)
                                .then(access => this.access = access)
                                .catch(error => this.error = error);
                        }
                        else
                        {
                            this.navigated = false;
                            this.access = new Access();
                        }
                        this.locationService.getLocations(this.idClient)
                            .then(locations => this.locations = locations)
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

    saveAccess()
    {
        this.accessService.saveAccess(this.idClient, this.idPackage, this.access)
            .then
                (
                    access =>
                    {
                        this.access = access;
                        this.goBack(access);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedAccess: Access = null)
    {
        this.close.emit(savedAccess);
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
