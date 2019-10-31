import { Injectable } from '@angular/core';
import { ReportVisits } from './report-visits';
import { Http } from '@angular/http';
import { BaseService } from '../utils/base-service.service';
import {LocationService} from "../locations/location.service";
@Injectable()
export class ReportVisitsService extends BaseService
{
  constructor(http: Http)
  {
    super(http);
  }

  getReport(idClient: number, idLocation: any, initialTime: string, finalTime: string): Promise<ReportVisits>
  {
    let queryString = "";
    if(initialTime)
    {
      queryString = "?initial-time=" + initialTime;
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
    return this.http.get(LocationService.getLocationUrl(idClient, idLocation)+'visits-per-category/' + queryString )
      .toPromise()
      .then(response => response.json())
      .catch(this.handleApiError);
  }
}


export const REPORT_VISITS_SERVICES : any[] = [ReportVisitsService];
