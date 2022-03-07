import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {NotificationService} from '../services/notification.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && this.authService.getUserRole()==='ADMIN') {
      return true;
    } else {
      this.notificationService.displayWarning('You do not have permissions for this!');
      this.router.navigate(['/main']);
      return false;
    }
  }
}
