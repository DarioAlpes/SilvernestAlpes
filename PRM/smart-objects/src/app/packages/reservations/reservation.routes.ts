import { Route }  from '@angular/router';
import { ReservationListComponent } from './reservation-list.component';
import { ReservationDetailsComponent } from './reservation-details.component';
import { PERSON_RESERVATION_ROUTES, PERSON_RESERVATION_ROUTES_COMPONENTS } from './persons-reservations/person-reservation.routes';

const RESERVATION_SPECIFIC_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages/:id_package/reservations',
    component: ReservationListComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/reservations/:id',
    component: ReservationDetailsComponent
  }
];

export const RESERVATION_ROUTES: Route[] = RESERVATION_SPECIFIC_ROUTES.concat(PERSON_RESERVATION_ROUTES);

export const RESERVATION_ROUTES_COMPONENTS: any[] = [ReservationListComponent, ReservationDetailsComponent]
                                                .concat(PERSON_RESERVATION_ROUTES_COMPONENTS)