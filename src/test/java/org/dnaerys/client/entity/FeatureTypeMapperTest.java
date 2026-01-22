package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.FeatureType;
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
 * Unit tests for FeatureTypeMapper.
 * Tests mapping of string values to FeatureType enum.
 *
 * FeatureType has only 3 valid values: TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE
 */
@DisplayName("FeatureTypeMapper Tests")
class FeatureTypeMapperTest {

    @Test
    @DisplayName("TRANSCRIPT maps correctly")
    void testTranscript() {
        assertThat(FeatureTypeMapper.fromString("TRANSCRIPT")).isEqualTo(FeatureType.TRANSCRIPT);
    }

    @Test
    @DisplayName("REGULATORYFEATURE maps correctly")
    void testRegulatoryFeature() {
        assertThat(FeatureTypeMapper.fromString("REGULATORYFEATURE")).isEqualTo(FeatureType.REGULATORYFEATURE);
    }

    @Test
    @DisplayName("MOTIFFEATURE maps correctly")
    void testMotifFeature() {
        assertThat(FeatureTypeMapper.fromString("MOTIFFEATURE")).isEqualTo(FeatureType.MOTIFFEATURE);
    }

    @ParameterizedTest(name = "Valid FeatureType ''{0}'' maps correctly")
    @DisplayName("All valid FeatureType values map to respective enums")
    @MethodSource("validFeatureTypeValues")
    void testAllValidFeatureTypes(String input, FeatureType expected) {
        assertThat(FeatureTypeMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validFeatureTypeValues() {
        return Stream.of(
            // Uppercase
            Arguments.of("TRANSCRIPT", FeatureType.TRANSCRIPT),
            Arguments.of("REGULATORYFEATURE", FeatureType.REGULATORYFEATURE),
            Arguments.of("MOTIFFEATURE", FeatureType.MOTIFFEATURE),
            // Lowercase
            Arguments.of("transcript", FeatureType.TRANSCRIPT),
            Arguments.of("regulatoryfeature", FeatureType.REGULATORYFEATURE),
            Arguments.of("motiffeature", FeatureType.MOTIFFEATURE),
            // Mixed case
            Arguments.of("Transcript", FeatureType.TRANSCRIPT),
            Arguments.of("RegulatoryFeature", FeatureType.REGULATORYFEATURE),
            Arguments.of("MotifFeature", FeatureType.MOTIFFEATURE)
        );
    }

    @Test
    @DisplayName("Invalid feature type returns UNRECOGNIZED")
    void testInvalidFeatureType() {
        assertThat(FeatureTypeMapper.fromString("GENE")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("EXON")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("INTRON")).isEqualTo(FeatureType.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "GENE", "REGULATORY_FEATURE", "MOTIF_FEATURE", "PROTEIN"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(FeatureTypeMapper.fromString(input)).isEqualTo(FeatureType.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(FeatureTypeMapper.fromString("  TRANSCRIPT  ")).isEqualTo(FeatureType.TRANSCRIPT);
        assertThat(FeatureTypeMapper.fromString("\tREGULATORYFEATURE\t")).isEqualTo(FeatureType.REGULATORYFEATURE);
    }

    @Test
    @DisplayName("Note: Underscore variants do NOT map (no underscores in enum names)")
    void testUnderscoreVariantsDoNotMap() {
        // The enum values are REGULATORYFEATURE and MOTIFFEATURE (no underscores)
        // So "REGULATORY_FEATURE" would normalize to "REGULATORY_FEATURE" which is not valid
        assertThat(FeatureTypeMapper.fromString("REGULATORY_FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("MOTIF_FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Note: Space/dash variants normalize to underscore which doesn't match")
    void testSpaceDashVariants() {
        // "REGULATORY FEATURE" -> "REGULATORY_FEATURE" which is not valid
        assertThat(FeatureTypeMapper.fromString("REGULATORY FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("REGULATORY-FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("MOTIF FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
        assertThat(FeatureTypeMapper.fromString("MOTIF-FEATURE")).isEqualTo(FeatureType.UNRECOGNIZED);
    }
}
