export interface Event {
  title: string;
  description: string;
  startDateAndTime: string;
  durationInMinutes: number;
  roundDurationInSeconds: number;
  street: string;
  postcode: string;
  city: string;
  maxParticipants: number;
  groups: {
    firstGroup: EventGroup;
    secondGroup: EventGroup;
  } | null;
  accessToken: string;
  public: boolean;
  ongoing?: boolean;
  active: boolean;
  status: EventStatus;
  hasRegistrationClosed: boolean;
  numberOfRegistrations?: number;
}

export interface EventGroup {
  title: string;
  description: string;
  numberOfRegistrations: number | undefined;
}

export enum EventStatus {
  canceled = 5,
  registrationOpen = 10,
  registrationClosed = 20,
  ongoingButNoUpcomingRound = 30,
  upcomingRoundAboutToStart = 40,
  roundOngoing = 50,
  finished = 25
}

export const EVENT_STATUS_CHANGED_EVENT_NAME = 'eventStatusChanged';

export interface EventStatusChanged {
  accessToken: string;
  status: EventStatus;
  firedAt: string;
  pairingToken?: string;
}
