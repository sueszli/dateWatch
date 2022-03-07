/* eslint-disable max-len */
/* eslint-disable @typescript-eslint/naming-convention */
/* eslint-disable @typescript-eslint/member-ordering */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subscription, interval, Subject } from 'rxjs';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent implements OnInit, OnDestroy {

  /**
   * Timer
   */
  private TOTAL_TIME = 15;
  private countDown: Subscription;
  public secondsLeft;

  /**
   * Confirmation of token
   */
  uuid = undefined;
  error = undefined;
  success = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router) {
  }

  ngOnInit(): void {
    // start timer
    const fixed = new Date();
    this.countDown = interval(1000).subscribe(
      () => {
        const delta = new Date().getTime() - fixed.getTime();
        this.secondsLeft = this.TOTAL_TIME - Math.floor(delta / 1000 % 60);

        if(this.secondsLeft === 0) {
          this.router.navigateByUrl('/');
        }
      }
    );

    // check confirmation token
    this.route.params.subscribe(params => {
      this.uuid = params['uuid'];
      console.log('confirm uuid', this.uuid);
      this.authService.confirmAccount(this.uuid).subscribe({
        next: value => {
          console.log('confirmation succeeded', value);
          this.success = true;
        },
        error: err => this.error = err
      });
    });
  }

  ngOnDestroy(): void {
    this.countDown.unsubscribe();
  }


}
