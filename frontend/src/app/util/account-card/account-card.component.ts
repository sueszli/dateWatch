import {Component, Input, OnInit} from '@angular/core';
import {Globals} from '../../global/globals';
import {OrganizerAccount} from '../../dtos/organizer-account';
import {ParticipantAccount} from '../../dtos/participant-account';
import {MatDialog} from '@angular/material/dialog';
import {AdminDialogComponent, AdminDialogData} from '../../components/dialog/admin-dialog/admin-dialog.component';
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from '../../components/dialog/confirm-dialog/confirm-dialog.component';
import {ProfileService} from '../../services/profile.service';
import {NotificationService} from '../../services/notification.service';
import {ToggleBanAccount} from '../../dtos/toggle-ban-account';

@Component({
  selector: 'app-account-card',
  templateUrl: './account-card.component.html',
  styleUrls: ['./account-card.component.scss']
})
export class AccountCardComponent implements OnInit {

  @Input() account;
  organizerAccount: OrganizerAccount;
  participantAccount: ParticipantAccount;

  isOrganizer: boolean;

  adminDialogData: AdminDialogData;
  confirmDialogData: ConfirmDialogData;

  constructor(public dialog: MatDialog,
              private notificationService: NotificationService,
              private profileService: ProfileService) {
  }

  get roles() {
    return Globals.roles;
  }

  ngOnInit(): void {
    this.isOrganizer = !!this.account.accountType.includes('organizer');

    if (this.isOrganizer) {
      this.organizerAccount = this.account;
      console.log('orga: ', this.organizerAccount);
    } else {
      this.participantAccount = this.account;
      console.log(this.participantAccount);
    }
    this.adminDialogData = {
      accountType: this.isOrganizer ?
        this.organizerAccount.accountType :
        this.participantAccount.accountType,
      buttonTextOne: 'unban account',
      buttonTextTwo: 'ban account (temporary)',
      email: this.isOrganizer ?
        this.organizerAccount.email :
        this.participantAccount.email,
      name: this.isOrganizer ?
        this.organizerAccount.contactPersonFirstName + this.organizerAccount.contactPersonLastName :
        this.participantAccount.nickname,
      banned: this.account.banned,
      banReason: this.account.banReason,
      deactivated: this.isOrganizer ? this.organizerAccount.deactivated : false
    };
  }

  openDialog() {
    // styling of dialog done here
    const dialogRef = this.dialog.open(AdminDialogComponent, {
      data: this.adminDialogData,
      width: '350px',
      panelClass: 'admin-dialog'
    });

    dialogRef.afterClosed().subscribe(action => {
      if (action !== undefined) {
        console.log(action);
        if (action) {
          // ban account
          this.confirmDialogData = {
            text: 'You are about to ban this user. A ban can be abolished by any administrator.',
            title: 'Are you sure?',
            buttonTextTrue: 'BAN',
            buttonTextFalse: 'CANCEL'
          };
          // safety confirm dialog
          const dialogConfirmRef = this.dialog.open(ConfirmDialogComponent, {
            width: '350px',
            data: this.confirmDialogData,
          });
          dialogConfirmRef.afterClosed().subscribe(result => {
            if (result) {
              // ban this account
              console.log('ban this account');
              this.account.banned = !this.account.banned;
              this.account.banReason = 'manually banned by admin';
              this.adminDialogData.banned = !this.adminDialogData.banned;
              this.adminDialogData.banReason = 'manually banned by admin';

              const togglebanaccount: ToggleBanAccount = {
                email: this.account.email,
                banReason: this.account.banReason
              };
              this.profileService.toggleBanStatus(togglebanaccount).subscribe(_ => {
                this.notificationService.displaySuccess('Successfully banned user!');
              });
            }
          });
        } else {
          // unban account
          console.log('unban this account');
          this.account.banned = !this.account.banned;
          this.adminDialogData.banned = !this.adminDialogData.banned;
          this.adminDialogData.banReason = null;
          const togglebanaccount: ToggleBanAccount = {
            email: this.account.email,
            banReason: null
          };
          this.profileService.toggleBanStatus(togglebanaccount).subscribe(_ => {
            this.notificationService.displaySuccess('Successfully unbanned user!');
          });
        }
      }
    });
  }
}
