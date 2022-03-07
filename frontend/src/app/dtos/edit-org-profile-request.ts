export class EditOrgProfileRequest {
  constructor(
    public email: string,
    public organizationName: string,
    public contactPersonFirstName: string,
    public contactPersonLastName: string,
  ) {}
}
