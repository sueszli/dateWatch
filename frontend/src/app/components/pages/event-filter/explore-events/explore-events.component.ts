import {Component, OnInit} from '@angular/core';
import {Event, EventStatus} from '../../../../dtos/event';
import {FormBuilder, FormGroup} from '@angular/forms';
import {EventService} from '../../../../services/event.service';
import {NotificationService} from '../../../../services/notification.service';
import {debounceTime, distinctUntilChanged} from 'rxjs';
import {EventFilter, FilterType} from '../../../../dtos/eventFilter/event-filter';

@Component({
  selector: 'app-explore-events',
  templateUrl: './explore-events.component.html',
  styleUrls: ['./explore-events.component.scss']
})
export class ExploreEventsComponent implements OnInit {

  events: Event[];

  today: Date = new Date();
  filterForm: FormGroup = this.formBuilder.group(
    {
      titleSubstring: [undefined, {updateOn: 'change'}],
      startDateAndTime: [undefined, {updateOn: 'change'}],
      endDateAndTime: [undefined, {updateOn: 'change'}],
      citySubstring: [undefined, {updateOn: 'change'}],
      organizerNameSubstring: [undefined, {updateOn: 'change'}],
      filterType: [FilterType.eventsParticipantPublic],
    }
  );

  constructor(
    private formBuilder: FormBuilder,
    private eventService: EventService,
    private notificationService: NotificationService,
  ) {
  }

  ngOnInit(): void {
    this.fetchEvents(this.filterForm.value);

    this.filterForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(data => {
      console.log('Filter', data);
      this.fetchEvents(data);
    });
  }

  onSearch(): void {
    const filter: EventFilter = this.filterForm.value;
    console.log('Filter', filter);

    this.fetchEvents(filter);
  }

  fetchEvents(filter: EventFilter): void {
    if (filter.startDateAndTime !== undefined && filter.startDateAndTime !== null) {
      filter.startDateAndTime = new Date(filter.startDateAndTime).toISOString();
    }
    if (filter.endDateAndTime !== undefined && filter.endDateAndTime !== null) {
      filter.endDateAndTime = new Date(filter.endDateAndTime).toISOString();
    }
    // fetch public events
    filter.isPublic = true;
    this.eventService.getPublicEvents(filter).subscribe({
      next: data => {
        this.events = data.filter(event => event.status !== EventStatus.registrationClosed);
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }
}
