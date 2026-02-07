package org.dnaerys.testdata;

import org.dnaerys.mcp.OneKGPdMCPServer.GenomicRegion;

/**
 * Test constants for Dnaerys MCP Server tests.
 *
 * Default trio: HG00405 (daughter), HG00403 & HG00404 (parents)
 * All sample IDs are pre-configured - no additional input required.
 */
public final class TestConstants {
    private TestConstants() {}

    // ========================================
    // DATASET CONSTANTS (verified values)
    // ========================================
    public static final long EXPECTED_TOTAL_SAMPLES = 3202L;
    public static final long MIN_EXPECTED_VARIANTS = 80_000_000L;
    public static final int MAX_RETURNED_ITEMS = 50;

    // ========================================
    // GENOMIC REGIONS (GRCh38 coordinates)
    // ========================================

    // BRCA1 - Breast cancer gene
    public static final String CHR_BRCA1 = "17";
    public static final int BRCA1_START = 43044295;
    public static final int BRCA1_END = 43170245;

    // TP53 - Tumor suppressor gene
    public static final String CHR_TP53 = "17";
    public static final int TP53_START = 7661779;
    public static final int TP53_END = 7687546;

    // CFTR - Cystic fibrosis gene
    public static final String CHR_CFTR = "7";
    public static final int CFTR_START = 117287120;
    public static final int CFTR_END = 117715971;

    // HBB - Hemoglobin beta gene (short region)
    public static final String CHR_HBB = "11";
    public static final int HBB_START = 5225464;
    public static final int HBB_END = 5229395;

    // Sparse region (few/no variants expected)
    public static final String CHR_SPARSE = "22";
    public static final int SPARSE_START = 50000000;
    public static final int SPARSE_END = 50001000;

    // Dense region (many variants expected)
    public static final String CHR_DENSE = "1";
    public static final int DENSE_START = 1000000;
    public static final int DENSE_END = 1100000;

    // ========================================
    // GENOMIC REGION RECORDS (for multi-region API)
    // ========================================
    public static final GenomicRegion REGION_BRCA1 = new GenomicRegion(CHR_BRCA1, BRCA1_START, BRCA1_END, null, null);
    public static final GenomicRegion REGION_TP53 = new GenomicRegion(CHR_TP53, TP53_START, TP53_END, null, null);
    public static final GenomicRegion REGION_CFTR = new GenomicRegion(CHR_CFTR, CFTR_START, CFTR_END, null, null);
    public static final GenomicRegion REGION_HBB = new GenomicRegion(CHR_HBB, HBB_START, HBB_END, null, null);
    public static final GenomicRegion REGION_SPARSE = new GenomicRegion(CHR_SPARSE, SPARSE_START, SPARSE_END, null, null);
    public static final GenomicRegion REGION_DENSE = new GenomicRegion(CHR_DENSE, DENSE_START, DENSE_END, null, null);

    // ========================================
    // SAMPLE IDs - Default trio: HG00405 (daughter), HG00403 & HG00404 (parents)
    // ========================================
    public static final String SAMPLE_FEMALE = "HG00405";   // Daughter
    public static final String SAMPLE_MALE = "HG00403";     // Parent 1
    public static final String SAMPLE_GENERAL = "HG00404";  // Parent 2

    // ========================================
    // TRIO IDs - Using the same family for all inheritance patterns
    // ========================================

    // De Novo trio (variants in proband absent in both parents)
    public static final String TRIO_DN_PARENT1 = "HG00403";
    public static final String TRIO_DN_PARENT2 = "HG00404";
    public static final String TRIO_DN_PROBAND = "HG00405";

    // Heterozygous Dominant trio (affected parent, unaffected parent, affected proband)
    public static final String TRIO_HD_AFFECTED = "HG00403";
    public static final String TRIO_HD_UNAFFECTED = "HG00404";
    public static final String TRIO_HD_PROBAND = "HG00405";

    // Homozygous Recessive trio (carrier parents, affected proband)
    public static final String TRIO_HR_CARRIER1 = "HG00403";
    public static final String TRIO_HR_CARRIER2 = "HG00404";
    public static final String TRIO_HR_AFFECTED = "HG00405";

    // ========================================
    // KINSHIP PAIRS
    // ========================================
    public static final String KINSHIP_PARENT = "HG00403";
    public static final String KINSHIP_CHILD = "HG00405";
    public static final String KINSHIP_UNRELATED1 = "HG00406";
    public static final String KINSHIP_UNRELATED2 = "HG00406";
}
