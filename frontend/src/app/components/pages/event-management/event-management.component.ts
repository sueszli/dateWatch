import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {EventService} from '../../../services/event.service';
import {ARRIVAL_STATISTICS_EVENT_NAME, ArrivalStatistics, OrganizerEvent} from '../../../dtos/organizer-event';
import {NotificationService} from '../../../services/notification.service';

@Component({
  selector: 'app-event-management',
  templateUrl: './event-management.component.html',
  styleUrls: ['./event-management.component.scss']
})
export class EventManagementComponent implements OnInit {

  accessToken: string = undefined;
  event: OrganizerEvent = undefined;
  arrivalStatistics: ArrivalStatistics = undefined;
  eventSource: EventSource = undefined;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private eventService: EventService,
              private notificationService: NotificationService) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessToken = params['accessToken'];
      this.refreshEvent();
    });
  }

  startEvent(): void {
    console.log('start event');
    this.eventService.startEvent(this.accessToken).subscribe({
      next: _ => {
        this.router.navigate(['/event', 'execution', this.accessToken]).then(r => console.log('redirected to', r));
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private refreshEvent(): void {
    this.eventService.getEvent<OrganizerEvent>(this.accessToken).subscribe({
      next: data => {
        this.event = data;
        console.log('received event', data);
        this.refreshArrivalStatistics();
        this.subscribeArrivalStatistics();
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private refreshArrivalStatistics(): void {
    this.eventService.getArrivals(this.event.accessToken).subscribe({
      next: arrivalStatistics => {
        this.arrivalStatistics = arrivalStatistics;
        console.log('received arrival statistics', this.arrivalStatistics);
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  private subscribeArrivalStatistics(): void {
    this.eventService.subscribeEvents<ArrivalStatistics>(ARRIVAL_STATISTICS_EVENT_NAME, stats => {
      console.log('updated arrival statistics', stats);
      this.arrivalStatistics = stats;
    });
  }
}
