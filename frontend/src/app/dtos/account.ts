export interface Account {
  email: string;
  password: string;
  verified: boolean;
  accountType: string;
  banned: boolean;
  banReason: string;
}

export interface UserStatus {
  currentlyAtEvent: boolean;
  eventAccessToken?: string;
}
