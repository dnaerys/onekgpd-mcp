package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.Consequence;
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
 * Unit tests for ConsequencesMapper.
 * Tests mapping of string values to Consequence enum.
 *
 * Test Case IDs: CON-001 through CON-007
 */
@DisplayName("ConsequencesMapper Tests")
class ConsequencesMapperTest {

    @Test
    @DisplayName("CON-001: 'STOP_GAINED' returns Consequence.STOP_GAINED")
    void testStopGained() {
        assertThat(ConsequencesMapper.fromString("STOP_GAINED")).isEqualTo(Consequence.STOP_GAINED);
    }

    @Test
    @DisplayName("CON-002: 'FRAMESHIFT_VARIANT' returns Consequence.FRAMESHIFT_VARIANT")
    void testFrameshiftVariant() {
        assertThat(ConsequencesMapper.fromString("FRAMESHIFT_VARIANT")).isEqualTo(Consequence.FRAMESHIFT_VARIANT);
    }

    @Test
    @DisplayName("CON-003: 'MISSENSE_VARIANT' returns Consequence.MISSENSE_VARIANT")
    void testMissenseVariant() {
        assertThat(ConsequencesMapper.fromString("MISSENSE_VARIANT")).isEqualTo(Consequence.MISSENSE_VARIANT);
    }

    @Test
    @DisplayName("CON-004: UTR variants map correctly")
    void testUtrVariants() {
        assertThat(ConsequencesMapper.fromString("FIVE_PRIME_UTR_VARIANT")).isEqualTo(Consequence.FIVE_PRIME_UTR_VARIANT);
        assertThat(ConsequencesMapper.fromString("THREE_PRIME_UTR_VARIANT")).isEqualTo(Consequence.THREE_PRIME_UTR_VARIANT);
    }

    @Test
    @DisplayName("CON-005: With dash 'stop-gained' returns Consequence.STOP_GAINED")
    void testWithDash() {
        assertThat(ConsequencesMapper.fromString("stop-gained")).isEqualTo(Consequence.STOP_GAINED);
    }

