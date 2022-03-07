import {Component, OnInit} from '@angular/core';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../../services/auth.service';
import {EventService} from '../../../services/event.service';
import {NotificationService} from '../../../services/notification.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ResetPwd} from '../../../dtos/reset-pwd';
import {
  ConfirmValidParentMatcher,
  CustomValidators,
  errorMessages,
  regExps
} from '../../../validators/custom-validators';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  hidePwd = true;
  hidePwdConfirm = true;
  account: OrganizerAccount = undefined;

  matcher = new ConfirmValidParentMatcher();
  formErrors = errorMessages;

  resetForm: FormGroup = this.formBuilder.group(
    {
      password: ['', {validators: [Validators.required, Validators.pattern(regExps.password)], updateOn: 'change'}],
      confirmPassword: ['', {updateOn: 'change'}],
    }, {validators: CustomValidators.childrenEqual});

  token = undefined;

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private eventService: EventService,
    private notificationService: NotificationService,
    private router: Router) {
  }

  ngOnInit(): void {
    // check confirmation token
    this.route.params.subscribe(params => {
      this.token = params['token'];
      console.log('confirm token', this.token);
    });
  }


  onFormSubmit(): void {
    const reset: ResetPwd = this.resetForm.value;
    this.authService.resetPassword(reset, this.token).subscribe({
      next: _ => {
        this.notificationService.displaySuccess(
          'Successfully set new password.');
        this.router.navigate(['/login']);
      },
      error: _ => {
        this.notificationService.displayError(
          'The token to reset your password is wrong! Please try sending a mail again!');
      }
    });
  }
}
