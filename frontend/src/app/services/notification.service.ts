import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr'; // see https://ngx-toastr.vercel.app/
import { HttpErrorResponse } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  /**
   * Toastr library settings to configure all notifications in the frontend system
   */
  private settings = {
    closeButton: true,
    newestOnTop: true,
    progressBar: true,
    positionClass: 'toast-bottom-center', // 'toast-<top/bottom>-<left/center/right>' or 'toast-<top/bottom>-full-width'
    preventDuplicates: true,

    timeOut: 5000,
    extendedTimeOut: 10000,
    enableHtml: true
  };


  constructor(private toastrService: ToastrService) { }


  /**
   * Show the user a default info notification.
   *
   * @param title title of the notification. Defaults to 'Info'.
   * @param message to show the user
   */
  public displayInfo(message: string, title = 'Info') {
    console.log(title + ': ', message);
    this.toastrService.info(message, title, this.settings);
  }

  /**
   * Show the user a default success notification.
   *
   * @param title title of the notification. Defaults to 'Success'.
   * @param message to show the user
   */
  public displaySuccess(message: string, title = 'Success') {
    console.log(title + ': ', message);
    this.toastrService.success(message, title, this.settings);
  }

  /**
   * Show the user a default warning notification.
   *
   * @param title title of the notification. Defaults to 'Warning'.
   * @param message to show the user.
   */
  public displayWarning(message: string, title = 'Warning') {
    console.log(title + ': ', message);
    this.toastrService.warning(message, title, this.settings);
  }

  /**
   * Show the user a default error notification.
   *
   * @param title title of the notification. Defaults to 'Error'.
   * @param message to show the user.
   */
  public displayError(message: string, title = 'Error'): void {
    console.log(title + ': ', message);
    this.toastrService.error(message, title, this.settings);
  }

  /**
   * Notify the user about the occurrence of the given {@link HttpErrorResponse} in an adequate manner.
   *
   * @param httpError             the {@link HttpErrorResponse} returned from the backend.
   * @param errorSpecificMessages a json object providing error messages for specific http error codes.
   *                              Can contain arbitrary many properties of the form "status-code: 'error-message'".
   *                              Eg: {401: 'Invalid email or password'}.
   */
  public displayHttpError(httpError: HttpErrorResponse, errorSpecificMessages: any = null): void {
    console.error('>>> RECEIVED ERROR: ', httpError);
    console.log('>>> ERROR STATUS: ', httpError.status);
    console.log('>>> ERROR MESSAGE: ', httpError.message);
    console.log('>>> ERROR ERROR: ', httpError.error);

    if (httpError.status === 0) {
      this.displayError('The server can not be reached.');
      return;
    } else if (errorSpecificMessages) {
      for (const key of Object.keys(errorSpecificMessages)) {
        if (httpError.status.toString() === key) {
          this.displayError(errorSpecificMessages[key]);
          return;
        }
      }
    }

    if (httpError.status === 422) {
      Object.keys(httpError.error)
        .forEach(key => {
          const errorMessage = httpError.error[key];
          if (errorMessage) {
            this.displayError(errorMessage, key);
          }
        });
    } else {
      this.displayError('An unexpected error occurred.');
    }
  }
}
