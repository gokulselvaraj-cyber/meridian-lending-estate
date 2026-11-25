// A portal sign-in profile: how a person logs in to this website. It is not a loan account.
export interface Account {
  username: string;
  displayName: string;
  lastSignIn: string;
  passwordResetRequested: boolean;
  borrowerId?: string;
}
