import { Route }  from '@angular/router';
import {ReportVisitsComponent} from "./report-visits.component";
import {ReportVisitsByTypeComponent} from "./report-visits.-by-type.component";

export const REPORT_VISITS_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/locations/:id_location/visits-per-category',
    component: ReportVisitsComponent
  },

  {
    path: 'clients/:id_client/location-type/:type/visits-per-category',
    component: ReportVisitsByTypeComponent
  }
];

export const REPORT_VISITS_COMPONENTS: any[] = [ReportVisitsComponent];
