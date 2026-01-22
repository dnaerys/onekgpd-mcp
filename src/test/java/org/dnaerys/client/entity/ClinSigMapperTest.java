package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.ClinSignificance;
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
 * Unit tests for ClinSigMapper.
 * Tests mapping of string values to ClinSignificance enum.
 *
 * Note: ClinVar significance values use a different naming convention in the proto:
 * - BENIGN maps to CLNSIG_BENIGN (not just BENIGN)
 */
@DisplayName("ClinSigMapper Tests")
class ClinSigMapperTest {

    @Test
    @DisplayName("Pathogenic values map correctly")
    void testPathogenicValues() {
        assertThat(ClinSigMapper.fromString("PATHOGENIC")).isEqualTo(ClinSignificance.PATHOGENIC);
        assertThat(ClinSigMapper.fromString("LIKELY_PATHOGENIC")).isEqualTo(ClinSignificance.LIKELY_PATHOGENIC);
        assertThat(ClinSigMapper.fromString("PATHOGENIC_LOW_PENETRANCE")).isEqualTo(ClinSignificance.PATHOGENIC_LOW_PENETRANCE);
        assertThat(ClinSigMapper.fromString("LIKELY_PATHOGENIC_LOW_PENETRANCE")).isEqualTo(ClinSignificance.LIKELY_PATHOGENIC_LOW_PENETRANCE);
    }

    @Test
    @DisplayName("Benign values map correctly")
    void testBenignValues() {
        // Note: Plain "BENIGN" maps to CLNSIG_BENIGN in the proto
        assertThat(ClinSigMapper.fromString("CLNSIG_BENIGN")).isEqualTo(ClinSignificance.CLNSIG_BENIGN);
        assertThat(ClinSigMapper.fromString("LIKELY_BENIGN")).isEqualTo(ClinSignificance.LIKELY_BENIGN);
    }

    @Test
    @DisplayName("Uncertain significance maps correctly")
    void testUncertainSignificance() {
        assertThat(ClinSigMapper.fromString("UNCERTAIN_SIGNIFICANCE")).isEqualTo(ClinSignificance.UNCERTAIN_SIGNIFICANCE);
    }

    @Test
    @DisplayName("Other clinical significance values map correctly")
    void testOtherValues() {
        assertThat(ClinSigMapper.fromString("DRUG_RESPONSE")).isEqualTo(ClinSignificance.DRUG_RESPONSE);
        assertThat(ClinSigMapper.fromString("ASSOCIATION")).isEqualTo(ClinSignificance.ASSOCIATION);
        assertThat(ClinSigMapper.fromString("RISK_FACTOR")).isEqualTo(ClinSignificance.RISK_FACTOR);
        assertThat(ClinSigMapper.fromString("PROTECTIVE")).isEqualTo(ClinSignificance.PROTECTIVE);
        assertThat(ClinSigMapper.fromString("AFFECTS")).isEqualTo(ClinSignificance.AFFECTS);
        assertThat(ClinSigMapper.fromString("CONFERS_SENSITIVITY")).isEqualTo(ClinSignificance.CONFERS_SENSITIVITY);
        assertThat(ClinSigMapper.fromString("NOT_PROVIDED")).isEqualTo(ClinSignificance.NOT_PROVIDED);
        assertThat(ClinSigMapper.fromString("OTHER")).isEqualTo(ClinSignificance.OTHER);
    }

    @Test
    @DisplayName("Conflicting interpretations maps correctly")
    void testConflictingInterpretations() {
        assertThat(ClinSigMapper.fromString("CONFLICTING_INTERPRETATIONS")).isEqualTo(ClinSignificance.CONFLICTING_INTERPRETATIONS);
    }

    @Test
    @DisplayName("Risk allele values map correctly")
    void testRiskAlleleValues() {
        assertThat(ClinSigMapper.fromString("UNCERTAIN_RISK_ALLELE")).isEqualTo(ClinSignificance.UNCERTAIN_RISK_ALLELE);
        assertThat(ClinSigMapper.fromString("LIKELY_RISK_ALLELE")).isEqualTo(ClinSignificance.LIKELY_RISK_ALLELE);
        assertThat(ClinSigMapper.fromString("ESTABLISHED_RISK_ALLELE")).isEqualTo(ClinSignificance.ESTABLISHED_RISK_ALLELE);
    }

