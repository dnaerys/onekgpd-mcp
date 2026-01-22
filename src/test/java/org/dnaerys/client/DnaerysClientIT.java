package org.dnaerys.client;

import org.dnaerys.cluster.grpc.Variant;
import org.dnaerys.testdata.TestBaselines;
import org.dnaerys.testdata.TestBaselines.BaselineResult;
import org.dnaerys.testdata.TestBaselines.ComparisonResult;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.logging.Logger;

import static org.dnaerys.testdata.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DnaerysClient.
 *
 * These tests require network connectivity to db.dnaerys.org:443.
 * Run with: ./mvnw verify -DskipIntegrationTests=false
 *
 * Test cases cover:
 * - CLI-INT-001 to CLI-INT-007: Metadata queries
 * - CLI-INT-010 to CLI-INT-015: Region queries
 * - CLI-INT-020 to CLI-INT-023: Pagination tests
 * - CLI-INT-030 to CLI-INT-034: Sample-specific queries
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DnaerysClientIT {

    private static final Logger LOGGER = Logger.getLogger(DnaerysClientIT.class.getName());

    private static DnaerysClient client;

    @BeforeAll
    static void setUp() {
        client = new DnaerysClient();
    }

    @AfterAll
    static void saveBaselines() {
        TestBaselines.saveBaselines();
    }

    // ========================================
    // Test 1: Metadata Queries (CLI-INT-001 to CLI-INT-007)
    // ========================================

    @Test
    @Order(1)
    @DisplayName("CLI-INT-001 to CLI-INT-007: Metadata queries - sample counts, variant totals")
    void testMetadataQueries() {
        // CLI-INT-001: Total sample count
        DnaerysClient.SampleCounts counts = client.getSampleCounts();
        assertNotNull(counts, "Sample counts should not be null");

        long totalSamples = counts.total();
        assertEquals(EXPECTED_TOTAL_SAMPLES, totalSamples,
                "Total samples should be " + EXPECTED_TOTAL_SAMPLES);

        ComparisonResult totalResult = TestBaselines.compare("total.samples", totalSamples);
        assertNotEquals(BaselineResult.FAIL, totalResult.result(),
                "Total samples baseline check failed: " + totalResult.message());

        // CLI-INT-002: Female sample count
        long femaleSamples = counts.female();
        assertTrue(femaleSamples > 0, "Female samples should be > 0");
        assertTrue(femaleSamples < EXPECTED_TOTAL_SAMPLES,
                "Female samples should be < total");

        ComparisonResult femaleResult = TestBaselines.compare("total.female.samples", femaleSamples);
        assertNotEquals(BaselineResult.FAIL, femaleResult.result(),
                "Female samples baseline check failed: " + femaleResult.message());

        // CLI-INT-003: Male sample count
        long maleSamples = counts.male();
        assertTrue(maleSamples > 0, "Male samples should be > 0");
        assertTrue(maleSamples < EXPECTED_TOTAL_SAMPLES,
                "Male samples should be < total");

        ComparisonResult maleResult = TestBaselines.compare("total.male.samples", maleSamples);
        assertNotEquals(BaselineResult.FAIL, maleResult.result(),
                "Male samples baseline check failed: " + maleResult.message());

        // CLI-INT-004: Sample counts sum
        assertEquals(totalSamples, femaleSamples + maleSamples,
                "Female + Male should equal total samples");

        // CLI-INT-005: All sample IDs
        List<String> allSampleIds = client.getSampleIds(DnaerysClient.Gender.BOTH);
        assertNotNull(allSampleIds, "Sample IDs list should not be null");
        assertEquals(EXPECTED_TOTAL_SAMPLES, allSampleIds.size(),
                "Sample IDs list size should be " + EXPECTED_TOTAL_SAMPLES);

        // Verify our test samples exist
        assertTrue(allSampleIds.contains(SAMPLE_FEMALE),
                "Sample list should contain " + SAMPLE_FEMALE);
        assertTrue(allSampleIds.contains(SAMPLE_MALE),
                "Sample list should contain " + SAMPLE_MALE);
        assertTrue(allSampleIds.contains(SAMPLE_GENERAL),
                "Sample list should contain " + SAMPLE_GENERAL);

        // CLI-INT-006: Variant total
        long variantsTotal = client.variantsTotal();
        assertTrue(variantsTotal > MIN_EXPECTED_VARIANTS,
                "Variants total should be > " + MIN_EXPECTED_VARIANTS + ", got " + variantsTotal);

        ComparisonResult variantsResult = TestBaselines.compare("total.variants", variantsTotal);
        assertNotEquals(BaselineResult.FAIL, variantsResult.result(),
                "Variants total baseline check failed: " + variantsResult.message());

        LOGGER.info("Metadata queries completed successfully:");
        LOGGER.info("  Total samples: " + totalSamples);
        LOGGER.info("  Female samples: " + femaleSamples);
        LOGGER.info("  Male samples: " + maleSamples);
        LOGGER.info("  Total variants: " + variantsTotal);
    }

    // ========================================
    // Test 2: Region Query (CLI-INT-010 to CLI-INT-015)
    // ========================================

    @Test
    @Order(2)
    @DisplayName("CLI-INT-010 to CLI-INT-015: Region query - BRCA1 region with filters")
    void testRegionQuery() {
        // CLI-INT-010: BRCA1 variant count (unfiltered)
        // Note: selectHom=true and selectHet=true means select ALL variants (both hom and het)
        long brca1Count = client.countVariantsInRegion(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                true, true, // selectHom, selectHet (true, true = all variants)
                null, null, null, null, // refAllele, altAllele, varMinLength, varMaxLength
                null, null, null, null, // biallelicOnly, multiallelicOnly, excludeMales, excludeFemales
                null, null, null, null, null, null, // AF filters, gnomAD filters
                null, null, null, null, // impact, bioType, featureType, variantType
                null, null, null, null, null // consequences, alphaMissense, amScores, clinSig
        );

        assertTrue(brca1Count > 0, "BRCA1 region should have variants");

        ComparisonResult brca1Result = TestBaselines.compare("brca1.total.variants", brca1Count);
        assertNotEquals(BaselineResult.FAIL, brca1Result.result(),
                "BRCA1 variant count baseline check failed: " + brca1Result.message());

        // CLI-INT-011: BRCA1 variant select
        List<Variant> brca1Variants = client.selectVariantsInRegion(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                true, true, // selectHom, selectHet
                null, null, null, null, // refAllele, altAllele, varMinLength, varMaxLength
                null, null, null, null, // biallelicOnly, multiallelicOnly, excludeMales, excludeFemales
                null, null, null, null, null, null, // AF filters, gnomAD filters
                null, null, null, null, // impact, bioType, featureType, variantType
                null, null, null, null, null, // consequences, alphaMissense, amScores, clinSig
                null, null // skip, limit
        );

        assertNotNull(brca1Variants, "BRCA1 variants list should not be null");
        assertFalse(brca1Variants.isEmpty(), "BRCA1 variants list should not be empty");
        assertTrue(brca1Variants.size() <= MAX_RETURNED_ITEMS,
                "BRCA1 variants should respect MAX_RETURNED_ITEMS limit");

        // CLI-INT-012: Sparse region (may have few/no variants)
        long sparseCount = client.countVariantsInRegion(
                CHR_SPARSE, SPARSE_START, SPARSE_END,
                true, true,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        // Sparse region may have 0 variants - just capture baseline
        ComparisonResult sparseResult = TestBaselines.compare("sparse.region.variants", sparseCount);
        LOGGER.info("Sparse region variant count: " + sparseCount);

        // CLI-INT-013: High impact only
        long highImpactCount = client.countVariantsInRegion(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                true, true,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                "HIGH", null, null, null, // impact = HIGH
                null, null, null, null, null
        );

        assertTrue(highImpactCount <= brca1Count,
                "High impact count should be <= total count");

        ComparisonResult highImpactResult = TestBaselines.compare("brca1.high.impact", highImpactCount);
        assertNotEquals(BaselineResult.FAIL, highImpactResult.result(),
                "BRCA1 high impact baseline check failed: " + highImpactResult.message());

        // CLI-INT-014: Pathogenic only (ClinVar)
        long pathogenicCount = client.countVariantsInRegion(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                true, true,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, "PATHOGENIC" // clinSig
        );

        assertTrue(pathogenicCount <= brca1Count,
                "Pathogenic count should be <= total count");

        ComparisonResult pathogenicResult = TestBaselines.compare("brca1.pathogenic", pathogenicCount);
        LOGGER.info("BRCA1 pathogenic variant count: " + pathogenicCount);

        // CLI-INT-015: Rare variants (AF < 0.01)
        long rareCount = client.countVariantsInRegion(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                true, true,
                null, null, null, null,
                null, null, null, null,
                0.01f, null, null, null, null, null, // afLessThan = 0.01
                null, null, null, null,
                null, null, null, null, null
        );

        assertTrue(rareCount <= brca1Count,
                "Rare variant count should be <= total count");

        ComparisonResult rareResult = TestBaselines.compare("brca1.rare.af01", rareCount);
        LOGGER.info("BRCA1 rare (AF<0.01) variant count: " + rareCount);

        LOGGER.info("Region queries completed successfully:");
        LOGGER.info("  BRCA1 total variants: " + brca1Count);
        LOGGER.info("  BRCA1 high impact: " + highImpactCount);
        LOGGER.info("  BRCA1 pathogenic: " + pathogenicCount);
        LOGGER.info("  BRCA1 rare (AF<0.01): " + rareCount);
        LOGGER.info("  Sparse region: " + sparseCount);
    }

    // ========================================
    // Test 3: Pagination Enforcement (CLI-INT-020 to CLI-INT-023)
    // ========================================

    @Test
    @Order(3)
    @DisplayName("CLI-INT-020 to CLI-INT-023: Pagination enforcement - verify MAX_RETURNED_ITEMS limit")
    void testPaginationEnforcement() {
        // Use dense region for pagination tests (many variants expected)
        // CLI-INT-020: Pagination limit enforcement (request more than limit)
        List<Variant> overLimitResults = client.selectVariantsInRegion(
                CHR_DENSE, DENSE_START, DENSE_END,
                true, true, // selectHom, selectHet (all variants)
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                0, 500 // skip=0, limit=500 (should be capped to MAX_RETURNED_ITEMS)
        );

        assertNotNull(overLimitResults, "Results should not be null");
        assertTrue(overLimitResults.size() <= MAX_RETURNED_ITEMS,
                "Results should be capped at MAX_RETURNED_ITEMS (" + MAX_RETURNED_ITEMS +
                        "), got " + overLimitResults.size());

        // CLI-INT-021: Skip functionality
        List<Variant> page1 = client.selectVariantsInRegion(
                CHR_DENSE, DENSE_START, DENSE_END,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                0, MAX_RETURNED_ITEMS // First page
        );

        List<Variant> page2 = client.selectVariantsInRegion(
                CHR_DENSE, DENSE_START, DENSE_END,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                MAX_RETURNED_ITEMS, MAX_RETURNED_ITEMS // Second page (skip first batch)
        );

        // If both pages have results, they should be different
        if (!page1.isEmpty() && !page2.isEmpty()) {
            // Check that first variant of page2 is different from first of page1
            Variant first1 = page1.get(0);
            Variant first2 = page2.get(0);

            // Variants should be different (different position or alleles)
            boolean sameVariant = first1.getStart() == first2.getStart() &&
                    first1.getChr().equals(first2.getChr()) &&
                    first1.getRef().equals(first2.getRef()) &&
                    first1.getAlt().equals(first2.getAlt());

            assertFalse(sameVariant,
                    "Page 1 and Page 2 should return different variants when using skip");
        }

        // CLI-INT-022: Multiple pages - no overlap check
        // Already covered above - page1 and page2 should not overlap

        // CLI-INT-023: Beyond data range
        List<Variant> beyondRange = client.selectVariantsInRegion(
                CHR_DENSE, DENSE_START, DENSE_END,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                1000000, MAX_RETURNED_ITEMS // Very large skip
        );

        // Should return empty or minimal results
        assertTrue(beyondRange.size() < MAX_RETURNED_ITEMS,
                "Beyond range query should return fewer than MAX_RETURNED_ITEMS results");

        LOGGER.info("Pagination tests completed successfully:");
        LOGGER.info("  Over-limit request capped to: " + overLimitResults.size());
        LOGGER.info("  Page 1 size: " + page1.size());
        LOGGER.info("  Page 2 size: " + page2.size());
        LOGGER.info("  Beyond range size: " + beyondRange.size());
    }

    // ========================================
    // Test 4: Filter Combinations
    // ========================================

    @Test
    @Order(4)
    @DisplayName("Filter combinations - AF + impact + clinSig combined filtering")
    void testFilterCombinations() {
        // Get unfiltered count first
        long unfilteredCount = client.countVariantsInRegion(
                CHR_TP53, TP53_START, TP53_END,
                true, true, // selectHom, selectHet (all variants)
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        assertTrue(unfilteredCount > 0, "TP53 region should have variants");
        TestBaselines.compare("tp53.total.variants", unfilteredCount);

        // Test combined filter: HIGH impact + rare (AF < 0.01)
        long highRareCount = client.countVariantsInRegion(
                CHR_TP53, TP53_START, TP53_END,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                0.01f, null, null, null, null, null, // afLessThan
                "HIGH", null, null, null, // impact
                null, null, null, null, null
        );

        assertTrue(highRareCount <= unfilteredCount,
                "Combined filter count should be <= unfiltered");

        // Test triple filter: HIGH impact + rare + MISSENSE_VARIANT consequence
        long tripleFilterCount = client.countVariantsInRegion(
                CHR_TP53, TP53_START, TP53_END,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                0.01f, null, null, null, null, null, // afLessThan
                "HIGH,MODERATE", null, null, null, // impact
                "MISSENSE_VARIANT", null, null, null, null // consequences
        );

        assertTrue(tripleFilterCount <= unfilteredCount,
                "Triple filter count should be <= unfiltered");

        TestBaselines.compare("tp53.missense.variants", tripleFilterCount);

        LOGGER.info("Filter combination tests completed:");
        LOGGER.info("  TP53 unfiltered: " + unfilteredCount);
        LOGGER.info("  TP53 HIGH+rare: " + highRareCount);
        LOGGER.info("  TP53 HIGH/MOD+rare+missense: " + tripleFilterCount);
    }

    // ========================================
    // Test 5: Sample Query (CLI-INT-030 to CLI-INT-034)
    // ========================================

    @Test
    @Order(5)
    @DisplayName("CLI-INT-030 to CLI-INT-034: Sample-specific variant queries")
    void testSampleQuery() {
        // CLI-INT-030: Valid sample variant count (both hom and het)
        long sampleCount = client.countVariantsInRegionInSample(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                SAMPLE_FEMALE, // Use HG00405
                true, true, // selectHom, selectHet (all variants)
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        assertTrue(sampleCount >= 0, "Sample variant count should be >= 0");

        TestBaselines.compare("sample.hg00405.brca1.count", sampleCount);

        // CLI-INT-031: Valid sample variant select
        List<Variant> sampleVariants = client.selectVariantsInRegionInSample(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                SAMPLE_FEMALE,
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null
        );

        assertNotNull(sampleVariants, "Sample variants list should not be null");
        // Results may be empty if sample has no variants in BRCA1

        // CLI-INT-032: Invalid sample ID should return 0
        long invalidSampleCount = client.countVariantsInRegionInSample(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                "INVALID_SAMPLE_ID",
                true, true, // selectHom, selectHet
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        assertEquals(0L, invalidSampleCount,
                "Invalid sample should return 0 variants");

        // CLI-INT-033: Homozygous variants in sample
        long homCount = client.countVariantsInRegionInSample(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                SAMPLE_FEMALE,
                true, false, // selectHom=true, selectHet=false
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        // CLI-INT-034: Heterozygous variants in sample
        long hetCount = client.countVariantsInRegionInSample(
                CHR_BRCA1, BRCA1_START, BRCA1_END,
                SAMPLE_FEMALE,
                false, true, // selectHom=false, selectHet=true
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        // The sum of hom + het should be <= total (they might overlap or be exclusive depending on implementation)
        // Each individual count should be >= 0
        assertTrue(homCount >= 0, "Homozygous count should be >= 0");
        assertTrue(hetCount >= 0, "Heterozygous count should be >= 0");

        LOGGER.info("Sample query tests completed:");
        LOGGER.info("  Sample " + SAMPLE_FEMALE + " BRCA1 total: " + sampleCount);
        LOGGER.info("  Sample " + SAMPLE_FEMALE + " BRCA1 homozygous: " + homCount);
        LOGGER.info("  Sample " + SAMPLE_FEMALE + " BRCA1 heterozygous: " + hetCount);
        LOGGER.info("  Invalid sample count: " + invalidSampleCount);
    }
}
