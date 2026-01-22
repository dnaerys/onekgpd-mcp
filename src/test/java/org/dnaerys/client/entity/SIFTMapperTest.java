package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.SIFT;
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
 * Unit tests for SIFTMapper.
 * Tests mapping of string values to SIFT enum.
 *
 * SIFT has only 2 valid prediction values: TOLERATED, DELETERIOUS
 */
@DisplayName("SIFTMapper Tests")
class SIFTMapperTest {

    @Test
    @DisplayName("TOLERATED maps correctly")
    void testTolerated() {
        assertThat(SIFTMapper.fromString("TOLERATED")).isEqualTo(SIFT.TOLERATED);
    }

    @Test
    @DisplayName("DELETERIOUS maps correctly")
    void testDeleterious() {
        assertThat(SIFTMapper.fromString("DELETERIOUS")).isEqualTo(SIFT.DELETERIOUS);
    }

    @ParameterizedTest(name = "Valid SIFT ''{0}'' maps correctly")
    @DisplayName("All valid SIFT values map to respective enums")
    @MethodSource("validSiftValues")
    void testAllValidSiftValues(String input, SIFT expected) {
        assertThat(SIFTMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validSiftValues() {
        return Stream.of(
            // Uppercase
            Arguments.of("TOLERATED", SIFT.TOLERATED),
            Arguments.of("DELETERIOUS", SIFT.DELETERIOUS),
            // Lowercase
            Arguments.of("tolerated", SIFT.TOLERATED),
            Arguments.of("deleterious", SIFT.DELETERIOUS),
            // Mixed case
            Arguments.of("Tolerated", SIFT.TOLERATED),
            Arguments.of("Deleterious", SIFT.DELETERIOUS),
            Arguments.of("ToLeRaTeD", SIFT.TOLERATED),
            Arguments.of("DeLeTerIoUs", SIFT.DELETERIOUS)
        );
    }

    @Test
    @DisplayName("Invalid SIFT value returns UNRECOGNIZED")
    void testInvalidSiftValue() {
        assertThat(SIFTMapper.fromString("BENIGN")).isEqualTo(SIFT.UNRECOGNIZED);
        assertThat(SIFTMapper.fromString("DAMAGING")).isEqualTo(SIFT.UNRECOGNIZED);
        assertThat(SIFTMapper.fromString("NEUTRAL")).isEqualTo(SIFT.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "BENIGN", "DAMAGING", "NEUTRAL", "LOW_CONFIDENCE"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(SIFTMapper.fromString(input)).isEqualTo(SIFT.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(SIFTMapper.fromString("  TOLERATED  ")).isEqualTo(SIFT.TOLERATED);
        assertThat(SIFTMapper.fromString("\tDELETERIOUS\t")).isEqualTo(SIFT.DELETERIOUS);
    }

    @Test
    @DisplayName("Null input returns UNRECOGNIZED")
    void testNullInput() {
        assertThat(SIFTMapper.fromString(null)).isEqualTo(SIFT.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Empty and blank strings return UNRECOGNIZED")
    void testEmptyAndBlankStrings() {
        assertThat(SIFTMapper.fromString("")).isEqualTo(SIFT.UNRECOGNIZED);
        assertThat(SIFTMapper.fromString("   ")).isEqualTo(SIFT.UNRECOGNIZED);
        assertThat(SIFTMapper.fromString("\t\n")).isEqualTo(SIFT.UNRECOGNIZED);
    }

    @Test
    @DisplayName("SIFT extended values not supported (no low confidence variants)")
    void testExtendedValuesNotSupported() {
        // SIFT sometimes reports "tolerated_low_confidence" or "deleterious_low_confidence"
        // but our enum only has the base values
        assertThat(SIFTMapper.fromString("TOLERATED_LOW_CONFIDENCE")).isEqualTo(SIFT.UNRECOGNIZED);
        assertThat(SIFTMapper.fromString("DELETERIOUS_LOW_CONFIDENCE")).isEqualTo(SIFT.UNRECOGNIZED);
    }
}
