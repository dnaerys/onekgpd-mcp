package org.dnaerys.mcp.generator;

import java.util.List;
import java.util.Map;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleIdArraySchemaGenerator implements OutputSchemaGenerator {
    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object", // CallToolResult as an object
            "description", "ALL Sample ID in 1000 Genomes Project",
            "properties", Map.of(
                "samples", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string")
                )
            ),
            "required", List.of("samples")
        );
    }
}