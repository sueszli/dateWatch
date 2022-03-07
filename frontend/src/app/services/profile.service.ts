import {Globals} from '../global/globals';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Account, UserStatus} from '../dtos/account';
import {AccountParticipationStatus} from '../dtos/participant-event';


@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private accountBaseUri: string = this.globals.backendUri + '/account';


  constructor(private globals: Globals, private httpClient: HttpClient) {
  }


  /**
   * Fetches the current profile from backend.
   */
  getProfile<T extends Account>(): Observable<T> {
    return this.httpClient.get<T>(this.accountBaseUri);
  }

  /**
   * Sets the provided profile to backend.
   *
   * @param profile with valid parameters (check them in backend)
   */
  setProfile(profile): Observable<boolean> {
    return this.httpClient.put<boolean>(this.accountBaseUri, profile, {responseType: 'json'});
  }

  /**
   * Remove pairings.
   * After that neither you nor your pairing partner will see this match.
   */
  revokePairingApprovals(otherPersonsEmail: string): Observable<void> {
    return this.httpClient.delete<void>(this.accountBaseUri + `/approved-pairings/${otherPersonsEmail}`);
  }

  /**
   * Deactivate the organizer's account.
   * After that it will not be possible to login with current user credentials.
   */
  deactivateProfile(): Observable<void> {
    return this.httpClient.delete<void>(this.accountBaseUri + '/deactivate', {responseType: 'json'});
  }

  /**
   * Delete the participant's account.
   * After that it will not be possible to login with current user credentials.
   */
  deleteProfile(): Observable<void> {
    return this.httpClient.delete<void>(this.accountBaseUri + '/delete', {responseType: 'json'});
  }

  /**
   * Ban or unban an account.
   * After that it will not be possible to login with current user credentials
   * or it will be possible again.
   */
  toggleBanStatus(togglebanaccount): Observable<void> {
    console.log(togglebanaccount);
    return this.httpClient.put<void>(this.accountBaseUri + '/ban', togglebanaccount, {responseType: 'json'});
  }

  status(): Observable<UserStatus> {
    return this.httpClient.get<UserStatus>(this.accountBaseUri + '/status');
  }

  participationStatus(): Observable<AccountParticipationStatus> {
    return this.httpClient.get<AccountParticipationStatus>(this.accountBaseUri + '/participation');
  }
}
