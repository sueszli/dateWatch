import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Globals {
  // interaction types for event from participant
  static interactionTypes = {
    showInterest: 'SHOW_INTEREST',
    promiseEvent: 'PROMISE_EVENT',
  };
  static roles = {
    participant: 'ROLE_PARTICIPANT',
    organizer: 'ROLE_ORGANIZER',
    admin: 'ROLE_ADMIN',
  };


  readonly backendUri: string = this.findBackendUrl();

  private findBackendUrl(): string {
    if (window.location.port === '4200') { // local `ng serve`, backend at localhost:8080
      return 'http://localhost:8080/api/v1';
    } else {
      // assume deployed somewhere and backend is available at same host/port as frontend
      return window.location.protocol + '//' + window.location.host + window.location.pathname + 'api/v1';
    }
  }
}


