import { Route }  from '@angular/router';
import { PackageListComponent } from './package-list.component';
import { PackageDetailsComponent } from './package-details.component';
import { AccessListComponent } from './accesses/access-list.component';
import { ACCESS_ROUTES, ACCESS_ROUTES_COMPONENTS } from './accesses/access.routes';
import { CONSUMPTION_ROUTES, CONSUMPTION_ROUTES_COMPONENTS } from './consumptions/consumption.routes';
import { EVENT_ROUTES, EVENT_ROUTES_COMPONENTS } from './events/event.routes';
import { RESERVATION_ROUTES, RESERVATION_ROUTES_COMPONENTS } from './reservations/reservation.routes';

const PACKAGE_SPECIFIC_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages',
    component: PackageListComponent
  },
  {
    path: 'clients/:id_client/packages/:id',
    component: PackageDetailsComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/access',
    component: AccessListComponent
  }
];

export const PACKAGE_ROUTES: Route[] = PACKAGE_SPECIFIC_ROUTES.concat(ACCESS_ROUTES, EVENT_ROUTES, CONSUMPTION_ROUTES,
                                                                      RESERVATION_ROUTES);

export const PACKAGE_ROUTES_COMPONENTS: any[] = [PackageListComponent, PackageDetailsComponent]
                                                .concat(ACCESS_ROUTES_COMPONENTS, EVENT_ROUTES_COMPONENTS,
                                                 CONSUMPTION_ROUTES_COMPONENTS, RESERVATION_ROUTES_COMPONENTS)
