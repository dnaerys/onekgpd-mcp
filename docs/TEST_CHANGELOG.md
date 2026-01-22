# Changelog

All notable changes to this project made by Claude will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added - 2026-01-22
#### Unit Tests with Mocking (Phase 4)
- DnaerysClient unit tests (`DnaerysClientTest.java`) - 61 test cases
  - `AnnotationBuildingTests` (11 tests) - Filter annotation composition for AF, impact, consequence, clinSig
  - `InputValidationTests` (15 tests) - Region validation, frequency bounds, allele format
  - `InheritanceModelInputValidationTests` (14 tests) - Sample ID validation for de novo, het dominant, hom recessive
  - `KinshipInputValidationTests` (6 tests) - Sample pair validation for kinship queries
  - `GrpcErrorHandlingTests` (4 tests) - gRPC NOT_FOUND/UNAVAILABLE error mapping
  - `PaginationLogicTests` (11 tests) - MAX_RETURNED_ITEMS (50) enforcement

- OneKGPdMCPServer unit tests (`OneKGPdMCPServerTest.java`) - 31 test cases
  - `MetadataToolsTests` (5 tests) - getSampleCounts, getVariantsTotal, getSampleIds
  - `VariantCountToolsTests` (4 tests) - countVariantsInRegion with filters
  - `VariantSelectToolsTests` (2 tests) - selectVariantsInRegion response mapping
  - `SampleToolsTests` (4 tests) - Sample-specific variant queries
  - `ParameterPassthroughTests` (3 tests) - Filter parameters passed to client correctly
  - `InheritanceModelToolsTests` (5 tests) - De novo, het dominant, hom recessive delegation
  - `KinshipToolsTests` (5 tests) - Kinship degree calculation and KinshipResult format
  - `ErrorHandlingTests` (3 tests) - gRPC exceptions propagated to MCP layer

### Technical Details - 2026-01-22 (Phase 4)
- Unit tests use Mockito mockStatic for GrpcChannel singleton mocking
- No network access required - all gRPC calls mocked
- Test execution time: ~1.6 seconds for 92 unit tests
- Total unit tests: 407 (314 mapper + 61 DnaerysClientTest + 31 OneKGPdMCPServerTest + 1 existing)
- Total tests: 419 (407 unit + 12 integration)
- Zero failures, zero errors
- All 4 phases complete

---

### Added - 2026-01-22
#### MCP Tool Integration Tests (Phase 3)
- OneKGPdMCPServer integration tests (`OneKGPdMCPServerIT.java`) - 7 test methods
  - `testMetadataTools()` - getSampleCounts validation (3202 samples) (MCP-INT-001)
  - `testVariantQueryTools()` - getSampleIds, getFemaleSamplesIds, getMaleSamplesIds (MCP-INT-002)
  - `testDeNovoInTrio()` - De novo inheritance in CFTR region (INH-DN-001 to INH-DN-004)
  - `testHetDominantInTrio()` - Heterozygous dominant inheritance (INH-HD-001 to INH-HD-003)
  - `testHomRecessiveInTrio()` - Homozygous recessive inheritance (INH-HR-001 to INH-HR-003)
  - `testKinship()` - Kinship degree calculation (INH-KIN-001 to INH-KIN-004)
  - `testJsonResponseStructure()` - VariantView JSON structure validation

- Kinship validation results:
  - Parent-child (HG00403↔HG00405): FIRST_DEGREE
  - Unrelated (HG00406↔HG00403): UNRELATED
  - Parent pair (HG00403↔HG00404): UNRELATED

### Technical Details - 2026-01-22 (Phase 3)
- Integration tests run against live db.dnaerys.org:80 (plain gRPC)
- Test execution time: ~25 seconds for 7 MCP integration tests
- Total integration tests: 12 (5 DnaerysClientIT + 7 OneKGPdMCPServerIT)
- Total tests: 327 (315 unit + 12 integration)
- Zero failures, zero errors
- Full suite execution time: ~63 seconds

---

