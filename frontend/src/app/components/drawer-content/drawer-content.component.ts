import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { ProfileService } from 'src/app/services/profile.service';
import {OrganizerAccount} from '../../dtos/organizer-account';
import {ParticipantAccount} from '../../dtos/participant-account';

@Component({
  selector: 'app-drawer-content',
  templateUrl: './drawer-content.component.html',
  styleUrls: ['./drawer-content.component.scss']
})
export class DrawerContentComponent implements OnInit {

  public participant = false;
  public organizer = false;
  public name = null;

  constructor(
    public authService: AuthService,
    public profileService: ProfileService
  ) { }

  ngOnInit() {
    this.profileService.getProfile().subscribe((p) => {
      if (p.accountType === 'organizer') {
        this.organizer = true;
        this.name = (p as OrganizerAccount).contactPersonFirstName + ' ' + (p as OrganizerAccount).contactPersonLastName;
      } else if (p.accountType === 'participant') {
        this.participant = true;
        this.name = (p as ParticipantAccount).nickname;
      }
    });
  }
}
