import {Component,Input, OnInit} from '@angular/core';
import {TableObject } from './table-object';
declare var moment: any;

@Component({
  selector: 'tableview',
  templateUrl: 'views/table/table.html',
})
export class TableViewComponent implements OnInit {

  public page:number = 1;
  public maxSize:number = 5;
  public numPages:number = 1;
  @Input() rows:Array<any>;
  @Input() columns:Array<any>;
  @Input() itemsPerPage:number;
  @Input() length:number;
  @Input() config:TableObject;
  @Input() TableData:Array<any>;
  @Input() click: Function;
  @Input() edit: Function;
  @Input() delete: Function;

  private data:Array<any> = this.TableData;

  public constructor() {

  }

  public ngOnInit():void {

    for( var i=0; i < this.columns.length; i++){
      if(this.columns[i].fieldtype == 'date'){
        for( var j=0; j < this.TableData.length; j++){
          let str =  this.TableData[j][this.columns[i].field];
          var y = +str.substr(0,4),
          m = +str.substr(4,2) - 1,
          d = +str.substr(6,2);
          var formdate = new Date(y,m,d,0,0,0,0);
          this.TableData[j][this.columns[i].field] = moment(formdate).format("MMMM Do, YYYY");
        }
      }
    }
    console.log(this.TableData);

  }



}
