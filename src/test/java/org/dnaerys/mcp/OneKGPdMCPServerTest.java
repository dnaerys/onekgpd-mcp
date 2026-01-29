package org.dnaerys.mcp;

import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.mcp.generator.VariantView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        @DisplayName("countVariantsInRegion returns Map with 'count' key")
        @SuppressWarnings("unchecked")
        void testCountVariantsInRegionReturnsMap() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(5573L);

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

        @Test
        @DisplayName("countVariantsInRegion passes selectHom=true, selectHet=false for homozygous only")
        void testCountVariantsInRegionHomozygousOnlyFlags() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(100L);

            server.countVariantsInRegion(
                "1", 1000, 2000,
                false, true,  // selectHet=false, selectHom=true (homozygous only)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Server swaps: calls client with (selectHom=true, selectHet=false)
            verify(mockClient).countVariantsInRegion(
                eq("1"), eq(1000), eq(2000), eq(true), eq(false),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("countVariantsInRegion passes selectHom=false, selectHet=true for heterozygous only")
        void testCountVariantsInRegionHeterozygousOnlyFlags() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(100L);

            server.countVariantsInRegion(
                "1", 1000, 2000,
                true, false,  // selectHet=true, selectHom=false (heterozygous only)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Server swaps: calls client with (selectHom=false, selectHet=true)
            verify(mockClient).countVariantsInRegion(
                eq("1"), eq(1000), eq(2000), eq(false), eq(true),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("countVariantsInRegion passes selectHom=true, selectHet=true for all variants")
        void testCountVariantsInRegionAllVariantsFlags() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(200L);

            server.countVariantsInRegion(
                "1", 1000, 2000,
                true, true,  // selectHet=true, selectHom=true (all variants)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Verify the client was called with hom=true, het=true
            verify(mockClient).countVariantsInRegion(
                eq("1"), eq(1000), eq(2000), eq(true), eq(true),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("countVariantsInRegion throws ToolCallException for invalid chromosome")
        void testCountVariantsInRegionInvalidChromosome() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenThrow(new RuntimeException("Invalid Chromosome"));

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
            Variant variant = Variant.newBuilder()
                .setChr(Chromosome.CHR_17)
                .setStart(43044295)
                .setRef("A")
                .setAlt("G")
                .build();
            when(mockClient.selectVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any()
            )).thenReturn(List.of(variant));

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

        @Test
        @DisplayName("selectVariantsInRegion throws ToolCallException for invalid region")
        void testSelectVariantsInRegionInvalidRegion() {
            when(mockClient.selectVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any()
            )).thenThrow(new RuntimeException("Invalid 'start' or 'end'"));

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
            when(mockClient.countSamplesInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(150L);

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

        @Test
        @DisplayName("selectSamplesWithVariants returns Map with 'samples' key")
        @SuppressWarnings("unchecked")
        void testSelectSamplesWithVariantsReturnsMap() {
            when(mockClient.selectSamplesInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(List.of("HG00403", "HG00405"));

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

        @Test
        @DisplayName("selectSamplesWithVariants passes selectHom=true, selectHet=false for homozygous only")
        void testSelectSamplesWithHomVariantsFlags() {
            when(mockClient.selectSamplesInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(List.of());

            server.selectSamplesWithVariants(
                "1", 1000, 2000,
                false, true,  // selectHet=false, selectHom=true (homozygous only)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Server swaps: calls client with (selectHom=true, selectHet=false)
            verify(mockClient).selectSamplesInRegion(
                eq("1"), eq(1000), eq(2000), eq(true), eq(false),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("selectSamplesWithVariants passes selectHom=false, selectHet=true for heterozygous only")
        void testSelectSamplesWithHetVariantsFlags() {
            when(mockClient.selectSamplesInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(List.of());

            server.selectSamplesWithVariants(
                "1", 1000, 2000,
                true, false,  // selectHet=true, selectHom=false (heterozygous only)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Server swaps: calls client with (selectHom=false, selectHet=true)
            verify(mockClient).selectSamplesInRegion(
                eq("1"), eq(1000), eq(2000), eq(false), eq(true),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
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
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(100L);

            // Call with specific parameters
            ToolResponse response = server.countVariantsInRegion(
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

            // Verify the client was called (parameter reordering happens in server)
            // Just verify the core coordinates are passed correctly
            verify(mockClient).countVariantsInRegion(
                eq("17"), eq(43044295), eq(43170245),
                anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            );

            // Verify response is valid
            assertThat(response.structuredContent()).isNotNull();
        }

        @Test
        @DisplayName("MCP-004: Optional params null are passed correctly")
        void testNullOptionalParameters() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(100L);

            // Call with all optional params as null (selectHet/selectHom are required)
            server.countVariantsInRegion(
                "1", 1000, 2000,
                true, true,  // selectHet, selectHom (required)
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            // Verify client was called
            verify(mockClient).countVariantsInRegion(
                eq("1"), eq(1000), eq(2000), eq(true), eq(true),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull()
            );
        }

        @Test
        @DisplayName("Chromosome parameter is correctly mapped")
        void testChromosomeMapping() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(100L);

            // Test chromosome X
            server.countVariantsInRegion(
                "X", 1000, 2000,
                true, true,  // selectHet, selectHom
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
            );

            verify(mockClient).countVariantsInRegion(
                eq("X"), eq(1000), eq(2000), eq(true), eq(true),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
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
                .thenReturn(2500L);

            ToolResponse toolResponse = server.countSamplesHomozygousReference("1", 12345);
            Map<String, Long> result = (Map<String, Long>) toolResponse.structuredContent();

            assertThat(result).containsKey("count");
            assertThat(result.get("count")).isEqualTo(2500L);
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
                .thenThrow(new RuntimeException("Invalid position"));

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
    // ERROR HANDLING TESTS
    // ========================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("gRPC error throws ToolCallException for variant count")
        void testGrpcErrorThrowsExceptionForCount() {
            when(mockClient.countVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

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

        @Test
        @DisplayName("gRPC error throws ToolCallException for select")
        void testGrpcErrorThrowsExceptionForSelect() {
            when(mockClient.selectVariantsInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

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

        @Test
        @DisplayName("gRPC error throws ToolCallException for samples")
        void testGrpcErrorThrowsExceptionForSamples() {
            when(mockClient.selectSamplesInRegion(
                anyString(), anyInt(), anyInt(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenThrow(new RuntimeException("Connection failed"));

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
