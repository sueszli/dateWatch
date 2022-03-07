import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged, Subject, Subscription, switchMap} from 'rxjs';
import {EventService} from '../../../../services/event.service';
import {ActivatedRoute, Router} from '@angular/router';
import {NotificationService} from '../../../../services/notification.service';
import {errorMessages} from '../../../../validators/custom-validators';

@Component({
  selector: 'app-manual-event-code-entry',
  templateUrl: './manual-event-code-entry.component.html',
  styleUrls: ['./manual-event-code-entry.component.scss']
})
export class ManualEventCodeEntryComponent implements OnInit {
  @Output() enteredAnEvent = new EventEmitter<boolean>();

  formErrors = errorMessages;

  eventCodeForm: FormGroup = this.formBuilder.group(
    {
      eventEntranceID: ['', {validators: [Validators.required], updateOn: 'change'}],
    });
  eventCode: string;

  private searchTerms = new Subject<string>();
  private routeSub: Subscription;

  constructor(private formBuilder: FormBuilder,
              private eventService: EventService,
              private route: ActivatedRoute,
              private router: Router,
              private notificationService: NotificationService) {
  }

  ngOnInit(): void {
    // ToDo: why this does not work?
    this.searchTerms.pipe(
      // wait 300ms after each keystroke before considering the term
      debounceTime(300),

      // ignore new term if same as previous term
      distinctUntilChanged(),

      // switch to new search observable each time the term changes
      switchMap((term: string) => this.eventService.reportArrival(this.eventCode, term)),
    );

    this.searchTerms.subscribe((code: string) =>
      this.eventService.reportArrival(this.eventCode, code)
        .subscribe({
            next: _ => {
              this.enteredAnEvent.emit(true);
            }, error: err => {
              if (err.status === 403) {
                this.notificationService.displayError('You are not in the list of the participants.');
                this.router.navigate(['/']);
              }
            }
          }
        ));

    this.routeSub = this.route.params.subscribe(params => {
      this.eventCode = (params['id']);
      // send test message to check, if user can participate in that event.
      this.searchTerms.next('init');
    });


  }

// Push a search term into the observable stream.
  inputChanged(term: string): void {
    this.searchTerms.next(term);
  }

}
