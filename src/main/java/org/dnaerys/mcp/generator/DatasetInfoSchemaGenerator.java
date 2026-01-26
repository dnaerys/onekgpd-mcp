package org.dnaerys.mcp.generator;

import java.util.Map;
import java.util.List;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DatasetInfoSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object", // CallToolResult as an object
            "description", "VARIANT and SAMPLE COUNTS for 1000 Genomes Project",
            "properties", Map.of(
                "variantsTotal", Map.of("type", "integer", "description", "Total number of variants in the dataset"),
                "samplesTotal", Map.of("type", "integer", "description", "Total number of samples in the dataset"),
                "samplesMaleCount", Map.of("type", "integer", "description", "Number of male samples"),
                "samplesFemaleCount", Map.of("type", "integer", "description", "Number of female samples")
            ),
            "required", List.of("variantsTotal", "samplesTotal", "samplesMaleCount", "samplesFemaleCount")
        );
    }
}