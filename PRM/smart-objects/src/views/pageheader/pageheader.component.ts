import {Component, Input, OnInit} from '@angular/core';



@Component({
  selector: 'pageheader',
  templateUrl: 'views/pageheader/pageheader.html'
})
export class PageHeader {

   @Input() title:string;
   constructor() {}

  }
