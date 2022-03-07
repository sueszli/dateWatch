import {Component, OnInit} from '@angular/core';
import {Account} from '../../../dtos/account';
import {Globals} from '../../../global/globals';
import {AuthService} from '../../../services/auth.service';
import {NotificationService} from '../../../services/notification.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  userRole = undefined;

  organizerAccounts: Account[] = [];
  participantAccounts: Account[] = [];

  constructor(
    public authService: AuthService,
    private notificationService: NotificationService) {
  }

  get roles() {
    return Globals.roles;
  }

  ngOnInit(): void {
    this.getAllAccounts();
  }

  getAllAccounts() {
    this.authService.getAllAccounts().subscribe({
      next: data => {
        console.log('received accounts', data);
        this.organizerAccounts = data.filter(d => d.accountType === 'organizer');
        this.participantAccounts = data.filter(d => d.accountType === 'participant');
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

}
