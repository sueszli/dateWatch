import {Component, OnInit} from '@angular/core';
import {EventService} from '../../../services/event.service';
import {ActivatedRoute} from '@angular/router';
import {AuthService} from '../../../services/auth.service';
import {Globals} from '../../../global/globals';
import {SelectDialogComponent, SelectDialogData} from '../../dialog/select-dialog/select-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '../../../services/notification.service';
import {Event, EventStatus} from '../../../dtos/event';
import {OrganizerEvent} from '../../../dtos/organizer-event';
import {Observable} from 'rxjs';
import {ParticipantEvent} from '../../../dtos/participant-event';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {ProfileService} from '../../../services/profile.service';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../dialog/confirm-dialog/confirm-dialog.component';
import {PostStatisticsDialogComponent} from '../../dialog/post-statistics-dialog/post-statistics-dialog.component';


@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.scss']
})
export class EventDetailsComponent implements OnInit {
  math = Math;
  accessToken = undefined;
  event: Event = undefined;
  organizer: OrganizerAccount;
  userRole = undefined;
  isFinished = false;
  selectDialogData: SelectDialogData;
  confirmDialogData: ConfirmDialogData;
  cancelEventConfirmDialogData: ConfirmDialogData;

  private registrationSubscription = {
    next: _ => {
      this.notificationService.displaySuccess('You registered for this event.\n' +
        'Your registration is not yet confirmed though!');
      this.refreshEvent();
    },
    error: err => {
      this.notificationService.displayHttpError(err);
    }
  };

  constructor(private eventService: EventService,
              private authService: AuthService,
              private profileService: ProfileService,
              private notificationService: NotificationService,
              private route: ActivatedRoute,
              public dialog: MatDialog) {
  }

  get roles() {
    return Globals.roles;
  }

  get organizerEvent(): OrganizerEvent {
    return this.event as OrganizerEvent;
  }

  get participantEvent(): ParticipantEvent {
    return this.event as ParticipantEvent;
  }

  ngOnInit(): void {
    this.userRole = localStorage.getItem('roles');
    this.route.params.subscribe(params => {
      this.accessToken = params['accessToken'];
      this.refreshEvent();
    });
    if (this.userRole.includes(this.roles.organizer)) {
      this.profileService.getProfile<OrganizerAccount>().subscribe({
        next: value => this.organizer = value,
        error: err => this.notificationService.displayHttpError(err)
      });
    }
  }

  register() {
    if (this.event.groups) {
      this.chooseGroup();
    } else {
      this.eventService.registerForEvent(this.accessToken).subscribe(this.registrationSubscription);
    }
  }

  deregister() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: this.confirmDialogData,
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.eventService.deregisterForEvent(this.accessToken).subscribe({
          next: _ => this.refreshEvent(),
          error: err => this.notificationService.displayHttpError(err)
        });
      }
    });
  }

  closeRegistration() {
    this.eventService.closeRegistration(this.accessToken).subscribe({
        next: _ => this.refreshEvent(),
        error: err => this.notificationService.displayHttpError(err)
      }
    );
  }

  cancelEvent() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '300px',
      data: this.cancelEventConfirmDialogData,
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.eventService.cancelEvent(this.accessToken).subscribe({
            next: _ => this.refreshEvent(),
            error: err => this.notificationService.displayHttpError(err)
          }
        );
      }
    });
  }

  registrationOpen(): boolean {
    return this.event.status === EventStatus.registrationOpen;
  }

  postStatistics(): void {
    this.eventService.getPostStatistics(this.accessToken).subscribe({
      next: value => {
        this.dialog.open(PostStatisticsDialogComponent, {
          width: '300px',
          data: value,
        });
      },
      error: err => this.notificationService.displayError(err)
    });
  }

  private chooseGroup() {
    const dialogRef = this.dialog.open(SelectDialogComponent, {
      width: '350px',
      data: this.selectDialogData,
    });

    dialogRef.afterClosed().subscribe(chosenGroupTitle => {
      if (chosenGroupTitle) {
        this.eventService.registerForEvent(this.accessToken, chosenGroupTitle).subscribe(this.registrationSubscription);
      }
    });
  }

  private refreshEvent() {
    console.log('refresh event');
    const observer: Observable<Event> =
      this.userRole.includes(this.roles.participant)
        ? this.eventService.getEvent<ParticipantEvent>(this.accessToken)
        : this.eventService.getEvent<OrganizerEvent>(this.accessToken);
    observer.subscribe({
      next: event => {
        console.log('received event', event);
        this.event = event;
        if (this.event.status === EventStatus.finished) {
          this.isFinished = true;
        }
        this.updateGroupSelectionDialogData();
        this.updateDeregistrationConfirmDialogData();
        this.updateCancelEventDialogData();
        const participantEvent = event as ParticipantEvent;
        if (participantEvent.organizer) {
          this.organizer = participantEvent.organizer;
        }
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private updateGroupSelectionDialogData() {
    if (this.event.groups) {
      this.selectDialogData = {
        label: 'Group',
        options: [this.event.groups.firstGroup.title, this.event.groups.secondGroup.title],
        text: 'Please note that the registration for the event is not yet final, as the organizer ' +
          'wants to ensure that the groups are evenly distributed.',
        title: 'Group selection'
      };
    }
  }

  private updateDeregistrationConfirmDialogData() {
    this.confirmDialogData = {
      text: 'If you unsubscribe, it is maybe not possible to register again when the registration is already closed.',
      title: 'Are you sure?',
      buttonTextTrue: 'UNSUBSCRIBE',
      buttonTextFalse: 'CANCEL'
    };
  }

  private updateCancelEventDialogData() {
    this.cancelEventConfirmDialogData = {
      text: 'Do you really want to cancel this event?',
      title: 'Are you sure?',
      buttonTextTrue: 'YES',
      buttonTextFalse: 'NO'
    };
  }
}
