export interface EventFilter {
  titleSubstring?: string;

  startDateAndTime?: string;
  endDateAndTime?: string;

  citySubstring?: string;

  isPublic?: boolean;

  filterType: FilterType;

  organizerNameSubstring?: string;
}

export enum FilterType {
  eventsOrganizer = 10,
  eventsParticipantPublic = 20,
  eventsParticipantPlanned = 30,
  eventsParticipantVisited = 40
}
