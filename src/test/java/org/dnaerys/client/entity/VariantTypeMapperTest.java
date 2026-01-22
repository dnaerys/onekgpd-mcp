package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.VariantType;
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
 * Unit tests for VariantTypeMapper.
 * Tests mapping of string values to VariantType enum.
 *
 * VariantType contains Sequence Ontology Variant class terms.
 */
@DisplayName("VariantTypeMapper Tests")
class VariantTypeMapperTest {

    @Test
    @DisplayName("SNV maps correctly")
    void testSnv() {
        assertThat(VariantTypeMapper.fromString("SNV")).isEqualTo(VariantType.SNV);
    }

    @Test
    @DisplayName("INSERTION maps correctly")
    void testInsertion() {
        assertThat(VariantTypeMapper.fromString("INSERTION")).isEqualTo(VariantType.INSERTION);
    }

    @Test
    @DisplayName("DELETION maps correctly")
    void testDeletion() {
        assertThat(VariantTypeMapper.fromString("DELETION")).isEqualTo(VariantType.DELETION);
    }

    @Test
    @DisplayName("INDEL maps correctly")
    void testIndel() {
        assertThat(VariantTypeMapper.fromString("INDEL")).isEqualTo(VariantType.INDEL);
    }

    @Test
    @DisplayName("SUBSTITUTION maps correctly")
    void testSubstitution() {
        assertThat(VariantTypeMapper.fromString("SUBSTITUTION")).isEqualTo(VariantType.SUBSTITUTION);
    }

    @ParameterizedTest(name = "Valid VariantType ''{0}'' maps correctly")
    @DisplayName("All valid VariantType values map to respective enums")
    @MethodSource("validVariantTypeValues")
    void testAllValidVariantTypes(String input, VariantType expected) {
        assertThat(VariantTypeMapper.fromString(input)).isEqualTo(expected);
    }

    static Stream<Arguments> validVariantTypeValues() {
        return Stream.of(
            // Basic variant types
            Arguments.of("SNV", VariantType.SNV),
            Arguments.of("INSERTION", VariantType.INSERTION),
            Arguments.of("DELETION", VariantType.DELETION),
            Arguments.of("INDEL", VariantType.INDEL),
            Arguments.of("SUBSTITUTION", VariantType.SUBSTITUTION),

            // Structural variants
            Arguments.of("INVERSION", VariantType.INVERSION),
            Arguments.of("TRANSLOCATION", VariantType.TRANSLOCATION),
            Arguments.of("DUPLICATION", VariantType.DUPLICATION),
            Arguments.of("TANDEM_DUPLICATION", VariantType.TANDEM_DUPLICATION),

            // Copy number variants
            Arguments.of("COPY_NUMBER_GAIN", VariantType.COPY_NUMBER_GAIN),
            Arguments.of("COPY_NUMBER_LOSS", VariantType.COPY_NUMBER_LOSS),
            Arguments.of("COPY_NUMBER_VARIATION", VariantType.COPY_NUMBER_VARIATION),

            // Mobile element variants
            Arguments.of("ALU_INSERTION", VariantType.ALU_INSERTION),
            Arguments.of("ALU_DELETION", VariantType.ALU_DELETION),
            Arguments.of("MOBILE_ELEMENT_INSERTION", VariantType.MOBILE_ELEMENT_INSERTION),
            Arguments.of("MOBILE_ELEMENT_DELETION", VariantType.MOBILE_ELEMENT_DELETION),
            Arguments.of("LINE1_INSERTION", VariantType.LINE1_INSERTION),
            Arguments.of("LINE1_DELETION", VariantType.LINE1_DELETION),
            Arguments.of("SVA_INSERTION", VariantType.SVA_INSERTION),
            Arguments.of("SVA_DELETION", VariantType.SVA_DELETION),
            Arguments.of("HERV_INSERTION", VariantType.HERV_INSERTION),
            Arguments.of("HERV_DELETION", VariantType.HERV_DELETION),

            // Breakpoint variants
            Arguments.of("INTERCHROMOSOMAL_BREAKPOINT", VariantType.INTERCHROMOSOMAL_BREAKPOINT),
            Arguments.of("INTRACHROMOSOMAL_BREAKPOINT", VariantType.INTRACHROMOSOMAL_BREAKPOINT),
            Arguments.of("INTERCHROMOSOMAL_TRANSLOCATION", VariantType.INTERCHROMOSOMAL_TRANSLOCATION),
            Arguments.of("INTRACHROMOSOMAL_TRANSLOCATION", VariantType.INTRACHROMOSOMAL_TRANSLOCATION),

            // Complex variants
            Arguments.of("COMPLEX_STRUCTURAL_ALTERATION", VariantType.COMPLEX_STRUCTURAL_ALTERATION),
            Arguments.of("COMPLEX_SUBSTITUTION", VariantType.COMPLEX_SUBSTITUTION),
            Arguments.of("COMPLEX_CHROMOSOMAL_REARRANGEMENT", VariantType.COMPLEX_CHROMOSOMAL_REARRANGEMENT),

            // Other variants
            Arguments.of("LOSS_OF_HETEROZYGOSITY", VariantType.LOSS_OF_HETEROZYGOSITY),
            Arguments.of("NOVEL_SEQUENCE_INSERTION", VariantType.NOVEL_SEQUENCE_INSERTION),
            Arguments.of("SHORT_TANDEM_REPEAT_VARIATION", VariantType.SHORT_TANDEM_REPEAT_VARIATION),
            Arguments.of("SEQUENCE_ALTERATION", VariantType.SEQUENCE_ALTERATION),
            Arguments.of("PROBE", VariantType.PROBE),

            // Case insensitivity
            Arguments.of("snv", VariantType.SNV),
            Arguments.of("insertion", VariantType.INSERTION),
            Arguments.of("Deletion", VariantType.DELETION)
        );
    }

