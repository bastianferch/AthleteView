import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class UrlService {
  private backendUrl = '';

  constructor() {
    this.defineBackendUrl();
  }

  getBackendUrl(): string {
    return this.backendUrl;
  }

  /**
   * When we will deploy multiple instances, we have to know the backend URL.
   * ToDo: adjust the path for deployment.
   */
  private defineBackendUrl() {
    const path = window.location.hostname;
    switch (path) {
      case 'localhost': {
        this.backendUrl = 'http://localhost:8080/api/';
        break;
      }
    }
  }
}
