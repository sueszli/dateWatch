import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {Event, EventStatus} from '../../dtos/event';
import {OrganizerAccount} from '../../dtos/organizer-account';
import {ParticipantEvent} from '../../dtos/participant-event';
import {Globals} from '../../global/globals';

@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['./event-card.component.scss']
})
export class EventCardComponent implements OnInit, OnChanges {

  @Input() event: Event;
  @Input() showIamHereButton = true;
  eventAddress: string;
  isOngoing = false;
  isFinished = false;
  isCanceled = false;
  organizer: OrganizerAccount;
  userRole = localStorage.getItem('roles');

  constructor() {
  }

  get roles() {
    return Globals.roles;
  }

  get participantEvent() {
    return this.event as ParticipantEvent;
  }

  ngOnInit(): void {
  }


  parseAddress(): void {
    this.eventAddress = `${this.event.street}, ${this.event.postcode} ${this.event.city}`;
  }

  ngOnChanges(): void {
    this.parseAddress();
    this.showParticipate();
    const participantEvent = this.event as ParticipantEvent;
    if (participantEvent.organizer) {
      this.organizer = participantEvent.organizer;
    }
  }

  showParticipate(): void {
    const currentTime = new Date().getTime();
    // time of event starting minus 1 hour.
    const parsedEventStartTime = new Date(this.event.startDateAndTime).getTime() - (60 * 60 * 1000);
    const parsedEventFinishTime = new Date(this.event.startDateAndTime).getTime() + (this.event.durationInMinutes * 60 * 1000);

    if (this.event.status === EventStatus.finished) {
      this.isFinished = true;
    } else if (this.event.status === EventStatus.canceled) {
      this.isCanceled = true;
    } else if (currentTime > parsedEventStartTime &&
      currentTime < parsedEventFinishTime) {
      this.isOngoing = true;
    }
  }
}
