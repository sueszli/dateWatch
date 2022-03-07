import {Account} from './account';

export interface OrganizerAccount extends Account {
  organizationName: string;
  contactPersonFirstName: string;
  contactPersonLastName: string;
  deactivated: boolean;
}
