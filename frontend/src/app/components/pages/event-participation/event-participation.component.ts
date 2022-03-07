import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {NotificationService} from '../../../services/notification.service';
import {interval, Subscription, take} from 'rxjs';
import {
  ParticipantEvent,
  PARTICIPATION_STATUS_CHANGED_EVENT_NAME,
  ParticipationStatus,
  ParticipationStatusChanged
} from '../../../dtos/participant-event';
import {EVENT_STATUS_CHANGED_EVENT_NAME, EventStatus, EventStatusChanged} from '../../../dtos/event';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogComponent} from '../../dialog/confirm-dialog/confirm-dialog.component';
import {InputDialogComponent} from '../../dialog/input-dialog/input-dialog.component';
import {ProfileService} from '../../../services/profile.service';


@Component({
  selector: 'app-event-participation',
  templateUrl: './event-participation.component.html',
  styleUrls: ['./event-participation.component.scss']
})
export class EventParticipationComponent implements OnInit, OnDestroy {

  event: ParticipantEvent;
  pairingToken: string;
  otherPersonsNickname: string;
  otherPersonsPairingToken: string;
  eventStatus: EventStatus;
  participationStatus: ParticipationStatus;
  remainingRoundTimeInPercent = 100;
  remainingRoundMinutes: number;
  remainingRoundSecondsAfterFullMinute: number;

  private accessToken: string;
  private roundTimerSubscription: Subscription;

  private numberOfActualizations = 100;


  constructor(private route: ActivatedRoute,
              private dialog: MatDialog,
              private router: Router,
              private eventService: EventService,
              private notificationService: NotificationService,
              private profileService: ProfileService) {
  }


  get waitingBlock() {
    return this.eventStatus === EventStatus.ongoingButNoUpcomingRound;
  }

  get pairingBlock() {
    return this.eventStatus === EventStatus.upcomingRoundAboutToStart;
  }

  get paired() {
    return this.participationStatus === ParticipationStatus.atEventPaired;
  }

  get closed() {
    return this.eventStatus === EventStatus.finished;
  }

  get roundOngoing() {
    return this.eventStatus === EventStatus.roundOngoing;
  }

