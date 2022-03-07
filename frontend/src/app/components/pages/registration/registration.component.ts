import {Component} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {AuthService} from '../../../services/auth.service';
import {ParticipantAccount} from '../../../dtos/participant-account';
import {EventService} from 'src/app/services/event.service';
import {NotificationService} from '../../../services/notification.service';
import {Router} from '@angular/router';
import {Account} from '../../../dtos/account';
import {
  ConfirmValidParentMatcher,
  CustomValidators,
  errorMessages,
  regExps
} from '../../../validators/custom-validators';


@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent {

  hide = true;
  hidePwdConfirm = true;
  registered = false;
  acceptedTermsOrganizer = false;
  acceptedTermsParticipant = false;

  organizerAccount: OrganizerAccount = undefined;
  participantAccount: ParticipantAccount = undefined;

  passwordMatcher = new ConfirmValidParentMatcher();
  formErrors = errorMessages;

  organizerRegisterForm: FormGroup = this.formBuilder.group(
    {
      organizationName: ['', {updateOn: 'change'}],
      contactPersonFirstName: ['', {validators: [Validators.required], updateOn: 'change'}],
      contactPersonLastName: ['', {validators: [Validators.required], updateOn: 'change'}],
      email: ['', {validators: [Validators.required, Validators.email], updateOn: 'change'}],
      passwordGroup: this.formBuilder.group({
        password: ['', {validators: [Validators.required, Validators.pattern(regExps.password)], updateOn: 'change'}],
        confirmPassword: ['', {validators: [Validators.required], updateOn: 'change'}],
      }, {validators: CustomValidators.childrenEqual}),
      accountType: ['organizer'],
    });

  participantRegisterForm: FormGroup = this.formBuilder.group(
    {
      nickname: ['', {validators: [Validators.required], updateOn: 'change'}],
      email: ['', {validators: [Validators.required, Validators.email], updateOn: 'change'}],
      phone: [null, {
        validators: [Validators.minLength(15),
          Validators.maxLength(17), Validators.pattern(regExps.phone)], updateOn: 'change'
      }],
      passwordGroup: this.formBuilder.group({
        password: ['', {validators: [Validators.required, Validators.pattern(regExps.password)], updateOn: 'change'}],
        confirmPassword: ['', {validators: [Validators.required], updateOn: 'change'}],
      }, {validators: CustomValidators.childrenEqual}),
      accountType: ['participant'],
    });


  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private eventService: EventService,
              private notificationService: NotificationService,
              private router: Router) {
  }


  onOrganizerFormSubmit(): void {
    const organizerAccount: OrganizerAccount = this.organizerRegisterForm.value;
    organizerAccount.password = this.organizerRegisterForm.value.passwordGroup.password;

    if (organizerAccount.organizationName === '') {
      organizerAccount.organizationName = null;
    }
    console.log('onFormSubmit()', organizerAccount);
    this.registerAccount<OrganizerAccount>(
      organizerAccount,
      data => this.organizerAccount = data,
      () => this.organizerRegisterForm.get('email'));
  }

  onParticipantFormSubmit(): void {
    const participantAccount: ParticipantAccount = this.participantRegisterForm.value;
    participantAccount.password = this.participantRegisterForm.value.passwordGroup.password;

    if (participantAccount.phone === '') {
      participantAccount.phone = null;
    }
    console.log('onFormSubmit()', participantAccount);
    this.registerAccount<ParticipantAccount>(
      participantAccount,
      data => this.participantAccount = data,
      () => this.participantRegisterForm.get('email'));
  }

  toggleTermsOrganizer(): void {
    this.acceptedTermsOrganizer = !this.acceptedTermsOrganizer;
  }

  toggleTermsParticipant(): void {
    this.acceptedTermsParticipant = !this.acceptedTermsParticipant;
  }


  private inverse(validator: ValidatorFn): ValidatorFn {
    return control => validator(control) ? null : {taken: true};
  }

  private registerAccount<T extends Account>(registrationValue: T,
                                             accountSetter: (data: T) => void,
                                             emailControlGetter: () => AbstractControl) {
    this.authService.registerAccount(registrationValue).subscribe({
      next: data => {
        this.registered = true;
        accountSetter(data);

        this.notificationService.displaySuccess('Check your emails for the confirmation link!');
        this.router.navigate(['/login'])
          .catch(reason => this.notificationService.displayError(reason.toString()));
      },
      error: err => {
        if (err.status === 409) {
          console.log('already taken');
          emailControlGetter().addValidators(this.inverse(Validators.pattern(emailControlGetter().value)));
          emailControlGetter().updateValueAndValidity();
        } else {
          this.notificationService.displayHttpError(err);
        }
      }
    });
  }
}
