import {Injectable} from '@angular/core';
import {AuthRequest} from '../dtos/auth-request';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {tap} from 'rxjs/operators';
// @ts-ignore
import jwt_decode from 'jwt-decode';
import {Globals} from '../global/globals';
import {Router} from '@angular/router';
import {Account} from '../dtos/account';
import {ForgotPwd} from '../dtos/forgot-pwd';
import {ResetPwd} from '../dtos/reset-pwd';
import {ChangePwd} from '../dtos/change-pwd';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';
  private registrationBaseUri: string = this.globals.backendUri + '/account';


  constructor(private httpClient: HttpClient, private globals: Globals, private router: Router) {
  }

  private static getTokenExpirationDate(token: string): Date {

    const decoded: any = jwt_decode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }


  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<string> {
    return this.httpClient.post(this.authBaseUri, authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.updateCurrentUserSession(authResponse))
      );
  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (AuthService.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
    localStorage.removeItem('roles');
    this.router.navigate(['/login']);
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    if (this.getToken() != null) {
      const decoded: any = jwt_decode(this.getToken());
      const authInfo: string[] = decoded.rol;
      if (authInfo.includes('ROLE_ADMIN')) {
        return 'ADMIN';
      } else if (authInfo.includes('ROLE_ORGANIZER')) {
        return 'ORGANIZER';
      } else if (authInfo.includes('ROLE_PARTICIPANT')) {
        return 'PARTICIPANT';
      } else if (authInfo.includes('ROLE_USER')) {
        return 'USER';
      }
    }
    return 'UNDEFINED';
  }

  registerAccount<T extends Account>(account: T): Observable<T> {
    const params = new HttpParams().set('confirmationUrl', location.origin + '/confirmation/');
    return this.httpClient.post<T>(this.registrationBaseUri + '/registration', account, {
      params
    });
  }

  confirmAccount(uuid: string): Observable<any> {
    const params = new HttpParams().set('token', uuid);
    return this.httpClient.put(this.registrationBaseUri + '/confirmation', null, {
      params,
    });
  }

  updateCurrentUserSession(authResponse: string) {
    const decoded: any = jwt_decode(authResponse.valueOf());
    localStorage.setItem('authToken', authResponse);
    localStorage.setItem('roles', decoded.rol);
  }

  forgotPassword(forgot: ForgotPwd): Observable<Account> {
    forgot.resetPasswordBaseUrl = location.origin + '/reset-password/';
    return this.httpClient.put<Account>(this.registrationBaseUri + '/forgot-password', forgot);
  }

  resetPassword(reset: ResetPwd, token: string): Observable<void> {
    reset.token = token;
    return this.httpClient.put<void>(this.registrationBaseUri + '/password-confirmation', reset);
  }

  changePassword(email: string): Observable<ChangePwd> {
    const params = new HttpParams().set('email', email);
    return this.httpClient.get<ChangePwd>(this.registrationBaseUri + '/password-change', {params});
  }

  getAllAccounts(): Observable<Account[]> {
    return this.httpClient.get<Account[]>(this.registrationBaseUri + '/all');
  }
}
