package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.AlphaMissense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AlphaMissenseMapper.
 * Tests mapping of string values to AlphaMissense enum.
 * Note: AlphaMissense requires the "AM_" prefix on enum values.
 *
 * Test Case IDs: AM-001 through AM-006
 */
@DisplayName("AlphaMissenseMapper Tests")
class AlphaMissenseMapperTest {

    @Test
    @DisplayName("AM-001: 'LIKELY_BENIGN' returns AlphaMissense.AM_LIKELY_BENIGN")
    void testLikelyBenign() {
        assertThat(AlphaMissenseMapper.fromString("LIKELY_BENIGN")).isEqualTo(AlphaMissense.AM_LIKELY_BENIGN);
    }

    @Test
    @DisplayName("AM-002: 'LIKELY_PATHOGENIC' returns AlphaMissense.AM_LIKELY_PATHOGENIC")
    void testLikelyPathogenic() {
        assertThat(AlphaMissenseMapper.fromString("LIKELY_PATHOGENIC")).isEqualTo(AlphaMissense.AM_LIKELY_PATHOGENIC);
    }

    @Test
    @DisplayName("AM-003: 'AMBIGUOUS' returns AlphaMissense.AM_AMBIGUOUS")
    void testAmbiguous() {
        assertThat(AlphaMissenseMapper.fromString("AMBIGUOUS")).isEqualTo(AlphaMissense.AM_AMBIGUOUS);
    }

    @Test
    @DisplayName("AM-004: Lowercase 'likely_benign' returns AlphaMissense.AM_LIKELY_BENIGN")
    void testLowercase() {
        assertThat(AlphaMissenseMapper.fromString("likely_benign")).isEqualTo(AlphaMissense.AM_LIKELY_BENIGN);
    }

    @Test
    @DisplayName("AM-005: Without prefix 'BENIGN' returns AlphaMissense.UNRECOGNIZED")
    void testWithoutPrefix() {
        // "BENIGN" is not a valid AlphaMissense value (would need AM_BENIGN which doesn't exist)
        assertThat(AlphaMissenseMapper.fromString("BENIGN")).isEqualTo(AlphaMissense.UNRECOGNIZED);
    }

    @Test
    @DisplayName("AM-006: Invalid 'PATHOGENIC' returns AlphaMissense.UNRECOGNIZED")
    void testInvalidPathogenic() {
        // "PATHOGENIC" alone is not valid - only LIKELY_PATHOGENIC maps to AM_LIKELY_PATHOGENIC
        assertThat(AlphaMissenseMapper.fromString("PATHOGENIC")).isEqualTo(AlphaMissense.UNRECOGNIZED);
    }

    @ParameterizedTest(name = "Valid AlphaMissense ''{0}'' maps correctly")
    @DisplayName("All valid AlphaMissense values map to respective enums")
    @MethodSource("validAlphaMissenseValues")
    void testAllValidValues(String input, AlphaMissense expected) {
        assertThat(AlphaMissenseMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validAlphaMissenseValues() {
        return Stream.of(
            Arguments.of("LIKELY_BENIGN", AlphaMissense.AM_LIKELY_BENIGN),
            Arguments.of("LIKELY_PATHOGENIC", AlphaMissense.AM_LIKELY_PATHOGENIC),
            Arguments.of("AMBIGUOUS", AlphaMissense.AM_AMBIGUOUS),
            // Case variations
            Arguments.of("likely_benign", AlphaMissense.AM_LIKELY_BENIGN),
            Arguments.of("likely_pathogenic", AlphaMissense.AM_LIKELY_PATHOGENIC),
            Arguments.of("ambiguous", AlphaMissense.AM_AMBIGUOUS),
            Arguments.of("Likely_Benign", AlphaMissense.AM_LIKELY_BENIGN),
            // With spaces/dashes (normalized to underscores)
            Arguments.of("LIKELY BENIGN", AlphaMissense.AM_LIKELY_BENIGN),
            Arguments.of("LIKELY-BENIGN", AlphaMissense.AM_LIKELY_BENIGN),
            Arguments.of("LIKELY PATHOGENIC", AlphaMissense.AM_LIKELY_PATHOGENIC),
            Arguments.of("LIKELY-PATHOGENIC", AlphaMissense.AM_LIKELY_PATHOGENIC)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "BENIGN", "PATHOGENIC", "AM_LIKELY_BENIGN", "UNKNOWN"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(AlphaMissenseMapper.fromString(input)).isEqualTo(AlphaMissense.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(AlphaMissenseMapper.fromString("  LIKELY_BENIGN  ")).isEqualTo(AlphaMissense.AM_LIKELY_BENIGN);
        assertThat(AlphaMissenseMapper.fromString("\tAMBIGUOUS\t")).isEqualTo(AlphaMissense.AM_AMBIGUOUS);
    }

    @Test
    @DisplayName("Prefix is automatically added - user should NOT include AM_ prefix")
    void testPrefixNotRequired() {
        // The mapper adds AM_ prefix internally, so input with AM_ prefix would result in AM_AM_...
        assertThat(AlphaMissenseMapper.fromString("AM_LIKELY_BENIGN")).isEqualTo(AlphaMissense.UNRECOGNIZED);
    }
}
