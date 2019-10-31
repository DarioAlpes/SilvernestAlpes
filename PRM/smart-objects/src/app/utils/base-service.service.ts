import { Headers, Http } from '@angular/http';
import 'rxjs/add/operator/toPromise';

export class BaseService
{

    constructor(protected http: Http)
    {}

    handleApiError(error: any)
    {
        var bodyMessage = "";
        try
        {
            bodyMessage = error.json().message || error.toString();
        }
        catch(err)
        {
            bodyMessage = error.toString();
        }
        var message = `Código de error ${error.status}. ${bodyMessage}`;
        console.error('An error occurred', message);
        return Promise.reject(message);
    }

    getCollection(url: string)
    {
        return this.http.get(url)
                   .toPromise()
                   .then(response => response.json())
                   .catch(this.handleApiError);
    }

    getItem(baseUrl: string, id: any)
    {
        return this.http.get(BaseService.getItemUrl(baseUrl, id))
                   .toPromise()
                   .then(response => response.json())
                   .catch(this.handleApiError);
    }

    updateItem(baseUrl: string, item: any, id: any = undefined)
    {
        if(id === undefined)
        {
            id = item.id;
        }

        let headers = new Headers({
            'Content-Type': 'application/json'});

        return this.http
               .put(BaseService.getItemUrl(baseUrl, id), JSON.stringify(item), {headers: headers})
               .toPromise()
               .then(response => response.json())
               .catch(this.handleApiError);
    }

    patchItem(baseUrl: string, body: any, id: any)
    {
        let headers = new Headers({
            'Content-Type': 'application/json'});

        return this.http
               .patch(BaseService.getItemUrl(baseUrl, id), JSON.stringify(body), {headers: headers})
               .toPromise()
               .then(response => response.json())
               .catch(this.handleApiError);
    }

    createItem(baseUrl: string, item: any)
    {
        let headers = new Headers({
            'Content-Type': 'application/json'});

        return this.http
               .post(baseUrl, JSON.stringify(item), {headers: headers})
               .toPromise()
               .then(response => response.json())
               .catch(this.handleApiError);
    }

    deleteItem(baseUrl: string, item: any, id: any = undefined)
    {
        if(id === undefined)
        {
            id = item.id;
        }
        let headers = new Headers({
            'Content-Type': 'application/json'});
        return this.http
               .delete(BaseService.getItemUrl(baseUrl, id), headers)
               .toPromise()
               .then(response => response.json())
               .catch(this.handleApiError);
    }

    saveItem(baseUrl: string, item: any, id: any = undefined)
    {
        if(id === undefined)
        {
            if(item.id)
            {
                id = item.id;
            }
        }

        if(id === undefined)
        {
            return this.createItem(baseUrl, item);
        }
        else
        {
            return this.updateItem(baseUrl, item, id);
        }
    }

    static getItemUrl(baseUrl: string, id: any)
    {
        return `${baseUrl}${id}/`;
    }
}