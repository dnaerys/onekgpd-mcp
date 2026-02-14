package org.dnaerys.mcp;

import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.mcp.OneKGPdMCPServer.GenomicRegion;
import org.dnaerys.mcp.OneKGPdMCPServer.SelectByAnnotations;
import org.dnaerys.mcp.generator.VariantView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
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
@QuarkusTest
class OneKGPdMCPServerTest {

    @Inject
    OneKGPdMCPServer server;

    @InjectMock
    DnaerysClient mockClient;

    @BeforeEach
    void setUp() {
        // Reset mock state before each test
        reset(mockClient);
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
            // Mock the client to return dataset info
            DnaerysClient.DatasetInfo datasetInfo = new DnaerysClient.DatasetInfo(
                138044723, 3202, 1598, 1604
            );
            when(mockClient.getDatasetInfo()).thenReturn(datasetInfo);

            ToolResponse toolResponse = server.getDatasetInfo();
            DnaerysClient.DatasetInfo result = (DnaerysClient.DatasetInfo) toolResponse.structuredContent();

            assertThat(result).isNotNull();
            assertThat(result.variantsTotal()).isEqualTo(138044723);
            assertThat(result.samplesTotal()).isEqualTo(3202);
            assertThat(result.samplesMaleCount()).isEqualTo(1598);
            assertThat(result.samplesFemaleCount()).isEqualTo(1604);
        }
    }

    // ========================================
    // VARIANT COUNT TOOLS TESTS
    // ========================================

    @Nested
    @DisplayName("Variant Count Tools Tests")
    class VariantCountToolsTests {

        @Test
        @DisplayName("countVariants returns Map with 'count' key")
        @SuppressWarnings("unchecked")
        void testCountVariantsReturnsMap() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(5573);

            ToolResponse toolResponse = server.countVariants(
                List.of(new GenomicRegion("17", 43044295, 43170245, null, null)),
                true, true,  // selectHet, selectHom
                null
            );
            Map<String, Integer> result = (Map<String, Integer>) toolResponse.structuredContent();

            assertThat(result).containsKey("count");
            assertThat(result.get("count")).isEqualTo(5573);
        }

        @Test
        @DisplayName("countVariants passes selectHom=true, selectHet=false for homozygous only")
        void testCountVariantsHomozygousOnlyFlags() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(100);

            server.countVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                false, true,  // selectHet=false, selectHom=true (homozygous only)
                null
            );

            // Server swaps: calls client with (selectHom=true, selectHet=false)
            verify(mockClient).countVariants(
                any(), eq(true), eq(false), any()
            );
        }

        @Test
        @DisplayName("countVariants passes selectHom=false, selectHet=true for heterozygous only")
        void testCountVariantsHeterozygousOnlyFlags() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(100);

            server.countVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, false,  // selectHet=true, selectHom=false (heterozygous only)
                null
            );

            // Server swaps: calls client with (selectHom=false, selectHet=true)
            verify(mockClient).countVariants(
                any(), eq(false), eq(true), any()
            );
        }

        @Test
        @DisplayName("countVariants passes selectHom=true, selectHet=true for all variants")
        void testCountVariantsAllVariantsFlags() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(200);

            server.countVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, true,  // selectHet=true, selectHom=true (all variants)
                null
            );

            // Verify the client was called with hom=true, het=true
            verify(mockClient).countVariants(
                any(), eq(true), eq(true), any()
            );
        }

        @Test
        @DisplayName("countVariants throws ToolCallException for invalid chromosome")
        void testCountVariantsInvalidChromosome() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenThrow(new RuntimeException("Invalid Chromosome"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countVariants(
                    List.of(new GenomicRegion("99", 1000, 2000, null, null)),
                    true, true,  // selectHet, selectHom
                    null
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
        @DisplayName("MCP-002: selectVariants returns Map with 'variants' key")
        @SuppressWarnings("unchecked")
        void testSelectVariantsReturnsMap() {
            Variant variant = Variant.newBuilder()
                .setChr(Chromosome.CHR_17)
                .setStart(43044295)
                .setRef("A")
                .setAlt("G")
                .build();
            when(mockClient.selectVariants(
                any(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of(variant));

            ToolResponse toolResponse = server.selectVariants(
                List.of(new GenomicRegion("17", 43044295, 43170245, null, null)),
                true, true,  // selectHet, selectHom
                null, null, null
            );
            Map<String, List<VariantView>> result = (Map<String, List<VariantView>>) toolResponse.structuredContent();

            assertThat(result).containsKey("variants");
            assertThat(result.get("variants")).isNotEmpty();
        }

        @Test
        @DisplayName("selectVariants throws ToolCallException for invalid region")
        void testSelectVariantsInvalidRegion() {
            when(mockClient.selectVariants(
                any(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenThrow(new RuntimeException("Invalid 'start' or 'end'"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.selectVariants(
                    List.of(new GenomicRegion("1", 2000, 1000, null, null)),
                    true, true,  // selectHet, selectHom
                    null, null, null
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
        @DisplayName("countSamples returns Map with 'count' key")
        @SuppressWarnings("unchecked")
        void testCountSamplesReturnsMap() {
            when(mockClient.countSamples(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(150);

            ToolResponse toolResponse = server.countSamples(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, true,  // selectHet, selectHom
                null
            );
            Map<String, Integer> result = (Map<String, Integer>) toolResponse.structuredContent();

            assertThat(result).containsKey("count");
            assertThat(result.get("count")).isEqualTo(150);
        }

        @Test
        @DisplayName("selectSamples returns Map with 'samples' key")
        @SuppressWarnings("unchecked")
        void testSelectSamplesReturnsMap() {
            when(mockClient.selectSamples(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(List.of("HG00403", "HG00405"));

            ToolResponse toolResponse = server.selectSamples(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, true,  // selectHet, selectHom
                null
            );
            Map<String, List<String>> result = (Map<String, List<String>>) toolResponse.structuredContent();

            assertThat(result).containsKey("samples");
            assertThat(result.get("samples")).contains("HG00403", "HG00405");
        }

        @Test
        @DisplayName("selectSamples passes selectHom=true, selectHet=false for homozygous only")
        void testSelectSamplesHomVariantsFlags() {
            when(mockClient.selectSamples(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(List.of());

            server.selectSamples(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                false, true,  // selectHet=false, selectHom=true (homozygous only)
                null
            );

            // Server swaps: calls client with (selectHom=true, selectHet=false)
            verify(mockClient).selectSamples(
                any(), eq(true), eq(false), any()
            );
        }

        @Test
        @DisplayName("selectSamples passes selectHom=false, selectHet=true for heterozygous only")
        void testSelectSamplesHetVariantsFlags() {
            when(mockClient.selectSamples(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(List.of());

            server.selectSamples(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, false,  // selectHet=true, selectHom=false (heterozygous only)
                null
            );

            // Server swaps: calls client with (selectHom=false, selectHet=true)
            verify(mockClient).selectSamples(
                any(), eq(false), eq(true), any()
            );
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
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(100);

            // Call with specific parameters (refAllele/altAllele are now in GenomicRegion)
            ToolResponse response = server.countVariants(
                List.of(new GenomicRegion("17", 43044295, 43170245, "A", "G")),
                true,                   // selectHet
                true,                   // selectHom
                new SelectByAnnotations(
                    0.01f, 0.0001f,                // afLessThan, afGreaterThan
                    0.02f, 0.0005f,                // gnomadExomeAfLT, gnomadExomeAfGT
                    0.03f, 0.0002f,                // gnomadGenomeAfLT, gnomadGenomeAfGT
                    "PATHOGENIC",                  // clinSignificance
                    "HIGH",                        // vepImpact
                    "TRANSCRIPT",                  // vepFeature
                    "PROTEIN_CODING",              // vepBiotype
                    "SNV",                         // vepVariantType
                    "MISSENSE_VARIANT",            // vepConsequences
                    "LIKELY_PATHOGENIC",            // alphaMissenseClass
                    0.9f, 0.5f,                    // alphaMissenseScoreLT, alphaMissenseScoreGT
                    true, false,                   // biallelicOnly, multiallelicOnly
                    false, true,                   // excludeMales, excludeFemales
                    10, 50                         // minVariantLengthBp, maxVariantLengthBp
                )
            );

            // Verify the client was called
            verify(mockClient).countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            );

            // Verify response is valid
            assertThat(response.structuredContent()).isNotNull();
        }

        @Test
        @DisplayName("MCP-004: Optional params null are passed correctly")
        void testNullOptionalParameters() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(100);

            // Call with all optional params as null (selectHet/selectHom are required)
            server.countVariants(
                List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                true, true,  // selectHet, selectHom (required)
                null
            );

            // Verify client was called with null annotations
            verify(mockClient).countVariants(
                any(), eq(true), eq(true), isNull()
            );
        }

        @Test
        @DisplayName("Chromosome parameter is correctly mapped via GenomicRegion")
        void testChromosomeMapping() {
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenReturn(100);

            // Test chromosome X via GenomicRegion
            server.countVariants(
                List.of(new GenomicRegion("X", 1000, 2000, null, null)),
                true, true,  // selectHet, selectHom
                null
            );

            verify(mockClient).countVariants(
                argThat(regions -> regions.size() == 1 && "X".equals(regions.get(0).chromosome())),
                eq(true), eq(true), any()
            );
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
            when(mockClient.countSamplesHomozygousReference(anyString(), anyInt()))
                .thenReturn(2500);

            ToolResponse toolResponse = server.countSamplesHomozygousReference("1", 12345);
            Map<String, Integer> result = (Map<String, Integer>) toolResponse.structuredContent();

            assertThat(result).containsKey("count");
            assertThat(result.get("count")).isEqualTo(2500);
        }

        @Test
        @DisplayName("selectSamplesHomozygousReference returns Map with 'samples' key")
        @SuppressWarnings("unchecked")
        void testSelectSamplesHomRefReturnsMap() {
            when(mockClient.selectSamplesHomozygousReference(anyString(), anyInt()))
                .thenReturn(List.of("HG00403", "HG00405"));

            ToolResponse toolResponse = server.selectSamplesHomozygousReference("1", 12345);
            Map<String, List<String>> result = (Map<String, List<String>>) toolResponse.structuredContent();

            assertThat(result).containsKey("samples");
            assertThat(result.get("samples")).contains("HG00403", "HG00405");
        }

        @Test
        @DisplayName("countSamplesHomozygousReference throws ToolCallException for invalid chromosome")
        void testCountSamplesHomRefInvalidChromosome() {
            when(mockClient.countSamplesHomozygousReference(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Invalid Chromosome"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countSamplesHomozygousReference("99", 12345)
            );

            assertThat(thrown.getMessage()).contains("Invalid Chromosome");
        }

        @Test
        @DisplayName("countSamplesHomozygousReference throws ToolCallException for invalid position")
        void testCountSamplesHomRefInvalidPosition() {
            when(mockClient.countSamplesHomozygousReference(anyString(), anyInt()))
                .thenThrow(new RuntimeException("'position' must be >= 0"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countSamplesHomozygousReference("1", 0)
            );

            assertThat(thrown.getMessage()).contains("'position' must be >= 0");
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
            when(mockClient.kinship(anyString(), anyString())).thenReturn("FIRST_DEGREE");

            ToolResponse toolResponse = server.getKinshipDegree("HG00403", "HG00405");
            OneKGPdMCPServer.KinshipResult result = (OneKGPdMCPServer.KinshipResult) toolResponse.structuredContent();

            assertThat(result.degree()).isEqualTo("FIRST_DEGREE");
        }

        @Test
        @DisplayName("getKinshipDegree passes sample IDs correctly")
        void testGetKinshipDegreePassesSampleIds() {
            when(mockClient.kinship(anyString(), anyString())).thenReturn("FIRST_DEGREE");

            server.getKinshipDegree("HG00403", "HG00405");

            verify(mockClient).kinship(eq("HG00403"), eq("HG00405"));
        }

        @Test
        @DisplayName("getKinshipDegree throws exception for null sample")
        void testGetKinshipDegreeNullSample() {
            when(mockClient.kinship(isNull(), anyString()))
                .thenThrow(new RuntimeException("Sample does not exist"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.getKinshipDegree(null, "HG00405")
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }

        @Test
        @DisplayName("getKinshipDegree throws exception for empty sample")
        void testGetKinshipDegreeEmptySample() {
            when(mockClient.kinship(eq(""), anyString()))
                .thenThrow(new RuntimeException("Sample does not exist"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.getKinshipDegree("", "HG00405")
            );

            assertThat(thrown.getMessage()).contains("does not exist");
        }
    }

    // ========================================
    // ALPHA MISSENSE STAT TOOL TESTS
    // ========================================

    @Nested
    @DisplayName("AlphaMissense Stat Tool Tests")
    class AlphaMissenseAvgToolTests {

        @Test
        @DisplayName("ComputeAlphaMissenseAvg returns wrapped result")
        void testComputeAlphaMissenseAvgReturnsWrappedResult() {
            DnaerysClient.AlphaMissenseAvg stat = new DnaerysClient.AlphaMissenseAvg(0.65, 0.12, 100);
            when(mockClient.computeAlphaMissenseAvg(any())).thenReturn(stat);

            ToolResponse toolResponse = server.computeAlphaMissenseAvg(
                List.of(new GenomicRegion("17", 43044295, 43170245, null, null))
            );
            DnaerysClient.AlphaMissenseAvg result = (DnaerysClient.AlphaMissenseAvg) toolResponse.structuredContent();

            assertThat(result).isNotNull();
            assertThat(result.alphaMissenseMean()).isEqualTo(0.65);
            assertThat(result.alphaMissenseDeviation()).isEqualTo(0.12);
            assertThat(result.variantCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("ComputeAlphaMissenseAvg throws ToolCallException on error")
        void testComputeAlphaMissenseAvgError() {
            when(mockClient.computeAlphaMissenseAvg(any()))
                .thenThrow(new RuntimeException("Connection failed"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.computeAlphaMissenseAvg(
                    List.of(new GenomicRegion("17", 43044295, 43170245, null, null))
                )
            );

            assertThat(thrown.getMessage()).contains("Connection failed");
        }

        @Test
        @DisplayName("ComputeAlphaMissenseAvg returns zeros for empty region")
        void testComputeAlphaMissenseAvgEmptyRegion() {
            DnaerysClient.AlphaMissenseAvg stat = new DnaerysClient.AlphaMissenseAvg(0d, 0d, 0);
            when(mockClient.computeAlphaMissenseAvg(any())).thenReturn(stat);

            ToolResponse toolResponse = server.computeAlphaMissenseAvg(
                List.of(new GenomicRegion("22", 50000000, 50001000, null, null))
            );
            DnaerysClient.AlphaMissenseAvg result = (DnaerysClient.AlphaMissenseAvg) toolResponse.structuredContent();

            assertThat(result.alphaMissenseMean()).isEqualTo(0d);
            assertThat(result.alphaMissenseDeviation()).isEqualTo(0d);
            assertThat(result.variantCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("ComputeAlphaMissenseAvg passes regions to client")
        void testComputeAlphaMissenseAvgPassesRegions() {
            DnaerysClient.AlphaMissenseAvg stat = new DnaerysClient.AlphaMissenseAvg(0.5, 0.1, 50);
            when(mockClient.computeAlphaMissenseAvg(any())).thenReturn(stat);

            List<GenomicRegion> regions = List.of(
                new GenomicRegion("17", 43044295, 43170245, null, null),
                new GenomicRegion("7", 117287120, 117715971, null, null)
            );

            server.computeAlphaMissenseAvg(regions);

            verify(mockClient).computeAlphaMissenseAvg(argThat(r ->
                r.size() == 2 &&
                "17".equals(r.get(0).chromosome()) &&
                "7".equals(r.get(1).chromosome())
            ));
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
            when(mockClient.countVariants(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.countVariants(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                    true, true,  // selectHet, selectHom
                    null
                )
            );

            assertThat(thrown.getMessage()).contains("Connection failed");
        }

        @Test
        @DisplayName("gRPC error throws ToolCallException for select")
        void testGrpcErrorThrowsExceptionForSelect() {
            when(mockClient.selectVariants(
                any(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.selectVariants(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                    true, true,  // selectHet, selectHom
                    null, null, null
                )
            );

            assertThat(thrown.getMessage()).contains("Connection failed");
        }

        @Test
        @DisplayName("gRPC error throws ToolCallException for samples")
        void testGrpcErrorThrowsExceptionForSamples() {
            when(mockClient.selectSamples(
                any(), anyBoolean(), anyBoolean(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

            ToolCallException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                ToolCallException.class,
                () -> server.selectSamples(
                    List.of(new GenomicRegion("1", 1000, 2000, null, null)),
                    true, true,  // selectHet, selectHom
                    null
                )
            );

            assertThat(thrown.getMessage()).contains("Connection failed");
        }
    }
}
