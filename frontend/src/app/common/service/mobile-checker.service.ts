import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class MobileCheckService {
  private mobileRegex = new RegExp(/Android|BlackBerry|iPhone|iPad|iPod|Opera Mini|IEMobile/, 'i')

  isMobile(maxWidth = 600): boolean {
    return this.mobileRegex.test(navigator.userAgent) || window.innerWidth < maxWidth
  }
}
