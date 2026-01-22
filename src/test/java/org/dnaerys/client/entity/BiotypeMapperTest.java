package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.BioType;
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
 * Unit tests for BiotypeMapper.
 * Tests mapping of string values to BioType enum.
 *
 * Test Case IDs: BIO-001 through BIO-007
 */
@DisplayName("BiotypeMapper Tests")
class BiotypeMapperTest {

    @Test
    @DisplayName("BIO-001: Protein coding returns BioType.PROTEIN_CODING")
    void testProteinCoding() {
        assertThat(BiotypeMapper.fromString("PROTEIN_CODING")).isEqualTo(BioType.PROTEIN_CODING);
    }

    @Test
    @DisplayName("BIO-002: lncRNA lowercase returns BioType.LNCRNA")
    void testLncRnaLowercase() {
        assertThat(BiotypeMapper.fromString("lncrna")).isEqualTo(BioType.LNCRNA);
    }

    @Test
    @DisplayName("BIO-003: Pseudogene returns BioType.PSEUDOGENE")
    void testPseudogene() {
        assertThat(BiotypeMapper.fromString("PSEUDOGENE")).isEqualTo(BioType.PSEUDOGENE);
    }

    @Test
    @DisplayName("BIO-004: With spaces 'protein coding' returns BioType.PROTEIN_CODING")
    void testWithSpaces() {
        assertThat(BiotypeMapper.fromString("protein coding")).isEqualTo(BioType.PROTEIN_CODING);
    }

    @Test
    @DisplayName("BIO-005: With dashes 'protein-coding' returns BioType.PROTEIN_CODING")
    void testWithDashes() {
        assertThat(BiotypeMapper.fromString("protein-coding")).isEqualTo(BioType.PROTEIN_CODING);
    }

    @ParameterizedTest(name = "BIO-006: Valid biotype ''{0}'' maps correctly")
    @DisplayName("BIO-006: All valid biotype values map to respective enums")
    @MethodSource("validBiotypeValues")
    void testAllValidBiotypes(String input, BioType expected) {
        assertThat(BiotypeMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validBiotypeValues() {
        return Stream.of(
            // Core biotypes
            Arguments.of("PROTEIN_CODING", BioType.PROTEIN_CODING),
            Arguments.of("LNCRNA", BioType.LNCRNA),
            Arguments.of("PSEUDOGENE", BioType.PSEUDOGENE),
            Arguments.of("PROCESSED_PSEUDOGENE", BioType.PROCESSED_PSEUDOGENE),
            Arguments.of("UNPROCESSED_PSEUDOGENE", BioType.UNPROCESSED_PSEUDOGENE),

            // RNA types
            Arguments.of("MIRNA", BioType.MIRNA),
            Arguments.of("SNRNA", BioType.SNRNA),
            Arguments.of("SNORNA", BioType.SNORNA),
            Arguments.of("RRNA", BioType.RRNA),
            Arguments.of("TRNA", BioType.TRNA),
            Arguments.of("VAULTRNA", BioType.VAULTRNA),

            // Immunoglobulin genes
            Arguments.of("IG_C_GENE", BioType.IG_C_GENE),
            Arguments.of("IG_D_GENE", BioType.IG_D_GENE),
            Arguments.of("IG_J_GENE", BioType.IG_J_GENE),
            Arguments.of("IG_V_GENE", BioType.IG_V_GENE),

            // T-cell receptor genes
            Arguments.of("TR_C_GENE", BioType.TR_C_GENE),
            Arguments.of("TR_D_GENE", BioType.TR_D_GENE),
            Arguments.of("TR_J_GENE", BioType.TR_J_GENE),
            Arguments.of("TR_V_GENE", BioType.TR_V_GENE),

            // Other biotypes
            Arguments.of("PROCESSED_TRANSCRIPT", BioType.PROCESSED_TRANSCRIPT),
            Arguments.of("NONSENSE_MEDIATED_DECAY", BioType.NONSENSE_MEDIATED_DECAY),
            Arguments.of("RETAINED_INTRON", BioType.RETAINED_INTRON),
            Arguments.of("ANTISENSE", BioType.ANTISENSE),

            // RegulatoryFeature biotypes
            Arguments.of("PROMOTER", BioType.PROMOTER),
            Arguments.of("ENHANCER", BioType.ENHANCER),
            Arguments.of("CTCF_BINDING_SITE", BioType.CTCF_BINDING_SITE),

            // Case insensitivity
            Arguments.of("protein_coding", BioType.PROTEIN_CODING),
            Arguments.of("Pseudogene", BioType.PSEUDOGENE)
        );
    }

    @Test
    @DisplayName("BIO-007: Invalid biotype 'UNKNOWN_TYPE' returns BioType.UNRECOGNIZED")
    void testInvalidBiotype() {
        assertThat(BiotypeMapper.fromString("UNKNOWN_TYPE")).isEqualTo(BioType.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "CODING", "NON_CODING_GENE"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(BiotypeMapper.fromString(input)).isEqualTo(BioType.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(BiotypeMapper.fromString("  PROTEIN_CODING  ")).isEqualTo(BioType.PROTEIN_CODING);
        assertThat(BiotypeMapper.fromString("\tLNCRNA\t")).isEqualTo(BioType.LNCRNA);
    }
}
