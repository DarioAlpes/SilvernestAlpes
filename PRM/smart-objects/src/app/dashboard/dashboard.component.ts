import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Dashboard } from './dashboard';
import { TableViewComponent } from '../../views/table/table.component';
import { PageHeader } from '../../views/pageheader/pageheader.component';

@Component({
  selector: 'dashboard',
  templateUrl: 'app/templates/dashboard/dashboard.html'
})
export class DashboardComponent
{

}
