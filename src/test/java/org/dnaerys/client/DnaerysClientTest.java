package org.dnaerys.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.test.WireMockGrpcResource;
import org.dnaerys.test.WireMockGrpcResource.InjectWireMockGrpc;
import org.dnaerys.test.WireMockGrpcResource.InjectWireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import org.dnaerys.mcp.OneKGPdMCPServer.GenomicRegion;
import org.dnaerys.mcp.OneKGPdMCPServer.SelectByAnnotations;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;

/**
 * Unit tests for DnaerysClient.
 * Tests pagination logic, annotation building, input validation, and error handling.
 *
 * Test Case IDs: CLI-PAG-001 through CLI-ERR-004
 *
 * Uses WireMock gRPC for mocking non-streaming gRPC responses.
 * Streaming tests (PaginationLogicTests) remain disabled due to WireMock gRPC 0.11.0 limitations.
 *
 * @see org.dnaerys.client.DnaerysClient
 */
@DisplayName("DnaerysClient Unit Tests")
@QuarkusTest
@QuarkusTestResource(WireMockGrpcResource.class)
class DnaerysClientTest {

    @Inject
    DnaerysClient client;

    @InjectWireMockGrpc
    WireMockGrpcService dnaerysService;

    @InjectWireMockServer
    WireMockServer wireMockServer;

    // Mock stub used only by disabled PaginationLogicTests (streaming RPC tests)
    // Kept for compilation only - not used by enabled tests
    @SuppressWarnings("unused")
    private DnaerysServiceGrpc.DnaerysServiceBlockingStub mockBlockingStub;

    private static final SelectByAnnotations NO_ANNOTATIONS = new SelectByAnnotations(
        null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);

    @BeforeEach
    void setUp() {
        // Reset all stubs between tests for isolation
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
        // Initialize mock stub for disabled PaginationLogicTests (compilation only)
        mockBlockingStub = mock(DnaerysServiceGrpc.DnaerysServiceBlockingStub.class);
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
                new SelectByAnnotations(0.01f, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getAfLt()).isEqualTo(0.01f);
            assertThat(annotations.getAfGt()).isZero();
        }

