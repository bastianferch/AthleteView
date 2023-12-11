import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UrlService } from '../../../config/service/UrlService';
import { Zone } from "../dto/Zone";

@Injectable({
  providedIn: 'root',
})


export class ZoneService {

  private zoneBaseUri: string;

  constructor(private httpClient: HttpClient, private urlService: UrlService) {
    const backendUrl = this.urlService.getBackendUrl();
    this.zoneBaseUri = backendUrl + 'zones';
  }

  editZones(zones: Zone[]): Observable<Zone[]> {
    return this.httpClient.put<Zone[]>(this.zoneBaseUri, zones);
  }

  getZones(): Observable<Zone[]> {
    return this.httpClient.get<Zone[]>(this.zoneBaseUri);
  }

  resetZones(): Observable<Zone[]> {
    return this.httpClient.delete<Zone[]>(this.zoneBaseUri);
  }

}
