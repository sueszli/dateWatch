import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';


@Injectable({
  providedIn: 'root'
})
export class DiffGuard implements CanActivate {

  constructor(private authService: AuthService,
              private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && this.authService.getUserRole()==='ADMIN') {
      this.router.navigate(['/admin']);
      return true;
    } else if (this.authService.isLoggedIn()) {
      this.router.navigate(['/main']);
      return false;
    }

    return true;
  }
}
