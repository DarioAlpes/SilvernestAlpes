import { Component } from '@angular/core';
import { PACKAGES_SERVICES } from './packages/package.service';
import { CLIENTS_SERVICES } from './clients/client.service';
import { LOCATIONS_SERVICES } from './locations/location.service';
import { SKU_SERVICES } from './skus/sku.service';
import { PERSON_SERVICES } from './persons/person.service';
import {MdIconRegistry} from '@angular2-material/icon';
import { MdUniqueSelectionDispatcher } from '@angular2-material/core';
import {Router, ActivatedRoute} from "@angular/router";
declare var moment: any;


@Component({
  selector: 'cjm-app',
  templateUrl: 'app/templates/app.html',
  providers: CLIENTS_SERVICES.concat(PACKAGES_SERVICES, LOCATIONS_SERVICES, SKU_SERVICES,
  PERSON_SERVICES,MdIconRegistry, MdUniqueSelectionDispatcher)
})
export class AppComponent
{
  constructor(private router: Router, private route: ActivatedRoute)
    { }
    title: string = "Ejemplo CJM";
    navMenu: Array<any>;
    subscriptions: any;

    ngOnInit():void {

            moment.locale('es');
            this.navMenu = [
            {label:'Ubicaciones', link:'/locations', icon:'location_on'},
            {label:'Skus', link:'/skus', icon:'store'},
            {label:'Personas', link:'/persons', icon:'person_pin'}
            ];
            console.log(this.router.url);
    }


}
