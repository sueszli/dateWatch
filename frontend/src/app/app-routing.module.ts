import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/pages/home/home.component';
import {RegistrationComponent} from './components/pages/registration/registration.component';
import {ConfirmationComponent} from './components/confirmation/confirmation.component';
import {LoginComponent} from './components/pages/login/login.component';
import {ProfileComponent} from './components/pages/profile/profile.component';
import {EventDetailsComponent} from './components/pages/event-details/event-details.component';
import {EventCreationComponent} from './components/pages/event-creation/event-creation.component';
import {AboutComponent} from './components/pages/about/about.component';
import {MainComponent} from './components/pages/main/main.component';
import {EventPipelineComponent} from './components/pages/event-pipeline/event-pipeline.component';
import {AuthGuard} from './guards/auth.guard';
import {DiffGuard} from './guards/diff.guard';
import {EventManagementComponent} from './components/pages/event-management/event-management.component';
import {ForgotPasswordComponent} from './components/pages/forgot-password/forgot-password.component';
import {ResetPasswordComponent} from './components/pages/reset-password/reset-password.component';
import {EventExecutionComponent} from './components/pages/event-execution/event-execution.component';
import {EventParticipationComponent} from './components/pages/event-participation/event-participation.component';
import {EventUpdateComponent} from './components/pages/event-update/event-update.component';
import {AdminComponent} from './components/pages/admin/admin.component';
import {AdminGuard} from './guards/admin.guard';
import {MatchesComponent} from './components/pages/matches/matches.component';
import {OrganizedEventsComponent} from './components/pages/event-filter/organized-events/organized-events.component';
import {PlannedEventsComponent} from './components/pages/event-filter/planned-events/planned-events.component';
import {VisitedEventsComponent} from './components/pages/event-filter/visited-events/visited-events.component';
import {ExploreEventsComponent} from './components/pages/event-filter/explore-events/explore-events.component';
import {FeedbackComponent} from './components/pages/feedback/feedback.component';
import {EventGuard} from './guards/event.guard';

const routes: Routes = [
  {path: '', canActivate: [DiffGuard, EventGuard], component: HomeComponent},
  // {path: 'message', canActivate: [AuthGuard], component: MessageComponent},
  {path: 'about', canActivate: [EventGuard], component: AboutComponent},
  {path: 'registration', canActivate: [DiffGuard, EventGuard], component: RegistrationComponent},
  {path: 'login', canActivate: [DiffGuard, EventGuard], component: LoginComponent},
  {path: 'confirmation/:uuid', canActivate: [EventGuard], component: ConfirmationComponent},
  {path: 'main', canActivate: [AuthGuard, EventGuard], component: MainComponent},
  {path: 'forgot-password', canActivate: [DiffGuard, EventGuard], component: ForgotPasswordComponent},
  {path: 'reset-password/:token', canActivate: [EventGuard], component: ResetPasswordComponent},
  {path: 'profile', canActivate: [AuthGuard, EventGuard], component: ProfileComponent},
  {path: 'event/create', canActivate: [AuthGuard, EventGuard], component: EventCreationComponent},
  {path: 'event/execution/:accessToken', canActivate: [AuthGuard], component: EventExecutionComponent},
  {path: 'event/participation/:accessToken', canActivate: [AuthGuard], component: EventParticipationComponent},
  {path: 'event/entrance/:id', canActivate: [AuthGuard, EventGuard], component: EventPipelineComponent},
  {path: 'event/management/:accessToken', canActivate: [AuthGuard, EventGuard], component: EventManagementComponent},
  {path: 'event/update/:accessToken', canActivate: [AuthGuard, EventGuard], component: EventUpdateComponent},
  {path: 'event/:accessToken', canActivate: [AuthGuard, EventGuard], component: EventDetailsComponent},
  {path: 'admin', canActivate: [AdminGuard], component: AdminComponent},
  {path: 'event/filter/organized', canActivate: [AuthGuard, EventGuard], component: OrganizedEventsComponent},
  {path: 'event/filter/planned', canActivate: [AuthGuard, EventGuard], component: PlannedEventsComponent},
  {path: 'event/filter/visited', canActivate: [AuthGuard, EventGuard], component: VisitedEventsComponent},
  {path: 'event/filter/explore', canActivate: [AuthGuard, EventGuard], component: ExploreEventsComponent},
  {path: 'matches', canActivate: [AuthGuard, EventGuard], component: MatchesComponent},
  {path: 'feedback', canActivate: [AuthGuard], component: FeedbackComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
