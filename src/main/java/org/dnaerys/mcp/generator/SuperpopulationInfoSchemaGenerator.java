package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SuperpopulationInfoSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        Map<String, Object> fields = Map.ofEntries(
            Map.entry("superpopulationCode", Map.of("type", "string", "description", "Superpopulation short code (e.g. EUR, EAS)")),
            Map.entry("superpopulation", Map.of("type", "string", "description", "Full superpopulation name")),
            Map.entry("sampleCount", Map.of("type", "integer", "description", "Total number of samples in this superpopulation")),
            Map.entry("populations", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Population codes belonging to this superpopulation"))
        );

        return Map.of(
            "type", "object",
            "properties", Map.of(
                "superpopulations", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", fields,
                        "required", List.of("superpopulationCode", "superpopulation", "sampleCount", "populations")
                    )
                )
            ),
            "required", List.of("superpopulations")
        );
    }
}
