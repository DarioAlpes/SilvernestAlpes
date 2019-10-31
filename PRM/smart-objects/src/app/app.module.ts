/**
 * Modulo principal de la aplicación
 * Created by Jorge on 12/08/2016.
 */
import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule}   from '@angular/forms';
import {appRouting, ROUTES_COMPONENTS}    from './app.routing';
import {MdButtonModule} from '@angular2-material/button';
import {MdCheckboxModule} from '@angular2-material/checkbox';
import {MdRadioModule} from '@angular2-material/radio';
import {MdSlideToggleModule} from '@angular2-material/slide-toggle';
import {MdSidenavModule} from '@angular2-material/sidenav';
import {MdListModule} from '@angular2-material/list';
import {MdGridListModule} from '@angular2-material/grid-list';
import {MdCardModule} from '@angular2-material/card';
import {MdIconModule} from '@angular2-material/icon';
import {MdProgressCircleModule} from '@angular2-material/progress-circle';
import {MdProgressBarModule} from '@angular2-material/progress-bar';
import {MdInputModule} from '@angular2-material/input';
import {MdTabsModule} from '@angular2-material/tabs';
import {MdToolbarModule} from '@angular2-material/toolbar';
import {MdTooltipModule} from '@angular2-material/tooltip';
import {MdMenuModule} from '@angular2-material/menu';

import { AppComponent }  from './app.component';
import {HttpModule} from "@angular/http";
import {FormViewComponent} from "../views/form/form.component";
import {PageHeader} from "../views/pageheader/pageheader.component";
import {TableViewComponent} from "../views/table/table.component";
import {GridViewComponent} from "../views/grid/grid.component";
import {DropdownModule} from "primeng/components/dropdown/dropdown";
import {DataTableModule} from "primeng/components/datatable/datatable";
import {DataGridModule} from "primeng/components/datagrid/datagrid";
import {CheckboxModule} from "primeng/components/checkbox/checkbox";
import {InputMaskModule} from "primeng/components/inputmask/inputmask";
import {CalendarModule} from "primeng/components/calendar/calendar";
import {nvD3} from 'ng2-nvd3'

@NgModule({
  imports: [
    BrowserModule,
    appRouting,
    HttpModule,
    FormsModule,
    MdButtonModule,
    MdCardModule,
    MdCheckboxModule,
    MdGridListModule,
    MdIconModule,
    MdInputModule,
    MdListModule,
    MdMenuModule,
    MdProgressBarModule,
    MdProgressCircleModule,
    MdRadioModule,
    MdSidenavModule,
    MdSlideToggleModule,
    MdTabsModule,
    MdToolbarModule,
    MdTooltipModule,
    ReactiveFormsModule,
    DropdownModule,
    DataTableModule,
    DataGridModule,
    CheckboxModule,
    InputMaskModule,
    CalendarModule],
  declarations: [ AppComponent, FormViewComponent, PageHeader, TableViewComponent,GridViewComponent, nvD3, ...ROUTES_COMPONENTS],
  bootstrap:    [ AppComponent ]
})
export class AppModule { }
