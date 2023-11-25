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

  importFitActivity(data: File[]) {
    var url = this.urlService.getBackendUrl() + 'activity/import'
    const formData = new FormData()
    for (let item of data) {
      formData.append("files", item)
    }
    return this.httpClient.post(url, formData)
  }
}