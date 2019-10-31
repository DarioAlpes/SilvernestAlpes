import { Component, OnInit} from '@angular/core';
import { Client } from './client';
import { ClientDetailsComponent } from './client-details.component';
import { ClientService } from './client.service';
import { Router } from '@angular/router';
import { TableViewComponent } from '../../views/table/table.component';
import { TableObject } from '../../views/table/table-object';
import { PageHeader } from '../../views/pageheader/pageheader.component';

@Component({
  selector: 'client-list',
  templateUrl: 'app/templates/clients/client-list.html',
})
export class ClientListComponent implements OnInit
{
    /** table view config */
    public rows:Array<any> = [];
    public columns:Array<any> = [
        {header: 'ID', field: 'id', filter: false, filtermode:'contains', sortable:true, styleClass: 'idcol'},
        {header: 'Nombre', field: 'name', sort: true, filter: false, filtermode:'startsWith', sortable:true}
    ];
    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 1;
    public length:number = 0;

    public config:TableObject = {
            paging: true,
            sorting: {columns: this.columns},
            filtering: {filterString: '', columnName: 'name'},
            edit: true,
            delete: true
        };
    public TableData:Array<any> = [];
    public boundCallBack: Function;
    public boundCallBack2: Function;
    public boundCallBackDelete: Function;


    /** end +table view config */

    constructor(private router: Router, private clientService: ClientService)
    {
    }
    selectedClient: Client;

    clients: Client[];

    showClient = false;

    error: any;

    onSelect(client: Client)
    {
        let link = ['/clients', client.id];
        this.router.navigate(link);
    }

    onDelete(client: Client)
    {
        this.clientService.deleteClient(client)
        .then(client => this.close(client))
        .catch(error => this.error = error);
    }

    getClients()
    {
        this.clientService.getClients()
        .then(clients => {this.clients = clients;
        this.TableData = clients;
        console.log(this.TableData)})
        .catch(error => this.error = error);

    }

    public addClient()
    {
        let link = ['/clients', 'new'];
        this.router.navigate(link);
    }

    close(savedClient: Client)
    {
        this.showClient = false;
        if (savedClient)
        {
            this.getClients();
        }
    }

    ngOnInit()
    {
        this.getClients();
        this.boundCallBack = this.addClient.bind(this);
        this.boundCallBack2 = this.onSelect.bind(this);
        this.boundCallBackDelete = this.onDelete.bind(this);
    }
}
