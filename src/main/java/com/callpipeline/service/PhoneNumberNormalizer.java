package com.callpipeline.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PhoneNumberNormalizer {

    public String normalize(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }

        String digits = phoneNumber.replaceAll("\\D", "");
        if (!StringUtils.hasText(digits)) {
            return null;
        }

        if (digits.length() > 10 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }

        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }

        return digits;
    }
}
