import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ReportVisitsService} from "./report-visits.service";
import {ReportVisits} from "./report-visits";
import { LocationService } from '../locations/location.service';
import { Location } from '../locations/location';
declare let d3: any;
declare var moment: any;

@Component({
  selector: 'report-visits',
  templateUrl: 'app/templates/reports/report-visits.html',
  providers: [ReportVisitsService]
})
export class ReportVisitsComponent implements OnInit, OnDestroy
{
  report: ReportVisits;

  subscriptions: any;
  navigated = false;
  error: any;
  idClient: number;
  idLocation: number;
  initialTime: string;
  finalTime: string;
  options: any;
  data: any;
  title: string;
  isTable: boolean = false;
  ini:Date;
  end:Date;
  es:any;

  constructor(private reportVisitsService: ReportVisitsService, private route: ActivatedRoute, private router: Router, private locationService:LocationService)
  {
  }

  ngOnInit()
  {
    this.es = {
          closeText: "Cerrar",
        	prevText: "<Ant",
        	nextText: "Sig>",
        	currentText: "Hoy",
        	monthNames: [ "enero","febrero","marzo","abril","mayo","junio",
        	"julio","agosto","septiembre","octubre","noviembre","diciembre" ],
        	monthNamesShort: [ "ene","feb","mar","abr","may","jun",
        	"jul","ago","sep","oct","nov","dic" ],
        	dayNames: [ "domingo","lunes","martes","miércoles","jueves","viernes","sábado" ],
        	dayNamesShort: [ "dom","lun","mar","mié","jue","vie","sáb" ],
        	dayNamesMin: [ "D","L","M","X","J","V","S" ],
        	weekHeader: "Sm",
        	dateFormat: "dd/mm/yy",
        	firstDay: 1,
        	isRTL: false,
        	showMonthAfterYear: false,
        	yearSuffix: ""
        };
    this.ini = moment().startOf('day').format('L');
    this.end = moment().startOf('day').subtract(1, 'week').format('L');
    this.options = {
      chart: {
        type: 'discreteBarChart',
        height: 450,
        margin : {
          top: 20,
          right: 20,
          bottom: 50,
          left: 55
        },
        x: function(d){return d.label;},
        y: function(d){return d.value;},
        showValues: true,
        valueFormat: function(d){
          return d3.format(',.4f')(d);
        },
        duration: 500,
        xAxis: {
          axisLabel: 'Categoría'
        },
        yAxis: {
          axisLabel: 'Cuenta de Visitantes Únicos',
          axisLabelDistance: -10
        }
      }
    };
    this.title = 'Reporte Visitas por Categoría';

    this.subscriptions = this.route.params.subscribe(
      params =>
      {
        if(params['id_client']!==undefined)
        {
          this.idClient = +params['id_client'];
          if(params['id_location'] !== undefined)
          {
            this.idLocation = +params['id_location'];
            this.locationService.getLocation(this.idClient,this.idLocation)
                .then(location => this.title = this.title + ' ' + location.name);
            this.navigated = true;
            this.getReport();
          }
          else
          {
            this.navigated = false;
          }
        }
        else
        {
          this.router.navigate(['clients']);
        }
      }
    );
  }

  goBack()
  {
    if (this.navigated)
    {
      window.history.back();
    }
  }
  switch(par)
  {
    this.isTable = par;
  }

  ngOnDestroy()
  {
    this.subscriptions.unsubscribe();
  }

  private getReport() {
    this.reportVisitsService.getReport(this.idClient, this.idLocation, this.initialTime, this.finalTime)
      .then
      (
        report =>
        {
          this.report = report;
          let reportValues = [];
          for(var key in this.report)
          {
            reportValues.push({"label": key, "value": this.report[key]});
          }
          this.data = [{key: "Visitas de la ubicación por categoría", values: reportValues}];
        }
      )
      .catch(error => this.error = error);
  }
}
