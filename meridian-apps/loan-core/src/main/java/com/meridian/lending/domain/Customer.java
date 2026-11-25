package com.meridian.lending.domain;

import java.time.LocalDate;

/** A person who borrows from Meridian. Stored in table CUSTOMER. */
public class Customer {
    private final String customerId;
    private final String fullName;
    @Pii private final String nationalId;
    @Pii private final LocalDate dateOfBirth;
    @Pii private final String email;
    @Pii private final String phone;
    @Pii private final String address;
    private final String postcode;
    private final boolean marketingConsent;

    public Customer(String customerId, String fullName, String nationalId, LocalDate dateOfBirth, String email,
                    String phone, String address, String postcode, boolean marketingConsent) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.postcode = postcode;
        this.marketingConsent = marketingConsent;
    }

    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getNationalId() { return nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPostcode() { return postcode; }
    public boolean hasMarketingConsent() { return marketingConsent; }

    /** The national id is masked to its last 4 characters whenever a customer is logged. */
    public String maskedNationalId() {
        if (nationalId == null || nationalId.length() <= 4) {
            return "****";
        }
        return "****" + nationalId.substring(nationalId.length() - 4);
    }

    @Override
    public String toString() {
        return "Customer{" + customerId + ", nationalId=" + maskedNationalId() + "}";
    }
}
