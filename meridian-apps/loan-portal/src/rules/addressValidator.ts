// Checks the address on the personal step. Accepts a 5-digit postcode or ZIP+4 (12345-6789).
const POSTCODE = /^\d{5}(-\d{4})?$/;

export function isValidAddress(street: string, city: string, postcode: string): boolean {
  return street.trim().length > 0 && city.trim().length > 0 && POSTCODE.test(postcode);
}
