import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EventService} from '../../../services/event.service';
import {Event} from '../../../dtos/event';
import {Router} from '@angular/router';
import {NotificationService} from '../../../services/notification.service';
import {HttpErrorResponse} from '@angular/common/http';
import {CustomValidators, errorMessages} from '../../../validators/custom-validators';


@Component({
  selector: 'app-event-creation',
  templateUrl: './event-creation.component.html',
  styleUrls: ['./event-creation.component.scss']
})
export class EventCreationComponent implements OnInit {

  today: Date = new Date();
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

  eventCreationForm: FormGroup = this.formBuilder.group(
    {
      title: ['', {validators: [Validators.required], updateOn: 'change'}],
      description: ['', {validators: [Validators.required], updateOn: 'change'}],
      startDateAndTime: ['', {
        validators: [Validators.required, CustomValidators.dateMin(EventCreationComponent.minimumStartDate())],
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
      public: [false, {validators: [Validators.required], updateOn: 'change'}],
    });


  constructor(private formBuilder: FormBuilder,
              private eventService: EventService,
              private notificationService: NotificationService,
              private router: Router) {
  }

  private static minimumStartDate(): number {
    const now = new Date(Date.now());
    now.setDate(now.getDate() + 1);
    return now.setHours(0, 0, 0, 0);
  }

  ngOnInit(): void {
  }

  onCreateEventFormSubmit(): void {
    const creationValue: Event = this.eventCreationForm.value;
    creationValue.durationInMinutes = creationValue.durationInMinutes + this.eventCreationForm.value.durationInHours * 60;
    console.log('onFormSubmit()', creationValue);
    this.eventService.createEvent(creationValue).subscribe({
      next: data => {
        this.event = data;
        console.log('received', this.event);
        this.notificationService.displaySuccess('Event created!');
        this.router.navigate(['/'])
          .catch(reason => this.notificationService.displayError(reason.toString()));
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  changeGroupsExist(): void {
    console.log('change group exists', this.groupsExist);
    if (this.groupsExist) {
      this.eventCreationForm.removeControl('groups');
    } else {
      this.eventCreationForm.registerControl('groups', this.groupsSubForm);
      this.eventCreationForm.updateValueAndValidity();
    }
  }
}
