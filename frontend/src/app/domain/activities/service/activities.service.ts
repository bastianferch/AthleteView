import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { UrlService } from "src/app/config/service/UrlService";


@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  constructor(
    private httpClient: HttpClient,
    private urlService: UrlService
  ) { }

  importFitActivity(data: any) {
    var url = this.urlService.getBackendUrl() + 'activity/import';
    const formData = new FormData()
    formData.append("files", data)
    return this.httpClient.post(url, formData);
  }
}