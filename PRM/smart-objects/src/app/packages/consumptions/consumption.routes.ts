import { Route }  from '@angular/router';
import { ConsumptionListComponent } from './consumption-list.component';
import { ConsumptionDetailsComponent } from './consumption-details.component';

export const CONSUMPTION_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages/:id_package/consumptions',
    component: ConsumptionListComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/consumptions/:id',
    component: ConsumptionDetailsComponent
  }
];

export const CONSUMPTION_ROUTES_COMPONENTS: any[] = [ConsumptionListComponent, ConsumptionDetailsComponent]