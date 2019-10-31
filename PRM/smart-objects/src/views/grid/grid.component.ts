import {Component,Input, OnInit} from '@angular/core';

@Component({
  selector: 'gridview',
  templateUrl: 'views/grid/grid.html'
})
export class GridViewComponent implements OnInit {

  public page:number = 1;
  public maxSize:number = 5;
  public numPages:number = 1;
  @Input() rows:Array<any>;
  @Input() columns:Array<any>;
  @Input() itemsPerPage:number;
  @Input() length:number;
  @Input() TableData:Array<any>;
  @Input() click: Function;
  @Input() edit: Function;
  @Input() delete: Function;

  private data:Array<any> = this.TableData;

  public constructor() {

  }

  public ngOnInit():void {
    console.log(this.TableData);
    

  }



}
