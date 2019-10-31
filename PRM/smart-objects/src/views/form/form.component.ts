import {Component,Input,Output, EventEmitter, OnInit} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'formview',
  templateUrl: 'views/form/formview.html'
})
export class FormViewComponent implements OnInit {

  @Input() fields:Array<any>;
  @Input() model;
  @Input() click: Function;
  @Input() imageCallback: Function;
  form: FormGroup;
  @Output() modelChange = new EventEmitter();
    change(newValue) {
      console.log('newvalue', newValue);
      this.model = newValue;
      this.modelChange.emit(newValue);
    }
        es:any;

  public constructor() {

  }

  public ngOnInit():void {
    let group: any = {};
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

    for (var i =0; i< this.fields.length; i++)
    {
      console.log(this.model);
      if (this.fields[i].type === 'dropdown' && this.model[this.fields[i].id] !== undefined )
      {
        console.log(this.model);
      }
      if (this.fields[i].type === 'date' && this.model[this.fields[i].id] !== undefined )
      {
        console.log(this.model);
          var stringdate = this.model[this.fields[i].id];
          console.log(stringdate);
          /**this.model[this.fields[i].id] = new Date(stringdate.substring(0, 4), stringdate.substring(4, 6)-1, stringdate.substring(6, 8),0,0,0,0);**/
          var day =stringdate.substring(6, 8);
          var month = stringdate.substring(4, 6);
          var year = stringdate.substring(0, 4);
          this.model[this.fields[i].id] = day + '/' + month + '/' + year;
          console.log(month + '/' + day + '/' + year);
      }
    }
    this.fields.forEach(field => {
      let validations = field.validations || [];
      group[field.id] = new FormControl(this.model[field.id], Validators.compose(validations));
    });
    this.form = new FormGroup(group);

  /*console.log(this.model);
    for (var i = 0; i < this.fields.length; i++) {
      let validations = this.fields[i].validations || [];
      validations.push(Validators.required);
      //this.controls[this.fields[i].id] = new Control('', Validators.compose(validations));
      if(this.fields[i].id == "name"){
        this.testControl = new Control('', Validators.compose(validations));
      }
    }*/

  }
  public formclick(){
console.log("funcion");
    for (var i=1; i<this.fields.length;i++)
    {
      console.log("for");
      if(this.fields[i].fieldtype ==='number')
      {
        console.log(this.fields[i].id);
        this.model[this.fields[i].id] = Number(this.model[this.fields[i].id].replace(/[^0-9\.]+/g,""));
      }
      if(this.fields[i].type ==='date')
      {
        var datepart = this.model[this.fields[i].id].split("/");
        console.log(datepart);
        console.log(datepart[2].toString() + datepart[1].toString() + datepart[0].toString());
        this.model[this.fields[i].id] = datepart[2].toString() + datepart[1].toString() + datepart[0].toString();
      }
    }
    this.click();
  /*console.log(this.model);
    for (var i = 0; i < this.fields.length; i++) {
      let validations = this.fields[i].validations || [];
      validations.push(Validators.required);
      //this.controls[this.fields[i].id] = new Control('', Validators.compose(validations));
      if(this.fields[i].id == "name"){
        this.testControl = new Control('', Validators.compose(validations));
      }
    }*/

  }



}
