package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class KinshipSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "degree", Map.of(
                    "type", "string",
                    "description", "The categorical degree of relatedness",
                    "enum", List.of(
                        "TWINS_MONOZYGOTIC",
                        "FIRST_DEGREE",
                        "SECOND_DEGREE",
                        "THIRD_DEGREE",
                        "UNRELATED"
                    )
                )
            ),
            "required", List.of("degree")
        );
    }
}