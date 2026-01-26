package org.dnaerys.mcp;

import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.ToolResponse;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.client.GrpcChannel;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.mcp.generator.VariantView;
import org.dnaerys.testdata.TestInjectionHelper;
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
        TestInjectionHelper.injectMcpResponse(server);
    }

    // ========================================
    // METADATA TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Metadata Tools Tests")
    class MetadataToolsTests {

        @Test
        @DisplayName("MCP-001: getDatasetInfo returns properly wrapped DatasetInfo")
        void testGetDatasetInfoReturnsWrappedResult() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock the datasetInfo response
                DatasetInfoResponse response = DatasetInfoResponse.newBuilder()
                    .setVariantsTotal(138044723)
                    .setSamplesTotal(3202)
                    .setMalesTotal(1598)
                    .setFemalesTotal(1604)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(response);

                ToolResponse toolResponse = server.getDatasetInfo();
                DnaerysClient.DatasetInfo result = (DnaerysClient.DatasetInfo) toolResponse.structuredContent();

                assertThat(result).isNotNull();
                assertThat(result.variantsTotal()).isEqualTo(138044723);
                assertThat(result.samplesTotal()).isEqualTo(3202);
                assertThat(result.samplesMaleCount()).isEqualTo(1598);
                assertThat(result.samplesFemaleCount()).isEqualTo(1604);
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
        @SuppressWarnings("unchecked")
        void testCountVariantsInRegionReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(5573L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                ToolResponse toolResponse = server.countVariantsInRegion(
                    "17", 43044295, 43170245,  // BRCA1
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );
                Map<String, Long> result = (Map<String, Long>) toolResponse.structuredContent();

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(5573L);
            }
        }

        @Test
        @DisplayName("countVariantsInRegion passes selectHom=true, selectHet=false for homozygous only")
        void testCountVariantsInRegionHomozygousOnlyFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                server.countVariantsInRegion(
                    "1", 1000, 2000,
                    false, true,  // selectHet=false, selectHom=true (homozygous only)
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
        @DisplayName("countVariantsInRegion passes selectHom=false, selectHet=true for heterozygous only")
        void testCountVariantsInRegionHeterozygousOnlyFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(100L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                server.countVariantsInRegion(
                    "1", 1000, 2000,
                    true, false,  // selectHet=true, selectHom=false (heterozygous only)
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
        @DisplayName("countVariantsInRegion passes selectHom=true, selectHet=true for all variants")
        void testCountVariantsInRegionAllVariantsFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountAllelesResponse response = CountAllelesResponse.newBuilder()
                    .setCount(200L)
                    .build();
                when(mockBlockingStub.countVariantsInRegion(any(CountAllelesInRegionRequest.class)))
                    .thenReturn(response);

                server.countVariantsInRegion(
                    "1", 1000, 2000,
                    true, true,  // selectHet=true, selectHom=true (all variants)
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );

                // Verify the request was made with hom=true, het=true
                verify(mockBlockingStub).countVariantsInRegion(argThat(request ->
                    request.getHom() && request.getHet()
                ));
            }
        }

        @Test
        @DisplayName("countVariantsInRegion throws ToolCallException for invalid chromosome")
        void testCountVariantsInRegionInvalidChromosome() {
            // Invalid chromosome should throw ToolCallException
            // (validation happens in DnaerysClient which throws RuntimeException,
            // caught by McpResponse.handle() and converted to ToolCallException)
            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countVariantsInRegion(
                    "99", 1000, 2000,  // Invalid chromosome
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
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
        @SuppressWarnings("unchecked")
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

                Iterator<AllelesResponse> iterator = mock(Iterator.class);
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenReturn(allelesResponse);
                when(mockBlockingStub.selectVariantsInRegion(any(AllelesInRegionRequest.class)))
                    .thenReturn(iterator);

                ToolResponse toolResponse = server.selectVariantsInRegion(
                    "17", 43044295, 43170245,  // BRCA1
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                );
                Map<String, List<VariantView>> result = (Map<String, List<VariantView>>) toolResponse.structuredContent();

                assertThat(result).containsKey("variants");
                assertThat(result.get("variants")).isNotEmpty();
            }
        }

        @Test
        @DisplayName("selectVariantsInRegion throws ToolCallException for invalid region")
        void testSelectVariantsInRegionInvalidRegion() {
            // Inverted coordinates should throw ToolCallException
            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.selectVariantsInRegion(
                    "1", 2000, 1000,  // Inverted coordinates
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Invalid 'start' or 'end'");
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
        @SuppressWarnings("unchecked")
        void testCountSamplesWithVariantsReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountSamplesResponse response = CountSamplesResponse.newBuilder()
                    .setCount(150)
                    .build();
                when(mockBlockingStub.countSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                ToolResponse toolResponse = server.countSamplesWithVariants(
                    "1", 1000, 2000,
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );
                Map<String, Long> result = (Map<String, Long>) toolResponse.structuredContent();

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(150L);
            }
        }

        @Test
        @DisplayName("selectSamplesWithVariants returns Map with 'samples' key")
        @SuppressWarnings("unchecked")
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

                ToolResponse toolResponse = server.selectSamplesWithVariants(
                    "1", 1000, 2000,
                    true, true,  // selectHet, selectHom
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null
                );
                Map<String, List<String>> result = (Map<String, List<String>>) toolResponse.structuredContent();

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples")).contains("HG00403", "HG00405");
            }
        }

        @Test
        @DisplayName("selectSamplesWithVariants passes selectHom=true, selectHet=false for homozygous only")
        void testSelectSamplesWithHomVariantsFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder().build();
                when(mockBlockingStub.selectSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                server.selectSamplesWithVariants(
                    "1", 1000, 2000,
                    false, true,  // selectHet=false, selectHom=true (homozygous only)
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
        @DisplayName("selectSamplesWithVariants passes selectHom=false, selectHet=true for heterozygous only")
        void testSelectSamplesWithHetVariantsFlags() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder().build();
                when(mockBlockingStub.selectSamplesInRegion(any(SamplesInRegionRequest.class)))
                    .thenReturn(response);

                server.selectSamplesWithVariants(
                    "1", 1000, 2000,
                    true, false,  // selectHet=true, selectHom=false (heterozygous only)
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
                    true,                   // selectHet
                    true,                   // selectHom
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
                    request.getHet() &&
                    request.getHom() &&
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

                // Call with all optional params as null (selectHet/selectHom are required)
                server.countVariantsInRegion(
                    "1", 1000, 2000,
                    true, true,  // selectHet, selectHom (required)
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
                    true, true,  // selectHet, selectHom
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
    // HOMOZYGOUS REFERENCE TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Homozygous Reference Tools Tests")
    class HomozygousReferenceToolsTests {

        @Test
        @DisplayName("countSamplesHomozygousReference returns Map with 'count' key")
        @SuppressWarnings("unchecked")
        void testCountSamplesHomRefReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                CountSamplesResponse response = CountSamplesResponse.newBuilder()
                    .setCount(2500)
                    .build();
                when(mockBlockingStub.countSamplesHomReference(any(SamplesHomRefRequest.class)))
                    .thenReturn(response);

                ToolResponse toolResponse = server.countSamplesHomozygousReference("1", 12345);
                Map<String, Long> result = (Map<String, Long>) toolResponse.structuredContent();

                assertThat(result).containsKey("count");
                assertThat(result.get("count")).isEqualTo(2500L);
            }
        }

        @Test
        @DisplayName("selectSamplesHomozygousReference returns Map with 'samples' key")
        @SuppressWarnings("unchecked")
        void testSelectSamplesHomRefReturnsMap() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                SamplesResponse response = SamplesResponse.newBuilder()
                    .addSamples("HG00403")
                    .addSamples("HG00405")
                    .build();
                when(mockBlockingStub.selectSamplesHomReference(any(SamplesHomRefRequest.class)))
                    .thenReturn(response);

                ToolResponse toolResponse = server.selectSamplesHomozygousReference("1", 12345);
                Map<String, List<String>> result = (Map<String, List<String>>) toolResponse.structuredContent();

                assertThat(result).containsKey("samples");
                assertThat(result.get("samples")).contains("HG00403", "HG00405");
            }
        }

        @Test
        @DisplayName("countSamplesHomozygousReference throws ToolCallException for invalid chromosome")
        void testCountSamplesHomRefInvalidChromosome() {
            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countSamplesHomozygousReference("99", 12345)
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("countSamplesHomozygousReference throws ToolCallException for invalid position")
        void testCountSamplesHomRefInvalidPosition() {
            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countSamplesHomozygousReference("1", 0)
            );

            assertThat(thrown.getMessage()).contains("Invalid position");
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

                // Mock datasetInfo to return sample IDs (required for kinship validation)
                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .addFemaleSamplesNames("HG00405")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                Relatedness relatedness = Relatedness.newBuilder()
                    .setDegree(KinshipDegree.FIRST_DEGREE)
                    .build();
                KinshipResponse response = KinshipResponse.newBuilder()
                    .addRel(relatedness)
                    .build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class)))
                    .thenReturn(response);

                ToolResponse toolResponse = server.getKinshipDegree("HG00403", "HG00405");
                OneKGPdMCPServer.KinshipResult result = (OneKGPdMCPServer.KinshipResult) toolResponse.structuredContent();

                assertThat(result.degree()).isEqualTo("FIRST_DEGREE");
            }
        }

        @Test
        @DisplayName("getKinshipDegree passes sample IDs correctly")
        void testGetKinshipDegreePassesSampleIds() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock datasetInfo to return sample IDs (required for kinship validation)
                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .addFemaleSamplesNames("HG00405")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                Relatedness relatedness = Relatedness.newBuilder()
                    .setDegree(KinshipDegree.FIRST_DEGREE)
                    .build();
                KinshipResponse response = KinshipResponse.newBuilder()
                    .addRel(relatedness)
                    .build();
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
        @DisplayName("getKinshipDegree throws exception for null sample")
        void testGetKinshipDegreeNullSample() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock empty dataset (sample will not be found)
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.getKinshipDegree(null, "HG00405")
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("getKinshipDegree throws exception for empty sample")
        void testGetKinshipDegreeEmptySample() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock empty dataset (sample will not be found)
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder().build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.getKinshipDegree("", "HG00405")
                );

                assertThat(thrown.getMessage()).contains("does not exist");
            }
        }

        @Test
        @DisplayName("getKinshipDegree throws exception for empty response")
        void testGetKinshipDegreeEmptyResponse() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenReturn(mockGrpcChannel);
                when(mockGrpcChannel.getBlockingStub()).thenReturn(mockBlockingStub);

                // Mock datasetInfo to return sample IDs (required for kinship validation)
                Cohort cohort = Cohort.newBuilder()
                    .addMaleSamplesNames("HG00403")
                    .addFemaleSamplesNames("HG00405")
                    .build();
                DatasetInfoResponse datasetResponse = DatasetInfoResponse.newBuilder()
                    .addCohorts(cohort)
                    .build();
                when(mockBlockingStub.datasetInfo(any(DatasetInfoRequest.class))).thenReturn(datasetResponse);

                // Return empty kinship response (no relatedness found)
                KinshipResponse response = KinshipResponse.newBuilder().build();
                when(mockBlockingStub.kinshipDuo(any(KinshipDuoRequest.class)))
                    .thenReturn(response);

                // Empty response will cause NoSuchElementException from getFirst()
                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.getKinshipDegree("HG00403", "HG00405")
                );

                assertThat(thrown).isNotNull();
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
        @DisplayName("gRPC error throws ToolCallException for variant count")
        void testGrpcErrorThrowsExceptionForCount() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.countVariantsInRegion(
                        "1", 1000, 2000,
                        true, true,  // selectHet, selectHom
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null
                    )
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }

        @Test
        @DisplayName("gRPC error throws ToolCallException for select")
        void testGrpcErrorThrowsExceptionForSelect() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.selectVariantsInRegion(
                        "1", 1000, 2000,
                        true, true,  // selectHet, selectHom
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null
                    )
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }

        @Test
        @DisplayName("gRPC error throws ToolCallException for samples")
        void testGrpcErrorThrowsExceptionForSamples() {
            try (MockedStatic<GrpcChannel> mockedStatic = mockStatic(GrpcChannel.class)) {
                mockedStatic.when(GrpcChannel::getInstance).thenThrow(new RuntimeException("Connection failed"));

                ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                    ToolCallException.class,
                    () -> server.selectSamplesWithVariants(
                        "1", 1000, 2000,
                        true, true,  // selectHet, selectHom
                        null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null
                    )
                );

                assertThat(thrown.getMessage()).contains("Connection failed");
            }
        }
    }
}
