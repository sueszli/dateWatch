import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Event} from '../dtos/event';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Globals} from '../global/globals';
import {ArrivalStatistics, OrganizerEvent} from '../dtos/organizer-event';
import {EventSourcePolyfill} from 'ng-event-source';
import {AuthService} from './auth.service';
import {ParticipantEvent} from '../dtos/participant-event';
import {EventFilter} from '../dtos/eventFilter/event-filter';
import {MatchParticipantAccount} from '../dtos/match-participant-account';
import {Feedback} from '../dtos/feedback';
import {PostStatisticsDto} from '../dtos/post-statistics-dto';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private eventBaseUri: string = this.globals.backendUri + '/event';
  private eventSource: EventSourcePolyfill;


  constructor(private httpClient: HttpClient, private globals: Globals, private authService: AuthService) {
  }


  /**
   * Fetch all events by current user. (Works for both participants and organizers).
   */
  getAllEvents<T extends Event>(): Observable<T[]> {
    // returns all events that the user is either registered to or that the user organizes (based on user role)
    return this.httpClient.get<T[]>(this.eventBaseUri);
  }

  getFilteredEvents(filter: EventFilter): Observable<Event[]> {
    let params = new HttpParams();
    Object.keys(filter).forEach((key) => params = params.append(key, filter[key]));
    return this.httpClient.get<OrganizerEvent[]>(this.eventBaseUri + '/filter', {params});
  }

  getPlannedEvents(filter: EventFilter): Observable<Event[]> {
    let params = new HttpParams();
    Object.keys(filter).forEach((key) => params = params.append(key, filter[key]));
    return this.httpClient.get<ParticipantEvent[]>(this.eventBaseUri + '/filter', {params});
  }

  getVisitedEvents(filter: EventFilter): Observable<Event[]> {
    let params = new HttpParams();
    Object.keys(filter).forEach((key) => params = params.append(key, filter[key]));
    return this.httpClient.get<ParticipantEvent[]>(this.eventBaseUri + '/filter', {params});
  }

  getPublicEvents(filter: EventFilter): Observable<Event[]> {
    let params = new HttpParams();
    Object.keys(filter).forEach((key) => params = params.append(key, filter[key]));
    return this.httpClient.get<ParticipantEvent[]>(this.eventBaseUri + '/filter', {params});
  }

  getEvent<T extends Event>(accessToken: string): Observable<T> {
    return this.httpClient.get<T>(`${this.eventBaseUri}/${accessToken}`);
  }

  createEvent(event: Event): Observable<OrganizerEvent> {
    return this.httpClient.post<OrganizerEvent>(this.eventBaseUri, event);
  }

  updateEvent(event: Event, accessToken: string): Observable<OrganizerEvent> {
    return this.httpClient.put<OrganizerEvent>(`${this.eventBaseUri}/${accessToken}/update`, event);
  }

  giveFeedback(feedback: Feedback): Observable<void> {
    return this.httpClient.post<void>(`${this.eventBaseUri}/feedback`, feedback);
  }

  getAllFeedback(): Observable<Feedback[]> {
    return this.httpClient.get<Feedback[]>(`${this.eventBaseUri}/feedback`);
  }

  closeRegistration(accessToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/registration`, null);
  }

  startEvent(accessToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/start`, null);
  }

  prepareNewRound(accessToken: string): Observable<void> {
    return this.httpClient.post<void>(`${this.eventBaseUri}/${accessToken}/round`, null);
  }

  startNextRound(accessToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/round`, null);
  }

  enterPairing(accessToken: string, otherPersonsPairingToken: string): Observable<void> {
    return this.httpClient.post<void>(`${this.eventBaseUri}/${accessToken}/pairing`, otherPersonsPairingToken);
  }

  approvePairing(accessToken: string, otherPersonsPairingToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/pairing`, otherPersonsPairingToken);
  }

  terminatePairing(accessToken: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.eventBaseUri}/${accessToken}/pairing`);
  }

  registerForEvent(accessToken: string, groupTitle: string = null): Observable<void> {
    return this.httpClient.post<void>(`${this.eventBaseUri}/${accessToken}/registration`, groupTitle);
  }

  deregisterForEvent(accessToken: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.eventBaseUri}/${accessToken}/registration`);
  }

  cancelEvent(accessToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/cancel`, null);
  }

  closeEvent(accessToken: string): Observable<void> {
    return this.httpClient.put<void>(`${this.eventBaseUri}/${accessToken}/close`, null);
  }

  reportArrival(accessToken: string, entranceToken: string): Observable<void> {
    return this.httpClient.post<void>(`${this.eventBaseUri}/${accessToken}/arrival`, entranceToken);
  }

  getArrivals(accessToken: string): Observable<ArrivalStatistics> {
    return this.httpClient.get<ArrivalStatistics>(`${this.eventBaseUri}/${accessToken}/arrival`);
  }

  getPostStatistics(accessToken: string): Observable<PostStatisticsDto> {
    return this.httpClient.get<PostStatisticsDto>(`${this.eventBaseUri}/${accessToken}/statistics`);
  }

  subscribeEvents<T>(eventType: string, consumer: (data: T) => void) {
    //todo: creates new subscription every second and timeout-retry is weird (see browser console), possibly close them?
    if (!this.eventSource) {
      this.eventSource = new EventSourcePolyfill(this.eventBaseUri + '/subscription',
        {headers: {authorization: this.authService.getToken()}});
      console.log('initialized event source');
    }
    console.log('add event listener for', eventType);
    this.eventSource.addEventListener(eventType, event => {
      const deserializedData: T = JSON.parse(event['data']);
      consumer(deserializedData);
    });
  }

  getAllMatches(): Observable<MatchParticipantAccount[]> {
    return this.httpClient.get<MatchParticipantAccount[]>(`${this.eventBaseUri}/matches`);
  }
}
