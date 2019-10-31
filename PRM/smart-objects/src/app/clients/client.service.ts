import { Injectable } from '@angular/core';
import { Client } from './client';
import { BaseService } from '../utils/base-service.service';
import { Http } from '@angular/http';

@Injectable()
export class ClientService extends BaseService
{
    constructor(http: Http)
    {
        super(http);
    }

    private static clientsUrl = 'https://smartobjectssas.appspot.com/clients/';

    static getClientUrl(id: any)
    {
        return BaseService.getItemUrl(ClientService.clientsUrl, id);
    }

    getClients(): Promise<Client[]>
    {
        return this.getCollection(ClientService.clientsUrl);
    }

    getClient(id: number): Promise<Client>
    {
        return this.getItem(ClientService.clientsUrl, id);
    }

    updateClient(client: Client): Promise<Client>
    {
        return this.updateItem(ClientService.clientsUrl, client);
    }

    saveClient(client: Client): Promise<Client>
    {
        return this.saveItem(ClientService.clientsUrl, client);
    }

    createClient(client: Client): Promise<Client>
    {
        return this.createItem(ClientService.clientsUrl, client);
    }

    deleteClient(client: Client): Promise<Client>
    {
        return this.deleteItem(ClientService.clientsUrl, client);
    }
}

export const CLIENTS_SERVICES : any[] = [ClientService];