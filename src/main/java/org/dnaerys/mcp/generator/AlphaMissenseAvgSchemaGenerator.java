package org.dnaerys.mcp.generator;

import java.util.Map;
import java.util.List;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlphaMissenseAvgSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object", // CallToolResult as an object
            "description", "AlphaMissense Mean and Deviation",
            "properties", Map.of(
                "alphaMissenseMean", Map.of("type", "number", "description", "AlphaMissense Score Mean value"),
                "alphaMissenseDeviation", Map.of("type", "number", "description", "AlphaMissense Score Population Standard Deviation"),
                "variantCount", Map.of("type", "integer", "description", "Number of variants")
            ),
            "required", List.of("alphaMissenseMean", "alphaMissenseDeviation", "variantCount")
        );
    }
}