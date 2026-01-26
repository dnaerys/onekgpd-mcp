# Test Specification: Dnaerys MCP Server for 1000 Genomes Project

**Document Version:** 1.3
**Created:** 2026-01-15
**Last Updated:** 2026-01-26
**Status:** Ready for Implementation
**Purpose:** Blueprint for implementing comprehensive test coverage

> **Note v1.3:** Inheritance model tools (`deNovoInTrio`, `hetDominantInTrio`, `homRecessiveInTrio`) have been **DEPRECATED** and removed from the implementation. Related test cases are marked with `[DEPRECATED]`. New homozygous reference tools have been added. See CHANGELOG-DEV.md for details.

---

## Key Updates in v1.2

> **🟢 Phase 1 works WITHOUT server access**
> - Maven configuration enforces test separation
> - Integration tests skipped by default (`skipIntegrationTests=true`)
> - Each phase documents server requirements
> - Baseline management strategy for data change detection
> - ✅ Pre-filled all sample IDs with trio HG00405/HG00403/HG00404
> - ✅ Removed all sample discovery mechanisms and language
> - ✅ Added `isUpdateMode()` implementation detail
> - ✅ Specification now ready to implement immediately (no user input required for Phases 2-3)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Codebase Analysis](#2-codebase-analysis)
3. [Testing Architecture](#3-testing-architecture)
   - 3.1 Two-Tier Model
   - 3.2 Test Naming Conventions
   - 3.3 Test Package Structure
   - **3.4 Test Separation by Server Requirement** *(NEW)*
4. [Test Data Requirements](#4-test-data-requirements)
   - 4.1-4.4 Genomic Regions, Samples, Filters, Expected Results
   - **4.5 Test Baseline Management** *(NEW)*
5. [Test Case Specifications](#5-test-case-specifications)
6. [Dependencies and Configuration](#6-dependencies-and-configuration)
   - **6.2 Maven Plugin Configuration** *(UPDATED - skipIntegrationTests)*
7. [Implementation Phases](#7-implementation-phases) *(UPDATED - server requirements per phase)*
8. [Success Criteria](#8-success-criteria) *(UPDATED - offline validation)*
9. [Risk Assessment](#9-risk-assessment)
10. [Assumptions and Constraints](#10-assumptions-and-constraints)
11. [Appendices](#11-appendices)
    - **Appendix C: Command Reference** *(UPDATED - phased commands)*

---

## 1. Executive Summary

### 1.1 Objective

Implement a two-tier testing strategy for the Dnaerys MCP server, providing:
- Fast unit tests for rapid development feedback
- Integration tests validating real gRPC server interactions
- Comprehensive coverage of the 31 MCP tools and supporting infrastructure

### 1.2 Scope

| In Scope | Out of Scope |
|----------|--------------|
| Entity mapper unit tests | Performance/load testing |
| DnaerysClient unit + integration tests | Security penetration testing |
| OneKGPdMCPServer tool tests | UI/UX testing |
| Pagination behavior validation | Database administration |
| Inheritance model tool testing | Proto file modifications |
| Error handling verification | gRPC server-side testing |

### 1.3 Key Metrics

| Metric | Target |
|--------|--------|
| Unit test execution time | < 10 seconds |
| Integration test execution time | < 2 minutes |
| Entity mapper coverage | 100% |
| DnaerysClient method coverage | 80% |
| MCP tool coverage (critical paths) | 70% |

---

## 2. Codebase Analysis

### 2.1 Component Inventory

#### 2.1.1 OneKGPdMCPServer (`src/main/java/org/dnaerys/mcp/OneKGPdMCPServer.java`)

**Size:** ~1200 LOC
**Role:** Main MCP server exposing 31 @Tool annotated methods

**Tool Categories:**

| Category | Count | Methods |
|----------|-------|---------|
| Metadata/Sample | 3 | `getDatasetInfo`, `countSamplesHomozygousReference`, `selectSamplesHomozygousReference` |
| Variant Counting | 3 | `countVariantsInRegion`, `countHomozygousVariantsInRegion`, `countHeterozygousVariantsInRegion` |
| Per-Sample Counting | 3 | `countVariantsInSample`, `countHomozygousVariantsInSample`, `countHeterozygousVariantsInSample` |
| Variant Retrieval | 6 | `variantsInRegion`, `homozygousVariantsInRegion`, `heterozygousVariantsInRegion`, `variantsInSample`, `homozygousVariantsInSample`, `heterozygousVariantsInSample` |
| Sample Discovery | 6 | `samplesInRegion`, `samplesHomInRegion`, `samplesHetInRegion`, `samplesListInRegion`, `samplesListHomInRegion`, `samplesListHetInRegion` |
| Inheritance Models | 1 | `kinship` (**[DEPRECATED]** `deNovoInTrio`, `hetDominantInTrio`, `homRecessiveInTrio` removed) |

**Architectural Characteristics:**
- No input validation (delegated to DnaerysClient)
- No error handling (exceptions propagate to MCP framework)
- Pure delegation pattern to DnaerysClient
- Heavy use of optional parameters (20+ per method)

#### 2.1.2 DnaerysClient (`src/main/java/org/dnaerys/client/DnaerysClient.java`)

**Size:** ~1000 LOC
**Role:** gRPC client handling all variant store queries

**Key Constants:**
```java
final Integer MAX_RETURNED_ITEMS = 50;
```

**Public Methods:**

| Method | Return Type | gRPC Endpoint |
|--------|-------------|---------------|
| `getDatasetInfo()` | `DatasetInfo` | `datasetInfo` |
| `countSamplesHomozygousReference(...)` | `long` | `countSamplesHomRef` |
| `selectSamplesHomozygousReference(...)` | `List<String>` | `selectSamplesHomRef` |
| `countVariantsInRegion(...)` | `long` | `countVariantsInRegion` |
| `countVariantsInRegionInSample(...)` | `long` | `countVariantsInRegion` |
| `selectVariantsInRegion(...)` | `List<String>` | `selectVariantsInRegion` |
| `selectVariantsInRegionInSample(...)` | `List<String>` | `selectVariantsInRegion` |
| `countSamplesInRegion(...)` | `long` | `countSamplesInRegion` |
| `selectSamplesInRegion(...)` | `List<String>` | `selectSamplesInRegion` |
| **[DEPRECATED]** ~~`selectDeNovo(...)`~~ | ~~`List<String>`~~ | ~~`selectDeNovo`~~ |
| **[DEPRECATED]** ~~`selectHetDominant(...)`~~ | ~~`List<String>`~~ | ~~`selectHetDominant`~~ |
| **[DEPRECATED]** ~~`selectHomRecessive(...)`~~ | ~~`List<String>`~~ | ~~`selectHomRecessive`~~ |
| `kinship(...)` | `String` | `kinshipDuo` |

**Error Handling Pattern:**
```java
try {
    // gRPC call
} catch (Throwable th) {
    th.printStackTrace();
    Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
}
return defaultValue;  // 0L, List.of("{}"), or ""
```

**Pagination Logic:**
```java
if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS)
    limit = MAX_RETURNED_ITEMS;
if (skip == null || skip < 0)
    skip = 0;
```

#### 2.1.3 Entity Mappers (`src/main/java/org/dnaerys/client/entity/`)

**Count:** 10 mapper classes
**Role:** Convert string filter parameters to gRPC enum values

| Mapper | Target Enum | Valid Values Count |
|--------|-------------|-------------------|
| `ImpactMapper` | `Impact` | 4 (HIGH, MODERATE, LOW, MODIFIER) |
| `BiotypeMapper` | `BioType` | 24 |
| `FeatureTypeMapper` | `FeatureType` | 3 (TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE) |
| `VariantTypeMapper` | `VariantType` | 9 |
| `ConsequencesMapper` | `Consequence` | 41 |
| `ClinSigMapper` | `ClinSignificance` | 14 |
| `AlphaMissenseMapper` | `AlphaMissense` | 3 (with AM_ prefix) |
| `SIFTMapper` | `SIFT` | 2 (TOLERATED, DELETERIOUS) |
| `PolyPhenMapper` | `PolyPhen` | 4 |

**Common Normalization Pattern:**
```java
String normalized = input
    .trim()                    // Remove whitespace
    .toUpperCase()             // Convert to uppercase
    .replace(' ', '_')         // Replace spaces with underscores
    .replace('-', '_');        // Replace dashes with underscores
```

**AlphaMissense Special Case:**
```java
String amNormalized = "AM_" + normalized;  // Prefix required
```

#### 2.1.4 GrpcChannel (`src/main/java/org/dnaerys/client/GrpcChannel.java`)

**Role:** Singleton managing gRPC connection
**Default Target:** `db.dnaerys.org:443` (TLS enabled)

### 2.2 Current Test State

**Existing Tests:**
- `GreetingResourceTest.java` - Sample QuarkusTest (unrelated to MCP functionality)
- `GreetingResourceIT.java` - Sample integration test

**Test Dependencies Present:**
- `quarkus-junit5`
- `rest-assured`

**Test Dependencies Missing:**
- `mockito-core` or `quarkus-junit5-mockito`
- Test-specific application properties

---

## 3. Testing Architecture

### 3.1 Two-Tier Model

```
┌─────────────────────────────────────────────────────────────────┐
│                        TIER 1: UNIT TESTS                       │
│                     ./mvnw test (no network)                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │Entity Mapper │  │DnaerysClient │  │  OneKGPdMCPServer     │  │
│  │   Tests      │  │  Unit Tests  │  │    Unit Tests        │  │
│  │              │  │              │  │                      │  │
│  │ • Normalization│ • Mock GrpcChannel│ • Mock DnaerysClient │  │
│  │ • Edge cases │  │ • Pagination │  │ • Delegation verify  │  │
│  │ • UNRECOGNIZED│  │ • Annotations│  │ • Parameter passing  │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   TIER 2: INTEGRATION TESTS                     │
│              ./mvnw verify (requires network)                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              DnaerysClient Integration Tests              │  │
│  │                                                           │  │
│  │  • Real gRPC calls to db.dnaerys.org:443                │  │
│  │  • Metadata queries (sample counts, variant totals)      │  │
│  │  • Region queries with known coordinates                 │  │
│  │  • Pagination enforcement validation                     │  │
│  │  • Filter combination testing                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              MCP Tool Integration Tests                   │  │
│  │                                                           │  │
│  │  • End-to-end tool execution                             │  │
│  │  • Inheritance model tools with real trios               │  │
│  │  • JSON response structure validation                    │  │
│  │  • Complex filter scenarios                              │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Test Naming Conventions

| Type | Suffix | Location | Example |
|------|--------|----------|---------|
| Unit Test | `Test.java` | `src/test/java/` | `ImpactMapperTest.java` |
| Integration Test | `IT.java` | `src/test/java/` | `DnaerysClientIT.java` |

### 3.3 Test Package Structure

```
src/test/java/org/dnaerys/
├── client/
│   ├── entity/
│   │   ├── ImpactMapperTest.java
│   │   ├── BiotypeMapperTest.java
│   │   ├── FeatureTypeMapperTest.java
│   │   ├── VariantTypeMapperTest.java
│   │   ├── ConsequencesMapperTest.java
│   │   ├── ClinSigMapperTest.java
│   │   ├── AlphaMissenseMapperTest.java
│   │   ├── SIFTMapperTest.java
│   │   └── PolyPhenMapperTest.java
│   ├── DnaerysClientTest.java
│   └── DnaerysClientIT.java
├── mcp/
│   ├── OneKGPdMCPServerTest.java
│   └── OneKGPdMCPServerIT.java
└── testdata/
    └── TestConstants.java
```

### 3.4 Test Separation by Server Requirement

> **⚠️ CRITICAL:** This separation ensures Phase 1 works without any server access.

#### 3.4.1 Unit Tests (🟢 No Server Required)

**Naming Pattern:** `*Test.java`
**Executed By:** Maven Surefire (`./mvnw test`)
**Phases:** 1, 4

```
src/test/java/org/dnaerys/
├── client/
│   ├── entity/                        # Phase 1
│   │   ├── ImpactMapperTest.java      # Mapper unit tests
│   │   ├── BiotypeMapperTest.java
│   │   ├── ConsequencesMapperTest.java
│   │   ├── ClinSigMapperTest.java
│   │   ├── AlphaMissenseMapperTest.java
│   │   ├── FeatureTypeMapperTest.java
│   │   ├── VariantTypeMapperTest.java
│   │   ├── SIFTMapperTest.java
│   │   └── PolyPhenMapperTest.java
│   └── DnaerysClientTest.java         # Phase 4 - Mocked client tests
├── mcp/
│   └── OneKGPdMCPServerTest.java       # Phase 4 - Mocked server tests
└── testdata/
    ├── TestConstants.java             # Shared constants
    └── TestBaselines.java             # Baseline comparison utility
```

#### 3.4.2 Integration Tests (🔴 Server Required)

**Naming Pattern:** `*IT.java` or `*IntegrationTest.java`
**Executed By:** Maven Failsafe (`./mvnw verify -DskipIntegrationTests=false`)
**Phases:** 2, 3

```
src/test/java/org/dnaerys/
├── client/
│   └── DnaerysClientIT.java           # Phase 2 - Real gRPC queries
└── mcp/
    └── OneKGPdMCPServerIT.java         # Phase 3 - End-to-end tool tests
```

#### 3.4.3 Test Resources

```
src/test/resources/
├── application.properties             # Test configuration
└── test-baselines.properties          # Integration test baselines (Phase 2+)
```

#### 3.4.4 Naming Convention Enforcement

| Pattern | Plugin | When Run | Server? |
|---------|--------|----------|---------|
| `*Test.java` | Surefire | `./mvnw test` | 🟢 No |
| `*IT.java` | Failsafe | `./mvnw verify -DskipIntegrationTests=false` | 🔴 Yes |
| `*IntegrationTest.java` | Failsafe | `./mvnw verify -DskipIntegrationTests=false` | 🔴 Yes |

**Enforcement Rules:**
- Surefire explicitly **excludes** `*IT.java` and `*IntegrationTest.java`
- Failsafe only **includes** `*IT.java` and `*IntegrationTest.java`
- Failsafe is **skipped by default** (requires `-DskipIntegrationTests=false`)

---

## 4. Test Data Requirements

### 4.1 Genomic Regions

#### 4.1.1 Known Gene Regions (Expected Variants)

| Region ID | Gene | Chromosome | Start | End | Expected Characteristics |
|-----------|------|------------|-------|-----|-------------------------|
| `REGION_BRCA1` | BRCA1 | 17 | 43044295 | 43170245 | High clinical significance, pathogenic variants |
| `REGION_TP53` | TP53 | 17 | 7661779 | 7687546 | ClinVar entries, well-characterized |
| `REGION_CFTR` | CFTR | 7 | 117287120 | 117715971 | Recessive disease variants |
| `REGION_HBB` | HBB | 11 | 5225464 | 5229395 | Short region, known variants |

#### 4.1.2 Test Boundary Regions

| Region ID | Chromosome | Start | End | Purpose |
|-----------|------------|-------|-----|---------|
| `REGION_SPARSE` | 22 | 50000000 | 50001000 | Few/no variants expected |
| `REGION_DENSE` | 1 | 1000000 | 1100000 | High variant density |
| `REGION_MINIMAL` | 1 | 1000000 | 1000001 | Single position |
| `REGION_CENTROMERE` | 1 | 121500000 | 125000000 | Gap region, no variants |

#### 4.1.3 Invalid Regions (Error Testing)

| Region ID | Chromosome | Start | End | Expected Error |
|-----------|------------|-------|-----|----------------|
| `REGION_INVALID_CHR` | "99" | 1000 | 2000 | Invalid chromosome |
| `REGION_INVERTED` | "1" | 2000 | 1000 | Start > End |
| `REGION_NEGATIVE` | "1" | -100 | 1000 | Negative coordinate |
| `REGION_OVERFLOW` | "1" | 0 | 999999999999 | Coordinate overflow |

### 4.2 Sample IDs

#### 4.2.1 Individual Samples

| Sample ID Constant | Sample ID | Sex | Use Case |
|--------------------|-----------|-----|----------|
| `SAMPLE_FEMALE` | HG00405 | Female | Female-specific filtering (daughter) |
| `SAMPLE_MALE` | HG00403 | Male | Male-specific filtering (parent 1) |
| `SAMPLE_GENERAL` | HG00404 | Any | General variant queries (parent 2) |

**NOTE:** Default sample IDs from trio HG00405/HG00403/HG00404 are pre-filled.
These can be used for all phases.

#### 4.2.2 Trio Families

| Trio Type | Parent1/Affected | Parent2/Unaffected | Proband | Use Case |
|-----------|------------------|-------------------|---------|----------|
| De Novo | HG00403 | HG00404 | HG00405 | `selectDeNovo` testing |
| Het Dominant | HG00403 (affected) | HG00404 (unaffected) | HG00405 | `selectHetDominant` testing |
| Hom Recessive | HG00403 (carrier) | HG00404 (carrier) | HG00405 (affected) | `selectHomRecessive` testing |

**NOTE:** All trio types use the same family (HG00405 daughter, HG00403/HG00404 parents).
Tests will validate against real inheritance patterns found in this trio.

#### 4.2.3 Kinship Pairs

| Pair Type | Sample1 | Sample2 | Expected Degree |
|-----------|---------|---------|-----------------|
| Parent-Child | HG00403 | HG00405 | First-degree |
| Parent-Child | HG00404 | HG00405 | First-degree |
| Unrelated | HG00403 | HG00406 | Unrelated |

**NOTE:** HG00406 is from a different family, used for unrelated kinship tests.

### 4.3 Filter Values

#### 4.3.1 Impact Filters

```java
// Valid values
"HIGH", "MODERATE", "LOW", "MODIFIER"

// Normalization test cases
"high" → HIGH
"  High  " → HIGH
"high-impact" → UNRECOGNIZED (invalid)
"" → UNRECOGNIZED
null → UNRECOGNIZED
```

#### 4.3.2 Consequence Filters (subset)

```java
// Common pathogenic consequences
"STOP_GAINED", "FRAMESHIFT_VARIANT", "SPLICE_ACCEPTOR_VARIANT",
"SPLICE_DONOR_VARIANT", "START_LOST", "STOP_LOST"

// Common benign consequences
"SYNONYMOUS_VARIANT", "INTRON_VARIANT", "3_PRIME_UTR_VARIANT"

// CSV combination test
"STOP_GAINED,FRAMESHIFT_VARIANT,MISSENSE_VARIANT"
```

#### 4.3.3 Clinical Significance Filters

```java
// Pathogenic spectrum
"PATHOGENIC", "LIKELY_PATHOGENIC", "PATHOGENIC_LOW_PENETRANCE"

// Benign spectrum
"BENIGN", "LIKELY_BENIGN", "BENIGN_LOW_PENETRANCE"

// Uncertain
"UNCERTAIN_SIGNIFICANCE", "CONFLICTING_CLASSIFICATIONS"
```

#### 4.3.4 Allele Frequency Thresholds

| Threshold | AF Value | Description |
|-----------|----------|-------------|
| Ultra-rare | < 0.0001 | Very rare variants |
| Rare | < 0.01 | Rare variants |
| Common | > 0.05 | Common variants |
| MAF filter | > 0.01 AND < 0.05 | Intermediate frequency |

### 4.4 Expected Results

#### 4.4.1 Dataset Constants

| Query | Expected Result | Tolerance |
|-------|-----------------|-----------|
| `countSamplesTotal()` | 3202 | Exact |
| `variantsTotal()` | > 80,000,000 | Order of magnitude |
| `sampleIds().size()` | 3202 | Exact |

#### 4.4.2 Pagination Validation

| Scenario | Request Limit | Expected Result Size |
|----------|---------------|---------------------|
| Under limit | 50 | ≤ 50 |
| At limit | 100 | ≤ 100 |
| Over limit | 500 | ≤ 100 (capped) |
| Null limit | null | ≤ 100 (default) |
| Negative limit | -1 | ≤ 100 (normalized) |

### 4.5 Test Baseline Management

> **Purpose:** Detect server-side data changes between test runs.
> Integration tests run against live data that may change over time.

#### 4.5.1 Baseline Establishment (First Run)

When integration tests first run successfully:
1. Capture actual counts for all test regions
2. Store in `src/test/resources/test-baselines.properties`
3. Commit baseline file to version control

**Example baseline file:**
```properties
# Test Baselines - Generated: 2026-01-15
# Dataset: 1000 Genomes Phase 3 (GRCh38)

# Metadata baselines
total.samples=3202
total.male.samples=1608
total.female.samples=1594
total.variants=88234567

# Region baselines
brca1.total.variants=3456
brca1.high.impact=89
tp53.total.variants=1234
tp53.missense.variants=456
cftr.heterozygous.variants=567
hbb.pathogenic.count=23

# Sparse region (expected low/zero)
sparse.region.variants=0
```

#### 4.5.2 Baseline Comparison (Subsequent Runs)

Integration tests compare actual results against baseline:

| Deviation | Action | Test Result |
|-----------|--------|-------------|
| ≤ 5% | None | ✅ PASS |
| 5-10% | Log warning | ✅ PASS (with warning) |
| > 10% | Fail test | ❌ FAIL |

**Rationale:** Small fluctuations may occur due to data updates. Large changes indicate either:
- Significant dataset update (requires baseline refresh)
- Test regression or server issue

#### 4.5.3 Baseline Update Process

When server data is intentionally updated:

```bash
# 1. Run tests with baseline update flag
./mvnw verify -DskipIntegrationTests=false -DupdateBaseline=true

# 2. Review changes to baseline file
git diff src/test/resources/test-baselines.properties

# 3. Commit updated baselines
git add src/test/resources/test-baselines.properties
git commit -m "Update test baselines after dataset refresh"
```

#### 4.5.4 Implementation Notes

**TestBaselines.java utility class:**
```java
package org.dnaerys.testdata;

public class TestBaselines {
    private static final String BASELINE_FILE = "test-baselines.properties";
    private static final double WARN_THRESHOLD = 0.05;  // 5%
    private static final double FAIL_THRESHOLD = 0.10;  // 10%

    // Load baseline for key, compare with actual, return result
    public static BaselineResult compare(String key, long actual);

    // Save new baseline value
    public static void update(String key, long value);

    // Check if running in update mode
    // Usage: ./mvnw verify -DskipIntegrationTests=false -DupdateBaseline=true
    public static boolean isUpdateMode() {
        return Boolean.getBoolean("updateBaseline");
    }
}
```

**Baseline comparison result:**
```java
public enum BaselineResult {
    PASS,           // Within 5%
    WARN,           // 5-10% deviation, logged
    FAIL,           // >10% deviation
    NO_BASELINE     // First run, baseline captured
}
```

#### 4.5.5 Sample ID Configuration

> **All sample IDs are pre-configured** with trio HG00405/HG00403/HG00404.
> No additional configuration required for Phases 2-3.

**Pre-Configured Sample IDs:**

| Constant | Value | Purpose |
|----------|-------|---------|
| `SAMPLE_FEMALE` | HG00405 | Female sample (daughter) |
| `SAMPLE_MALE` | HG00403 | Male sample (parent 1) |
| `SAMPLE_GENERAL` | HG00404 | General queries (parent 2) |

**Pre-Configured Trio IDs (same family for all patterns):**

| Trio Type | Parent1 | Parent2 | Proband |
|-----------|---------|---------|---------|
| De Novo | HG00403 | HG00404 | HG00405 |
| Het Dominant | HG00403 | HG00404 | HG00405 |
| Hom Recessive | HG00403 | HG00404 | HG00405 |

**Pre-Configured Kinship Pairs:**

| Pair Type | Sample1 | Sample2 | Expected Degree |
|-----------|---------|---------|-----------------|
| Parent-Child | HG00403 | HG00405 | First-degree |
| Unrelated | HG00406 | HG00406 | Unrelated |

**NOTE:** Default sample IDs from trio HG00405/HG00403/HG00404 are pre-filled.
These can be used for all phases. Sample HG00406 is used for unrelated kinship tests.

---

## 5. Test Case Specifications

### 5.1 Entity Mapper Tests

#### 5.1.1 ImpactMapperTest

| Test Case ID | Description | Input | Expected Output |
|--------------|-------------|-------|-----------------|
| IMP-001 | Valid uppercase | "HIGH" | `Impact.HIGH` |
| IMP-002 | Valid lowercase | "high" | `Impact.HIGH` |
| IMP-003 | Valid mixed case | "High" | `Impact.HIGH` |
| IMP-004 | Whitespace trimming | "  HIGH  " | `Impact.HIGH` |
| IMP-005 | All valid values | "MODERATE", "LOW", "MODIFIER" | Respective enums |
| IMP-006 | Invalid value | "CRITICAL" | `Impact.UNRECOGNIZED` |
| IMP-007 | Empty string | "" | `Impact.UNRECOGNIZED` |
| IMP-008 | Null input | null | `Impact.UNRECOGNIZED` |
| IMP-009 | Dash normalization | "HIGH-IMPACT" | `Impact.UNRECOGNIZED` |
| IMP-010 | Space normalization | "HIGH IMPACT" | `Impact.UNRECOGNIZED` |

#### 5.1.2 BiotypeMapperTest

| Test Case ID | Description | Input | Expected Output |
|--------------|-------------|-------|-----------------|
| BIO-001 | Protein coding | "PROTEIN_CODING" | `BioType.PROTEIN_CODING` |
| BIO-002 | lncRNA lowercase | "lncrna" | `BioType.LNCRNA` |
| BIO-003 | Pseudogene | "PSEUDOGENE" | `BioType.PSEUDOGENE` |
| BIO-004 | With spaces | "protein coding" | `BioType.PROTEIN_CODING` |
| BIO-005 | With dashes | "protein-coding" | `BioType.PROTEIN_CODING` |
| BIO-006 | All 24 valid values | Each biotype | Respective enums |
| BIO-007 | Invalid biotype | "UNKNOWN_TYPE" | `BioType.UNRECOGNIZED` |

#### 5.1.3 AlphaMissenseMapperTest

| Test Case ID | Description | Input | Expected Output |
|--------------|-------------|-------|-----------------|
| AM-001 | Likely benign | "LIKELY_BENIGN" | `AlphaMissense.AM_LIKELY_BENIGN` |
| AM-002 | Likely pathogenic | "LIKELY_PATHOGENIC" | `AlphaMissense.AM_LIKELY_PATHOGENIC` |
| AM-003 | Ambiguous | "AMBIGUOUS" | `AlphaMissense.AM_AMBIGUOUS` |
| AM-004 | Lowercase | "likely_benign" | `AlphaMissense.AM_LIKELY_BENIGN` |
| AM-005 | Without prefix | "BENIGN" | `AlphaMissense.UNRECOGNIZED` |
| AM-006 | Invalid | "PATHOGENIC" | `AlphaMissense.UNRECOGNIZED` |

#### 5.1.4 ConsequencesMapperTest

| Test Case ID | Description | Input | Expected Output |
|--------------|-------------|-------|-----------------|
| CON-001 | Stop gained | "STOP_GAINED" | `Consequence.STOP_GAINED` |
| CON-002 | Frameshift | "FRAMESHIFT_VARIANT" | `Consequence.FRAMESHIFT_VARIANT` |
| CON-003 | Missense | "MISSENSE_VARIANT" | `Consequence.MISSENSE_VARIANT` |
| CON-004 | 3' UTR | "3_PRIME_UTR_VARIANT" | `Consequence.3_PRIME_UTR_VARIANT` |
| CON-005 | With dash | "stop-gained" | `Consequence.STOP_GAINED` |
| CON-006 | All 41 values | Each consequence | Respective enums |
| CON-007 | Invalid | "UNKNOWN_CONSEQUENCE" | `Consequence.UNRECOGNIZED` |

### 5.2 DnaerysClient Unit Tests

#### 5.2.1 Pagination Tests

| Test Case ID | Description | Setup | Assertion |
|--------------|-------------|-------|-----------|
| CLI-PAG-001 | Null limit normalization | `limit=null` | Request uses `limit=100` |
| CLI-PAG-002 | Negative limit normalization | `limit=-1` | Request uses `limit=100` |
| CLI-PAG-003 | Over-limit normalization | `limit=500` | Request uses `limit=100` |
| CLI-PAG-004 | Valid limit passthrough | `limit=50` | Request uses `limit=50` |
| CLI-PAG-005 | Null skip normalization | `skip=null` | Request uses `skip=0` |
| CLI-PAG-006 | Negative skip normalization | `skip=-10` | Request uses `skip=0` |
| CLI-PAG-007 | Valid skip passthrough | `skip=200` | Request uses `skip=200` |

#### 5.2.2 Annotation Building Tests

| Test Case ID | Description | Input Filters | Expected Annotation Fields |
|--------------|-------------|---------------|---------------------------|
| CLI-ANN-001 | AF less than | `afLessThan=0.01` | `annotations.afLt=0.01` |
| CLI-ANN-002 | AF greater than | `afGreaterThan=0.05` | `annotations.afGt=0.05` |
| CLI-ANN-003 | Impact filter | `impact="HIGH,MODERATE"` | `annotations.impact=[HIGH,MODERATE]` |
| CLI-ANN-004 | Combined filters | Multiple | All fields set correctly |
| CLI-ANN-005 | Empty filters | All null | Empty annotations object |
| CLI-ANN-006 | Invalid values filtered | `impact="HIGH,INVALID"` | Only `HIGH` in annotations |

#### 5.2.3 Error Handling Tests

| Test Case ID | Description | Simulated Error | Expected Return |
|--------------|-------------|-----------------|-----------------|
| CLI-ERR-001 | gRPC connection failure | `StatusRuntimeException` | `0L` for counts |
| CLI-ERR-002 | Timeout | Deadline exceeded | `List.of("{}")` for selects |
| CLI-ERR-003 | Invalid response | Malformed protobuf | Safe default |
| CLI-ERR-004 | Logging verification | Any error | Log at SEVERE level |

### 5.3 DnaerysClient Integration Tests

#### 5.3.1 Metadata Query Tests

| Test Case ID | Description | Method | Validation |
|--------------|-------------|--------|------------|
| CLI-INT-001 | Total sample count | `countSamplesTotal()` | Returns 3202 |
| CLI-INT-002 | Female sample count | `countFemaleSamplesTotal()` | Returns > 0, < 3202 |
| CLI-INT-003 | Male sample count | `countMaleSamplesTotal()` | Returns > 0, < 3202 |
| CLI-INT-004 | Sample counts sum | Female + Male | Equals total |
| CLI-INT-005 | All sample IDs | `samplesIds()` | Size = 3202 |
| CLI-INT-006 | Variant total | `variantsTotal()` | Returns > 80,000,000 |
| CLI-INT-007 | Nodes total | `nodesTotal()` | Returns > 0 |

#### 5.3.2 Region Query Tests

| Test Case ID | Description | Region | Filters | Validation |
|--------------|-------------|--------|---------|------------|
| CLI-INT-010 | BRCA1 variant count | BRCA1 | None | Count > 0 |
| CLI-INT-011 | BRCA1 variant select | BRCA1 | None | List size > 0, ≤ 100 |
| CLI-INT-012 | Sparse region | SPARSE | None | Count may be 0 |
| CLI-INT-013 | High impact only | BRCA1 | impact="HIGH" | Count < unfiltered |
| CLI-INT-014 | Pathogenic only | BRCA1 | clinSig="PATHOGENIC" | Count < unfiltered |
| CLI-INT-015 | Rare variants | BRCA1 | afLessThan=0.01 | Count < unfiltered |

#### 5.3.3 Pagination Integration Tests

| Test Case ID | Description | Request | Validation |
|--------------|-------------|---------|------------|
| CLI-INT-020 | Pagination limit enforcement | limit=500, dense region | Result size ≤ 100 |
| CLI-INT-021 | Skip functionality | skip=50, limit=50 | Different results than skip=0 |
| CLI-INT-022 | Multiple pages | skip=0, then skip=100 | No overlapping results |
| CLI-INT-023 | Beyond data range | skip=1000000 | Empty or minimal results |

#### 5.3.4 Sample-Specific Query Tests

| Test Case ID | Description | Sample ID | Validation |
|--------------|-------------|-----------|------------|
| CLI-INT-030 | Valid sample variant count | Valid ID | Count ≥ 0 |
| CLI-INT-031 | Valid sample variant select | Valid ID | JSON list returned |
| CLI-INT-032 | Invalid sample ID | "INVALID_SAMPLE" | Returns 0 or error handling |
| CLI-INT-033 | Homozygous in sample | Valid ID | Count ≤ total count |
| CLI-INT-034 | Heterozygous in sample | Valid ID | Count ≤ total count |

### 5.4 Inheritance Model Integration Tests

> **[DEPRECATED]** The following test sections (5.4.1 - 5.4.3) reference tools that have been removed from the implementation. These test cases are no longer applicable. See Section 5.4.5 for the new Homozygous Reference tests.

#### 5.4.1 De Novo Tests **[DEPRECATED]**

| Test Case ID | Description | Trio | Region | Validation |
|--------------|-------------|------|--------|------------|
| ~~INH-DN-001~~ | ~~Basic de novo query~~ | ~~Valid trio~~ | ~~BRCA1~~ | ~~Returns JSON list~~ |
| ~~INH-DN-002~~ | ~~De novo with filters~~ | ~~Valid trio~~ | ~~BRCA1, impact="HIGH"~~ | ~~Filtered results~~ |
| ~~INH-DN-003~~ | ~~De novo pagination~~ | ~~Valid trio~~ | ~~Dense region~~ | ~~Size ≤ 100~~ |
| ~~INH-DN-004~~ | ~~De novo empty region~~ | ~~Valid trio~~ | ~~SPARSE~~ | ~~May return empty~~ |
| ~~INH-DN-005~~ | ~~Invalid parent ID~~ | ~~Invalid, Valid, Valid~~ | ~~BRCA1~~ | ~~Error handling~~ |

#### 5.4.2 Heterozygous Dominant Tests **[DEPRECATED]**

| Test Case ID | Description | Trio | Validation |
|--------------|-------------|------|------------|
| ~~INH-HD-001~~ | ~~Basic het dominant~~ | ~~Affected parent, Unaffected parent, Proband~~ | ~~Returns JSON list~~ |
| ~~INH-HD-002~~ | ~~Het dominant with high impact~~ | ~~Same~~ | ~~Only high impact variants~~ |
| ~~INH-HD-003~~ | ~~Het dominant rare only~~ | ~~Same, afLessThan=0.01~~ | ~~Only rare variants~~ |

#### 5.4.3 Homozygous Recessive Tests **[DEPRECATED]**

| Test Case ID | Description | Trio | Validation |
|--------------|-------------|------|------------|
| ~~INH-HR-001~~ | ~~Basic hom recessive~~ | ~~Carrier1, Carrier2, Affected~~ | ~~Returns JSON list~~ |
| ~~INH-HR-002~~ | ~~Hom recessive with consequence filter~~ | ~~Same~~ | ~~Only specified consequences~~ |
| ~~INH-HR-003~~ | ~~Hom recessive pathogenic~~ | ~~Same, clinSig="PATHOGENIC"~~ | ~~Only pathogenic variants~~ |

#### 5.4.4 Kinship Tests

| Test Case ID | Description | Sample Pair | Validation |
|--------------|-------------|-------------|------------|
| INH-KIN-001 | Known related pair | Parent-Child | Returns degree string |
| INH-KIN-002 | Known unrelated pair | Unrelated | Returns appropriate degree |
| INH-KIN-003 | Same sample | Sample, Sample | Expected self-kinship |
| INH-KIN-004 | Invalid sample | Valid, Invalid | Error handling |

#### 5.4.5 Homozygous Reference Tests (NEW)

| Test Case ID | Description | Parameters | Validation |
|--------------|-------------|------------|------------|
| HOM-REF-001 | Count samples homozygous reference | chromosome, position | Returns count ≥ -1 |
| HOM-REF-002 | Select samples homozygous reference | chromosome, position | Returns sample ID list |
| HOM-REF-003 | Invalid chromosome | "99", valid position | Error handling |
| HOM-REF-004 | Invalid position | valid chromosome, -1 | Error handling |

### 5.5 OneKGPdMCPServer Tests

#### 5.5.1 Unit Tests (Mocked Client)

| Test Case ID | Description | Tool Method | Mock Verification |
|--------------|-------------|-------------|-------------------|
| MCP-001 | getDatasetInfo delegation | `getDatasetInfo()` | Calls `client.getDatasetInfo()`, returns wrapped DatasetInfo |
| MCP-002 | variantsInRegion delegation | `variantsInRegion(...)` | Calls `client.selectVariantsInRegion(...)` with correct params |
| MCP-003 | Parameter passthrough | `variantsInRegion(chr, start, end, ...)` | All params passed to client |
| MCP-004 | Optional params null | `variantsInRegion(chr, start, end, null, null, ...)` | Nulls passed correctly |
| MCP-005 | Homozygous reference tools | `countSamplesHomozygousReference(...)` | Calls client method, returns wrapped map |

#### 5.5.2 Integration Tests

| Test Case ID | Description | Tool Method | Validation |
|--------------|-------------|-------------|------------|
| MCP-INT-001 | Full tool execution | `getDatasetInfo()` | Returns DatasetInfo with 3202 samples |
| MCP-INT-002 | Region query tool | `variantsInRegion(...)` | Returns valid JSON list |
| ~~MCP-INT-003~~ | ~~Inheritance tool~~ **[DEPRECATED]** | ~~`deNovoInTrio(...)`~~ | ~~Returns valid JSON list~~ |
| MCP-INT-004 | Kinship tool | `kinship(...)` | Returns degree string |
| MCP-INT-005 | Homozygous reference (NEW) | `countSamplesHomozygousReference(...)` | Returns count map |

---

## 6. Dependencies and Configuration

### 6.1 Maven Dependencies to Add

```xml
<!-- Add to pom.xml <dependencies> section -->

<!-- Mockito for unit testing -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5-mockito</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ for fluent assertions (optional but recommended) -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### 6.2 Maven Plugin Configuration

> **⚠️ CRITICAL:** This configuration ensures integration tests are **skipped by default**.
> Phase 1 can be implemented and tested without any server access.

```xml
<!-- Add to pom.xml <properties> section -->
<properties>
    <!-- Integration tests are SKIPPED by default -->
    <!-- Enable with: -DskipIntegrationTests=false -->
    <skipIntegrationTests>true</skipIntegrationTests>
</properties>
```

```xml
<!-- Surefire for unit tests (*Test.java) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <!-- Exclude ALL integration test patterns from unit test phase -->
        <excludes>
            <exclude>**/*IT.java</exclude>
            <exclude>**/*IntegrationTest.java</exclude>
        </excludes>
    </configuration>
</plugin>

<!-- Failsafe for integration tests (*IT.java) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*IT.java</include>
            <include>**/*IntegrationTest.java</include>
        </includes>
        <!-- Skip integration tests unless explicitly enabled -->
        <skipITs>${skipIntegrationTests}</skipITs>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Test Execution Matrix:**

| Command | Unit Tests | Integration Tests | Server Required |
|---------|------------|-------------------|-----------------|
| `./mvnw test` | ✅ Run | ❌ Skip | No |
| `./mvnw verify` | ✅ Run | ❌ Skip | No |
| `./mvnw verify -DskipIntegrationTests=false` | ✅ Run | ✅ Run | **Yes** |
| `./mvnw test -DskipTests` | ❌ Skip | ❌ Skip | No |

### 6.3 Test Application Properties

```properties
# src/test/resources/application.properties

# Logging
%test.quarkus.log.level=INFO
%test.quarkus.log.category."org.dnaerys".level=DEBUG

# gRPC server for integration tests
%test.dnaerys.host=db.dnaerys.org
%test.dnaerys.port=443
%test.dnaerys.tls=true

# Disable SSE for tests (use synchronous calls)
%test.quarkus.mcp.sse.enabled=false
```

### 6.4 TestConstants.java

```java
package org.dnaerys.testdata;

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
```

---

## 7. Implementation Phases

### Phase 1: Test Infrastructure + Entity Mapper Tests

> **🟢 SERVER REQUIREMENT: NONE**
> All Phase 1 tests run completely offline. No network access required.

**Duration:** First implementation session
**Dependencies:** None

**Deliverables:**
1. Maven dependency updates (pom.xml)
2. Test application properties
3. TestConstants.java with pre-configured sample IDs (HG00405/HG00403/HG00404)
4. 9 entity mapper test classes
5. Basic test execution verification

**Files to Create:**
- `src/test/resources/application.properties`
- `src/test/java/org/dnaerys/testdata/TestConstants.java`
- `src/test/java/org/dnaerys/client/entity/ImpactMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/BiotypeMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/FeatureTypeMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/VariantTypeMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/ConsequencesMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/ClinSigMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/AlphaMissenseMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/SIFTMapperTest.java`
- `src/test/java/org/dnaerys/client/entity/PolyPhenMapperTest.java`

**Files to Modify:**
- `pom.xml` (add dependencies, add skipIntegrationTests property)

**Execution Commands:**
```bash
# Run all Phase 1 tests (no server needed)
./mvnw clean test

# Run specific mapper test
./mvnw test -Dtest=ImpactMapperTest

# Run all mapper tests
./mvnw test -Dtest="*MapperTest"

# Verify no integration tests accidentally run
./mvnw test 2>&1 | grep -i "IT.java"  # Should return nothing
```

**Success Criteria:**
- [ ] `./mvnw test` runs without network access
- [ ] All 9 mapper test classes pass
- [ ] Test execution < 10 seconds
- [ ] No gRPC connection attempts in logs
- [ ] No `*IT.java` tests executed (verify with grep)

### Phase 2: DnaerysClient Integration Tests (Core Queries)

> **🔴 SERVER REQUIREMENT: REQUIRED**
> gRPC server must be accessible at `db.dnaerys.org:443`

**Duration:** Second implementation session
**Dependencies:** Phase 1 complete, network access to test server

**Deliverables:**
1. DnaerysClientIT.java with 5 core query test methods
2. Pagination enforcement tests
3. Test baselines captured for data change detection
4. Sample query tests using pre-configured IDs (HG00405/HG00403/HG00404)

**Test Methods:**
1. `testMetadataQueries()` - Sample counts, variant totals
2. `testRegionQuery()` - BRCA1 region variant count and select
3. `testPaginationEnforcement()` - Verify 50 item limit
4. `testFilterCombinations()` - AF + impact filtering
5. `testSampleQuery()` - Per-sample variant queries

**Files to Create:**
- `src/test/java/org/dnaerys/client/DnaerysClientIT.java`
- `src/test/resources/test-baselines.properties` (auto-generated on first run)

**Pre-Flight Check (before running tests):**
```bash
# Verify server connectivity
curl -v --connect-timeout 5 https://db.dnaerys.org:443

# Alternative: Use grpcurl if available
grpcurl -plaintext db.dnaerys.org:443 list
```

**Execution Commands:**
```bash
# Run integration tests (requires server)
./mvnw verify -DskipIntegrationTests=false

# Run specific integration test class
./mvnw verify -DskipIntegrationTests=false -Dit.test=DnaerysClientIT

# Run specific integration test method
./mvnw verify -DskipIntegrationTests=false -Dit.test=DnaerysClientIT#testMetadataQueries

# Run full suite (unit + integration)
./mvnw clean verify -DskipIntegrationTests=false
```

**Success Criteria:**
- [ ] Pre-flight connectivity check passes
- [ ] `./mvnw verify -DskipIntegrationTests=false` executes integration tests
- [ ] All 5 integration test methods pass
- [ ] Confirms connectivity to db.dnaerys.org:443
- [ ] Test baselines captured in properties file
- [ ] Tests complete within 2 minute timeout

### Phase 3: MCP Tool Integration Tests

> **🔴 SERVER REQUIREMENT: REQUIRED**
> gRPC server must be accessible at `db.dnaerys.org:443`

**Duration:** Third implementation session
**Dependencies:** Phase 2 complete, network access to test server

**Deliverables:**
1. OneKGPdMCPServerIT.java with key tool tests
2. ~~Inheritance model tests using pre-configured trio (HG00403/HG00404/HG00405)~~ **[DEPRECATED]**
3. Kinship tests using pre-configured pairs (HG00403↔HG00405, HG00406 unrelated)
4. JSON response structure validation
5. Homozygous reference tests (count/select samples with 0/0 genotype) **(NEW)**

**Test Methods:**
1. `testMetadataTools()` - getDatasetInfo
2. `testVariantQueryTools()` - variantsInRegion
3. ~~`testDeNovoInTrio()`~~ - **[DEPRECATED]** De novo inheritance (removed)
4. ~~`testHetDominantInTrio()`~~ - **[DEPRECATED]** Het dominant inheritance (removed)
5. ~~`testHomRecessiveInTrio()`~~ - **[DEPRECATED]** Hom recessive inheritance (removed)
6. `testKinship()` - Kinship calculation
7. `testHomozygousReference()` - **(NEW)** Count/select samples with 0/0 genotype

**Files to Create:**
- `src/test/java/org/dnaerys/mcp/OneKGPdMCPServerIT.java`

**Pre-Flight Check:**
```bash
# Verify server connectivity
curl -v --connect-timeout 5 https://db.dnaerys.org:443
```

**Execution Commands:**
```bash
# Run MCP integration tests
./mvnw verify -DskipIntegrationTests=false -Dit.test=OneKGPdMCPServerIT

# Run all integration tests (Phase 2 + Phase 3)
./mvnw verify -DskipIntegrationTests=false
```

**Success Criteria:**
- [x] ~~All inheritance model tools tested with pre-configured trio~~ **[DEPRECATED]** - Inheritance tools removed
- [ ] JSON responses validated for structure
- [ ] Kinship returns expected format for parent-child and unrelated pairs
- [ ] Homozygous reference tools tested with valid/invalid inputs

### Phase 4: Unit Tests + Comprehensive Coverage

> **🟢 SERVER REQUIREMENT: NONE**
> All Phase 4 unit tests run completely offline using mocks.

**Duration:** Fourth implementation session
**Dependencies:** Phase 3 complete

**Deliverables:**
1. DnaerysClientTest.java (mocked unit tests)
2. OneKGPdMCPServerTest.java (mocked unit tests)
3. Edge case and error handling tests
4. Coverage report generation

**Files to Create:**
- `src/test/java/org/dnaerys/client/DnaerysClientTest.java`
- `src/test/java/org/dnaerys/mcp/OneKGPdMCPServerTest.java`

**Execution Commands:**
```bash
# Run all unit tests (no server needed)
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# Run full suite with coverage (requires server for integration tests)
./mvnw clean verify -DskipIntegrationTests=false jacoco:report
```

**Success Criteria:**
- [ ] Unit tests run without network (Phase 1 + Phase 4 combined)
- [ ] Error handling paths covered
- [ ] Mockito verification of delegation patterns
- [ ] Overall coverage targets met
- [ ] No gRPC connection attempts in unit test logs

---

## 8. Success Criteria

### 8.1 Phase-Level Success

| Phase | Criterion | Measurement | Server? |
|-------|-----------|-------------|---------|
| Phase 1 | All mapper tests pass | `./mvnw test` exits 0 | 🟢 No |
| Phase 1 | No network required | Tests pass with network disabled | 🟢 No |
| Phase 1 | No integration tests triggered | Log contains no `*IT.java` execution | 🟢 No |
| Phase 1 | Fast execution | Test run < 10 seconds | 🟢 No |
| Phase 2 | Pre-flight connectivity passes | `curl https://db.dnaerys.org:443` succeeds | 🔴 Yes |
| Phase 2 | Integration tests connect | Tests communicate with gRPC server | 🔴 Yes |
| Phase 2 | Pagination verified | MAX_RETURNED_ITEMS enforced | 🔴 Yes |
| Phase 2 | Baselines captured | `test-baselines.properties` created | 🔴 Yes |
| Phase 3 | ~~Inheritance tools tested~~ | ~~All 3 patterns validated~~ **[DEPRECATED]** | ~~🔴 Yes~~ |
| Phase 3 | Homozygous reference tested | Count/select methods validated | 🔴 Yes |
| Phase 3 | Trio IDs validated | Kinship confirms relationships | 🔴 Yes |
| Phase 4 | Unit tests pass offline | `./mvnw test` passes without network | 🟢 No |
| Phase 4 | Coverage targets met | Reports show required percentages | 🟢 No |

**Offline Validation Command:**
```bash
# Verify Phase 1 + Phase 4 work completely offline
# 1. Disable network (e.g., airplane mode, disconnect)
# 2. Run: ./mvnw clean test
# 3. Verify: All tests pass, no connection errors in logs
```

### 8.2 Overall Success

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Unit test execution | < 10s | `./mvnw test` timing |
| Integration test execution | < 2min | `./mvnw verify` timing |
| Entity mapper coverage | 100% | JaCoCo report |
| DnaerysClient coverage | 80% | JaCoCo report |
| MCP tool coverage | 70% | JaCoCo report |
| Test stability | 100% pass rate | CI pipeline history |

### 8.3 Quality Gates

Before considering testing complete:
- [ ] No flaky tests (tests that sometimes pass/fail)
- [ ] All tests have descriptive names
- [ ] Test data is documented
- [ ] Integration tests can run in CI environment
- [ ] README updated with test execution instructions

---

## 9. Risk Assessment

### 9.1 Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| gRPC server unavailable | Medium | High | Retry logic, skip integration tests in CI |
| Test data changes | Low | Medium | Use stable regions, document expected ranges |
| Proto schema changes | Low | High | Pin proto version, regenerate before testing |
| Quarkus/Mockito incompatibility | Low | Medium | Use quarkus-junit5-mockito extension |
| GrpcChannel singleton mocking | Medium | Medium | Use constructor injection or spy |

### 9.2 Data Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Pre-configured sample IDs invalid | Low | High | IDs verified from 1000 Genomes pedigree data |
| Trio family not actually related | Low | High | Kinship test validates parent-child relationship |
| Region coordinates outdated | Low | Medium | Use well-documented gene regions |

### 9.3 Schedule Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Scope creep | Medium | Medium | Strict phase boundaries |
| Server unavailable during Phase 2-3 | Medium | Medium | Skip integration tests, proceed with Phase 4 |
| Complex inheritance logic | Medium | Medium | Start with simple cases |

---

## 10. Assumptions and Constraints

### 10.1 Assumptions

1. **Server Availability:** The gRPC server at db.dnaerys.org:443 is available for testing
2. **Data Stability:** The 1000 Genomes dataset (3202 samples) remains unchanged
3. **Sample IDs Valid:** Pre-configured trio HG00405/HG00403/HG00404 exists in dataset
4. **Build Environment:** Java 21 and Maven are correctly configured
5. **Network Access:** Integration test environment has outbound network access
6. **Proto Compatibility:** Current proto definitions match server expectations

### 10.2 Constraints

1. **No Server-Side Mocking:** Cannot modify the gRPC server behavior
2. **Real Data Only:** Integration tests must use real genomic data
3. **MAX_RETURNED_ITEMS:** Cannot test retrieving more than 50 items per call
4. **TLS Required:** Production server requires encrypted connections
5. **Read-Only Access:** Tests cannot modify server-side data

### 10.3 Out of Scope

1. Performance/load testing
2. Security testing
3. gRPC server implementation testing
4. Proto file modifications
5. UI testing
6. Database administration

---

## 11. Appendices

### Appendix A: Mapper Valid Values Reference

#### A.1 Impact Values
```
HIGH, MODERATE, LOW, MODIFIER
```

#### A.2 BioType Values (24)
```
PROTEIN_CODING, LNCRNA, PROCESSED_PSEUDOGENE, UNPROCESSED_PSEUDOGENE,
MISC_RNA, SNRNA, MIRNA, SNORNA, RRNA, MT_TRNA, MT_RRNA, SCARNA,
RIBOZYME, SRNA, SCRNA, VAULTRNA, TRANSCRIBED_UNPROCESSED_PSEUDOGENE,
TRANSCRIBED_PROCESSED_PSEUDOGENE, TRANSLATED_PROCESSED_PSEUDOGENE,
IG_C_GENE, IG_D_GENE, IG_J_GENE, IG_V_GENE, TR_C_GENE
```

#### A.3 Consequence Values (41)
```
TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT,
STOP_GAINED, FRAMESHIFT_VARIANT, STOP_LOST, START_LOST,
TRANSCRIPT_AMPLIFICATION, FEATURE_ELONGATION, FEATURE_TRUNCATION,
INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT,
PROTEIN_ALTERING_VARIANT, SPLICE_DONOR_5TH_BASE_VARIANT,
SPLICE_REGION_VARIANT, SPLICE_DONOR_REGION_VARIANT,
SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT,
START_RETAINED_VARIANT, STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT,
CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, 5_PRIME_UTR_VARIANT,
3_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT,
INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, NON_CODING_TRANSCRIPT_VARIANT,
CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT,
TFBS_ABLATION, TFBS_AMPLIFICATION, TF_BINDING_SITE_VARIANT,
REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION,
REGULATORY_REGION_VARIANT, INTERGENIC_VARIANT, SEQUENCE_VARIANT
```

#### A.4 ClinSignificance Values (14)
```
BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC,
PATHOGENIC, DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE,
AFFECTS, CONFLICTING_CLASSIFICATIONS, OTHER, NOT_PROVIDED,
BENIGN_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE
```

### Appendix B: JSON Response Structure

#### B.1 Variant JSON (from selectVariantsInRegion)
```json
{
  "chromosome": "17",
  "position": 43044295,
  "ref": "A",
  "alt": "G",
  "af": 0.001,
  "gnomadGenomesAf": 0.0012,
  "gnomadExomesAf": 0.0015,
  "vep": {
    "impact": "HIGH",
    "consequence": "STOP_GAINED",
    "biotype": "PROTEIN_CODING",
    "gene": "BRCA1",
    "hgvsp": "p.Arg1234*"
  },
  "clinvar": {
    "significance": "PATHOGENIC",
    "reviewStatus": "REVIEWED_BY_EXPERT_PANEL"
  },
  "alphaMissense": {
    "class": "LIKELY_PATHOGENIC",
    "score": 0.95
  }
}
```

### Appendix C: Command Reference

#### C.1 Phase 1: Unit Tests (No Server Required)

```bash
# ========================================
# PHASE 1: UNIT TESTS (🟢 NO SERVER)
# ========================================

# Compile project
./mvnw compile

# Run all unit tests (mapper tests, mocked client tests)
./mvnw test

# Run specific unit test class
./mvnw test -Dtest=ImpactMapperTest

# Run specific test method
./mvnw test -Dtest=ImpactMapperTest#testValidImpact

# Run all mapper tests
./mvnw test -Dtest="*MapperTest"

# Verify no integration tests accidentally run
./mvnw test 2>&1 | grep -i "IT.java"  # Should return nothing

# Run with verbose output
./mvnw test -X
```

#### C.2 Phase 2+: Integration Tests (Server Required)

```bash
# ========================================
# PHASE 2+: INTEGRATION TESTS (🔴 SERVER REQUIRED)
# ========================================

# Pre-flight: Verify server is reachable
curl -v --connect-timeout 5 https://db.dnaerys.org:443

# Run integration tests only (skip unit tests)
./mvnw verify -DskipIntegrationTests=false -DskipTests

# Run full suite: unit + integration
./mvnw clean verify -DskipIntegrationTests=false

# Run specific integration test class
./mvnw verify -DskipIntegrationTests=false -Dit.test=DnaerysClientIT

# Run specific integration test method
./mvnw verify -DskipIntegrationTests=false -Dit.test=DnaerysClientIT#testMetadataQueries

# Run all client integration tests
./mvnw verify -DskipIntegrationTests=false -Dit.test="*ClientIT"

# Run MCP server integration tests
./mvnw verify -DskipIntegrationTests=false -Dit.test="*ServerIT"
```

#### C.3 CI/CD Commands

```bash
# ========================================
# CI/CD USAGE
# ========================================

# CI without server access (Phase 1 validation only)
./mvnw clean test

# CI with server access (full validation)
./mvnw clean verify -DskipIntegrationTests=false

# Generate coverage report (unit tests only)
./mvnw test jacoco:report

# Generate coverage report (full suite, requires server)
./mvnw clean verify -DskipIntegrationTests=false jacoco:report

# Quick smoke test (compile + unit tests)
./mvnw clean test -DskipTests=false -q
```

#### C.4 Development Commands

```bash
# ========================================
# DEVELOPMENT
# ========================================

# Run in dev mode (for manual testing)
./mvnw quarkus:dev

# Build uber-jar
./mvnw package -DskipTests -Dquarkus.package.jar.type=uber-jar

# Run uber-jar locally
java -Dquarkus.profile=dev -jar target/onekgpd-mcp-runner.jar
```

#### C.5 Baseline Management

```bash
# ========================================
# BASELINE MANAGEMENT (Phase 2+)
# ========================================

# Capture/update test baselines (after known data changes)
./mvnw verify -DskipIntegrationTests=false -DupdateBaseline=true

# View current baselines
cat src/test/resources/test-baselines.properties
```

---

**Document Status:** v1.3 - Ready for Implementation

**Key Changes in v1.3:**
- ⚠️ **DEPRECATED**: Inheritance model tools (`deNovoInTrio`, `hetDominantInTrio`, `homRecessiveInTrio`) removed
- ⚠️ **DEPRECATED**: Test cases INH-DN-001 to INH-DN-005, INH-HD-001 to INH-HD-003, INH-HR-001 to INH-HR-003
- ⚠️ **DEPRECATED**: MCP-INT-003 inheritance tool integration test
- ✅ **NEW**: `getDatasetInfo()` replaces `getSampleCounts()` and `variantsTotal()`
- ✅ **NEW**: `countSamplesHomozygousReference()` and `selectSamplesHomozygousReference()` tools added
- ✅ **NEW**: Test cases HOM-REF-001 to HOM-REF-004 for homozygous reference
- ✅ **NEW**: MCP-INT-005 homozygous reference integration test
- ✅ Updated DnaerysClient public methods table
- ✅ Updated tool categories table

**Key Changes in v1.2:**
- ✅ Pre-filled all sample IDs with trio HG00405/HG00403/HG00404
- ✅ Removed all sample discovery mechanisms and language
- ✅ Added `isUpdateMode()` implementation detail (Section 4.5.4)
- ✅ Specification now ready to implement immediately (no user input required)
- ✅ Updated Phase 2 and Phase 3 to use pre-configured sample IDs
- ✅ Removed all "ACTION REQUIRED" and "TBD" placeholders

**Key Changes in v1.1:**
- ✅ Maven `skipIntegrationTests` property added (Section 6.2)
- ✅ Server requirements documented per phase (Section 7)
- ✅ Test separation by naming convention (Section 3.4)
- ✅ Baseline management strategy (Section 4.5)
- ✅ Command reference updated with phased commands (Appendix C)
- ✅ Success criteria includes offline validation (Section 8.1)

**Pre-Configured Test Data:**

| Constant | Value | Role |
|----------|-------|------|
| `SAMPLE_FEMALE` | HG00405 | Daughter/Proband |
| `SAMPLE_MALE` | HG00403 | Parent 1 |
| `SAMPLE_GENERAL` | HG00404 | Parent 2 |
| `KINSHIP_UNRELATED1` | HG00406 | Unrelated sample |

**Implementation Ready:**
All phases can proceed without additional user input:
```bash
# Phase 1: No server needed
./mvnw clean test

# Phases 2-3: Server required
./mvnw verify -DskipIntegrationTests=false
```
