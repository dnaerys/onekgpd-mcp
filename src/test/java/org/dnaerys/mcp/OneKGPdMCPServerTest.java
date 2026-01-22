package org.dnaerys.mcp;

import org.dnaerys.client.DnaerysClient;
import org.dnaerys.client.GrpcChannel;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.mcp.generator.VariantView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OneKGPdMCPServer.
 * Tests delegation patterns, response wrapping, and parameter passthrough.
 *
 * Test Case IDs: MCP-001 through MCP-004
 *
 * @see org.dnaerys.mcp.OneKGPdMCPServer
 */
@DisplayName("OneKGPdMCPServer Unit Tests")
@ExtendWith(MockitoExtension.class)
class OneKGPdMCPServerTest {

    private OneKGPdMCPServer server;

    @Mock
    private GrpcChannel mockGrpcChannel;

    @Mock
    private DnaerysServiceGrpc.DnaerysServiceBlockingStub mockBlockingStub;

    @BeforeEach
    void setUp() {
        server = new OneKGPdMCPServer();
    }

    // ========================================
    // METADATA TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Metadata Tools Tests")
    class MetadataToolsTests {

        @Test
        @DisplayName("MCP-001: getSampleCounts returns properly wrapped SampleCounts")
        void testGetSampleCountsReturnsWrappedResult() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock the datasetInfo response
                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .setSamplesTotal(3202)
                    .setMalesTotal(1598)
                    .setFemalesTotal(1604)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                DnaerysClient.SampleCounts result = server.getSampleCounts();

