package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PopulationInfoSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        Map<String, Object> fields = Map.ofEntries(
            Map.entry("populationCode", Map.of("type", "string", "description", "Population short code (e.g. GBR, CHS)")),
            Map.entry("population", Map.of("type", "string", "description", "Full population name")),
            Map.entry("superpopulationCode", Map.of("type", "string", "description", "Superpopulation short code (e.g. EUR, EAS)")),
            Map.entry("superpopulation", Map.of("type", "string", "description", "Full superpopulation name")),
            Map.entry("sampleCount", Map.of("type", "integer", "description", "Total number of samples in this population"))
        );

        return Map.of(
            "type", "object",
            "properties", Map.of(
                "populations", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", fields,
                        "required", List.of("populationCode", "population", "superpopulationCode", "superpopulation", "sampleCount")
                    )
                )
            ),
            "required", List.of("populations")
        );
    }
}
