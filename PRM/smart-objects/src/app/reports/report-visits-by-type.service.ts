import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import {ClientService} from "../clients/client.service";
import {ReportByLocation} from "./report-by-location";
@Injectable()
export class ReportVisitsByTypeService extends BaseService
{
  constructor(http: Http)
  {
    super(http);
  }

  getReport(idClient: number, type: string, initialTime: string, finalTime: string): Promise<ReportByLocation[]>
  {
    let queryString = "";
    if(type)
    {
      queryString = "?type=" + type;
    }

    if(initialTime) {
      if(queryString === "")
      {
        queryString = "?"
      }
      else
      {
        queryString += "&"
      }
      queryString += "initial-time=" + initialTime;
    }

    if(finalTime) {
      if(queryString === "")
      {
        queryString = "?"
      }
      else
      {
        queryString += "&"
      }
      queryString += "final-time=" + finalTime;
    }
    return this.http.get(ClientService.getClientUrl(idClient)+ 'visits-per-category/' + queryString)
      .toPromise()
      .then(response => response.json())
      .catch(this.handleApiError);
  }
}


export const REPORT_VISITS_BY_TYPE_SERVICES : any[] = [ReportVisitsByTypeService];
