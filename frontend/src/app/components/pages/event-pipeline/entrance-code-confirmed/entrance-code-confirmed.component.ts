import {Component, OnInit} from '@angular/core';
import {EventService} from '../../../../services/event.service';
import {Router} from '@angular/router';
import {EVENT_STATUS_CHANGED_EVENT_NAME, EventStatus, EventStatusChanged} from '../../../../dtos/event';

@Component({
  selector: 'app-entrance-code-confirmed',
  templateUrl: './entrance-code-confirmed.component.html',
  styleUrls: ['./entrance-code-confirmed.component.scss']
})
export class EntranceCodeConfirmedComponent implements OnInit {
  showSummary = false;
  skipIntro = false;

  constructor(private eventService: EventService, private router: Router) {
  }

  ngOnInit(): void {
  }

  enterEvent(): void {
    this.skipIntro = true;
    this.eventService.subscribeEvents<EventStatusChanged>(EVENT_STATUS_CHANGED_EVENT_NAME, start => {
      if (start.status === EventStatus.ongoingButNoUpcomingRound) {
        this.router.navigate(['/event', 'participation', start.accessToken]).then(r => console.log('redirected to', r));
      }
    });
  }
}
