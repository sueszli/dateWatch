import {Component, OnInit} from '@angular/core';
import {ProfileService} from '../../../services/profile.service';
import {Account} from '../../../dtos/account';
import {FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {OrganizerAccount} from '../../../dtos/organizer-account';
import {ParticipantAccount} from '../../../dtos/participant-account';
import {AuthService} from '../../../services/auth.service';
import {NotificationService} from '../../../services/notification.service';
import {errorMessages, regExps} from '../../../validators/custom-validators';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {DeactivateAccountDialogComponent} from '../../dialog/deactivate-account-dialog/deactivate-account-dialog.component';
import {DeleteAccountDialogComponent} from '../../dialog/delete-account-dialog/delete-account-dialog.component';


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  initialValues = null;
  editEnabled = false;
  profile: Account;
  accountType: string;
  formErrors = errorMessages;

  emailControl = new FormControl({value: '', disabled: '' + this.editEnabled},
    {validators: [Validators.required, Validators.email], updateOn: 'change'});
  // name to show at profile page (both organizer and participant).
  nameControl = new FormControl({value: '', disabled: 'true'},
    {validators: [Validators.required], updateOn: 'change'});

  phoneControl = new FormControl({value: null, disabled: 'true'}, {
    validators: [Validators.minLength(15),
      Validators.maxLength(17), Validators.pattern(regExps.phone)], updateOn: 'change'
  });

  firstNameControl = new FormControl({value: 'test', disabled: 'true'},
    {validators: [Validators.required], updateOn: 'change'});
  lastNameControl = new FormControl({value: 'test', disabled: 'true'},
    {validators: [Validators.required], updateOn: 'change'});
  orgControl = new FormControl({value: '', disabled: 'true'});

  editForm: FormGroup = this.formBuilder.group({
    email: this.emailControl,
    name: this.nameControl,
    contactPersonFirstName: this.firstNameControl,
    contactPersonLastName: this.lastNameControl,
    organizationName: this.orgControl,
    phone: this.phoneControl
  });


  constructor(
    private profileService: ProfileService,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.getProfile();
  }


  /**
   * Fetches the current profile.
   * Sets the variables to form builder to show user current profile info.
   */
  getProfile(): void {
    this.profileService.getProfile().subscribe((p) => {
      this.profile = p;
      this.accountType = p.accountType;
      this.emailControl.setValue(p.email);
      if (this.accountType === 'organizer') {
        const op: OrganizerAccount = p as OrganizerAccount;
        this.nameControl.setValue(op.contactPersonFirstName + ' ' + op.contactPersonLastName);
        this.firstNameControl.setValue(op.contactPersonFirstName);
        this.lastNameControl.setValue(op.contactPersonLastName);
        this.orgControl.setValue(op.organizationName);
      } else if (this.accountType === 'participant') {
        const pp: ParticipantAccount = p as ParticipantAccount;
        this.nameControl.setValue(pp.nickname);
        this.phoneControl.setValue(pp.phone);
      }
      this.initialValues = this.editForm.value;
      console.log(this.initialValues);
    });
  }

  toggleEditButton(): void {
    this.editEnabled = !this.editEnabled;
    if (!this.editEnabled) {
      this.onReset();
      this.nameControl.disable();
      this.orgControl.disable();
      this.emailControl.disable();
      this.firstNameControl.disable();
      this.lastNameControl.disable();
      this.phoneControl.disable();
    } else {
      this.nameControl.enable();
      this.orgControl.enable();
      this.emailControl.enable();
      this.firstNameControl.enable();
      this.lastNameControl.enable();
      this.phoneControl.enable();
    }
  }

  deactivateAccount(): void {
    const dialogRef = this.dialog.open(DeactivateAccountDialogComponent, {
      width: '350px'
    });

    dialogRef.afterClosed().subscribe(bool => {
      if (bool) {
        this.profileService.deactivateProfile().subscribe(_ => {
          this.notificationService.displaySuccess('Successfully deactivated account!');
          this.authService.logoutUser();
        });
      }
    });
  }

  deleteAccount(): void {
    const dialogRef = this.dialog.open(DeleteAccountDialogComponent, {
      width: '350px'
    });

    dialogRef.afterClosed().subscribe( bool => {
      if (bool) {
        this.profileService.deleteProfile().subscribe(_ => {
          this.notificationService.displaySuccess('Successfully deleted account!');
          this.authService.logoutUser();
        });
      }
    });
  }

  changePassword(): void {
    this.authService.changePassword(this.profile.email).subscribe({
      next: value => {
        this.router.navigate(['/reset-password/' + value.token]);
      },
      error: _ => {
        this.notificationService.displayError(
          'Could not find a user matching this mail! Please try again!');
      }
    });
  }

  onReset(): void {
    console.log('Reset form');
    this.editForm.reset(this.initialValues);
  }

  onFormSubmit(): void {
    let req;
    this.editEnabled = !this.editEnabled;
    if (this.accountType === 'organizer') {
      this.nameControl.setValue(this.editForm.value.contactPersonFirstName + ' ' +
        this.editForm.value.contactPersonLastName);
      req = {
        email: this.editForm.value.email,
        organizationName: this.editForm.value.organizationName,
        contactPersonFirstName: this.editForm.value.contactPersonFirstName,
        contactPersonLastName: this.editForm.value.contactPersonLastName,
        password: null,
        verified: null,
        accountType: 'organizer',
      };
    } else {
      if (this.editForm.value.phone === '') {
        this.editForm.value.phone = null;
      }
      req = {
        email: this.editForm.value.email,
        nickname: this.editForm.value.name,
        password: null,
        verified: null,
        phone: this.editForm.value.phone,
        accountType: 'participant'
      };
    }

    this.profileService.setProfile(req).subscribe({
      next: b => {
        if (b) {
          if (this.profile.email !== req.email) {
            this.authService.logoutUser();
            this.notificationService.displaySuccess('Please login again to use your new email!');
          } else {
            this.editForm.disable();
            this.notificationService.displaySuccess('Updated account!');
          }
        } else {
          this.notificationService.displayError('Could not update account!');
        }
      },
      error: err => {
        if (err.status === 409) {
          console.log('already taken');
          this.editForm.get('email').addValidators(
            this.inverse(Validators.pattern(this.editForm.get('email').value)));
          this.editForm.get('email').updateValueAndValidity();
        }
      }
    });
  }

  private inverse(validator: ValidatorFn): ValidatorFn {
    return control => validator(control) ? null : {taken: true};
  }
}
