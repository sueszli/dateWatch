import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {ProfileService} from '../services/profile.service';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EventGuard implements CanActivate {

  constructor(private authService: AuthService, private profileService: ProfileService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): boolean | Observable<boolean> {
    if (!this.authService.isLoggedIn()) {
      return true;
    }

    return this.profileService.status().pipe(map(status => {
        console.log('user status', status);
        if (status.currentlyAtEvent) {
          console.log('user is at an event, redirecting...');
          if (this.authService.getUserRole() === 'ORGANIZER') {
            console.log('redirect to event execution of', status.eventAccessToken);
            this.router.navigate(['/event', 'execution', status.eventAccessToken]);
          } else if (this.authService.getUserRole() === 'PARTICIPANT') {
            console.log('redirect to event participation of', status.eventAccessToken);
            this.router.navigate(['/event', 'participation', status.eventAccessToken]);
          }
          return false;
        }
        return true;
      }));
    }
}
