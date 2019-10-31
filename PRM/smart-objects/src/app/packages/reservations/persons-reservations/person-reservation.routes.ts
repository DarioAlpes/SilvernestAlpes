import { Route }  from '@angular/router';
import { PersonReservationListComponent } from './person-reservation-list.component';
import { PersonReservationDetailsComponent } from './person-reservation-details.component';
import { PERSON_CONSUMPTION_ROUTES, PERSON_CONSUMPTION_ROUTES_COMPONENTS } from './person-consumptions/person-consumption.routes';

const PERSON_RESERVATION_SPECIFIC_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/packages/:id_package/persons-reservations',
    component: PersonReservationListComponent
  },
  {
    path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id',
    component: PersonReservationDetailsComponent
  }
];

export const PERSON_RESERVATION_ROUTES: Route[] = PERSON_RESERVATION_SPECIFIC_ROUTES.concat(PERSON_CONSUMPTION_ROUTES);

export const PERSON_RESERVATION_ROUTES_COMPONENTS: any[] = [PersonReservationListComponent, PersonReservationDetailsComponent]
                                                .concat(PERSON_CONSUMPTION_ROUTES_COMPONENTS)