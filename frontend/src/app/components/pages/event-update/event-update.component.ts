import {Component, OnInit} from '@angular/core';
import {Event} from '../../../dtos/event';
import {HttpErrorResponse} from '@angular/common/http';
import {CustomValidators, errorMessages} from '../../../validators/custom-validators';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EventService} from '../../../services/event.service';
import {NotificationService} from '../../../services/notification.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OrganizerEvent} from '../../../dtos/organizer-event';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-event-update',
  templateUrl: './event-update.component.html',
  styleUrls: ['./event-update.component.scss']
})
export class EventUpdateComponent implements OnInit {

  today: Date = new Date();
  accessToken = undefined;
  event: Event = undefined;
  error: HttpErrorResponse = undefined;
  groupsExist = true;
  formErrors = errorMessages;

  groupsSubForm = this.formBuilder.group({
    firstGroup: this.formBuilder.group({
      title: ['Male', {updateOn: 'change'}],
      description: ['Group of all males.', {validators: [Validators.required], updateOn: 'change'}]
    }),
    secondGroup: this.formBuilder.group({
      title: ['Female', {validators: [Validators.required], updateOn: 'change'}],
      description: ['Group of all females.', {validators: [Validators.required], updateOn: 'change'}]
    }),
  });

  eventUpdatingForm: FormGroup = this.formBuilder.group(
    {
      title: ['', {validators: [Validators.required], updateOn: 'change'}],
      description: ['', {validators: [Validators.required], updateOn: 'change'}],
      startDateAndTime: ['', {
        validators: [Validators.required, CustomValidators.dateMin(EventUpdateComponent.minimumStartDate())],
        updateOn: 'change'
      }],
      durationInMinutes: [0, {validators: [Validators.required], updateOn: 'change'}],
      durationInHours: [1, {validators: [Validators.required], updateOn: 'change'}],
      roundDurationInSeconds: [180, {validators: [Validators.required, Validators.min(1)], updateOn: 'change'}],
      street: ['', {validators: [Validators.required], updateOn: 'change'}],
      postcode: ['', {validators: [Validators.required], updateOn: 'change'}],
      city: ['', {validators: [Validators.required], updateOn: 'change'}],
      maxParticipants: [0, {validators: [Validators.required, CustomValidators.isEven()], updateOn: 'change'}],
      groups: this.groupsSubForm,
    });


  constructor(private formBuilder: FormBuilder,
              private eventService: EventService,
              private notificationService: NotificationService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  private static minimumStartDate(): number {
    const now = new Date(Date.now());
    now.setDate(now.getDate() + 1);
    return now.setHours(0, 0, 0, 0);
  }

  ngOnInit(): void {
    this.loadEvent();
  }

  loadEvent(): void {
    this.route.params.subscribe(params => {
      this.accessToken = params['accessToken'];
      const observer: Observable<Event> = this.eventService.getEvent<OrganizerEvent>(this.accessToken);
      observer.subscribe({
        next: event => {
          console.log('received event', event);
          this.event = event;
          this.groupsExist = this.event.groups !== null;
          if (this.groupsExist) {
            this.eventUpdatingForm.registerControl('groups', this.groupsSubForm);
            this.loadGroups();
            this.eventUpdatingForm.updateValueAndValidity();
            this.loadForm();
          } else {
            this.loadForm();
            this.eventUpdatingForm.removeControl('groups');
          }
        },
        error: err => this.notificationService.displayHttpError(err)
      });
    });
  }

  loadForm(): void{
    this.eventUpdatingForm = this.formBuilder.group(
      {
        title: [this.event.title, {validators: [Validators.required], updateOn: 'change'}],
        description: [this.event.description, {validators: [Validators.required], updateOn: 'change'}],
        startDateAndTime: [this.event.startDateAndTime, {
          validators: [Validators.required, CustomValidators.dateMin(EventUpdateComponent.minimumStartDate())],
          updateOn: 'change'
        }],
        durationInMinutes: [this.getMin(this.event.durationInMinutes), {validators: [Validators.required], updateOn: 'change'}],
        durationInHours: [this.getHour(this.event.durationInMinutes), {validators: [Validators.required], updateOn: 'change'}],
        roundDurationInSeconds: [this.event.roundDurationInSeconds, {
          validators: [Validators.required, Validators.min(1)],
          updateOn: 'change'
        }],
        street: [this.event.street, {validators: [Validators.required], updateOn: 'change'}],
        postcode: [this.event.postcode, {validators: [Validators.required], updateOn: 'change'}],
        city: [this.event.city, {validators: [Validators.required], updateOn: 'change'}],
        maxParticipants: [this.event.maxParticipants, {validators: [Validators.required, CustomValidators.isEven()], updateOn: 'change'}],
        groups: this.groupsSubForm,
      });
    this.eventUpdatingForm.updateValueAndValidity();
  }

  loadGroups(): void{
    this.groupsSubForm = this.formBuilder.group({
      firstGroup: this.formBuilder.group({
        title: [this.event.groups.firstGroup.title, {updateOn: 'change'}],
        description: [this.event.groups.firstGroup.description, {validators: [Validators.required], updateOn: 'change'}]
      }),
      secondGroup: this.formBuilder.group({
        title: [this.event.groups.secondGroup.title, {validators: [Validators.required], updateOn: 'change'}],
        description: [this.event.groups.secondGroup.description, {validators: [Validators.required], updateOn: 'change'}]
      }),
    });
  }

  onUpdateEventFormSubmit(): void {
    const updateValue: Event = this.eventUpdatingForm.value;
    updateValue.durationInMinutes = updateValue.durationInMinutes + this.eventUpdatingForm.value.durationInHours * 60;
    console.log('onFormSubmit()', updateValue);
    updateValue.public = this.event.public;
    this.eventService.updateEvent(updateValue, this.accessToken).subscribe({
      next: data => {
        this.event = data;
        console.log('received', this.event);
        this.notificationService.displaySuccess('Event updated!');
        this.router.navigate(['/'])
          .catch(reason => this.notificationService.displayError(reason.toString()));
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  getMin(duration: number): number{
    return duration%60;
  }

  getHour(duration: number): number{
    return Math.floor(duration/60);
  }
}