                assertThat(result).isNotNull();
                assertThat(result.total()).isEqualTo(3202);
                assertThat(result.male()).isEqualTo(1598);
                assertThat(result.female()).isEqualTo(1604);
            }
        }

        @Test
        @DisplayName("getVariantsTotal returns Map with 'count' key")
        void testGetVariantsTotalReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .setVariantsTotal(138044723)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                Map<String, Long> result = server.getVariantsTotal();

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(138044723L);
            }
        }

        @Test
        @DisplayName("getSampleIds returns Map with 'samples' key")
        void testGetSampleIdsReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Cohort cohort = Cohort.newBuilder()
                    .addFemaleSamplesNames("HG00405")
                    .addMaleSamplesNames("HG00403")
                    .build();
                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                Map<String, List<String>> result = server.getSampleIds();

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples")).contains("HG00405", "HG00403");
            }
        }

        @Test
        @DisplayName("getFemaleSamplesIds returns only female samples")
        void testGetFemaleSamplesIdsReturnsOnlyFemales() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Cohort cohort = Cohort.newBuilder()
                    .addFemaleSamplesNames("HG00405")
                    .addFemaleSamplesNames("NA12878")
                    .addMaleSamplesNames("HG00403")
                    .build();
                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                Map<String, List<String>> result = server.getFemaleSamplesIds();

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples"))
                    .contains("HG00405", "NA12878")
                    .doesNotContain("HG00403");
            }
        }

        @Test
        @DisplayName("getMaleSamplesIds returns only male samples")
        void testGetMaleSamplesIdsReturnsOnlyMales() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Cohort cohort = Cohort.newBuilder()
                    .addFemaleSamplesNames("HG00405")
                    .addMaleSamplesNames("HG00403")
                    .addMaleSamplesNames("NA12877")
                    .build();
                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                Map<String, List<String>> result = server.getMaleSamplesIds();

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples"))
                    .contains("HG00403", "NA12877")
                    .doesNotContain("HG00405");
            }
        }
    }

    // ========================================
    // VARIANT COUNT TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Variant Count Tools Tests")
    class VariantCountToolsTests {

        @Test
        @DisplayName("countVariantsInRegion returns Map with 'count' key")
        void testCountVariantsInRegionReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(5573L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                Map<String, Long> result = server.countVariantsInRegion(
                    "17", 43044295, 43170245,  // BRCA1
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(5573L);
            }
        }

        @Test
        @DisplayName("countHomozygousVariantsInRegion passes selectHom=true, selectHet=false")
        void testCountHomozygousVariantsPassesCorrectFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                server.countHomozygousVariantsInRegion(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                // Verify the request was made with hom=true, het=false
                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    request.getHom() && !request.getHet()
                ));
            }
        }

        @Test
        @DisplayName("countHeterozygousVariantsInRegion passes selectHom=false, selectHet=true")
        void testCountHeterozygousVariantsPassesCorrectFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                server.countHeterozygousVariantsInRegion(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                // Verify the request was made with hom=false, het=true
                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    !request.getHom() && request.getHet()
                ));
            }
        }

        @Test
        @DisplayName("countVariantsInRegion returns 0 for invalid chromosome")
        void testCountVariantsInRegionInvalidChromosome() {
            // Invalid chromosome should return 0 without making gRPC call
            // (validation happens before gRPC call)
            Map<String, Long> result = server.countVariantsInRegion(
                "99", 1000, 2000,  // Invalid chromosome
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            assertThat(result).containsKey("count");
            assertThat(result.get("count")).isZero();
        }
    }

    // ========================================
    // VARIANT SELECT TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Variant Select Tools Tests")
    class VariantSelectToolsTests {

        @Test
        @DisplayName("MCP-002: selectVariantsInRegion returns Map with 'variants' key")
        void testSelectVariantsInRegionReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Variant variant = Variant.newBuilder()
                    .setChr(Chromosome.CHR_17)
                    .setStart(43044295)
                    .setRef("A")
                    .setAlt("G")
                    .build();
                AllelesResponse allelesResponse = AllelesResponse.newBuilder()
                    .addVariants(variant)
                    .build();

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> iterator = mock(Iterator.class);
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenReturn(allelesResponse);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(iterator);

                Map<String, List<VariantView>> result = server.selectVariantsInRegion(
                    "17", 43044295, 43170245,  // BRCA1
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                assertThat(result).containsKey("variants");
                assertThat(result.get("variants")).isNotEmpty();
            }
        }

        @Test
        @DisplayName("selectVariantsInRegion returns empty list for invalid region")
        void testSelectVariantsInRegionInvalidRegion() {
            Map<String, List<VariantView>> result = server.selectVariantsInRegion(
                "1", 2000, 1000,  // Inverted coordinates
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
            );

            assertThat(result).containsKey("variants");
            assertThat(result.get("variants")).isEmpty();
        }
    }

    // ========================================
    // SAMPLE TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Sample Tools Tests")
    class SampleToolsTests {

        @Test
        @DisplayName("countSamplesWithVariants returns Map with 'count' key")
        void testCountSamplesWithVariantsReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountSamplesResponse response = CountSamplesResponse.newBuilder()
                    .setCount(150)
                    .build();
                when(mockBlockingStub.countSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                Map<String, Long> result = server.countSamplesWithVariants(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(150L);
            }
        }

        @Test
        @DisplayName("selectSamplesWithVariants returns Map with 'samples' key")
        void testSelectSamplesWithVariantsReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder()
                    .addSamples("HG00403")
                    .addSamples("HG00405")
                    .build();
                when(mockBlockingStub.selectSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                Map<String, List<String>> result = server.selectSamplesWithVariants(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples")).contains("HG00403", "HG00405");
            }
        }

        @Test
        @DisplayName("selectSamplesWithHomVariants passes selectHom=true, selectHet=false")
        void testSelectSamplesWithHomVariantsFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder().build();
                when(mockBlockingStub.selectSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                server.selectSamplesWithHomVariants(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                verify(mockBlockingStub).selectSamplesInRegion(argThat(request ->
                    request.getHom() && !request.getHet()
                ));
            }
        }

        @Test
        @DisplayName("selectSamplesWithHetVariants passes selectHom=false, selectHet=true")
        void testSelectSamplesWithHetVariantsFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder().build();
                when(mockBlockingStub.selectSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                server.selectSamplesWithHetVariants(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                verify(mockBlockingStub).selectSamplesInRegion(argThat(request ->
                    !request.getHom() && request.getHet()
                ));
            }
        }
    }

    // ========================================
    // PARAMETER PASSTHROUGH TESTS
    // ========================================

    @Nested
    @DisplayName("Parameter Passthrough Tests")
    class ParameterPassthroughTests {

        @Test
        @DisplayName("MCP-003: Parameters are passed through to client correctly")
        void testParameterPassthrough() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                // Call with specific parameters
                server.countVariantsInRegion(
                    "17",                    // chromosome
                    43044295,               // start
                    43170245,               // end
                    "A",                    // refAllele
                    "G",                    // altAllele
                    0.01f,                  // afLessThan
                    0.0001f,                // afGreaterThan
                    0.02f,                  // gnomadExomeAfLT
                    0.0005f,                // gnomadExomeAfGT
                    0.03f,                  // gnomadGenomeAfLT
                    0.0002f,                // gnomadGenomeAfGT
                    "PATHOGENIC",           // clinSignificance
                    "HIGH",                 // vepImpact
                    "TRANSCRIPT",           // vepFeature
                    "PROTEIN_CODING",       // vepBiotype
                    "SNV",                  // vepVariantType
                    "MISSENSE_VARIANT",     // vepConsequences
                    "LIKELY_PATHOGENIC",    // alphaMissenseClass
                    0.9f,                   // alphaMissenseScoreLT
                    0.5f,                   // alphaMissenseScoreGT
                    true,                   // biallelicOnly
                    false,                  // multiallelicOnly
                    false,                  // excludeMales
                    true,                   // excludeFemales
                    10,                     // minVariantLengthBp
                    50                      // maxVariantLengthBp
                );

                // Verify the request contains the expected parameters
                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    request.getChr() == Chromosome.CHR_17 &&
                    request.getStart() == 43044295 &&
                    request.getEnd() == 43170245 &&
                    request.getRef().equals("A") &&
                    request.getAlt().equals("G") &&
                    request.getAnn().getAfLt() == 0.01f &&
                    request.getAnn().getAfGt() == 0.0001f
                ));
            }
        }

        @Test
        @DisplayName("MCP-004: Optional params null are passed correctly")
        void testNullOptionalParameters() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                // Call with all optional params as null
                server.countVariantsInRegion(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                // Verify the request was made with empty annotations
                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    request.getRef().isEmpty() &&
                    request.getAlt().isEmpty() &&
                    request.getAnn().getImpactList().isEmpty() &&
                    request.getAnn().getBioTypeList().isEmpty() &&
                    request.getAnn().getConsequenceList().isEmpty()
                ));
            }
        }

        @Test
        @DisplayName("Chromosome parameter is correctly mapped")
        void testChromosomeMapping() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                // Test chromosome X
                server.countVariantsInRegion(
                    "X", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    request.getChr() == Chromosome.CHR_X
                ));
            }
        }
    }

    // ========================================
    // INHERITANCE MODEL TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Inheritance Model Tools Tests")
    class InheritanceModelToolsTests {

        @Test
        @DisplayName("deNovoInTrio returns Map with 'variants' key")
        void testDeNovoInTrioReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Variant variant = Variant.newBuilder()
                    .setChr(Chromosome.CHR_1)
                    .setStart(1000)
                    .build();
                AllelesResponse allelesResponse = AllelesResponse.newBuilder()
                    .addVariants(variant)
                    .build();

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> iterator = mock(Iterator.class);
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenReturn(allelesResponse);
                when(mockBlockingStub.selectDeNovo(any(DeNovoRequest.class)))
                    .thenReturn(iterator);

                Map<String, List<VariantView>> result = server.deNovoInTrio(
                    "HG00403", "HG00404", "HG00405",  // trio
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                assertThat(result).containsKey("variants");
                assertThat(result.get("variants")).isNotEmpty();
            }
        }

        @Test
        @DisplayName("deNovoInTrio passes trio IDs correctly")
        void testDeNovoInTrioPassesTrioIds() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectDeNovo(any(DeNovoRequest.class)))
                    .thenReturn(emptyIterator);

                server.deNovoInTrio(
                    "HG00403", "HG00404", "HG00405",
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                verify(mockBlockingStub).selectDeNovo(argThat(request ->
                    request.getParent1().equals("HG00403") &&
                    request.getParent2().equals("HG00404") &&
                    request.getProband().equals("HG00405")
                ));
            }
        }

        @Test
        @DisplayName("deNovoInTrio returns empty list for null parent")
        void testDeNovoInTrioNullParent() {
            Map<String, List<VariantView>> result = server.deNovoInTrio(
                null, "HG00404", "HG00405",  // null parent
                "1", 1000, 2000,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
            );

            assertThat(result).containsKey("variants");
            assertThat(result.get("variants")).isEmpty();
        }

        @Test
        @DisplayName("hetDominantInTrio passes affected/unaffected correctly")
        void testHetDominantInTrioPassesCorrectly() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectHetDominant(any(HetDominantRequest.class)))
                    .thenReturn(emptyIterator);

                server.hetDominantInTrio(
                    "HG00403", "HG00404", "HG00405",  // affected, unaffected, proband
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                verify(mockBlockingStub).selectHetDominant(argThat(request ->
                    request.getAffectedParent().equals("HG00403") &&
                    request.getUnaffectedParent().equals("HG00404") &&
                    request.getAffectedChild().equals("HG00405")
                ));
            }
        }

        @Test
        @DisplayName("homRecessiveInTrio passes carrier parents correctly")
        void testHomRecessiveInTrioPassesCorrectly() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                @SuppressWarnings("unchecked")
                Iterator<AllelesResponse> emptyIterator = mock(Iterator.class);
                when(emptyIterator.hasNext()).thenReturn(false);
                when(mockBlockingStub.selectHomRecessive(any(HomRecessiveRequest.class)))
                    .thenReturn(emptyIterator);

                server.homRecessiveInTrio(
                    "HG00403", "HG00404", "HG00405",  // carrier1, carrier2, affected
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                verify(mockBlockingStub).selectHomRecessive(argThat(request ->
                    request.getUnaffectedParent1().equals("HG00403") &&
                    request.getUnaffectedParent2().equals("HG00404") &&
                    request.getAffectedChild().equals("HG00405")
                ));
            }
        }
    }

    // ========================================
    // KINSHIP TOOL TESTS
    // ========================================

    @Nested
    @DisplayName("Kinship Tool Tests")
    class KinshipToolTests {

        @Test
        @DisplayName("getKinshipDegree returns relatedness degree in KinshipResult")
        void testGetKinshipDegreeReturnsDegree() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                Relatedness relatedness = Relatedness.newBuilder()
                    .setDegree(KinshipDegree.FIRST_DEGREE)
                    .build();
                KinshipResponse response = KinshipResponse.newBuilder()
                    .addRel(relatedness)
                    .build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class)))
                    .thenReturn(response);

                OneKGPdMCPServer.KinshipResult result = server.getKinshipDegree("HG00403", "HG00405");

                assertThat(result.degree()).isEqualTo("FIRST_DEGREE");
            }
        }

        @Test
        @DisplayName("getKinshipDegree passes sample IDs correctly")
        void testGetKinshipDegreePassesSampleIds() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                KinshipResponse response = KinshipResponse.newBuilder().build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class)))
                    .thenReturn(response);

                server.getKinshipDegree("HG00403", "HG00405");

                verify(mockBlockingStub).kinshipDuo(argThat(request ->
                    request.getSample1().equals("HG00403") &&
                    request.getSample2().equals("HG00405")
                ));
            }
        }

        @Test
        @DisplayName("getKinshipDegree returns empty string for null sample")
        void testGetKinshipDegreeNullSample() {
            OneKGPdMCPServer.KinshipResult result = server.getKinshipDegree(null, "HG00405");
            assertThat(result.degree()).isEmpty();
        }

        @Test
        @DisplayName("getKinshipDegree returns empty string for empty sample")
        void testGetKinshipDegreeEmptySample() {
            OneKGPdMCPServer.KinshipResult result = server.getKinshipDegree("", "HG00405");
            assertThat(result.degree()).isEmpty();
        }

        @Test
        @DisplayName("getKinshipDegree returns empty string for empty response")
        void testGetKinshipDegreeEmptyResponse() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Return empty response
                KinshipResponse response = KinshipResponse.newBuilder().build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class)))
                    .thenReturn(response);

                OneKGPdMCPServer.KinshipResult result = server.getKinshipDegree("HG00403", "HG00405");

                assertThat(result.degree()).isEmpty();
            }
        }
    }

    // ========================================
    // ERROR HANDLING TESTS
    // ========================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("gRPC error returns 0 for variant count")
        void testGrpcErrorReturnsZeroCount() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                Map<String, Long> result = server.countVariantsInRegion(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                assertThat(result.get("count")).isZero();
            }
        }

        @Test
        @DisplayName("gRPC error returns empty list for select")
        void testGrpcErrorReturnsEmptyList() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                Map<String, List<VariantView>> result = server.selectVariantsInRegion(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );

                assertThat(result.get("variants")).isEmpty();
            }
        }

        @Test
        @DisplayName("gRPC error returns empty map for samples")
        void testGrpcErrorReturnsEmptySamplesList() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                Map<String, List<String>> result = server.selectSamplesWithVariants(
                    "1", 1000, 2000,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                assertThat(result.get("samples")).isEmpty();
            }
        }
    }
}
