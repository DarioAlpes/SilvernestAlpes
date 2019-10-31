import { Routes, RouterModule }  from '@angular/router';
import { PACKAGE_ROUTES, PACKAGE_ROUTES_COMPONENTS } from './packages/package.routes';
import { CLIENT_ROUTES, CLIENT_ROUTES_COMPONENTS } from './clients/client.routes';
import { DASHBOARD_ROUTES, DASHBOARD_ROUTES_COMPONENTS } from './dashboard/dashboard.routes';
import { SKU_ROUTES, SKU_ROUTES_COMPONENTS } from './skus/sku.routes';
import { PERSON_ROUTES, PERSON_ROUTES_COMPONENTS } from './persons/person.routes';
import {REPORT_VISITS_COMPONENTS, REPORT_VISITS_ROUTES} from "./reports/report-visits.routes";

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/clients',
    pathMatch: 'full'
  },
  ...PACKAGE_ROUTES,
  ...CLIENT_ROUTES,
  ...SKU_ROUTES,
  ...PERSON_ROUTES,
  ...DASHBOARD_ROUTES,
  ...REPORT_VISITS_ROUTES
];

const GLOBAL_COMPONENTS: any[] = [];
export const appRouting = RouterModule.forRoot(appRoutes);
export const ROUTES_COMPONENTS: any[] = GLOBAL_COMPONENTS.concat(PACKAGE_ROUTES_COMPONENTS, CLIENT_ROUTES_COMPONENTS,
  SKU_ROUTES_COMPONENTS, PERSON_ROUTES_COMPONENTS, DASHBOARD_ROUTES_COMPONENTS, REPORT_VISITS_COMPONENTS);
