import { Route }  from '@angular/router';
import { PersonListComponent } from './person-list.component';
import { PersonDetailsComponent } from './person-details.component';
import { PersonTimelineComponent } from './person-timeline.component';

export const PERSON_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/persons',
    component: PersonListComponent
  },
  {
    path: 'clients/:id_client/persons/:id',
    component: PersonDetailsComponent
  },
  {
    path: 'clients/:id_client/persons/:id/timeline',
    component: PersonTimelineComponent
  }
];

export const PERSON_ROUTES_COMPONENTS: any[] = [PersonListComponent, PersonDetailsComponent, PersonTimelineComponent];
