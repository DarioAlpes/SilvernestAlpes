import { Route }  from '@angular/router';
import { DashboardComponent } from './dashboard.component';

export const DASHBOARD_ROUTES: Route[] = [
  {
    path: 'dashboard',
    component: DashboardComponent
  }
];

export const DASHBOARD_ROUTES_COMPONENTS: any[] = [DashboardComponent]