import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const whiteList = ['/authentication', '/account/registration', '/account/confirmation',
      '/account/forgot-password', '/account/password-confirmation'];
    return next.handle(
      whiteList.find(whiteListedEndpoint => req.url.startsWith(this.globals.backendUri + whiteListedEndpoint))
        ? req
        : req.clone({ headers: req.headers.set('Authorization', this.authService.getToken()) })
    );
  }
}