    @ParameterizedTest(name = "CON-006: Valid consequence ''{0}'' maps correctly")
    @DisplayName("CON-006: All valid consequence values map to respective enums")
    @MethodSource("validConsequenceValues")
    void testAllValidConsequences(String input, Consequence expected) {
        assertThat(ConsequencesMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validConsequenceValues() {
        return Stream.of(
            // High impact consequences
            Arguments.of("TRANSCRIPT_ABLATION", Consequence.TRANSCRIPT_ABLATION),
            Arguments.of("SPLICE_ACCEPTOR_VARIANT", Consequence.SPLICE_ACCEPTOR_VARIANT),
            Arguments.of("SPLICE_DONOR_VARIANT", Consequence.SPLICE_DONOR_VARIANT),
            Arguments.of("STOP_GAINED", Consequence.STOP_GAINED),
            Arguments.of("FRAMESHIFT_VARIANT", Consequence.FRAMESHIFT_VARIANT),
            Arguments.of("STOP_LOST", Consequence.STOP_LOST),
            Arguments.of("START_LOST", Consequence.START_LOST),

            // Moderate impact consequences
            Arguments.of("TRANSCRIPT_AMPLIFICATION", Consequence.TRANSCRIPT_AMPLIFICATION),
            Arguments.of("INFRAME_INSERTION", Consequence.INFRAME_INSERTION),
            Arguments.of("INFRAME_DELETION", Consequence.INFRAME_DELETION),
            Arguments.of("MISSENSE_VARIANT", Consequence.MISSENSE_VARIANT),
            Arguments.of("PROTEIN_ALTERING_VARIANT", Consequence.PROTEIN_ALTERING_VARIANT),

            // Low impact consequences
            Arguments.of("SPLICE_REGION_VARIANT", Consequence.SPLICE_REGION_VARIANT),
            Arguments.of("SYNONYMOUS_VARIANT", Consequence.SYNONYMOUS_VARIANT),
            Arguments.of("START_RETAINED_VARIANT", Consequence.START_RETAINED_VARIANT),
            Arguments.of("STOP_RETAINED_VARIANT", Consequence.STOP_RETAINED_VARIANT),

            // Modifier consequences
            Arguments.of("FIVE_PRIME_UTR_VARIANT", Consequence.FIVE_PRIME_UTR_VARIANT),
            Arguments.of("THREE_PRIME_UTR_VARIANT", Consequence.THREE_PRIME_UTR_VARIANT),
            Arguments.of("INTRON_VARIANT", Consequence.INTRON_VARIANT),
            Arguments.of("UPSTREAM_GENE_VARIANT", Consequence.UPSTREAM_GENE_VARIANT),
            Arguments.of("DOWNSTREAM_GENE_VARIANT", Consequence.DOWNSTREAM_GENE_VARIANT),
            Arguments.of("INTERGENIC_VARIANT", Consequence.INTERGENIC_VARIANT),

            // Splice variants
            Arguments.of("SPLICE_POLYPYRIMIDINE_TRACT_VARIANT", Consequence.SPLICE_POLYPYRIMIDINE_TRACT_VARIANT),
            Arguments.of("SPLICE_DONOR_5TH_BASE_VARIANT", Consequence.SPLICE_DONOR_5TH_BASE_VARIANT),
            Arguments.of("SPLICE_DONOR_REGION_VARIANT", Consequence.SPLICE_DONOR_REGION_VARIANT),

            // Regulatory consequences
            Arguments.of("TFBS_ABLATION", Consequence.TFBS_ABLATION),
            Arguments.of("TFBS_AMPLIFICATION", Consequence.TFBS_AMPLIFICATION),
            Arguments.of("TF_BINDING_SITE_VARIANT", Consequence.TF_BINDING_SITE_VARIANT),
            Arguments.of("REGULATORY_REGION_VARIANT", Consequence.REGULATORY_REGION_VARIANT),

            // Other consequences
            Arguments.of("CODING_SEQUENCE_VARIANT", Consequence.CODING_SEQUENCE_VARIANT),
            Arguments.of("MATURE_MIRNA_VARIANT", Consequence.MATURE_MIRNA_VARIANT),
            Arguments.of("NMD_TRANSCRIPT_VARIANT", Consequence.NMD_TRANSCRIPT_VARIANT),
            Arguments.of("NON_CODING_TRANSCRIPT_VARIANT", Consequence.NON_CODING_TRANSCRIPT_VARIANT),
            Arguments.of("NON_CODING_TRANSCRIPT_EXON_VARIANT", Consequence.NON_CODING_TRANSCRIPT_EXON_VARIANT),
            Arguments.of("FEATURE_ELONGATION", Consequence.FEATURE_ELONGATION),
            Arguments.of("FEATURE_TRUNCATION", Consequence.FEATURE_TRUNCATION),
            Arguments.of("SEQUENCE_VARIANT", Consequence.SEQUENCE_VARIANT),
            Arguments.of("CODING_TRANSCRIPT_VARIANT", Consequence.CODING_TRANSCRIPT_VARIANT),

            // Case insensitivity
            Arguments.of("stop_gained", Consequence.STOP_GAINED),
            Arguments.of("missense_variant", Consequence.MISSENSE_VARIANT),
            Arguments.of("Frameshift_Variant", Consequence.FRAMESHIFT_VARIANT)
        );
    }

    @Test
    @DisplayName("CON-007: Invalid 'UNKNOWN_CONSEQUENCE' returns Consequence.UNRECOGNIZED")
    void testInvalidConsequence() {
        assertThat(ConsequencesMapper.fromString("UNKNOWN_CONSEQUENCE")).isEqualTo(Consequence.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "STOP", "FRAMESHIFT", "BENIGN", "PATHOGENIC"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(ConsequencesMapper.fromString(input)).isEqualTo(Consequence.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(ConsequencesMapper.fromString("  STOP_GAINED  ")).isEqualTo(Consequence.STOP_GAINED);
        assertThat(ConsequencesMapper.fromString("\tMISSENSE_VARIANT\t")).isEqualTo(Consequence.MISSENSE_VARIANT);
    }

    @Test
    @DisplayName("Dash and space normalization works correctly")
    void testNormalization() {
        assertThat(ConsequencesMapper.fromString("stop-gained")).isEqualTo(Consequence.STOP_GAINED);
        assertThat(ConsequencesMapper.fromString("stop gained")).isEqualTo(Consequence.STOP_GAINED);
        assertThat(ConsequencesMapper.fromString("frameshift-variant")).isEqualTo(Consequence.FRAMESHIFT_VARIANT);
        assertThat(ConsequencesMapper.fromString("frameshift variant")).isEqualTo(Consequence.FRAMESHIFT_VARIANT);
    }
}
