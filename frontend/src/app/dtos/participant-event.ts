import {Event} from './event';
import {OrganizerAccount} from './organizer-account';


export interface ParticipantEvent extends Event {
  organizer: OrganizerAccount;
  participation: {
    confirmed: boolean;
    registered: boolean;
    group?: string | null;
  } | null;
}

export const PARTICIPATION_STATUS_CHANGED_EVENT_NAME = 'participationStatus';

export enum ParticipationStatus {
  unconfirmedRegistration = 10,
  confirmedRegistration = 20,
  atEventNotPaired = 30,
  atEventPaired = 40,
  leftEvent = 15,
  turnedDownConfirmedRegistration = 8
}

export interface ParticipationStatusChanged {
  status: ParticipationStatus;
  otherPersonsNickname?: string;
  otherPersonsPairingToken?: string;
}

export interface AccountParticipationStatus {
  status: ParticipationStatus;
  ownPairingToken?: string;
  otherPersonsNickname?: string;
  otherPersonsPairingToken?: string;
  roundStartedAt?: Date;
}