    @ParameterizedTest(name = "Valid ClinSig ''{0}'' maps correctly")
    @DisplayName("All valid ClinSignificance values map to respective enums")
    @MethodSource("validClinSigValues")
    void testAllValidClinSigValues(String input, ClinSignificance expected) {
        assertThat(ClinSigMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validClinSigValues() {
        return Stream.of(
            Arguments.of("CLNSIG_BENIGN", ClinSignificance.CLNSIG_BENIGN),
            Arguments.of("LIKELY_BENIGN", ClinSignificance.LIKELY_BENIGN),
            Arguments.of("UNCERTAIN_SIGNIFICANCE", ClinSignificance.UNCERTAIN_SIGNIFICANCE),
            Arguments.of("LIKELY_PATHOGENIC", ClinSignificance.LIKELY_PATHOGENIC),
            Arguments.of("PATHOGENIC", ClinSignificance.PATHOGENIC),
            Arguments.of("DRUG_RESPONSE", ClinSignificance.DRUG_RESPONSE),
            Arguments.of("ASSOCIATION", ClinSignificance.ASSOCIATION),
            Arguments.of("RISK_FACTOR", ClinSignificance.RISK_FACTOR),
            Arguments.of("PROTECTIVE", ClinSignificance.PROTECTIVE),
            Arguments.of("AFFECTS", ClinSignificance.AFFECTS),
            Arguments.of("CONFERS_SENSITIVITY", ClinSignificance.CONFERS_SENSITIVITY),
            Arguments.of("CONFLICTING_INTERPRETATIONS", ClinSignificance.CONFLICTING_INTERPRETATIONS),
            Arguments.of("NOT_PROVIDED", ClinSignificance.NOT_PROVIDED),
            Arguments.of("OTHER", ClinSignificance.OTHER),
            Arguments.of("LIKELY_PATHOGENIC_LOW_PENETRANCE", ClinSignificance.LIKELY_PATHOGENIC_LOW_PENETRANCE),
            Arguments.of("PATHOGENIC_LOW_PENETRANCE", ClinSignificance.PATHOGENIC_LOW_PENETRANCE),
            Arguments.of("UNCERTAIN_RISK_ALLELE", ClinSignificance.UNCERTAIN_RISK_ALLELE),
            Arguments.of("LIKELY_RISK_ALLELE", ClinSignificance.LIKELY_RISK_ALLELE),
            Arguments.of("ESTABLISHED_RISK_ALLELE", ClinSignificance.ESTABLISHED_RISK_ALLELE),
            // Case insensitivity
            Arguments.of("pathogenic", ClinSignificance.PATHOGENIC),
            Arguments.of("likely_pathogenic", ClinSignificance.LIKELY_PATHOGENIC),
            Arguments.of("Uncertain_Significance", ClinSignificance.UNCERTAIN_SIGNIFICANCE)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "BENIGN", "MALIGNANT", "UNKNOWN_SIGNIFICANCE"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(ClinSigMapper.fromString(input)).isEqualTo(ClinSignificance.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(ClinSigMapper.fromString("  PATHOGENIC  ")).isEqualTo(ClinSignificance.PATHOGENIC);
        assertThat(ClinSigMapper.fromString("\tLIKELY_PATHOGENIC\t")).isEqualTo(ClinSignificance.LIKELY_PATHOGENIC);
    }

    @Test
    @DisplayName("Dash and space normalization works correctly")
    void testNormalization() {
        assertThat(ClinSigMapper.fromString("LIKELY-PATHOGENIC")).isEqualTo(ClinSignificance.LIKELY_PATHOGENIC);
        assertThat(ClinSigMapper.fromString("LIKELY PATHOGENIC")).isEqualTo(ClinSignificance.LIKELY_PATHOGENIC);
        assertThat(ClinSigMapper.fromString("uncertain-significance")).isEqualTo(ClinSignificance.UNCERTAIN_SIGNIFICANCE);
        assertThat(ClinSigMapper.fromString("drug response")).isEqualTo(ClinSignificance.DRUG_RESPONSE);
    }
}