  get roundOngoingWithPartner() {
    return this.eventStatus === EventStatus.roundOngoing && this.participationStatus === ParticipationStatus.atEventPaired;
  }


  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessToken = params['accessToken'];
      this.refreshEvent();
    });
    this.subscribeEventStatus();
    this.subscribeParticipationStatus();
    this.refreshParticipationStatus();
  }

  ngOnDestroy(): void {
    this.ensureRoundTimerUnsubscribed();
  }

  openDialog() {
    // styling of dialog done here
    const dialogRef = this.dialog.open(InputDialogComponent, {
      width: '300px',
      disableClose: true,
      data: {
        label: 'Your feedback here',
        text: 'Do you want to help the organizer improving his/her next events? Then please give some feedback!',
        title: 'Feedback',
        cancelButton: 'No thanks',
        enterButton: 'Send'
      }
    });

    dialogRef.afterClosed().subscribe(message => {
      if (message !== undefined) {
        this.eventService.giveFeedback({eventAccessToken: this.event.accessToken, message}).subscribe({
          next: _ => {
            this.finishEvent();
          },
          error: _ => {
            // no error if feedback fails
            this.finishEvent();
          }
        });
      } else {
        this.finishEvent();
      }
    });
  }

  finishEvent(): void {
    this.eventService.closeEvent(this.accessToken).subscribe({
      next: _ => this.router.navigate(['/']),
      error: err => this.notificationService.displayHttpError(err)
    });
  }


  sendPairingToken(token: string): void {
    console.log('request pairing', token);
    this.eventService.enterPairing(this.accessToken, token).subscribe({
      next: _ => {
        console.log('pairing request did not throw any error');
      },
      error: err => this.notificationService.displayHttpError(err, {
        403: 'You are not allowed to date yourself',
        409: 'The person you are trying to request as a partner is already the partner of another person',
        428: 'You may only date people from the other group'
      })
    });
  }

  terminatePairing(): void {
    this.eventService.terminatePairing(this.accessToken).subscribe({
      next: _ => {
        console.log('terminated pairing');
      }, error: err => this.notificationService.displayHttpError(err)
    });
  }

  private refreshEvent(): void {
    console.log('refresh event');
    this.eventService.getEvent<ParticipantEvent>(this.accessToken).subscribe({
      next: event => {
        console.log('received event', event);
        this.event = event;
        this.eventStatus = event.status;
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private subscribeEventStatus() {
    this.eventService.subscribeEvents<EventStatusChanged>(EVENT_STATUS_CHANGED_EVENT_NAME, statusChanged => {
      console.log('status of the event changed', statusChanged);
      this.eventStatus = statusChanged.status;
      if (statusChanged.pairingToken) {
        this.pairingToken = statusChanged.pairingToken;
      }
      if (this.roundOngoing) {
        this.handleRound(new Date(statusChanged.firedAt), this.otherPersonsPairingToken);
      }
    });
  }

  private subscribeParticipationStatus(): void {
    console.log('subscribe to participation status');
    this.eventService.subscribeEvents<ParticipationStatusChanged>(PARTICIPATION_STATUS_CHANGED_EVENT_NAME, statusChanged => {
      console.log('update participation status to', statusChanged);
      this.participationStatus = statusChanged.status;
      this.otherPersonsNickname = statusChanged.otherPersonsNickname;
      this.otherPersonsPairingToken = statusChanged.otherPersonsPairingToken;
    });
  }

  private refreshParticipationStatus(): void {
    console.log('refresh participation status');
    this.profileService.participationStatus().subscribe({
      next: status => {
        console.log('update participation status to', status);
        this.pairingToken = status.ownPairingToken;
        this.participationStatus = status.status;
        this.otherPersonsNickname = status.otherPersonsNickname;
        this.otherPersonsPairingToken = status.otherPersonsPairingToken;
        console.log('status', status);
        if (status.roundStartedAt && this.roundOngoing) {
          this.handleRound(new Date(status.roundStartedAt), this.otherPersonsPairingToken);
        }
      }, error: err => this.notificationService.displayHttpError(err)
    });
  }

  private handleRound(roundStartedAt: Date, otherPersonsPairingToken: string): void {
    this.ensureRoundTimerUnsubscribed();

    const roundDurationInMs = 1000 * this.event.roundDurationInSeconds;
    const msBetweenRoundAndTimerStart = new Date().getTime() - roundStartedAt.getTime();
    const msRemainingAtTimerStart = roundDurationInMs - msBetweenRoundAndTimerStart;

    const updateIntervalInMs = msRemainingAtTimerStart / this.numberOfActualizations;

    const remainingRoundSeconds = this.event.roundDurationInSeconds - 1000 * msBetweenRoundAndTimerStart;
    this.remainingRoundMinutes = Math.floor(remainingRoundSeconds / 60);
    this.remainingRoundSecondsAfterFullMinute = remainingRoundSeconds % 60;
    this.remainingRoundTimeInPercent = 100 * (msBetweenRoundAndTimerStart / roundDurationInMs);

    this.roundTimerSubscription = interval(updateIntervalInMs)
      .pipe(take(this.numberOfActualizations))
      .subscribe(counter => {
        console.log('HI');
        const msPassedSinceTimerStart = (counter + 1) * updateIntervalInMs;
        const msPassedSinceRoundStart = msPassedSinceTimerStart + msBetweenRoundAndTimerStart;
        const roundProgressPercent = 100 * (msPassedSinceRoundStart / roundDurationInMs);
        this.remainingRoundTimeInPercent = Math.floor(100 - roundProgressPercent);

        const passedSeconds = (roundDurationInMs - msPassedSinceRoundStart) / 1000;
        this.remainingRoundMinutes = Math.floor(passedSeconds / 60);
        this.remainingRoundSecondsAfterFullMinute = Math.floor(passedSeconds) % 60;

        if ((counter + 1) === this.numberOfActualizations && otherPersonsPairingToken) {
          this.openMatchDialog(otherPersonsPairingToken);
        }
      });
  }

  private ensureRoundTimerUnsubscribed() {
    if (this.roundTimerSubscription) {
      this.roundTimerSubscription.unsubscribe();
      this.roundTimerSubscription = null;
    }
  }

  private openMatchDialog(otherPersonsPairingToken: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '300px',
      disableClose: true,
      data: {
        text: 'Do you want to exchange contact infos with this person after the event?\n' +
          'Don\'t worry, they won\'t be informed about your decision until after the event.',
        title: 'Is it a match?',
        buttonTextTrue: 'YES',
        buttonTextFalse: 'NO'
      }
    });

    dialogRef.afterClosed().subscribe(approvesPairing => {
      if (approvesPairing) {
        this.eventService.approvePairing(this.accessToken, otherPersonsPairingToken).subscribe({
            next: _ => this.notificationService.displayInfo(
              'After the event you will be informed if the other person feels the same way :)',
              'You\'d like to repeat this'),
            error: err => this.notificationService.displayHttpError(err)
          }
        );
      } else {
        this.notificationService.displayInfo(
          'That\'s fine. The other person won\'t be informed until after the event.',
          'You\'d rather leave it at that');
      }
    });
  }
}
