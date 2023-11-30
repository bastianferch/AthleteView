import { ElementRef, Injectable, OnDestroy, Renderer2, RendererFactory2 } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

/**
 * Service holds the number of active requests and tells
 * if spinner must be showed.
 *
 * Requests counts in **LoadInterceptor**.
 */
@Injectable({
  providedIn: 'root',
})
export class SpinnerService implements OnDestroy {
  private _destroyStream: Subject<void> = new Subject<void>();
  private renderer: Renderer2;

  private activeRequests = 0;
  private spinnerActiveDivs: ElementRef[] = [];
  private isActive$ = new BehaviorSubject<boolean>(undefined);

  constructor(private router: Router,
    rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);

    this.router.events.pipe(takeUntil(this._destroyStream)).subscribe((e) => {
      /**
       * This condition is used for situations when some requests got stuck
       * and the user just trying to go to another page.
       * Without this even if the user goes to another page, this service will
       * have unresolved requests, so the spinner will be activated forever.
       */
      if (e instanceof NavigationStart) {
        this.resetActiveRequests();
      }
    });
  }

  ngOnDestroy(): void {
    this._destroyStream.next();
  }

  checkActive() {
    const isNotZeroActiveRequests = this.activeRequests !== 0;
    this.isActive$.next(isNotZeroActiveRequests);
  }

  addActiveRequest() {
    this.activeRequests++;
    this.checkActive();
  }

  removeActiveRequest() {
    /**
     * For situations when we changed page but there were some requests
     * on the previous page that wasn't resolved, so they are resolving
     * together with current page requests and can make this counter negative.
     */
    if (this.activeRequests - 1 >= 0) {
      this.activeRequests--;
    }
    this.checkActive();
  }

  finishLoadingForComponent(component: ElementRef) {
    if (!component) {
      return;
    }
    this.spinnerActiveDivs.splice(this.spinnerActiveDivs.indexOf(component));
    const element = component.nativeElement;
    const loadingCompTemplate = element.querySelector('#component-spinner');
    if (!loadingCompTemplate) {
      // Spinner was finished already / did not exist.
      return;
    }
    this.renderer.removeChild(element, loadingCompTemplate);
  }

  resetActiveRequests() {
    this.activeRequests = 0;
    this.spinnerActiveDivs.forEach((c) => this.finishLoadingForComponent(c));
    this.spinnerActiveDivs = [];
    this.checkActive();
  }

  get isActive(): Observable<boolean> {
    return this.isActive$.asObservable();
  }

}
