import { Route }  from '@angular/router';
import { PersonConsumptionListComponent } from './person-consumption-list.component';
import { PersonConsumptionDetailsComponent } from './person-consumption-details.component';

export const PERSON_CONSUMPTION_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id_person_reservation/person-consumptions',
    component: PersonConsumptionListComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id_person_reservation/person-consumptions/:id',
    component: PersonConsumptionDetailsComponent
  }
];

export const PERSON_CONSUMPTION_ROUTES_COMPONENTS: any[] = [PersonConsumptionListComponent, PersonConsumptionDetailsComponent]