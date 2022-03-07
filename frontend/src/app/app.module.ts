import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HomeComponent} from './components/pages/home/home.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RegistrationComponent} from './components/pages/registration/registration.component';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {ConfirmationComponent} from './components/confirmation/confirmation.component';
import {LoginComponent} from './components/pages/login/login.component';
import {ProfileComponent} from './components/pages/profile/profile.component';
import {MatTabsModule} from '@angular/material/tabs';
import {EventDetailsComponent} from './components/pages/event-details/event-details.component';
import {EventCreationComponent} from './components/pages/event-creation/event-creation.component';
import {NgxMatDatetimePickerModule} from '@angular-material-components/datetime-picker';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {NgxMatMomentModule} from '@angular-material-components/moment-adapter';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatNativeDateModule, MatOptionModule, MatRippleModule} from '@angular/material/core';
import {MatCardModule} from '@angular/material/card';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {InputDialogComponent} from './components/dialog/input-dialog/input-dialog.component';
import {SelectDialogComponent} from './components/dialog/select-dialog/select-dialog.component';
import {ManualEventCodeEntryComponent} from './components/pages/event-pipeline/manual-event-code-entry/manual-event-code-entry.component';
import {EventPipelineComponent} from './components/pages/event-pipeline/event-pipeline.component';
import {EntranceCodeConfirmedComponent} from './components/pages/event-pipeline/entrance-code-confirmed/entrance-code-confirmed.component';
import {ToastrModule} from 'ngx-toastr';
import {CommonModule} from '@angular/common';
import {AboutComponent} from './components/pages/about/about.component';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MainComponent} from './components/pages/main/main.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatRadioModule} from '@angular/material/radio';
import {SimpleHeaderComponent} from './components/header/simple-header/simple-header.component';
import {DrawerContentComponent} from './components/drawer-content/drawer-content.component';
import {EventCardComponent} from './util/event-card/event-card.component';
import {EventManagementComponent} from './components/pages/event-management/event-management.component';
import {ForgotPasswordComponent} from './components/pages/forgot-password/forgot-password.component';
import {ResetPasswordComponent} from './components/pages/reset-password/reset-password.component';
import {EventExecutionComponent} from './components/pages/event-execution/event-execution.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {EventParticipationComponent} from './components/pages/event-participation/event-participation.component';
import {ConfirmDialogComponent} from './components/dialog/confirm-dialog/confirm-dialog.component';
import {EventUpdateComponent} from './components/pages/event-update/event-update.component';
import {AccountCardComponent} from './util/account-card/account-card.component';
import {AdminComponent} from './components/pages/admin/admin.component';
import {AdminDialogComponent} from './components/dialog/admin-dialog/admin-dialog.component';
import {DeactivateAccountDialogComponent} from './components/dialog/deactivate-account-dialog/deactivate-account-dialog.component';
import {DeleteAccountDialogComponent} from './components/dialog/delete-account-dialog/delete-account-dialog.component';
import {MatchesComponent} from './components/pages/matches/matches.component';
import {OrganizedEventsComponent} from './components/pages/event-filter/organized-events/organized-events.component';
import {PlannedEventsComponent} from './components/pages/event-filter/planned-events/planned-events.component';
import {VisitedEventsComponent} from './components/pages/event-filter/visited-events/visited-events.component';
import {MatChipsModule} from '@angular/material/chips';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatExpansionModule} from '@angular/material/expansion';
import {ExploreEventsComponent} from './components/pages/event-filter/explore-events/explore-events.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {FeedbackComponent} from './components/pages/feedback/feedback.component';
import { PostStatisticsDialogComponent } from './components/dialog/post-statistics-dialog/post-statistics-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    RegistrationComponent,
    ConfirmationComponent,
    LoginComponent,
    EventDetailsComponent,
    InputDialogComponent,
    SelectDialogComponent,
    EventCreationComponent,
    ProfileComponent,
    ManualEventCodeEntryComponent,
    EventPipelineComponent,
    EntranceCodeConfirmedComponent,
    AboutComponent,
    MainComponent,
    SimpleHeaderComponent,
    EventCardComponent,
    DrawerContentComponent,
    EventManagementComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    EventExecutionComponent,
    EventParticipationComponent,
    ConfirmDialogComponent,
    EventUpdateComponent,
    AccountCardComponent,
    AdminComponent,
    AdminDialogComponent,
    DeactivateAccountDialogComponent,
    DeleteAccountDialogComponent,
    MatchesComponent,
    OrganizedEventsComponent,
    PlannedEventsComponent,
    VisitedEventsComponent,
    ExploreEventsComponent,
    FeedbackComponent,
    PostStatisticsDialogComponent,
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ReactiveFormsModule,
        HttpClientModule,
        NgbModule,
        FormsModule,
        BrowserAnimationsModule,
        MatButtonModule,
        MatToolbarModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatTabsModule,
        MatDividerModule,
        NgxMatDatetimePickerModule,
        MatDatepickerModule,
        NgxMatMomentModule,
        CommonModule,
        ToastrModule.forRoot({
            positionClass: 'toast-bottom-right',
            preventDuplicates: true,
            countDuplicates: true,
            resetTimeoutOnDuplicate: true
        }), // ToastrModule added
        MatCardModule,
        MatRippleModule,
        MatCheckboxModule,
        MatSidenavModule,
        MatListModule,
        MatDialogModule,
        MatSelectModule,
        MatOptionModule,
        MatCardModule,
        MatSlideToggleModule,
        MatRadioModule,
        MatTooltipModule,
        MatChipsModule,
        MatAutocompleteModule,
        MatExpansionModule,
        MatNativeDateModule,
        MatProgressSpinnerModule
    ],
  providers: [httpInterceptorProviders,
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'fill'}}],
  bootstrap: [AppComponent]
})
export class AppModule {
}
