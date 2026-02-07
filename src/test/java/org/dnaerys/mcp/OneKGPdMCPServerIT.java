package org.dnaerys.mcp;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.mcp.OneKGPdMCPServer.GenomicRegion;
import org.dnaerys.mcp.generator.VariantView;
import org.dnaerys.test.WireMockGrpcResource;
import org.dnaerys.test.WireMockGrpcResource.InjectWireMockGrpc;
import org.dnaerys.test.WireMockGrpcResource.InjectWireMockServer;
import org.dnaerys.testdata.TestBaselines;
import org.dnaerys.testdata.TestBaselines.BaselineResult;
import org.dnaerys.testdata.TestBaselines.ComparisonResult;
import org.junit.jupiter.api.*;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.dnaerys.testdata.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.wiremock.grpc.dsl.WireMockGrpc.message;
import static org.wiremock.grpc.dsl.WireMockGrpc.method;

/**
 * Integration tests for OneKGPdMCPServer MCP tools.
 *
 * These tests require network connectivity to db.dnaerys.org.
 * Run with: ./mvnw verify -DskipITs=false -Dit.test=OneKGPdMCPServerIT
 *
 * Test cases cover:
 * - MCP-INT-001: Metadata tools (getSampleCounts)
 * - MCP-INT-002: Region query tools (variantsInRegion via client)
 * - INH-DN-001 to INH-DN-004: De novo trio queries
 * - INH-HD-001 to INH-HD-003: Heterozygous dominant trio queries
 * - INH-HR-001 to INH-HR-003: Homozygous recessive trio queries
 * - INH-KIN-001 to INH-KIN-004: Kinship calculation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
@QuarkusTestResource(WireMockGrpcResource.class)
class OneKGPdMCPServerIT {

    private static final Logger LOGGER = Logger.getLogger(OneKGPdMCPServerIT.class.getName());

    // Expected sample counts
    private static final int EXPECTED_FEMALE_SAMPLES = 1599;
    private static final int EXPECTED_MALE_SAMPLES = 1603;

    @Inject
    OneKGPdMCPServer server;

    @InjectWireMockGrpc
    WireMockGrpcService dnaerysService;

    @InjectWireMockServer
    WireMockServer wireMockServer;

    @BeforeEach
    void setupStubs() {
        wireMockServer.resetAll();

        // 1. Stub for DatasetInfo (Metadata queries)
        // Note: SAMPLE_FEMALE=HG00405=KINSHIP_CHILD, SAMPLE_MALE=HG00403=KINSHIP_PARENT
        List<String> femaleSampleNames = generateSampleNames("F", EXPECTED_FEMALE_SAMPLES, SAMPLE_FEMALE);
        List<String> maleSampleNames = generateSampleNames("M", EXPECTED_MALE_SAMPLES, SAMPLE_MALE, SAMPLE_GENERAL, KINSHIP_UNRELATED1);

        Cohort cohort = Cohort.newBuilder()
                .setCohortName("1KGP")
                .setSamplesCount((int) EXPECTED_TOTAL_SAMPLES)
                .setFemaleCount(EXPECTED_FEMALE_SAMPLES)
                .setMaleCount(EXPECTED_MALE_SAMPLES)
                .addAllFemaleSamplesNames(femaleSampleNames)
                .addAllMaleSamplesNames(maleSampleNames)
                .build();

        dnaerysService.stubFor(method("DatasetInfo")
                .willReturn(message(DatasetInfoResponse.newBuilder()
                        .setSamplesTotal((int) EXPECTED_TOTAL_SAMPLES)
                        .setFemalesTotal(EXPECTED_FEMALE_SAMPLES)
                        .setMalesTotal(EXPECTED_MALE_SAMPLES)
                        .setVariantsTotal(85_000_000)
                        .addCohorts(cohort)
                        .build())));

        // 2. Stub for CountSamplesHomReference
        dnaerysService.stubFor(method("CountSamplesHomReference")
                .willReturn(message(CountSamplesResponse.newBuilder()
                        .setCount(2500)
                        .build())));

        // 3. Stub for KinshipDuo
        dnaerysService.stubFor(method("KinshipDuo")
                .willReturn(message(KinshipResponse.newBuilder()
                        .addRel(Relatedness.newBuilder()
                                .setSample1(KINSHIP_PARENT)
                                .setSample2(KINSHIP_CHILD)
                                .setDegree(KinshipDegree.FIRST_DEGREE)
                                .setPhiBwf(0.25f)
                                .build())
                        .build())));

        // 4. Stub for SelectVariantsInRegion (streaming - for JSON structure test)
        dnaerysService.stubFor(method("SelectVariantsInRegion")
                .willReturn(message(AllelesResponse.newBuilder()
                        .addVariants(Variant.newBuilder()
                                .setChr(Chromosome.CHR_17)
                                .setStart(BRCA1_START + 100)
                                .setEnd(BRCA1_START + 100)
                                .setRef("A")
                                .setAlt("G")
                                .setAf(0.05f)
                                .setAc(320)
                                .setAn(6404)
                                .build())
                        .addVariants(Variant.newBuilder()
                                .setChr(Chromosome.CHR_17)
                                .setStart(BRCA1_START + 200)
                                .setEnd(BRCA1_START + 200)
                                .setRef("C")
                                .setAlt("T")
                                .setAf(0.02f)
                                .setAc(128)
                                .setAn(6404)
                                .build())
                        .build())));
    }

    /**
     * Generate sample names including specific required samples.
     */
    private List<String> generateSampleNames(String prefix, int count, String... requiredSamples) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        // Add required samples first
        for (String sample : requiredSamples) {
            names.add(sample);
        }
        // Fill remaining with generated names
        for (int i = names.size(); i < count; i++) {
            names.add(prefix + String.format("%05d", i));
        }
        return names;
    }

    @AfterAll
    static void saveBaselines() {
        TestBaselines.saveBaselines();
    }

    // ========================================
    // Test 1: Metadata Tools (MCP-INT-001)
    // ========================================

    @Test
    @Order(1)
    @DisplayName("MCP-INT-001: Metadata tools - getDatasetInfo")
    void testMetadataTools() {
        // Test getDatasetInfo tool
        ToolResponse toolResponse = server.getDatasetInfo();
        DnaerysClient.DatasetInfo info = (DnaerysClient.DatasetInfo) toolResponse.structuredContent();
        assertNotNull(info, "getDatasetInfo should return non-null result");

        // Validate total samples
        assertEquals(EXPECTED_TOTAL_SAMPLES, info.samplesTotal(),
                "Total samples should be " + EXPECTED_TOTAL_SAMPLES);

        // Validate female/male counts
        assertTrue(info.samplesFemaleCount() > 0, "Female samples should be > 0");
        assertTrue(info.samplesMaleCount() > 0, "Male samples should be > 0");
        assertEquals(info.samplesTotal(), info.samplesFemaleCount() + info.samplesMaleCount(),
                "Female + Male should equal total");

        // Validate variants total
        assertTrue(info.variantsTotal() > 80_000_000, "Variants total should be > 80M");

        // Baseline comparison (informational when running with WireMock mocks)
        ComparisonResult result = TestBaselines.compare("mcp.sample.counts", info.samplesTotal());
        LOGGER.info("Baseline result for mcp.sample.counts: " + result.message());

        LOGGER.info("Metadata tools test completed:");
        LOGGER.info("  Total samples: " + info.samplesTotal());
        LOGGER.info("  Female: " + info.samplesFemaleCount());
        LOGGER.info("  Male: " + info.samplesMaleCount());
        LOGGER.info("  Total variants: " + info.variantsTotal());
    }

    // ========================================
    // Test 2: Variant Query Tools (MCP-INT-002)
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(2)
    @DisplayName("MCP-INT-002: Variant query tools")
    void testVariantQueryTools() {
        // Methods getSampleIds, getFemaleSamplesIds, getMaleSamplesIds are removed from implementation.
        // TBD: update this test to verify variant retrieval
    }

    // ========================================
    // Test 3: Homozygous Reference (HOM-REF-001 to HOM-REF-003)
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(3)
    @DisplayName("HOM-REF-001 to HOM-REF-003: Homozygous reference queries")
    void testHomozygousReference() {
        // HOM-REF-001: Count samples homozygous reference at a position
        // Use a known variant position in BRCA1 region for better test reliability
        int testPosition = 43044346; // Known variant position from BRCA1
        try {
            ToolResponse countResponse = server.countSamplesHomozygousReference(
                    CHR_BRCA1,   // chromosome
                    testPosition // position
            );
            Map<String, Long> countResult = (Map<String, Long>) countResponse.structuredContent();

            assertNotNull(countResult, "Count result should not be null");
            assertTrue(countResult.containsKey("count"), "Result should contain 'count' key");
            long count = countResult.get("count");
            // Count can be -1 if no variants exist at this position
            assertTrue(count >= -1, "Count should be >= -1");

            LOGGER.info("Homozygous reference tests completed:");
            LOGGER.info("  Count at BRCA1 position " + testPosition + ": " + count);
        } catch (Exception e) {
            // If the server doesn't support this method yet, skip with warning
            if (e.getMessage() != null && e.getMessage().contains("unreachable")) {
                LOGGER.warning("Homozygous reference test skipped - backend method may not be available: " + e.getMessage());
                // Don't fail the test if backend is unavailable for this specific feature
                org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Homozygous reference backend method not available");
            } else {
                throw e;
            }
        }
    }

    // ========================================
    // Test 4: Kinship Calculation (INH-KIN-001 to INH-KIN-004)
    // ========================================

    @Test
    @Order(4)
    @DisplayName("INH-KIN-001 to INH-KIN-004: Kinship degree calculation")
    void testKinship() {
        // INH-KIN-001: Known related pair (parent-child)
        ToolResponse parentChildResponse = server.getKinshipDegree(
                KINSHIP_PARENT,  // HG00403
                KINSHIP_CHILD    // HG00405
        );
        OneKGPdMCPServer.KinshipResult parentChildResult = (OneKGPdMCPServer.KinshipResult) parentChildResponse.structuredContent();

        assertNotNull(parentChildResult, "Kinship result should not be null");
        assertNotNull(parentChildResult.degree(), "Kinship degree should not be null");
        assertFalse(parentChildResult.degree().isEmpty(), "Kinship degree should not be empty");

        LOGGER.info("Parent-child kinship: " + parentChildResult.degree());

        // INH-KIN-002: Known unrelated pair
        ToolResponse unrelatedResponse = server.getKinshipDegree(
                KINSHIP_UNRELATED1,  // HG00406
                SAMPLE_MALE          // HG00403 - from different family
        );
        OneKGPdMCPServer.KinshipResult unrelatedResult = (OneKGPdMCPServer.KinshipResult) unrelatedResponse.structuredContent();

        assertNotNull(unrelatedResult, "Unrelated kinship result should not be null");
        // Unrelated pairs may return empty string or specific degree
        LOGGER.info("Unrelated kinship: " + unrelatedResult.degree());

        // INH-KIN-003: Same sample (self-kinship)
        ToolResponse selfResponse = server.getKinshipDegree(
                SAMPLE_FEMALE,  // HG00405
                SAMPLE_FEMALE   // HG00405
        );
        OneKGPdMCPServer.KinshipResult selfResult = (OneKGPdMCPServer.KinshipResult) selfResponse.structuredContent();

        assertNotNull(selfResult, "Self-kinship result should not be null");
        LOGGER.info("Self kinship: " + selfResult.degree());

        // INH-KIN-004: Parent pair (should be unrelated unless siblings)
        ToolResponse parentPairResponse = server.getKinshipDegree(
                SAMPLE_MALE,     // HG00403
                SAMPLE_GENERAL   // HG00404
        );
        OneKGPdMCPServer.KinshipResult parentPairResult = (OneKGPdMCPServer.KinshipResult) parentPairResponse.structuredContent();

        assertNotNull(parentPairResult, "Parent pair kinship result should not be null");
        LOGGER.info("Parent pair kinship (HG00403-HG00404): " + parentPairResult.degree());

        // Validate that parent-child relationship is detected
        // First-degree relatives include parent-child, full siblings
        // The exact string depends on server implementation
        // Just verify we get a non-empty result for known relatives
        assertTrue(parentChildResult.degree() != null && !parentChildResult.degree().isEmpty(),
                "Parent-child pair should return a kinship degree");

        LOGGER.info("Kinship tests completed:");
        LOGGER.info("  Parent-child (HG00403-HG00405): " + parentChildResult.degree());
        LOGGER.info("  Unrelated (HG00406-HG00403): " + unrelatedResult.degree());
        LOGGER.info("  Self (HG00405-HG00405): " + selfResult.degree());
        LOGGER.info("  Parent pair (HG00403-HG00404): " + parentPairResult.degree());
    }

    // ========================================
    // Test 5: Validate JSON Response Structure
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(5)
    @DisplayName("Validate JSON response structure from variant tools")
    void testJsonResponseStructure() {
        // Get some variants to validate structure using selectVariantsInRegion
        ToolResponse toolResponse = server.selectVariantsInRegion(
                new GenomicRegion(CHR_BRCA1, BRCA1_START, BRCA1_END, null, null),
                true, true,  // selectHet, selectHom
                null, 0, 5  // Get just 5 variants
        );
        Map<String, List<VariantView>> result = (Map<String, List<VariantView>>) toolResponse.structuredContent();

        assertNotNull(result, "Result should not be null");
        List<VariantView> variants = result.get("variants");

        if (!variants.isEmpty()) {
            VariantView firstVariant = variants.get(0);

            // Validate required fields are present (non-null)
            assertTrue(firstVariant.chrIdx() >= 0, "Chromosome index should be >= 0");
            assertTrue(firstVariant.pos() > 0, "Position should be > 0");
            assertNotNull(firstVariant.ref(), "Reference allele should not be null");
            assertNotNull(firstVariant.alt(), "Alternative allele should not be null");

            // AF should be between 0 and 1
            assertTrue(firstVariant.AF() >= 0.0f && firstVariant.AF() <= 1.0f,
                    "AF should be between 0 and 1");

            // AC and AN should be >= 0
            assertTrue(firstVariant.AC() >= 0, "AC should be >= 0");
            assertTrue(firstVariant.AN() >= 0, "AN should be >= 0");

            LOGGER.info("JSON structure validation passed:");
            LOGGER.info("  First variant: chr=" + firstVariant.chrIdx() +
                    ", pos=" + firstVariant.pos() +
                    ", ref=" + firstVariant.ref() +
                    ", alt=" + firstVariant.alt() +
                    ", AF=" + firstVariant.AF());
        } else {
            LOGGER.info("No variants returned for structure validation (empty result is valid)");
        }
    }
}
