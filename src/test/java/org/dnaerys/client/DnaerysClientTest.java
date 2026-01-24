package org.dnaerys.client;

import org.dnaerys.cluster.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DnaerysClient.
 * Tests pagination logic, annotation building, input validation, and error handling.
 *
 * Test Case IDs: CLI-PAG-001 through CLI-ERR-004
 *
 * @see org.dnaerys.client.DnaerysClient
 */
@DisplayName("DnaerysClient Unit Tests")
@ExtendWith(MockitoExtension.class)
class DnaerysClientTest {

    private DnaerysClient client;

    @Mock
    private GrpcChannel mockGrpcChannel;

    @Mock
    private DnaerysServiceGrpc.DnaerysServiceBlockingStub mockBlockingStub;

    @BeforeEach
    void setUp() {
        client = new DnaerysClient();
    }

    // ========================================
    // ANNOTATION BUILDING TESTS (CLI-ANN-*)
    // ========================================

    @Nested
    @DisplayName("Annotation Building Tests")
    class AnnotationBuildingTests {

        @Test
        @DisplayName("CLI-ANN-001: AF less than filter is set correctly")
        void testAfLessThan() {
            Annotations annotations = client.composeAnnotations(
                0.01f, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getAfLt()).isEqualTo(0.01f);
            assertThat(annotations.getAfGt()).isZero();
        }

        @Test
        @DisplayName("CLI-ANN-002: AF greater than filter is set correctly")
        void testAfGreaterThan() {
            Annotations annotations = client.composeAnnotations(
                null, 0.05f, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getAfGt()).isEqualTo(0.05f);
            assertThat(annotations.getAfLt()).isZero();
        }

