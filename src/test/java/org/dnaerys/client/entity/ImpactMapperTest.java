package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.Impact;
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
 * Unit tests for ImpactMapper.
 * Tests mapping of string values to Impact enum.
 *
 * Test Case IDs: IMP-001 through IMP-010
 */
@DisplayName("ImpactMapper Tests")
class ImpactMapperTest {

    @Test
    @DisplayName("IMP-001: Valid uppercase 'HIGH' returns Impact.HIGH")
    void testValidUppercase() {
        assertThat(ImpactMapper.fromString("HIGH")).isEqualTo(Impact.HIGH);
    }

    @Test
    @DisplayName("IMP-002: Valid lowercase 'high' returns Impact.HIGH")
    void testValidLowercase() {
        assertThat(ImpactMapper.fromString("high")).isEqualTo(Impact.HIGH);
    }

    @Test
    @DisplayName("IMP-003: Valid mixed case 'High' returns Impact.HIGH")
    void testValidMixedCase() {
        assertThat(ImpactMapper.fromString("High")).isEqualTo(Impact.HIGH);
    }

    @Test
    @DisplayName("IMP-004: Whitespace trimming '  HIGH  ' returns Impact.HIGH")
    void testWhitespaceTrimming() {
        assertThat(ImpactMapper.fromString("  HIGH  ")).isEqualTo(Impact.HIGH);
    }

    @ParameterizedTest(name = "IMP-005: Valid value ''{0}'' maps correctly")
    @DisplayName("IMP-005: All valid values map to respective enums")
    @MethodSource("validImpactValues")
    void testAllValidValues(String input, Impact expected) {
        assertThat(ImpactMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validImpactValues() {
        return Stream.of(
            Arguments.of("HIGH", Impact.HIGH),
            Arguments.of("MODERATE", Impact.MODERATE),
            Arguments.of("LOW", Impact.LOW),
            Arguments.of("MODIFIER", Impact.MODIFIER),
            Arguments.of("moderate", Impact.MODERATE),
            Arguments.of("low", Impact.LOW),
            Arguments.of("modifier", Impact.MODIFIER)
        );
    }

    @Test
    @DisplayName("IMP-006: Invalid value 'CRITICAL' returns Impact.UNRECOGNIZED")
    void testInvalidValue() {
        assertThat(ImpactMapper.fromString("CRITICAL")).isEqualTo(Impact.UNRECOGNIZED);
    }

    @Test
    @DisplayName("IMP-007: Empty string returns Impact.UNRECOGNIZED")
    void testEmptyString() {
        assertThat(ImpactMapper.fromString("")).isEqualTo(Impact.UNRECOGNIZED);
    }

    @Test
    @DisplayName("IMP-008: Null input returns Impact.UNRECOGNIZED")
    void testNullInput() {
        assertThat(ImpactMapper.fromString(null)).isEqualTo(Impact.UNRECOGNIZED);
    }

    @Test
    @DisplayName("IMP-009: Dash normalization 'HIGH-IMPACT' returns Impact.UNRECOGNIZED")
    void testDashNormalization() {
        // "HIGH-IMPACT" normalizes to "HIGH_IMPACT" which is not a valid enum value
        assertThat(ImpactMapper.fromString("HIGH-IMPACT")).isEqualTo(Impact.UNRECOGNIZED);
    }

    @Test
    @DisplayName("IMP-010: Space normalization 'HIGH IMPACT' returns Impact.UNRECOGNIZED")
    void testSpaceNormalization() {
        // "HIGH IMPACT" normalizes to "HIGH_IMPACT" which is not a valid enum value
        assertThat(ImpactMapper.fromString("HIGH IMPACT")).isEqualTo(Impact.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "INVALID", "HIGH_IMPACT", "VERY_HIGH"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(ImpactMapper.fromString(input)).isEqualTo(Impact.UNRECOGNIZED);
    }
}
