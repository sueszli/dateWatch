import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs';
import {Event} from 'src/app/dtos/event';
import {EventService} from 'src/app/services/event.service';
import {NotificationService} from 'src/app/services/notification.service';
import {EventFilter, FilterType} from '../../../../dtos/eventFilter/event-filter';

@Component({
  selector: 'app-planned-events',
  templateUrl: './planned-events.component.html',
  styleUrls: ['./planned-events.component.scss']
})
export class PlannedEventsComponent implements OnInit {

  events: Event[];

  today: Date = new Date();
  filterForm: FormGroup = this.formBuilder.group(
    {
      titleSubstring: [undefined, {updateOn: 'change'}],
      startDateAndTime: [undefined, {updateOn: 'change'}],
      endDateAndTime: [undefined, {updateOn: 'change'}],
      citySubstring: [undefined, {updateOn: 'change'}],
      organizerNameSubstring: [undefined, {updateOn: 'change'}],
      filterType: [FilterType.eventsParticipantPlanned],
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
    if (filter.startDateAndTime !== undefined && filter.startDateAndTime !== null) {
      filter.endDateAndTime = new Date(filter.endDateAndTime).toISOString();
    }
    // fetch public and private events
    this.eventService.getPlannedEvents(filter).subscribe({
      next: data => {
        this.events = data;
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }
}
