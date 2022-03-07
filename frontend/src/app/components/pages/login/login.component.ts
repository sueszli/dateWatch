import {Component, OnInit} from '@angular/core';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../../services/auth.service';
import {Router} from '@angular/router';
import {AuthRequest} from '../../../dtos/auth-request';
import {EventService} from '../../../services/event.service';
import {NotificationService} from '../../../services/notification.service';
import {errorMessages} from '../../../validators/custom-validators';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  hide = true;
  account: OrganizerAccount = undefined;
  formErrors = errorMessages;

  loginForm: FormGroup = this.formBuilder.group(
    {
      email: ['', {validators: [Validators.required, Validators.email], updateOn: 'change'}],
      password: ['', {validators: [Validators.required], updateOn: 'change'}],
    });


  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private eventService: EventService,
              private notificationService: NotificationService,
              private router: Router) {
  }

  ngOnInit(): void {
  }


  onFormSubmit(): void {
    const authRequest: AuthRequest = this.loginForm.value;
    this.authService.loginUser(authRequest).subscribe({
      next: _ => {
        this.router.navigate(['/main']);
        console.log('Successfully logged in.', this.account);
      },
      error: err => this.notificationService.displayHttpError(err, {
        401: 'Invalid email or password',
        403: err.error // banned or deactivated
      })
    });
  }
}