### Added - 2026-01-22
#### Integration Tests (Phase 2)
- DnaerysClient integration tests (`DnaerysClientIT.java`) - 5 test methods
  - `testMetadataQueries()` - Sample counts (3202), variant totals (138M+) (CLI-INT-001 to CLI-INT-007)
  - `testRegionQuery()` - BRCA1 region queries with filters (CLI-INT-010 to CLI-INT-015)
  - `testPaginationEnforcement()` - MAX_RETURNED_ITEMS (50) limit validation (CLI-INT-020 to CLI-INT-023)
  - `testFilterCombinations()` - AF + impact + consequence combined filtering
  - `testSampleQuery()` - Per-sample variant queries using HG00405 (CLI-INT-030 to CLI-INT-034)

- Test baseline management system
  - `TestBaselines.java` - Utility for baseline comparison with configurable thresholds (0.5% warn, 1% fail)
  - `test-baselines.properties` - Auto-generated baselines from first test run:
    - `total.samples=3202`, `total.variants=138044723`
    - `brca1.total.variants=5573`, `brca1.high.impact=8`, `brca1.pathogenic=3`
    - `tp53.total.variants=1206`, `tp53.missense.variants=26`
    - `sample.hg00405.brca1.count=30`
  - Baseline update mode: `./mvnw verify -DskipIntegrationTests=false -DupdateBaseline=true`

### Technical Details - 2026-01-22
- Integration tests run against live db.dnaerys.org:80 (plain gRPC)
- Test execution time: ~1.6 seconds for all 5 integration tests
- Total tests: 320 (315 unit + 5 integration)
- Zero failures, zero errors
- Pre-flight connectivity check validated

---

### Added - 2026-01-15
#### Test Infrastructure (Phase 1)
- Entity mapper unit tests for all 9 mapper classes (314 test cases total)
  - `ImpactMapperTest` - 24 test cases covering normalization, case handling, and edge cases (IMP-001 to IMP-010)
  - `BiotypeMapperTest` - 41 test cases for 47 biotype values including RNA types, IG/TR genes (BIO-001 to BIO-007)
  - `AlphaMissenseMapperTest` - 27 test cases with AM_ prefix handling (AM-001 to AM-006)
  - `ConsequencesMapperTest` - 57 test cases for 41 consequence types (CON-001 to CON-007)
  - `ClinSigMapperTest` - 37 test cases for 19 clinical significance values
  - `FeatureTypeMapperTest` - 24 test cases for 3 feature types (TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE)
  - `VariantTypeMapperTest` - 52 test cases for 34 variant types including SVs
  - `SIFTMapperTest` - 23 test cases for 2 SIFT predictions (TOLERATED, DELETERIOUS)
  - `PolyPhenMapperTest` - 29 test cases for 4 PolyPhen predictions

- Test infrastructure setup
  - `TestConstants.java` - Pre-configured test data with:
    - Dataset constants (3202 samples, 80M+ variants, MAX_RETURNED_ITEMS=100)
    - Genomic regions (BRCA1, TP53, CFTR, HBB, sparse, dense)
    - Sample IDs (SAMPLE_FEMALE=HG00405, SAMPLE_MALE=HG00403, SAMPLE_GENERAL=HG00404)
    - Trio configurations for De Novo, Het Dominant, Hom Recessive inheritance
    - Kinship pairs for parent-child and unrelated tests
  - `application.properties` (test resources) - Test configuration with logging, gRPC settings

- Maven build configuration
  - `skipIntegrationTests` property (default: true) for test separation
  - Surefire plugin configuration (excludes *IT.java, *IntegrationTest.java)
  - Failsafe plugin configuration (includes *IT.java, *IntegrationTest.java, skipITs=${skipIntegrationTests})
  - Test dependencies: quarkus-junit5-mockito, assertj-core 3.24.2

### Changed - 2026-01-15
- `pom.xml` - Added test dependencies and plugin configurations for two-tier test architecture

### Technical Details
- All Phase 1 tests run offline (no network required)
- Test execution time: ~1.4 seconds (mapper tests only), ~5 seconds (full suite with Quarkus)
- Total tests: 315 (314 mapper tests + 1 existing GreetingResourceTest)
- Zero failures, zero errors
- Follows specification TEST_SPECIFICATION.md v1.2
- Integration tests properly skipped by default (`./mvnw verify` shows "Tests are skipped")
