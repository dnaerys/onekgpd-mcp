package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PopulationStatsSchemaGenerator implements OutputSchemaGenerator {

    static Map<String, Object> populationStatsFields() {
        return Map.ofEntries(
            Map.entry("populationCode", Map.of("type", "string", "description", "Population short code")),
            Map.entry("population", Map.of("type", "string", "description", "Full population name")),
            Map.entry("superpopulationCode", Map.of("type", "string", "description", "Superpopulation short code")),
            Map.entry("superpopulation", Map.of("type", "string", "description", "Full superpopulation name")),
            Map.entry("sampleCount", Map.of("type", "integer", "description", "Total number of samples")),
            Map.entry("maleCount", Map.of("type", "integer", "description", "Number of male samples")),
            Map.entry("femaleCount", Map.of("type", "integer", "description", "Number of female samples")),
            Map.entry("phase3Count", Map.of("type", "integer", "description", "Number of samples in Phase 3")),
            Map.entry("trioCount", Map.of("type", "integer", "description", "Number of trio children (samples with both parents)"))
        );
    }

    static List<String> populationStatsRequired() {
        return List.of("populationCode", "population", "superpopulationCode", "superpopulation",
            "sampleCount", "maleCount", "femaleCount", "phase3Count", "trioCount");
    }

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "populations", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", populationStatsFields(),
                        "required", populationStatsRequired()
                    )
                )
            ),
            "required", List.of("populations")
        );
    }
}
