package org.dnaerys.mcp.generator;

import java.util.Map;
import java.util.List;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VariantBurdenSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object", // CallToolResult as an object
            "description", "Samples having certain number of variants.",
            "properties", Map.of(
                "histogram", Map.of("type", "string", "description", "number of samples having certain number of variants"),
                "highestBurdenSamples", Map.of("type", "string", "description", "sample names with maximum burden"),
                "secondHighestBurdenSamples", Map.of("type", "string", "description", "sample names with 2nd highest burden")
            ),
            "required", List.of("histogram", "highestBurdenSamples", "secondHighestBurdenSamples")
        );
    }
}