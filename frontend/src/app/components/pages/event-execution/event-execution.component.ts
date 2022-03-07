import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {
  ARRIVAL_STATISTICS_EVENT_NAME,
  ArrivalStatistics,
  OrganizerEvent,
  PAIRING_STATISTICS_EVENT,
  PairingStatistics
} from '../../../dtos/organizer-event';
import {NotificationService} from '../../../services/notification.service';
import {EVENT_STATUS_CHANGED_EVENT_NAME, EventStatus, EventStatusChanged} from '../../../dtos/event';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../dialog/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-event-execution',
  templateUrl: './event-execution.component.html',
  styleUrls: ['./event-execution.component.scss']
})
export class EventExecutionComponent implements OnInit {

  event: OrganizerEvent;
  eventStatus: EventStatus;
  arrivalStatistics: ArrivalStatistics;
  pairingStatistics: PairingStatistics;
  private accessToken: string;

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private notificationService: NotificationService,
    private dialog: MatDialog) {
  }

  get readyForNewRound() {
    return this.eventStatus === EventStatus.ongoingButNoUpcomingRound;
  }

  get readyForStart() {
    return this.eventStatus === EventStatus.upcomingRoundAboutToStart;
  }

  get roundOngoing() {
    return this.eventStatus === EventStatus.roundOngoing;
  }

  get closed() {
    return this.eventStatus === EventStatus.finished;
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessToken = params['accessToken'];
      this.refreshEvent();
      this.refreshArrivalStatistics();
    });
    this.subscribeEventStatus();
    this.subscribeArrivalStatistics();
    this.subscribePairingStatistics();
  }

  newRound(): void {
    this.pairingStatistics = null;
    this.eventService.prepareNewRound(this.accessToken).subscribe({
      next: _ => console.log('started round preparation - participants can now construct pairs'),
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  startRound(): void {
    this.eventService.startNextRound(this.accessToken).subscribe({
      next: _ => console.log('started round - participants should now talk with each other'),
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  closeEvent(): void {
    console.log('close event');
    const dialogData: ConfirmDialogData = {
      text: 'Are you sure you want to end the event?\nParticipants will not be able to participate any longer!',
      title: 'End Event',
      buttonTextTrue: 'End',
      buttonTextFalse: 'Cancel'
    };
    this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {data: dialogData})
      .afterClosed().subscribe({
      next: confirm => {
        if (confirm) {
          this.eventService.closeEvent(this.accessToken).subscribe({error: err => this.notificationService.displayHttpError(err)});
        }
      }
    });
  }

  private refreshEvent(): void {
    console.log('refresh event');
    this.eventService.getEvent<OrganizerEvent>(this.accessToken).subscribe({
      next: event => {
        console.log('received event', event);
        this.event = event;
        this.eventStatus = event.status;
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private refreshArrivalStatistics(): void {
    this.eventService.getArrivals(this.accessToken).subscribe({
      next: statistics => {
        console.log('received arrival statistics', statistics);
        this.arrivalStatistics = statistics;
        console.log('assigned', this.arrivalStatistics);
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private subscribeEventStatus(): void {
    this.eventService.subscribeEvents<EventStatusChanged>(EVENT_STATUS_CHANGED_EVENT_NAME, status => {
      console.log('received event status', status);
      this.eventStatus = status.status;
    });
  }

  private subscribeArrivalStatistics(): void {
    this.eventService.subscribeEvents<ArrivalStatistics>(ARRIVAL_STATISTICS_EVENT_NAME, statistics => {
      console.log('received arrival statistics', statistics);
      this.arrivalStatistics = statistics;
      console.log('assigned', this.arrivalStatistics);
    });
  }

  private subscribePairingStatistics(): void {
    this.eventService.subscribeEvents<PairingStatistics>(PAIRING_STATISTICS_EVENT, statistics => {
      console.log('received pairing statistics', statistics);
      this.pairingStatistics = statistics;
    });
  }
}
