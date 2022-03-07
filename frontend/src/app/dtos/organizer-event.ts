import {Event} from './event';


export interface OrganizerEvent extends Event {
  entranceToken: string;
}

export const ARRIVAL_STATISTICS_EVENT_NAME = 'arrivals';

export interface ArrivalStatistics {
  arrivedParticipants: number | undefined;
  arrivedFirstGroupParticipants: number | undefined;
  arrivedSecondGroupParticipants: number | undefined;
}

export const PAIRING_STATISTICS_EVENT = 'pairings';

export interface PairingStatistics {
  formedPairingsForUpcomingRound: number;
  maximumPairingsPossible: number;
}
