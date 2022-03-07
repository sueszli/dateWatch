import {Account} from './account';

export interface ParticipantAccount extends Account {
  nickname: string;
  phone: string;
}
