package com.meridian.lending.rules;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Checks a customer's address before an application is saved. */
@Component
public class AddressValidator {
    private static final Pattern POSTCODE = Pattern.compile("^\\d{5}$");

    public boolean isValid(String street, String city, String postcode) {
        return street != null && !street.isBlank()
                && city != null && !city.isBlank()
                && postcode != null && POSTCODE.matcher(postcode).matches();
    }
}