    @Test
    @DisplayName("Invalid variant type returns UNRECOGNIZED")
    void testInvalidVariantType() {
        assertThat(VariantTypeMapper.fromString("UNKNOWN")).isEqualTo(VariantType.UNRECOGNIZED);
        assertThat(VariantTypeMapper.fromString("MUTATION")).isEqualTo(VariantType.UNRECOGNIZED);
        assertThat(VariantTypeMapper.fromString("VARIANT")).isEqualTo(VariantType.UNRECOGNIZED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "INVALID", "MUTATION", "VARIANT", "POINT_MUTATION"})
    @DisplayName("Various invalid inputs return UNRECOGNIZED")
    void testInvalidInputs(String input) {
        assertThat(VariantTypeMapper.fromString(input)).isEqualTo(VariantType.UNRECOGNIZED);
    }

    @Test
    @DisplayName("Whitespace trimming works correctly")
    void testWhitespaceTrimming() {
        assertThat(VariantTypeMapper.fromString("  SNV  ")).isEqualTo(VariantType.SNV);
        assertThat(VariantTypeMapper.fromString("\tINSERTION\t")).isEqualTo(VariantType.INSERTION);
    }

    @Test
    @DisplayName("Dash and space normalization works correctly")
    void testNormalization() {
        assertThat(VariantTypeMapper.fromString("COPY-NUMBER-GAIN")).isEqualTo(VariantType.COPY_NUMBER_GAIN);
        assertThat(VariantTypeMapper.fromString("COPY NUMBER GAIN")).isEqualTo(VariantType.COPY_NUMBER_GAIN);
        assertThat(VariantTypeMapper.fromString("tandem-duplication")).isEqualTo(VariantType.TANDEM_DUPLICATION);
        assertThat(VariantTypeMapper.fromString("tandem duplication")).isEqualTo(VariantType.TANDEM_DUPLICATION);
    }
}
