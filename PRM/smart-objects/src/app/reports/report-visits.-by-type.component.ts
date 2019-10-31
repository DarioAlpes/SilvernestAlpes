import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ReportVisitsByTypeService} from "./report-visits-by-type.service";
import {ReportByLocation} from "./report-by-location";
declare let d3: any;
declare var moment: any;

@Component({
  selector: 'report-visits-by-service',
  templateUrl: 'app/templates/reports/report-visits-by-type.html',
  providers: [ReportVisitsByTypeService]
})
export class ReportVisitsByTypeComponent implements OnInit, OnDestroy
{
  report: ReportByLocation[];

  subscriptions: any;
  navigated = false;
  error: any;
  idClient: number;
  locationType: string;
  initialTime: string;
  finalTime: string;
  options: any;
  data: any;
  data2: any;
  title: string;
  isTable: boolean = true;
  ini:Date;
  end:Date;
  es:any;

  constructor(private reportVisitsByTypeService: ReportVisitsByTypeService, private route: ActivatedRoute, private router: Router)
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
          if(params['type'] !== undefined)
          {
            this.locationType = params['type'].toUpperCase();
            this.title = this.title + ' ' + this.locationType;
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
    this.reportVisitsByTypeService.getReport(this.idClient, this.locationType, this.initialTime, this.finalTime)
      .then
      (
        report =>
        {
          let reportValues = [];
          let reportValues2 = [];
          this.report = report;
          for(let i=0; i<this.report.length;i++) {
            for(var key in this.report[i].report)
            {
              reportValues.push(
                {"label": "Visitas de tipo '"+key+"' para la ubicación " + this.report[i]["location-name"],
                  "value": this.report[i].report[key]});
            }
            reportValues2.push({"name":this.report[i]["location-name"],"A": this.report[i].report["A"],
              "B": this.report[i].report["B"], "C": this.report[i].report["C"], "D": this.report[i].report["D"],
              "Otro": this.report[i].report[""]})
          }
          this.data2 = [{key: "Visitas de la ubicaciónes de tipo " + this.locationType + " por categoría", values: reportValues2}];
          this.data =[{key: "Visitas de la ubicaciónes de tipo " + this.locationType + " por categoría", values: reportValues}];
        }
      )
      .catch(error => this.error = error);
  }
}
