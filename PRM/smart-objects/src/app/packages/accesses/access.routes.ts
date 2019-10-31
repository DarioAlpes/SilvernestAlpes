import { Route }  from '@angular/router';
import { AccessListComponent } from './access-list.component';
import { AccessDetailsComponent } from './access-details.component';

export const ACCESS_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages/:id_package/accesses',
    component: AccessListComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/accesses/:id',
    component: AccessDetailsComponent
  }
];

export const ACCESS_ROUTES_COMPONENTS: any[] = [AccessListComponent, AccessDetailsComponent]