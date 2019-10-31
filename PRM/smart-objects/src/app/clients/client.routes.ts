import { Route }  from '@angular/router';
import { ClientListComponent } from './client-list.component';
import { ClientDetailsComponent } from './client-details.component';

export const CLIENT_ROUTES: Route[] = [
  {
    path: 'clients',
    component: ClientListComponent
  },
  {
    path: 'clients/:id',
    component: ClientDetailsComponent
  }
];

export const CLIENT_ROUTES_COMPONENTS: any[] = [ClientListComponent, ClientDetailsComponent]