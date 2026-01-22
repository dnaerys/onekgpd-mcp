package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.PolyPhen;
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
 * Unit tests for PolyPhenMapper.
 * Tests mapping of string values to PolyPhen enum.
 *
 * PolyPhen has 4 valid prediction values: BENIGN, POSSIBLY_DAMAGING, PROBABLY_DAMAGING, UNKNOWN
 */
@DisplayName("PolyPhenMapper Tests")
class PolyPhenMapperTest {

    @Test
    @DisplayName("BENIGN maps correctly")
    void testBenign() {
        assertThat(PolyPhenMapper.fromString("BENIGN")).isEqualTo(PolyPhen.BENIGN);
    }

    @Test
    @DisplayName("POSSIBLY_DAMAGING maps correctly")
    void testPossiblyDamaging() {
        assertThat(PolyPhenMapper.fromString("POSSIBLY_DAMAGING")).isEqualTo(PolyPhen.POSSIBLY_DAMAGING);
    }

    @Test
    @DisplayName("PROBABLY_DAMAGING maps correctly")
    void testProbablyDamaging() {
        assertThat(PolyPhenMapper.fromString("PROBABLY_DAMAGING")).isEqualTo(PolyPhen.PROBABLY_DAMAGING);
    }

    @Test
    @DisplayName("UNKNOWN maps correctly")
    void testUnknown() {
        assertThat(PolyPhenMapper.fromString("UNKNOWN")).isEqualTo(PolyPhen.UNKNOWN);
    }

    @ParameterizedTest(name = "Valid PolyPhen ''{0}'' maps correctly")
    @DisplayName("All valid PolyPhen values map to respective enums")
    @MethodSource("validPolyPhenValues")
    void testAllValidPolyPhenValues(String input, PolyPhen expected) {
        assertThat(PolyPhenMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validPolyPhenValues() {
        return Stream.of(
            // Uppercase
            Arguments.of("BENIGN", PolyPhen.BENIGN),
            Arguments.of("POSSIBLY_DAMAGING", PolyPhen.POSSIBLY_DAMAGING),
            Arguments.of("PROBABLY_DAMAGING", PolyPhen.PROBABLY_DAMAGING),
            Arguments.of("UNKNOWN", PolyPhen.UNKNOWN),
            // Lowercase
            Arguments.of("benign", PolyPhen.BENIGN),
            Arguments.of("possibly_damaging", PolyPhen.POSSIBLY_DAMAGING),
            Arguments.of("probably_damaging", PolyPhen.PROBABLY_DAMAGING),
            Arguments.of("unknown", PolyPhen.UNKNOWN),
            // Mixed case
            Arguments.of("Benign", PolyPhen.BENIGN),
            Arguments.of("Possibly_Damaging", PolyPhen.POSSIBLY_DAMAGING),
            Arguments.of("Probably_Damaging", PolyPhen.PROBABLY_DAMAGING),
            Arguments.of("Unknown", PolyPhen.UNKNOWN)
        );
    }

    @Test
    @DisplayName("Dash and space normalization works correctly")
    void testNormalization() {
        assertThat(PolyPhenMapper.fromString("POSSIBLY-DAMAGING")).isEqualTo(PolyPhen.POSSIBLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("PROBABLY-DAMAGING")).isEqualTo(PolyPhen.PROBABLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("POSSIBLY DAMAGING")).isEqualTo(PolyPhen.POSSIBLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("PROBABLY DAMAGING")).isEqualTo(PolyPhen.PROBABLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("possibly-damaging")).isEqualTo(PolyPhen.POSSIBLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("probably damaging")).isEqualTo(PolyPhen.PROBABLY_DAMAGING);
    }

    @Test
    @DisplayName("Invalid PolyPhen value returns UNRECOGNIZED")
    void testInvalidPolyPhenValue() {
        assertThat(PolyPhenMapper.fromString("DAMAGING")).isEqualTo(PolyPhen.UNRECOGNIZED);
        assertThat(PolyPhenMapper.fromString("NEUTRAL")).isEqualTo(PolyPhen.UNRECOGNIZED);
        assertThat(PolyPhenMapper.fromString("TOLERATED")).isEqualTo(PolyPhen.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "DAMAGING", "NEUTRAL", "TOLERATED", "DELETERIOUS"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(PolyPhenMapper.fromString(input)).isEqualTo(PolyPhen.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(PolyPhenMapper.fromString("  BENIGN  ")).isEqualTo(PolyPhen.BENIGN);
        assertThat(PolyPhenMapper.fromString("\tPOSSIBLY_DAMAGING\t")).isEqualTo(PolyPhen.POSSIBLY_DAMAGING);
        assertThat(PolyPhenMapper.fromString("  probably_damaging  ")).isEqualTo(PolyPhen.PROBABLY_DAMAGING);
    }

    @Test
    @DisplayName("Null input returns UNRECOGNIZED")
    void testNullInput() {
        assertThat(PolyPhenMapper.fromString(null)).isEqualTo(PolyPhen.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Empty and blank strings return UNRECOGNIZED")
    void testEmptyAndBlankStrings() {
        assertThat(PolyPhenMapper.fromString("")).isEqualTo(PolyPhen.UNRECOGNIZED);
        assertThat(PolyPhenMapper.fromString("   ")).isEqualTo(PolyPhen.UNRECOGNIZED);
        assertThat(PolyPhenMapper.fromString("\t\n")).isEqualTo(PolyPhen.UNRECOGNIZED);
    }
}
