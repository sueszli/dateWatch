import {Component} from '@angular/core';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NotificationService} from '../../../services/notification.service';
import {Router} from '@angular/router';
import {ForgotPwd} from '../../../dtos/forgot-pwd';
import {AuthService} from '../../../services/auth.service';
import {errorMessages} from '../../../validators/custom-validators';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {

  hide = true;
  account: OrganizerAccount = undefined;
  formErrors = errorMessages;

  forgotForm: FormGroup = this.formBuilder.group(
    {
      email: ['', {validators: [Validators.required, Validators.email], updateOn: 'change'}]
    });


  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {
  }

  onFormSubmit(): void {
    const forgot: ForgotPwd = this.forgotForm.value;
    console.log(forgot.email);
    this.authService.forgotPassword(forgot).subscribe({
      next: _ => {
        this.notificationService.displaySuccess('Please check your mailbox!');
        this.router.navigate(['/']);
      },
      error: _ => {
        this.notificationService.displayError(
          'Could not find a user matching this mail! Please try again!');
      }
    });
  }
}