        @Test
        @DisplayName("CLI-ANN-002: AF greater than filter is set correctly")
        void testAfGreaterThan() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, 0.05f, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getAfGt()).isEqualTo(0.05f);
            assertThat(annotations.getAfLt()).isZero();
        }

        @Test
        @DisplayName("CLI-ANN-003: Impact filter parses CSV correctly")
        void testImpactFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, "HIGH,MODERATE", null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getImpactList())
                .hasSize(2)
                .containsExactlyInAnyOrder(Impact.HIGH, Impact.MODERATE);
        }

        @Test
        @DisplayName("CLI-ANN-004: Combined filters are all set correctly")
        void testCombinedFilters() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(
                    0.01f, 0.0001f,        // AF lt, gt
                    0.02f, 0.0005f,        // gnomAD exome AF
                    0.05f, 0.001f,         // gnomAD genome AF
                    "PATHOGENIC",          // clinSignificance
                    "HIGH",                // vepImpact
                    "TRANSCRIPT",          // vepFeature
                    "PROTEIN_CODING",      // vepBiotype
                    "SNV",                 // vepVariantType
                    "MISSENSE_VARIANT",    // vepConsequences
                    "LIKELY_PATHOGENIC",   // alphaMissenseClass
                    0.9f, 0.5f,            // AM score lt, gt
                    true, false,           // biallelic, multiallelic
                    true, false,           // excludeMales, excludeFemales
                    null, null)            // minLen, maxLen
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
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getAfLt()).isZero();
            assertThat(annotations.getAfGt()).isZero();
            assertThat(annotations.getImpactList()).isEmpty();
            assertThat(annotations.getBioTypeList()).isEmpty();
            assertThat(annotations.getConsequenceList()).isEmpty();
        }

        @Test
        @DisplayName("CLI-ANN-006: Invalid values throw RuntimeException")
        void testInvalidValuesThrow() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.composeAnnotations(
                    new SelectByAnnotations(null, null, null, null, null, null,
                        null, "HIGH,INVALID,MODERATE", null, null, null, null,
                        null, null, null, null, null, null, null, null, null)
                )
            );
            assertThat(thrown.getMessage()).contains("Invalid parameter");
        }

        @Test
        @DisplayName("Negative AF values throw RuntimeException")
        void testNegativeAfThrows() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.composeAnnotations(
                    new SelectByAnnotations(-0.01f, -0.05f, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null)
                )
            );
            assertThat(thrown.getMessage()).contains("Invalid parameter");
        }

        @Test
        @DisplayName("Zero AF values are not set")
        void testZeroAfNotSet() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(0.0f, 0.0f, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getAfLt()).isZero();
            assertThat(annotations.getAfGt()).isZero();
        }

        @Test
        @DisplayName("gnomAD genome AF filters are set correctly")
        void testGnomadGenomeAf() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, 0.05f, 0.001f,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getGnomadGenomesAfLt()).isEqualTo(0.05f);
            assertThat(annotations.getGnomadGenomesAfGt()).isEqualTo(0.001f);
        }

        @Test
        @DisplayName("gnomAD exome AF filters are set correctly")
        void testGnomadExomeAf() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, 0.02f, 0.0005f, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getGnomadExomesAfLt()).isEqualTo(0.02f);
            assertThat(annotations.getGnomadExomesAfGt()).isEqualTo(0.0005f);
        }

        @Test
        @DisplayName("BioType filter parses CSV correctly")
        void testBioTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, "PROTEIN_CODING,LNCRNA", null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getBioTypeList())
                .hasSize(2)
                .containsExactlyInAnyOrder(BioType.PROTEIN_CODING, BioType.LNCRNA);
        }

        @Test
        @DisplayName("FeatureType filter parses CSV correctly")
        void testFeatureTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, "TRANSCRIPT,REGULATORYFEATURE", null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getFeatureTypeList())
                .hasSize(2)
                .containsExactlyInAnyOrder(FeatureType.TRANSCRIPT, FeatureType.REGULATORYFEATURE);
        }

        @Test
        @DisplayName("VariantType filter parses CSV correctly")
        void testVariantTypeFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, "SNV,INSERTION,DELETION", null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getVariantTypeList())
                .hasSize(3)
                .containsExactlyInAnyOrder(VariantType.SNV, VariantType.INSERTION, VariantType.DELETION);
        }

        @Test
        @DisplayName("Consequences filter parses CSV correctly")
        void testConsequencesFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, "STOP_GAINED,FRAMESHIFT_VARIANT,MISSENSE_VARIANT",
                    null, null, null, null, null, null, null, null, null)
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
                new SelectByAnnotations(null, null, null, null, null, null,
                    "PATHOGENIC,LIKELY_PATHOGENIC", null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getClinsgnList())
                .hasSize(2)
                .containsExactlyInAnyOrder(ClinSignificance.PATHOGENIC, ClinSignificance.LIKELY_PATHOGENIC);
        }

        @Test
        @DisplayName("AlphaMissense filter parses CSV correctly")
        void testAlphaMissenseFilter() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    "LIKELY_PATHOGENIC,AMBIGUOUS", null, null, null, null, null, null, null, null)
            );

            assertThat(annotations.getAmClassList())
                .hasSize(2)
                .containsExactlyInAnyOrder(AlphaMissense.AM_LIKELY_PATHOGENIC, AlphaMissense.AM_AMBIGUOUS);
        }

        @Test
        @DisplayName("AlphaMissense score filters are set correctly")
        void testAlphaMissenseScoreFilters() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, 0.9f, 0.5f, null, null, null, null, null, null)
            );

            assertThat(annotations.getAmScoreLt()).isEqualTo(0.9f);
            assertThat(annotations.getAmScoreGt()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("Boolean filters (biallelic, multiallelic) are set when true")
        void testBooleanFiltersTrue() {
            Annotations annotations = client.composeAnnotations(
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, true, true, true, true, null, null)
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
                new SelectByAnnotations(null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, false, false, false, false, null, null)
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
                new SelectByAnnotations(null, null, null, null, null, null,
                    "", "", "", "", "", "",
                    "", null, null, null, null, null, null, null, null)
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
                () -> client.countVariants(
                    List.of(new GenomicRegion("99", 1000, 2000, null, null)), true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Invalid chromosome throws RuntimeException for select")
        void testInvalidChromosomeSelect() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectVariants(
                    List.of(new GenomicRegion("99", 1000, 2000, null, null)), true, true,
                    NO_ANNOTATIONS, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Negative start position throws RuntimeException for count")
        void testNegativeStartCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion("1", -100, 2000, null, null)), true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid genomic region");
        }

        @Test
        @DisplayName("Start > End throws RuntimeException for count")
        void testInvertedCoordinatesCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion("1", 2000, 1000, null, null)), true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid genomic region");
        }

        @Test
        @DisplayName("Start > End throws RuntimeException for select")
        void testInvertedCoordinatesSelect() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectVariants(
                    List.of(new GenomicRegion("1", 2000, 1000, null, null)), true, true,
                    NO_ANNOTATIONS, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid genomic region");
        }

        @Test
        @DisplayName("Null samples list throws RuntimeException for count in samples")
        void testNullSamplesCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInSamples(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)), null, true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Samples ID must not be empty");
        }

        @Test
        @DisplayName("Empty samples list throws RuntimeException for count in samples")
        void testEmptySamplesCount() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariantsInSamples(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)), List.of(), true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Samples ID must not be empty");
        }

        @ParameterizedTest(name = "Chromosome ''{0}'' is invalid")
        @ValueSource(strings = {"0", "23", "99", "chrM", "invalid", ""})  // Note: "MT" is valid (maps to CHR_MT)
        @DisplayName("Invalid chromosome values throw RuntimeException")
        void testVariousInvalidChromosomes(String chromosome) {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion(chromosome, 1000, 2000, null, null)), true, true,
                    NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("Negative minVariantLengthBp throws RuntimeException")
        void testNegativeMinVariantLengthThrows() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                    new SelectByAnnotations(null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null, null, -5, null)
                )
            );

            assertThat(thrown.getMessage()).contains("'minVariantLengthBp' must be >= 0");
        }

        @Test
        @DisplayName("Empty regions list throws RuntimeException")
        void testEmptyRegionsList() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(), true, true, NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("'regions' list cannot be empty");
        }

        @Test
        @DisplayName("Null regions list throws RuntimeException")
        void testNullRegionsList() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    null, true, true, NO_ANNOTATIONS
                )
            );

            assertThat(thrown.getMessage()).contains("'regions' list cannot be empty");
        }

        @Test
        @DisplayName("Negative maxVariantLengthBp throws RuntimeException")
        void testNegativeMaxVariantLengthThrows() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                    new SelectByAnnotations(null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, -10)
                )
            );

            assertThat(thrown.getMessage()).contains("'maxVariantLengthBp' must be >= 0");
        }

        @Test
        @DisplayName("minVariantLengthBp > maxVariantLengthBp throws RuntimeException")
        void testMinGreaterThanMaxVariantLengthThrows() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countVariants(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                    new SelectByAnnotations(null, null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null, null, null, 50, 10)
                )
            );

            assertThat(thrown.getMessage()).contains("'minVariantLengthBp' must be <= 'maxVariantLengthBp'");
        }
    }

    // ========================================
    // HOMOZYGOUS REFERENCE INPUT VALIDATION TESTS
    // ========================================

    @Nested
    @DisplayName("Homozygous Reference Input Validation Tests")
    class HomozygousReferenceInputValidationTests {

        @Test
        @DisplayName("countSamplesHomozygousReference with invalid chromosome throws RuntimeException")
        void testCountHomRefInvalidChromosome() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countSamplesHomozygousReference("99", 1000)
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("countSamplesHomozygousReference with invalid position throws RuntimeException")
        void testCountHomRefInvalidPosition() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countSamplesHomozygousReference("1", 0)
            );

            assertThat(thrown.getMessage()).contains("'position' must be >= 0");
        }

        @Test
        @DisplayName("countSamplesHomozygousReference with negative position throws RuntimeException")
        void testCountHomRefNegativePosition() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countSamplesHomozygousReference("1", -100)
            );

            assertThat(thrown.getMessage()).contains("'position' must be >= 0");
        }

        @Test
        @DisplayName("selectSamplesHomozygousReference with invalid chromosome throws RuntimeException")
        void testSelectHomRefInvalidChromosome() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectSamplesHomozygousReference("99", 1000)
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("selectSamplesHomozygousReference with invalid position throws RuntimeException")
        void testSelectHomRefInvalidPosition() {
            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.selectSamplesHomozygousReference("1", 0)
            );

            assertThat(thrown.getMessage()).contains("'position' must be >= 0");
        }
    }

    // ========================================
    // KINSHIP INPUT VALIDATION TESTS
    // Uses WireMock gRPC for mocking non-streaming DatasetInfo and KinshipDuo RPCs.
    // ========================================

    @Nested
    @DisplayName("Kinship Input Validation Tests")
    class KinshipInputValidationTests {

        @Test
        @DisplayName("Kinship with non-existent sample1 throws RuntimeException")
        void testKinshipNonExistentSample1() {
            // Stub datasetInfo to return empty cohorts (sample won't be found)
            DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(message(datasetResponse))
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.kinship("NONEXISTENT", "HG00405")
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }

        @Test
        @DisplayName("Kinship with non-existent sample2 throws RuntimeException")
        void testKinshipNonExistentSample2() {
            // Stub datasetInfo to return sample1 but not sample2
            Cohort cohort = Cohort.newBuilder()
                .addMaleSamplesNames("HG00403")
                .build();
            DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                .addCohorts(cohort)
                .build();
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(message(datasetResponse))
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.kinship("HG00403", "NONEXISTENT")
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }

        @Test
        @DisplayName("Kinship with null sample1 throws RuntimeException")
        void testKinshipNullSample1() {
            DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(message(datasetResponse))
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.kinship(null, "HG00405")
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }

        @Test
        @DisplayName("Kinship with null sample2 throws RuntimeException")
        void testKinshipNullSample2() {
            Cohort cohort = Cohort.newBuilder()
                .addMaleSamplesNames("HG00403")
                .build();
            DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                .addCohorts(cohort)
                .build();
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(message(datasetResponse))
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.kinship("HG00403", null)
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }

        @Test
        @DisplayName("Kinship with valid samples succeeds")
        void testKinshipValidSamples() {
            // Stub datasetInfo to return both samples
            Cohort cohort = Cohort.newBuilder()
                .addMaleSamplesNames("HG00403")
                .addFemaleSamplesNames("HG00405")
                .build();
            DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                .addCohorts(cohort)
                .build();
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(message(datasetResponse))
            );

            // Stub kinship response
            Relatedness relatedness = Relatedness.newBuilder()
                .setDegree(KinshipDegree.FIRST_DEGREE)
                .build();
            KinshipResponse kinshipResponse = KinshipResponse.newBuilder()
                .addRel(relatedness)
                .build();
            dnaerysService.stubFor(
                method("KinshipDuo")
                    .willReturn(message(kinshipResponse))
            );

            String result = client.kinship("HG00403", "HG00405");
            assertThat(result).isEqualTo("FIRST_DEGREE");
        }
    }

    // ========================================
    // GRPC ERROR HANDLING TESTS
    // Uses WireMock gRPC for mocking error responses.
    // ========================================

    @Nested
    @DisplayName("gRPC Error Handling Tests")
    class GrpcErrorHandlingTests {

        @Test
        @DisplayName("CLI-ERR-001: gRPC UNAVAILABLE error throws RuntimeException for getDatasetInfo")
        void testGetDatasetInfoGrpcFailure() {
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(Status.UNAVAILABLE, "Connection failed")
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.getDatasetInfo()
            );

            assertThat(thrown.getMessage()).contains("UNAVAILABLE");
        }

        @Test
        @DisplayName("CLI-ERR-002: gRPC UNAVAILABLE error throws RuntimeException for countSamplesHomozygousReference")
        void testCountSamplesHomRefGrpcFailure() {
            dnaerysService.stubFor(
                method("CountSamplesHomReference")
                    .willReturn(Status.UNAVAILABLE, "Connection failed")
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.countSamplesHomozygousReference("1", 1000)
            );

            assertThat(thrown.getMessage()).contains("UNAVAILABLE");
        }

        @Test
        @DisplayName("CLI-ERR-003: gRPC UNAVAILABLE error throws RuntimeException for getSampleIds")
        void testGetSampleIdsGrpcFailure() {
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(Status.UNAVAILABLE, "Connection failed")
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.getSampleIds(DnaerysClient.Gender.BOTH)
            );

            assertThat(thrown.getMessage()).contains("UNAVAILABLE");
        }

        @Test
        @DisplayName("CLI-ERR-004: gRPC UNAVAILABLE error throws RuntimeException for kinship")
        void testKinshipGrpcFailure() {
            dnaerysService.stubFor(
                method("DatasetInfo")
                    .willReturn(Status.UNAVAILABLE, "Connection failed")
            );

            RuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> client.kinship("HG00403", "HG00405")
            );

            assertThat(thrown.getMessage()).contains("UNAVAILABLE");
        }
    }

    // ========================================
    // PAGINATION LOGIC TESTS (Server Streaming RPC)
    // These tests use SelectVariantsInRegion which is a server streaming RPC.
    // WireMock gRPC 0.11.0 has limited streaming support (returns single message only).
    // These tests remain disabled until WireMock gRPC adds full streaming support.
    // ========================================

    @Nested
    @DisplayName("Pagination Logic Tests")
    @org.junit.jupiter.api.Disabled("WireMock gRPC 0.11.0 has limited streaming RPC support")
    class PaginationLogicTests {

        @Test
        @DisplayName("CLI-PAG-001: Null limit is normalized to MAX_RETURNED_ITEMS")
        void testNullLimitNormalization() {
            // Return empty iterator
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            // Call with null limit
            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, null, null
            );

            // Verify the request was made with limit=50 (MAX_RETURNED_ITEMS)
            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getLimit() == 50 && request.getSkip() == 0
            ));
        }

        @Test
        @DisplayName("CLI-PAG-002: Negative limit is normalized to MAX_RETURNED_ITEMS")
        void testNegativeLimitNormalization() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            // Call with negative limit
            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, null, -1
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getLimit() == 50
            ));
        }

        @Test
        @DisplayName("CLI-PAG-003: Over-limit is normalized to MAX_RETURNED_ITEMS")
        void testOverLimitNormalization() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            // Call with over limit
            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, null, 500
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getLimit() == 50
            ));
        }

        @Test
        @DisplayName("CLI-PAG-004: Valid limit is passed through")
        void testValidLimitPassthrough() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            // Call with valid limit
            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, null, 25
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getLimit() == 25
            ));
        }

        @Test
        @DisplayName("CLI-PAG-005: Null skip is normalized to 0")
        void testNullSkipNormalization() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, null, 50
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getSkip() == 0
            ));
        }

        @Test
        @DisplayName("CLI-PAG-006: Negative skip is normalized to 0")
        void testNegativeSkipNormalization() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, -10, 50
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getSkip() == 0
            ));
        }

        @Test
        @DisplayName("CLI-PAG-007: Valid skip is passed through")
        void testValidSkipPassthrough() {
            @SuppressWarnings("unchecked")
            Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
            when(emptyIterator.hasNext()).thenReturn(false);
            when(mockBlockingStub.selectVariantsInMultiRegions(any(AllelesInMultiRegionsRequest.class)))
                .thenReturn(emptyIterator);

            client.selectVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)), true, true,
                NO_ANNOTATIONS, 200, 50
            );

            verify(mockBlockingStub).selectVariantsInMultiRegions(argThat(request ->
                request.getSkip() == 200
            ));
        }
    }

    // ========================================
    // ALPHA MISSENSE STAT RECORD TESTS
    // ========================================

    @Nested
    @DisplayName("AlphaMissenseAvg Record Tests")
    class AlphaMissenseAvgRecordTests {

        @Test
        @DisplayName("AlphaMissenseAvg record holds correct values")
        void testAlphaMissenseAvgRecord() {
            DnaerysClient.AlphaMissenseAvg stat = new DnaerysClient.AlphaMissenseAvg(0.65, 0.12, 100);

            assertThat(stat.alphaMissenseMean()).isEqualTo(0.65);
            assertThat(stat.alphaMissenseDeviation()).isEqualTo(0.12);
            assertThat(stat.variantCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("AlphaMissenseAvg record equality")
        void testAlphaMissenseAvgEquality() {
            DnaerysClient.AlphaMissenseAvg stat1 = new DnaerysClient.AlphaMissenseAvg(0.65, 0.12, 100);
            DnaerysClient.AlphaMissenseAvg stat2 = new DnaerysClient.AlphaMissenseAvg(0.65, 0.12, 100);

            assertThat(stat1).isEqualTo(stat2);
        }
    }

    // ========================================
    // DATASET INFO RECORD TESTS
    // ========================================

    @Nested
    @DisplayName("DatasetInfo Record Tests")
    class DatasetInfoRecordTests {

        @Test
        @DisplayName("DatasetInfo record holds correct values")
        void testDatasetInfoRecord() {
            DnaerysClient.DatasetInfo info = new DnaerysClient.DatasetInfo(138044723, 3202, 1598, 1604);

            assertThat(info.variantsTotal()).isEqualTo(138044723);
            assertThat(info.samplesTotal()).isEqualTo(3202);
            assertThat(info.samplesMaleCount()).isEqualTo(1598);
            assertThat(info.samplesFemaleCount()).isEqualTo(1604);
        }

        @Test
        @DisplayName("DatasetInfo record equality")
        void testDatasetInfoEquality() {
            DnaerysClient.DatasetInfo info1 = new DnaerysClient.DatasetInfo(1000, 100, 50, 50);
            DnaerysClient.DatasetInfo info2 = new DnaerysClient.DatasetInfo(1000, 100, 50, 50);

            assertThat(info1).isEqualTo(info2);
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
