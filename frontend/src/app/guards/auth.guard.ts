import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {NotificationService} from '../services/notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && this.authService.getUserRole()==='ADMIN') {
      this.router.navigate(['/admin']);
      return false;
    } else if (this.authService.isLoggedIn()) {
      return true;
    } else {
      this.notificationService.displayWarning('Please login to proceed!');
      this.router.navigate(['/login']);
      return false;
    }
  }
}
