import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';

/**
 * Custom validator functions for reactive form validation
 */
export class CustomValidators {
  /**
   * Validates that child controls in the form group are equal
   */
  static childrenEqual: ValidatorFn = (formGroup: FormGroup) => {
    const [firstControlName, ...otherControlNames] = Object.keys(formGroup.controls || {});
    const isValid = otherControlNames.every(controlName =>
      formGroup.get(controlName).value === formGroup.get(firstControlName).value);
    return isValid ? null : {childrenNotEqual: true};
  };

  /**
   * Validates a date on a given max timestamp
   */
  static dateMax(timestamp: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      const maxDate = new Date(new Date(timestamp).setHours(0, 0, 0, 0));
      const selectedDate = new Date(Date.parse(control.value));
      if (selectedDate > maxDate) {
        return {max: maxDate};
      }
      return null;
    };
  }

  /**
   * Validates a date on a given min timestamp
   */
  static dateMin(timestamp: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      const minDate = new Date(new Date(timestamp));
      const selectedDate = new Date(Date.parse(control.value));
      if (selectedDate < minDate) {
        return {min: minDate};
      }
      return null;
    };
  }

  /**
   * Validates a number if it is even.
   */
  static isEven(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      if (control.value % 2 === 0) {
        return null;
      }
      return {notEven: control.value};
    };
  }
}

/**
 * Custom ErrorStateMatcher which returns true (error exists)
 * when the parent form group is invalid and the control has been touched
 */
export class ConfirmValidParentMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return control.parent.invalid && control.touched;
  }
}

/**
 * Collection of reusable RegExps
 */
export const regExps: { [key: string]: RegExp } = {
  password: /^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{7,15}$/,
  phone: /^(?:00|\+)[0-9\s]{14,16}$/
};

/**
 * Collection of reusable error messages
 */
export const errorMessages: { [key: string]: string } = {
  password: 'Password must be between 7 and 15 characters, and contain at least one number and special character',
  confirmPassword: 'Passwords must match',
  required: 'Field is required',
  email: 'Invalid email',
  emailAlreadyTaken: 'Already taken',
  phoneWrongFormat: 'Wrong Format! e.g. +43 120 1234567',
  dateMin: 'Earliest begin is tomorrow'
};
