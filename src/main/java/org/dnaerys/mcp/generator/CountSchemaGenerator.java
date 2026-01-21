package org.dnaerys.mcp.generator;

import java.util.List;
import java.util.Map;
import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CountSchemaGenerator implements OutputSchemaGenerator {
    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "count", Map.of(
                    "type", "integer"
                )
            ),
            "required", List.of("count")
        );
    }
}