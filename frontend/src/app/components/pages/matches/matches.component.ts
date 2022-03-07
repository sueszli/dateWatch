import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {debounceTime, distinctUntilChanged, Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {MatchParticipantAccount} from 'src/app/dtos/match-participant-account';
import {EventService} from 'src/app/services/event.service';
import {NotificationService} from 'src/app/services/notification.service';
import {ProfileService} from '../../../services/profile.service';

/**
 * Important: Event titles are not unique, but their accessTokens are.
 * The Events are sorted chronologically, and the contained matches are sorted alphabetically.
 *
 * In this class the user can search for events and substrings in nicknames of matches.
 * The performance is optimized by also introducing a cache (in form of a map - not as a browser cookie, so that
 * this page stays synchronized with the server-side database).
 */
@Component({
  selector: 'app-matches',
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.scss']
})
export class MatchesComponent implements OnInit {

  // input-fields
  @ViewChild('eventInput') eventInput: ElementRef<HTMLInputElement>;
  eventSearchCtrl = new FormControl();
  nickNameForm: FormGroup = this.formBuilder.group({
    nickName: [undefined, {updateOn: 'change'}]
  });

  baseResultMatches: Map<string, MatchParticipantAccount[]> = new Map<string, MatchParticipantAccount[]>();
  baseResultMatchesArray: { eventTitle: string; matches: MatchParticipantAccount[] }[] = [];

  resultMatchesArray: { eventTitle: string; matches: MatchParticipantAccount[] }[] = [];

  // auto complete
  currentlyChosen: string[] = [];
  titleSuggestions: string[] = [];
  $titleSuggestions: Observable<string[]>;

  constructor(
    private eventService: EventService,
    private profileService: ProfileService,
    private notificationService: NotificationService,
    private formBuilder: FormBuilder
  ) {
  }

  /**
   * INITIALIZATION
   */
  ngOnInit() {
    this.fetchMatches();

    // listen to user input in event-search
    const eventFilter = input => this.titleSuggestions.filter(elem => elem.toLowerCase().includes(input.toLowerCase()));
    this.$titleSuggestions = this.eventSearchCtrl.valueChanges.pipe(
      startWith(null),
      map((elem: string | null) => (elem ? eventFilter(elem) : this.titleSuggestions.slice())),
    );

    // listen to user input in nick-name-search
    this.nickNameForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe(data => {
      console.log(data);
      // deep copy of base array
      this.resultMatchesArray = [];
      this.baseResultMatchesArray.forEach(val => this.resultMatchesArray.push(Object.assign({}, val)));
      this.resultMatchesArray.forEach(eventMatch => {
        eventMatch.matches = eventMatch.matches.filter(match =>
          match.nickname.toLocaleLowerCase().includes(data.nickName.toLowerCase()));
      });
      this.resultMatchesArray = this.resultMatchesArray.filter(eventMatch => eventMatch.matches.length > 0);
    });
  }

  /**
   * REMOVE A CHIP
   */
  removeChip(title: string): void {
    const index = this.currentlyChosen.indexOf(title);
    if (index >= 0) {
      this.currentlyChosen.splice(index, 1);
      this.titleSuggestions.push(title);
    }

    // deep copy of base array
    this.resultMatchesArray = [];
    this.baseResultMatchesArray.forEach(val => this.resultMatchesArray.push(Object.assign({}, val)));
    console.log('resultMatchesArray', this.resultMatchesArray);
    this.resultMatchesArray = this.resultMatchesArray.filter(eventMatch =>
      this.currentlyChosen.length === 0 || this.currentlyChosen.includes(eventMatch.eventTitle));
    console.log('resultMatchesArray', this.resultMatchesArray);
  }

  /**
   * ADD A CHIP
   */
  addChip(chosen: MatAutocompleteSelectedEvent): void {
    const title = chosen.option.viewValue;
    this.currentlyChosen.push(title);
    this.titleSuggestions = this.titleSuggestions.filter(elem => elem !== title);

    // deep copy of base array
    this.resultMatchesArray = [];
    this.baseResultMatchesArray.forEach(val => this.resultMatchesArray.push(Object.assign({}, val)));
    this.resultMatchesArray = this.resultMatchesArray.filter(eventMatch =>
      this.currentlyChosen.length === 0 || this.currentlyChosen.includes(eventMatch.eventTitle));

    // empty form
    this.eventInput.nativeElement.value = '';
    this.eventSearchCtrl.setValue(null);
  }

  formatTime(arg: string): string {
    // format date
    const date = new Date(arg);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${day}.${month}.${year}`;
  }

  fetchMatches(): void {
    // fetch all matches
    this.eventService.getAllMatches().subscribe({
      next: data => {
        data.forEach(match => {
          if (this.baseResultMatches.has(match.eventTitle)) {
            this.baseResultMatches.set(match.eventTitle, this.baseResultMatches.get(match.eventTitle).concat(match));
          } else {
            this.baseResultMatches.set(match.eventTitle, [match]);
          }
        });
        this.baseResultMatchesArray = Array.from(this.baseResultMatches, ([eventTitle, matches]) => ({
          eventTitle,
          matches
        }));
        // deep copy of base array
        this.resultMatchesArray = [];
        this.baseResultMatchesArray.forEach(val => this.resultMatchesArray.push(Object.assign({}, val)));

        this.titleSuggestions = Array.from(this.baseResultMatches.keys());
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }

  revokeMatch(otherPersonsEmail: string, otherPersonsUsername: string) {
    this.profileService.revokePairingApprovals(otherPersonsEmail).subscribe({
      next: _ => {
        this.notificationService.displaySuccess(`Removed match with user '${otherPersonsUsername}'`);

        for (const eventMatch of this.baseResultMatchesArray) {
          eventMatch.matches = eventMatch.matches
            .filter(match => match.email !== otherPersonsEmail);
        }
        this.baseResultMatchesArray = this.baseResultMatchesArray.filter(({matches}) => matches.length >= 1);

        this.resultMatchesArray = [];
        this.baseResultMatchesArray.forEach(val => this.resultMatchesArray.push(Object.assign({}, val)));
      },
      error: err => this.notificationService.displayHttpError(err)
    });
  }
}
