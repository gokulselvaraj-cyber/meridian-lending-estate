// The person applying for or holding a loan. loan-core calls this a Customer.
export interface Borrower {
  customerId: string;
  fullName: string;
  dateOfBirth: string;
  email: string;
  phone: string;
  street: string;
  city: string;
  postcode: string;
}
