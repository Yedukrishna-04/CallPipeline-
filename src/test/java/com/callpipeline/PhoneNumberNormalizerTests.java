package com.callpipeline;

import com.callpipeline.service.PhoneNumberNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberNormalizerTests {

    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void stripsCountryCodeAndFormatting() {
        assertThat(normalizer.normalize("+91 98765 43210")).isEqualTo("9876543210");
        assertThat(normalizer.normalize("98765-43210")).isEqualTo("9876543210");
        assertThat(normalizer.normalize("91-9876543210")).isEqualTo("9876543210");
    }
}
