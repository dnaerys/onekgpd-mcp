package org.dnaerys.mcp.generator;

import java.util.Map;
import java.util.List;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleCountsSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object", // CallToolResult as an object
            "description", "SAMPLE COUNTS for 1000 Genomes Project",
            "properties", Map.of(
                "total", Map.of("type", "integer", "description", "Total number of samples in the dataset"),
                "male", Map.of("type", "integer", "description", "Total number of male samples"),
                "female", Map.of("type", "integer", "description", "Total number of female samples")
            ),
            "required", List.of("total", "male", "female")
        );
    }
}