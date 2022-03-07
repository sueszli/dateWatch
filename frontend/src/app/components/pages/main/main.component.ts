import {Component, OnInit} from '@angular/core';
import {AuthService} from 'src/app/services/auth.service';
import {EventService} from 'src/app/services/event.service';
import {Router} from '@angular/router';
import {InputDialogComponent, InputDialogData} from '../../dialog/input-dialog/input-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '../../../services/notification.service';
import {Globals} from '../../../global/globals';
import {Event, EventStatus} from '../../../dtos/event';
import {ParticipantEvent} from '../../../dtos/participant-event';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {

  inputDialogData: InputDialogData;
  userRole = undefined;

  events: Event[] = [];

  constructor(
    public authService: AuthService,
    private eventService: EventService,
    private router: Router,
    private notificationService: NotificationService,
    public dialog: MatDialog) {
  }

  get roles() {
    return Globals.roles;
  }

  get confirmedEvents() {
    return (this.events as ParticipantEvent[]).filter(e => e.participation?.confirmed && e.status !== EventStatus.finished);
  }

  get unconfirmedEvents() {
    return (this.events as ParticipantEvent[]).filter(e => !e.participation?.confirmed);
  }

  get ongoingEvents() {
    return this.events.filter(e => e.hasRegistrationClosed);
  }

  ngOnInit() {
    this.userRole = localStorage.getItem('roles');
    this.inputDialogData = {
      label: 'Enter code here',
      text: 'Have you received an invite code?\nWoah! That sounds pretty awesome!',
      title: 'Invite Code',
      cancelButton: 'Cancel',
      enterButton: 'Enter'
    };
    this.refreshEvents();
  }

  openDialog() {
    // styling of dialog done here
    const dialogRef = this.dialog.open(InputDialogComponent, {
      data: this.inputDialogData,
      width: '350px'
    });

    dialogRef.afterClosed().subscribe(token => {
      if (token !== undefined) {
        this.findEventByInviteToken(token);
      }
    });
  }

  findEventByInviteToken(accessToken: string) {
    this.eventService.getEvent(accessToken).subscribe({
      next: _ => {
        this.router.navigate(['event/' + accessToken]);
      },
      error: _ => {
        this.notificationService.displayError('Could not find the entered invite code!');
      }
    });
  }

  refreshEvents() {
    this.eventService.getAllEvents().subscribe({
      next: data => {
        console.log('received events', data);
        this.events = data;
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }
}
