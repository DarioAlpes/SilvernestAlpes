import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Client } from './client';
import { ClientService } from './client.service';
import {Validators, FormControl} from '@angular/forms';
@Component({
  selector: 'client-detail',
  templateUrl: 'app/templates/clients/client-details.html'
})
export class ClientDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    client: Client;

    @Output()
    close = new EventEmitter();


    subscriptions: any;
    navigated = false;
    error: any;
    public boundCallBack: Function;

    public fields:Array<any> = [
        {id: 'id',label: 'Id', type:'noedit'},
        {id: 'name', label: 'Nombre', type:'input', validations: [Validators.required] }
    ];

    static containsAnA(control: FormControl) {
      if (control.value.indexOf("A") === -1) {
        return {"error_message": "El valor debe contener al menos una A"};
      }
      return null;
    }

    static containsAB(control: FormControl) {
      if (control.value.indexOf("B") === -1) {
        return {"error_message": "El valor debe contener al menos una B"};
      }
      return null;
    }

  static containsNoC(control: FormControl) {
    if (control.value.indexOf("C") !== -1) {
      return {"error_message": "El valor no debe contener ninguna C"};
    }
    return null;
  }

    constructor(private clientService: ClientService,  private route: ActivatedRoute)
    {
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
              if(params['id'] !== undefined && params['id'] !== 'new' )
              {
                this.navigated = true;
                let id = +params['id'];
                this.clientService.getClient(id)
                    .then(client => {this.client = client;
                    console.log(this.client);})
                    .catch(error => this.error = error);

              }
              else
              {
                if(params['id'] == 'new' )
                {
                  this.navigated = true;
                  this.client = new Client();
                }
                else
                {
                  this.navigated = false;
                }

              }
            }
        );
        this.boundCallBack = this.saveClient.bind(this);
    }

    saveClient()
    {
        this.clientService.saveClient(this.client)
            .then
                (
                    client =>
                    {
                        this.client = client;
                        this.goBack(client);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedClient: Client = null)
    {
        this.close.emit(savedClient);
        if (this.navigated)
        {
            window.history.back();
        }
    }

    ngOnDestroy()
    {
        this.subscriptions.unsubscribe();
    }
}