        @Test
        @DisplayName("CLI-ANN-003: Impact filter parses CSV correctly")
        void testImpactFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                "HIGH,MODERATE", null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getImpactList())
                .hasSize(2)
                .containsExactlyInAnyOrder(Impact.HIGH, Impact.MODERATE);
        }

        @Test
        @DisplayName("CLI-ANN-004: Combined filters are all set correctly")
        void testCombinedFilters() {
            Annotations annotations = client.composeAnnotations(
                0.01f, 0.0001f,  // AF lt, gt
                0.05f, 0.001f,   // gnomAD genome AF
                0.02f, 0.0005f,  // gnomAD exome AF
                "HIGH", "PROTEIN_CODING", "TRANSCRIPT", "SNV",
                "MISSENSE_VARIANT", "PATHOGENIC",
                "LIKELY_PATHOGENIC", 0.9f, 0.5f,
                true, false, true, false
            );

            assertThat(annotations.getAfLt()).isEqualTo(0.01f);
            assertThat(annotations.getAfGt()).isEqualTo(0.0001f);
            assertThat(annotations.getGnomadGenomesAfLt()).isEqualTo(0.05f);
            assertThat(annotations.getGnomadGenomesAfGt()).isEqualTo(0.001f);
            assertThat(annotations.getGnomadExomesAfLt()).isEqualTo(0.02f);
            assertThat(annotations.getGnomadExomesAfGt()).isEqualTo(0.0005f);
            assertThat(annotations.getImpactList()).contains(Impact.HIGH);
            assertThat(annotations.getBioTypeList()).contains(BioType.PROTEIN_CODING);
            assertThat(annotations.getFeatureTypeList()).contains(FeatureType.TRANSCRIPT);
            assertThat(annotations.getVariantTypeList()).contains(VariantType.SNV);
            assertThat(annotations.getConsequenceList()).contains(Consequence.MISSENSE_VARIANT);
            assertThat(annotations.getClinsgnList()).contains(ClinSignificance.PATHOGENIC);
            assertThat(annotations.getAmClassList()).contains(AlphaMissense.AM_LIKELY_PATHOGENIC);
            assertThat(annotations.getAmScoreLt()).isEqualTo(0.9f);
            assertThat(annotations.getAmScoreGt()).isEqualTo(0.5f);
            assertThat(annotations.getBiallelicOnly()).isTrue();
            assertThat(annotations.getMultiallelicOnly()).isFalse();
            assertThat(annotations.getExcludeMales()).isTrue();
            assertThat(annotations.getExcludeFemales()).isFalse();
        }

        @Test
        @DisplayName("CLI-ANN-005: Empty/null filters produce empty annotations object")
        void testEmptyFilters() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getAfLt()).isZero();
            assertThat(annotations.getAfGt()).isZero();
            assertThat(annotations.getImpactList()).isEmpty();
            assertThat(annotations.getBioTypeList()).isEmpty();
            assertThat(annotations.getConsequenceList()).isEmpty();
        }

        @Test
        @DisplayName("CLI-ANN-006: Invalid values are filtered out from CSV")
        void testInvalidValuesFiltered() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                "HIGH,INVALID,MODERATE", null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getImpactList())
                .hasSize(2)
                .containsExactlyInAnyOrder(Impact.HIGH, Impact.MODERATE)
                .doesNotContain(Impact.UNRECOGNIZED);
        }

        @Test
        @DisplayName("Negative AF values are not set")
        void testNegativeAfNotSet() {
            Annotations annotations = client.composeAnnotations(
                -0.01f, -0.05f, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getAfLt()).isZero();
            assertThat(annotations.getAfGt()).isZero();
        }

        @Test
        @DisplayName("Zero AF values are not set")
        void testZeroAfNotSet() {
            Annotations annotations = client.composeAnnotations(
                0.0f, 0.0f, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getAfLt()).isZero();
            assertThat(annotations.getAfGt()).isZero();
        }

        @Test
        @DisplayName("gnomAD genome AF filters are set correctly")
        void testGnomadGenomeAf() {
            Annotations annotations = client.composeAnnotations(
                null, null, 0.05f, 0.001f, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getGnomadGenomesAfLt()).isEqualTo(0.05f);
            assertThat(annotations.getGnomadGenomesAfGt()).isEqualTo(0.001f);
        }

        @Test
        @DisplayName("gnomAD exome AF filters are set correctly")
        void testGnomadExomeAf() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, 0.02f, 0.0005f,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getGnomadExomesAfLt()).isEqualTo(0.02f);
            assertThat(annotations.getGnomadExomesAfGt()).isEqualTo(0.0005f);
        }

        @Test
        @DisplayName("BioType filter parses CSV correctly")
        void testBioTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, "PROTEIN_CODING,LNCRNA", null, null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getBioTypeList())
                .hasSize(2)
                .containsExactlyInAnyOrder(BioType.PROTEIN_CODING, BioType.LNCRNA);
        }

        @Test
        @DisplayName("FeatureType filter parses CSV correctly")
        void testFeatureTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, "TRANSCRIPT,REGULATORYFEATURE", null, null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getFeatureTypeList())
                .hasSize(2)
                .containsExactlyInAnyOrder(FeatureType.TRANSCRIPT, FeatureType.REGULATORYFEATURE);
        }

        @Test
        @DisplayName("VariantType filter parses CSV correctly")
        void testVariantTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, "SNV,INSERTION,DELETION", null, null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getVariantTypeList())
                .hasSize(3)
                .containsExactlyInAnyOrder(VariantType.SNV, VariantType.INSERTION, VariantType.DELETION);
        }

        @Test
        @DisplayName("Consequences filter parses CSV correctly")
        void testConsequencesFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, "STOP_GAINED,FRAMESHIFT_VARIANT,MISSENSE_VARIANT", null,
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getConsequenceList())
                .hasSize(3)
                .containsExactlyInAnyOrder(
                    Consequence.STOP_GAINED,
                    Consequence.FRAMESHIFT_VARIANT,
                    Consequence.MISSENSE_VARIANT
                );
        }

        @Test
        @DisplayName("ClinSignificance filter parses CSV correctly")
        void testClinSignificanceFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, "PATHOGENIC,LIKELY_PATHOGENIC",
                null, null, null, null, null, null, null
            );

            assertThat(annotations.getClinsgnList())
                .hasSize(2)
                .containsExactlyInAnyOrder(ClinSignificance.PATHOGENIC, ClinSignificance.LIKELY_PATHOGENIC);
        }

        @Test
        @DisplayName("AlphaMissense filter parses CSV correctly")
        void testAlphaMissenseFilter() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                "LIKELY_PATHOGENIC,AMBIGUOUS", null, null, null, null, null, null
            );

            assertThat(annotations.getAmClassList())
                .hasSize(2)
                .containsExactlyInAnyOrder(AlphaMissense.AM_LIKELY_PATHOGENIC, AlphaMissense.AM_AMBIGUOUS);
        }

        @Test
        @DisplayName("AlphaMissense score filters are set correctly")
        void testAlphaMissenseScoreFilters() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, 0.9f, 0.5f, null, null, null, null
            );

            assertThat(annotations.getAmScoreLt()).isEqualTo(0.9f);
            assertThat(annotations.getAmScoreGt()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("Boolean filters (biallelic, multiallelic) are set when true")
        void testBooleanFiltersTrue() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, true, true, true, true
            );

            assertThat(annotations.getBiallelicOnly()).isTrue();
            assertThat(annotations.getMultiallelicOnly()).isTrue();
            assertThat(annotations.getExcludeMales()).isTrue();
            assertThat(annotations.getExcludeFemales()).isTrue();
        }

        @Test
        @DisplayName("Boolean filters are not set when false")
        void testBooleanFiltersFalse() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, false, false, false, false
            );

            // When false, the flags should not be set (default is false)
            assertThat(annotations.getBiallelicOnly()).isFalse();
            assertThat(annotations.getMultiallelicOnly()).isFalse();
            assertThat(annotations.getExcludeMales()).isFalse();
            assertThat(annotations.getExcludeFemales()).isFalse();
        }

        @Test
        @DisplayName("Empty string filters are treated as null")
        void testEmptyStringFilters() {
            Annotations annotations = client.composeAnnotations(
                null, null, null, null, null, null,
                "", "", "", "", "", "",
                "", null, null, null, null, null, null
            );

            assertThat(annotations.getImpactList()).isEmpty();
            assertThat(annotations.getBioTypeList()).isEmpty();
            assertThat(annotations.getFeatureTypeList()).isEmpty();
            assertThat(annotations.getVariantTypeList()).isEmpty();
            assertThat(annotations.getConsequenceList()).isEmpty();
            assertThat(annotations.getClinsgnList()).isEmpty();
            assertThat(annotations.getAmClassList()).isEmpty();
        }
    }

    // ========================================
    // INPUT VALIDATION TESTS
    // ========================================

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Invalid chromosome throws RuntimeException for count")
        void testInvalidChromosomeCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegion(
                    "99", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Invalid chromosome throws RuntimeException for select")
        void testInvalidChromosomeSelect() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectVariantsInRegion(
                    "99", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Negative start position throws RuntimeException for count")
        void testNegativeStartCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegion(
                    "1", -100, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid 'start' or 'end'");
        }

        @Test
        @DisplayName("Start > End throws RuntimeException for count")
        void testInvertedCoordinatesCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegion(
                    "1", 2000, 1000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid 'start' or 'end'");
        }

        @Test
        @DisplayName("Start > End throws RuntimeException for select")
        void testInvertedCoordinatesSelect() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectVariantsInRegion(
                    "1", 2000, 1000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid 'start' or 'end'");
        }

        @Test
        @DisplayName("Null sample ID throws RuntimeException for count in sample")
        void testNullSampleIdCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegionInSample(
                    "1", 1000, 2000, null, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Sample ID must not be empty");
        }

        @Test
        @DisplayName("Empty sample ID throws RuntimeException for count in sample")
        void testEmptySampleIdCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegionInSample(
                    "1", 1000, 2000, "", true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Sample ID must not be empty");
        }

        @ParameterizedTest(name = "Chromosome ''{0}'' is invalid")
        @ValueSource(strings = {"0", "23", "99", "chrM", "invalid", ""})  // Note: "MT" is valid (maps to CHR_MT)
        @DisplayName("Invalid chromosome values throw RuntimeException")
        void testVariousInvalidChromosomes(String chromosome) {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInRegion(
                    chromosome, 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Variant min/max length normalization with valid coordinates")
        void testVariantLengthNormalization() {
            // When minLen > maxLen, both should be normalized to 0/MAX_VALUE
            // This test verifies the method accepts negative length values
            // (validation passes, but gRPC call will fail without mocking)
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                // Test that negative variant lengths are normalized (not rejected)
                long count = client.countVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, -5, -10, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
                );

                // Should succeed with normalized values
                assertThat(count).isEqualTo(100L);
            }
        }
    }

    // ========================================
    // INHERITANCE MODEL INPUT VALIDATION TESTS
    // ========================================

    @Nested
    @DisplayName("Inheritance Model Input Validation Tests")
    class InheritanceModelInputValidationTests {

        @Test
        @DisplayName("selectDeNovo with null parent1 throws RuntimeException")
        void testDeNovoNullParent1() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    null, "HG00404", "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("parent1 must not be empty");
        }

        @Test
        @DisplayName("selectDeNovo with empty parent1 throws RuntimeException")
        void testDeNovoEmptyParent1() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    "", "HG00404", "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("parent1 must not be empty");
        }

        @Test
        @DisplayName("selectDeNovo with null parent2 throws RuntimeException")
        void testDeNovoNullParent2() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    "HG00403", null, "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("parent2 must not be empty");
        }

        @Test
        @DisplayName("selectDeNovo with null proband throws RuntimeException")
        void testDeNovoNullProband() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    "HG00403", "HG00404", null,
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("proband must not be empty");
        }

        @Test
        @DisplayName("selectDeNovo with invalid chromosome throws RuntimeException")
        void testDeNovoInvalidChromosome() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    "HG00403", "HG00404", "HG00405",
                    "99", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("selectDeNovo with invalid region throws RuntimeException")
        void testDeNovoInvalidRegion() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectDeNovo(
                    "HG00403", "HG00404", "HG00405",
                    "1", 2000, 1000,  // start > end
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid 'start' or 'end'");
        }

        @Test
        @DisplayName("selectHetDominant with null affected parent throws RuntimeException")
        void testHetDominantNullAffectedParent() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHetDominant(
                    null, "HG00404", "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("affectedParent must not be empty");
        }

        @Test
        @DisplayName("selectHetDominant with null unaffected parent throws RuntimeException")
        void testHetDominantNullUnaffectedParent() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHetDominant(
                    "HG00403", null, "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("unaffectedParent must not be empty");
        }

        @Test
        @DisplayName("selectHetDominant with null proband throws RuntimeException")
        void testHetDominantNullProband() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHetDominant(
                    "HG00403", "HG00404", null,
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("proband must not be empty");
        }

        @Test
        @DisplayName("selectHomRecessive with null parent1 throws RuntimeException")
        void testHomRecessiveNullParent1() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHomRecessive(
                    null, "HG00404", "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("unaffectedParent1 must not be empty");
        }

        @Test
        @DisplayName("selectHomRecessive with null parent2 throws RuntimeException")
        void testHomRecessiveNullParent2() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHomRecessive(
                    "HG00403", null, "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("unaffectedParent2 must not be empty");
        }

        @Test
        @DisplayName("selectHomRecessive with null proband throws RuntimeException")
        void testHomRecessiveNullProband() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectHomRecessive(
                    "HG00403", "HG00404", null,
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null
                )
            );

            assertThat(thrown.getMessage()).contains("proband must not be empty");
        }
    }

    // ========================================
    // KINSHIP INPUT VALIDATION TESTS
    // ========================================

    @Nested
    @DisplayName("Kinship Input Validation Tests")
    class KinshipInputValidationTests {

        @Test
        @DisplayName("Kinship with non-existent sample1 throws RuntimeException")
        void testKinshipNonExistentSample1() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock datasetInfo to return empty cohorts (sample won't be found)
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.kinship("NONEXISTENT", "HG00405")
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("Kinship with non-existent sample2 throws RuntimeException")
        void testKinshipNonExistentSample2() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock datasetInfo to return sample1 but not sample2
                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.kinship("HG00403", "NONEXISTENT")
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("Kinship with null sample1 throws RuntimeException")
        void testKinshipNullSample1() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.kinship(null, "HG00405")
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("Kinship with null sample2 throws RuntimeException")
        void testKinshipNullSample2() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.kinship("HG00403", null)
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("Kinship with valid samples succeeds")
        void testKinshipValidSamples() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock datasetInfo to return both samples
                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .addFemaleSamplesNames("HG00405")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                // Mock kinship response
                Relatedness relatedness = Relatedness.newBuilder()
                    .setDegree(KinshipDegree.FIRST_DEGREE)
                    .build();
                KinshipResponse kinshipResponse = KinshipResponse.newBuilder()
                    .addRel(relatedness)
                    .build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class))).thenReturn(kinshipResponse);

                String result = client.kinship("HG00403", "HG00405");
                assertThat(result).isEqualTo("FIRST_DEGREE");
            }
        }
    }

    // ========================================
    // GRPC ERROR HANDLING TESTS (with mocking)
    // ========================================

    @Nested
    @DisplayName("gRPC Error Handling Tests")
    class GrpcErrorHandlingTests {

        @Test
        @DisplayName("CLI-ERR-001: gRPC connection failure throws RuntimeException for variantsTotal")
        void testVariantsTotalGrpcFailure() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.variantsTotal()
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }

        @Test
        @DisplayName("CLI-ERR-002: gRPC error throws RuntimeException for getSampleCounts")
        void testGetSampleCountsGrpcFailure() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.getSampleCounts()
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }

        @Test
        @DisplayName("CLI-ERR-003: gRPC error throws RuntimeException for getSampleIds")
        void testGetSampleIdsGrpcFailure() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.getSampleIds(DnaerysClient.Gender.BOTH)
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }

        @Test
        @DisplayName("CLI-ERR-004: gRPC error throws RuntimeException for kinship")
        void testKinshipGrpcFailure() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> client.kinship("HG00403", "HG00405")
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }
    }

    // ========================================
    // PAGINATION LOGIC TESTS (with mocking)
    // ========================================

    @Nested
    @DisplayName("Pagination Logic Tests")
    class PaginationLogicTests {

        @Test
        @DisplayName("CLI-PAG-001: Null limit is normalized to MAX_RETURNED_ITEMS")
        void testNullLimitNormalization() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Return empty iterator
                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                // Call with null limit
                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null  // skip=null, limit=null
                );

                // Verify the request was made with limit=50 (MAX_RETURNED_ITEMS)
                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getLimit() == 50 && request.getSkip() == 0
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-002: Negative limit is normalized to MAX_RETURNED_ITEMS")
        void testNegativeLimitNormalization() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                // Call with negative limit
                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, -1  // limit=-1
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getLimit() == 50
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-003: Over-limit is normalized to MAX_RETURNED_ITEMS")
        void testOverLimitNormalization() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                // Call with over limit
                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, 500  // limit=500 (over max)
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getLimit() == 50
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-004: Valid limit is passed through")
        void testValidLimitPassthrough() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                // Call with valid limit
                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, 25  // limit=25 (valid)
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getLimit() == 25
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-005: Null skip is normalized to 0")
        void testNullSkipNormalization() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, 50  // skip=null
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getSkip() == 0
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-006: Negative skip is normalized to 0")
        void testNegativeSkipNormalization() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    -10, 50  // skip=-10
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getSkip() == 0
                ));
            }
        }

        @Test
        @DisplayName("CLI-PAG-007: Valid skip is passed through")
        void testValidSkipPassthrough() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(emptyIterator);

                client.selectVariantsInRegion(
                    "1", 1000, 2000, true, true,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    200, 50  // skip=200 (valid)
                );

                verify(mockBlockingStub).selectVariantsInRegion(argThat(request ->
                    request.getSkip() == 200
                ));
            }
        }
    }

    // ========================================
    // SAMPLE COUNTS RECORD TESTS
    // ========================================

    @Nested
    @DisplayName("SampleCounts Record Tests")
    class SampleCountsRecordTests {

        @Test
        @DisplayName("SampleCounts record holds correct values")
        void testSampleCountsRecord() {
            DnaerysClient.SampleCounts counts = new DnaerysClient.SampleCounts(3202, 1598, 1604);

            assertThat(counts.total()).isEqualTo(3202);
            assertThat(counts.male()).isEqualTo(1598);
            assertThat(counts.female()).isEqualTo(1604);
        }

        @Test
        @DisplayName("SampleCounts record equality")
        void testSampleCountsEquality() {
            DnaerysClient.SampleCounts counts1 = new DnaerysClient.SampleCounts(100, 50, 50);
            DnaerysClient.SampleCounts counts2 = new DnaerysClient.SampleCounts(100, 50, 50);

            assertThat(counts1).isEqualTo(counts2);
        }
    }

    // ========================================
    // GENDER ENUM TESTS
    // ========================================

    @Nested
    @DisplayName("Gender Enum Tests")
    class GenderEnumTests {

        @Test
        @DisplayName("Gender enum has all expected values")
        void testGenderEnumValues() {
            assertThat(DnaerysClient.Gender.values())
                .containsExactly(
                    DnaerysClient.Gender.MALE,
                    DnaerysClient.Gender.FEMALE,
                    DnaerysClient.Gender.BOTH
                );
        }
    }
}
