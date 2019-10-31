import { Route }  from '@angular/router';
import { EventListComponent } from './event-list.component';
import { EventDetailsComponent } from './event-details.component';

export const EVENT_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/events',
    component: EventListComponent
  },
  {
    path: 'clients/:id_client/events/:id',
    component: EventDetailsComponent
  }
];

export const EVENT_ROUTES_COMPONENTS: any[] = [EventListComponent, EventDetailsComponent]