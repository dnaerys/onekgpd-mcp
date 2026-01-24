package org.dnaerys.mcp;

import io.quarkiverse.mcp.server.ToolResponse;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.mcp.generator.VariantView;
import org.dnaerys.testdata.TestBaselines;
import org.dnaerys.testdata.TestBaselines.BaselineResult;
import org.dnaerys.testdata.TestInjectionHelper;
import org.dnaerys.testdata.TestBaselines.ComparisonResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.dnaerys.testdata.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OneKGPdMCPServer MCP tools.
 *
 * These tests require network connectivity to db.dnaerys.org.
 * Run with: ./mvnw verify -DskipIntegrationTests=false -Dit.test=OneKGPdMCPServerIT
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
class OneKGPdMCPServerIT {

    private static final Logger LOGGER = Logger.getLogger(OneKGPdMCPServerIT.class.getName());

    private static OneKGPdMCPServer server;

    @BeforeAll
    static void setUp() {
        server = new OneKGPdMCPServer();
        TestInjectionHelper.injectMcpResponse(server);
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
    @DisplayName("MCP-INT-001: Metadata tools - getSampleCounts")
    void testMetadataTools() {
        // Test getSampleCounts tool
        ToolResponse toolResponse = server.getSampleCounts();
        DnaerysClient.SampleCounts counts = (DnaerysClient.SampleCounts) toolResponse.structuredContent();
        assertNotNull(counts, "getSampleCounts should return non-null result");

        // Validate total samples
        assertEquals(EXPECTED_TOTAL_SAMPLES, counts.total(),
                "Total samples should be " + EXPECTED_TOTAL_SAMPLES);

        // Validate female/male counts
        assertTrue(counts.female() > 0, "Female samples should be > 0");
        assertTrue(counts.male() > 0, "Male samples should be > 0");
        assertEquals(counts.total(), counts.female() + counts.male(),
                "Female + Male should equal total");

        // Baseline comparison
        ComparisonResult result = TestBaselines.compare("mcp.sample.counts", counts.total());
        assertNotEquals(BaselineResult.FAIL, result.result(),
                "Sample counts baseline check failed: " + result.message());

        LOGGER.info("Metadata tools test completed:");
        LOGGER.info("  Total samples: " + counts.total());
        LOGGER.info("  Female: " + counts.female());
        LOGGER.info("  Male: " + counts.male());
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
    // Test 3: De Novo Inheritance (INH-DN-001 to INH-DN-004)
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(3)
    @DisplayName("INH-DN-001 to INH-DN-004: De novo trio inheritance queries")
    void testDeNovoInTrio() {
        // INH-DN-001: Basic de novo query in CFTR region (larger region, more likely to find variants)
        ToolResponse deNovoResponse = server.deNovoInTrio(
                TRIO_DN_PARENT1,  // parent1 (HG00403)
                TRIO_DN_PARENT2,  // parent2 (HG00404)
                TRIO_DN_PROBAND,  // proband (HG00405)
                CHR_CFTR,         // chromosome
                CFTR_START,       // start
                CFTR_END,         // end
                null, null,       // refAllele, altAllele
                null, null,       // afLessThan, afGreaterThan
                null, null,       // gnomadExomeAfLessThan/GreaterThan
                null, null,       // gnomadGenomeAfLessThan/GreaterThan
                null,             // clinSignificance
                null,             // vepImpact
                null,             // vepFeature
                null,             // vepBiotype
                null,             // vepVariantType
                null,             // vepConsequences
                null,             // alphaMissenseClass
                null, null,       // alphaMissenseScoreLT/GT
                null, null,       // biallelicOnly, multiallelicOnly
                null, null,       // excludeMales, excludeFemales
                null, null,       // minVariantLengthBp, maxVariantLengthBp
                null, null        // skip, limit
        );
        Map<String, List<VariantView>> deNovoResult = (Map<String, List<VariantView>>) deNovoResponse.structuredContent();

        assertNotNull(deNovoResult, "De novo result should not be null");
        assertTrue(deNovoResult.containsKey("variants"), "Result should contain 'variants' key");

        List<VariantView> deNovoVariants = deNovoResult.get("variants");
        assertNotNull(deNovoVariants, "Variants list should not be null");
        // De novo variants may be empty - this is valid, just capture baseline
        assertTrue(deNovoVariants.size() <= MAX_RETURNED_ITEMS,
                "Result should respect MAX_RETURNED_ITEMS limit");

        TestBaselines.compare("mcp.denovo.cftr.count", deNovoVariants.size());

        // INH-DN-002: De novo with HIGH impact filter (more specific)
        ToolResponse highImpactResponse = server.deNovoInTrio(
                TRIO_DN_PARENT1, TRIO_DN_PARENT2, TRIO_DN_PROBAND,
                CHR_CFTR, CFTR_START, CFTR_END,
                null, null, null, null, null, null, null, null, null,
                "HIGH,MODERATE",  // vepImpact - broader filter
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        Map<String, List<VariantView>> highImpactResult = (Map<String, List<VariantView>>) highImpactResponse.structuredContent();

        assertNotNull(highImpactResult, "High impact de novo result should not be null");
        List<VariantView> highImpactVariants = highImpactResult.get("variants");
        assertNotNull(highImpactVariants, "High impact variants list should not be null");
        assertTrue(highImpactVariants.size() <= deNovoVariants.size(),
                "Filtered results should be <= unfiltered (or both empty)");

        // INH-DN-003: De novo pagination
        ToolResponse paginatedResponse = server.deNovoInTrio(
                TRIO_DN_PARENT1, TRIO_DN_PARENT2, TRIO_DN_PROBAND,
                CHR_DENSE, DENSE_START, DENSE_END,  // Dense region for pagination test
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, 0, 10  // skip=0, limit=10
        );
        Map<String, List<VariantView>> paginatedResult = (Map<String, List<VariantView>>) paginatedResponse.structuredContent();

        assertNotNull(paginatedResult, "Paginated de novo result should not be null");
        assertTrue(paginatedResult.get("variants").size() <= 10,
                "Paginated results should respect limit");

        // INH-DN-004: De novo in sparse region (may return empty)
        ToolResponse sparseResponse = server.deNovoInTrio(
                TRIO_DN_PARENT1, TRIO_DN_PARENT2, TRIO_DN_PROBAND,
                CHR_SPARSE, SPARSE_START, SPARSE_END,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );
        Map<String, List<VariantView>> sparseResult = (Map<String, List<VariantView>>) sparseResponse.structuredContent();

        assertNotNull(sparseResult, "Sparse region de novo result should not be null");
        // Sparse region may legitimately return no variants

        LOGGER.info("De novo trio tests completed:");
        LOGGER.info("  CFTR de novo variants: " + deNovoVariants.size());
        LOGGER.info("  CFTR high impact de novo: " + highImpactVariants.size());
        LOGGER.info("  Dense region paginated: " + paginatedResult.get("variants").size());
        LOGGER.info("  Sparse region: " + sparseResult.get("variants").size());
    }

    // ========================================
    // Test 4: Heterozygous Dominant Inheritance (INH-HD-001 to INH-HD-003)
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(4)
    @DisplayName("INH-HD-001 to INH-HD-003: Heterozygous dominant trio inheritance queries")
    void testHetDominantInTrio() {
        // INH-HD-001: Basic het dominant query
        ToolResponse hetDomResponse = server.hetDominantInTrio(
                TRIO_HD_AFFECTED,    // affectedParent (HG00403)
                TRIO_HD_UNAFFECTED,  // unaffectedParent (HG00404)
                TRIO_HD_PROBAND,     // proband (HG00405)
                CHR_CFTR,            // chromosome
                CFTR_START,          // start
                CFTR_END,            // end
                null, null,          // refAllele, altAllele
                null, null,          // afLessThan, afGreaterThan
                null, null,          // gnomadExomeAfLessThan/GreaterThan
                null, null,          // gnomadGenomeAfLessThan/GreaterThan
                null,                // clinSignificance
                null,                // vepImpact
                null,                // vepFeature
                null,                // vepBiotype
                null,                // vepVariantType
                null,                // vepConsequences
                null,                // alphaMissenseClass
                null, null,          // alphaMissenseScoreLT/GT
                null, null,          // biallelicOnly, multiallelicOnly
                null, null,          // excludeMales, excludeFemales
                null, null,          // minVariantLengthBp, maxVariantLengthBp
                null, null           // skip, limit
        );
        Map<String, List<VariantView>> hetDomResult = (Map<String, List<VariantView>>) hetDomResponse.structuredContent();

        assertNotNull(hetDomResult, "Het dominant result should not be null");
        assertTrue(hetDomResult.containsKey("variants"), "Result should contain 'variants' key");

        List<VariantView> hetDomVariants = hetDomResult.get("variants");
        assertNotNull(hetDomVariants, "Variants list should not be null");
        assertTrue(hetDomVariants.size() <= MAX_RETURNED_ITEMS,
                "Result should respect MAX_RETURNED_ITEMS limit");

        TestBaselines.compare("mcp.hetdom.cftr.count", hetDomVariants.size());

        // INH-HD-002: Het dominant with HIGH impact
        ToolResponse highImpactResponse = server.hetDominantInTrio(
                TRIO_HD_AFFECTED, TRIO_HD_UNAFFECTED, TRIO_HD_PROBAND,
                CHR_CFTR, CFTR_START, CFTR_END,
                null, null, null, null, null, null, null, null, null,
                "HIGH,MODERATE",  // vepImpact
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        Map<String, List<VariantView>> highImpactResult = (Map<String, List<VariantView>>) highImpactResponse.structuredContent();

        assertNotNull(highImpactResult, "High impact het dominant result should not be null");
        List<VariantView> highImpactVariants = highImpactResult.get("variants");
        assertTrue(highImpactVariants.size() <= hetDomVariants.size(),
                "Filtered results should be <= unfiltered");

        // INH-HD-003: Het dominant rare only (AF < 0.01)
        ToolResponse rareResponse = server.hetDominantInTrio(
                TRIO_HD_AFFECTED, TRIO_HD_UNAFFECTED, TRIO_HD_PROBAND,
                CHR_CFTR, CFTR_START, CFTR_END,
                null, null,
                0.01f, null,  // afLessThan = 0.01 (rare variants)
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
        Map<String, List<VariantView>> rareResult = (Map<String, List<VariantView>>) rareResponse.structuredContent();

        assertNotNull(rareResult, "Rare het dominant result should not be null");
        List<VariantView> rareVariants = rareResult.get("variants");
        assertTrue(rareVariants.size() <= hetDomVariants.size(),
                "Rare variants should be <= total");

        LOGGER.info("Het dominant trio tests completed:");
        LOGGER.info("  CFTR het dominant variants: " + hetDomVariants.size());
        LOGGER.info("  CFTR high impact het dominant: " + highImpactVariants.size());
        LOGGER.info("  CFTR rare (AF<0.01) het dominant: " + rareVariants.size());
    }

    // ========================================
    // Test 5: Homozygous Recessive Inheritance (INH-HR-001 to INH-HR-003)
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(5)
    @DisplayName("INH-HR-001 to INH-HR-003: Homozygous recessive trio inheritance queries")
    void testHomRecessiveInTrio() {
        // INH-HR-001: Basic hom recessive query
        ToolResponse homRecResponse = server.homRecessiveInTrio(
                TRIO_HR_CARRIER1,  // unaffectedParent1 (HG00403)
                TRIO_HR_CARRIER2,  // unaffectedParent2 (HG00404)
                TRIO_HR_AFFECTED,  // proband (HG00405)
                CHR_CFTR,          // chromosome - CFTR is classic recessive disease gene
                CFTR_START,        // start
                CFTR_END,          // end
                null, null,        // refAllele, altAllele
                null, null,        // afLessThan, afGreaterThan
                null, null,        // gnomadExomeAfLessThan/GreaterThan
                null, null,        // gnomadGenomeAfLessThan/GreaterThan
                null,              // clinSignificance
                null,              // vepImpact
                null,              // vepFeature
                null,              // vepBiotype
                null,              // vepVariantType
                null,              // vepConsequences
                null,              // alphaMissenseClass
                null, null,        // alphaMissenseScoreLT/GT
                null, null,        // biallelicOnly, multiallelicOnly
                null, null,        // excludeMales, excludeFemales
                null, null,        // minVariantLengthBp, maxVariantLengthBp
                null, null         // skip, limit
        );
        Map<String, List<VariantView>> homRecResult = (Map<String, List<VariantView>>) homRecResponse.structuredContent();

        assertNotNull(homRecResult, "Hom recessive result should not be null");
        assertTrue(homRecResult.containsKey("variants"), "Result should contain 'variants' key");

        List<VariantView> homRecVariants = homRecResult.get("variants");
        assertNotNull(homRecVariants, "Variants list should not be null");
        assertTrue(homRecVariants.size() <= MAX_RETURNED_ITEMS,
                "Result should respect MAX_RETURNED_ITEMS limit");

        TestBaselines.compare("mcp.homrec.cftr.count", homRecVariants.size());

        // INH-HR-002: Hom recessive with consequence filter
        ToolResponse consequenceResponse = server.homRecessiveInTrio(
                TRIO_HR_CARRIER1, TRIO_HR_CARRIER2, TRIO_HR_AFFECTED,
                CHR_CFTR, CFTR_START, CFTR_END,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                "MISSENSE_VARIANT,FRAMESHIFT_VARIANT",  // vepConsequences
                null, null, null, null, null, null, null, null, null, null, null
        );
        Map<String, List<VariantView>> consequenceResult = (Map<String, List<VariantView>>) consequenceResponse.structuredContent();

        assertNotNull(consequenceResult, "Consequence-filtered hom recessive result should not be null");
        List<VariantView> consequenceVariants = consequenceResult.get("variants");
        assertTrue(consequenceVariants.size() <= homRecVariants.size(),
                "Filtered results should be <= unfiltered");

        // INH-HR-003: Hom recessive pathogenic only (ClinVar)
        ToolResponse pathogenicResponse = server.homRecessiveInTrio(
                TRIO_HR_CARRIER1, TRIO_HR_CARRIER2, TRIO_HR_AFFECTED,
                CHR_CFTR, CFTR_START, CFTR_END,
                null, null, null, null, null, null, null, null,
                "PATHOGENIC,LIKELY_PATHOGENIC",  // clinSignificance
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        Map<String, List<VariantView>> pathogenicResult = (Map<String, List<VariantView>>) pathogenicResponse.structuredContent();

        assertNotNull(pathogenicResult, "Pathogenic hom recessive result should not be null");
        List<VariantView> pathogenicVariants = pathogenicResult.get("variants");
        assertTrue(pathogenicVariants.size() <= homRecVariants.size(),
                "Pathogenic variants should be <= total");

        LOGGER.info("Hom recessive trio tests completed:");
        LOGGER.info("  CFTR hom recessive variants: " + homRecVariants.size());
        LOGGER.info("  CFTR consequence-filtered: " + consequenceVariants.size());
        LOGGER.info("  CFTR pathogenic: " + pathogenicVariants.size());
    }

    // ========================================
    // Test 6: Kinship Calculation (INH-KIN-001 to INH-KIN-004)
    // ========================================

    @Test
    @Order(6)
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
    // Test 7: Validate JSON Response Structure
    // ========================================

    @SuppressWarnings("unchecked")
    @Test
    @Order(7)
    @DisplayName("Validate JSON response structure from inheritance tools")
    void testJsonResponseStructure() {
        // Get some variants to validate structure
        ToolResponse toolResponse = server.hetDominantInTrio(
                TRIO_HD_AFFECTED, TRIO_HD_UNAFFECTED, TRIO_HD_PROBAND,
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, 0, 5  // Get just 5 variants
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
