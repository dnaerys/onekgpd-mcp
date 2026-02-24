package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SuperpopulationSummarySchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        Map<String, Object> fields = Map.ofEntries(
            Map.entry("superpopulationCode", Map.of("type", "string", "description", "Superpopulation short code")),
            Map.entry("superpopulation", Map.of("type", "string", "description", "Full superpopulation name")),
            Map.entry("sampleCount", Map.of("type", "integer", "description", "Total number of samples in superpopulation")),
            Map.entry("maleCount", Map.of("type", "integer", "description", "Number of male samples")),
            Map.entry("femaleCount", Map.of("type", "integer", "description", "Number of female samples")),
            Map.entry("phase3Count", Map.of("type", "integer", "description", "Number of Phase 3 samples")),
            Map.entry("trioCount", Map.of("type", "integer", "description", "Number of trio children")),
            Map.entry("populations", Map.of(
                "type", "array",
                "description", "Per-population breakdown within this superpopulation",
                "items", Map.of(
                    "type", "object",
                    "properties", PopulationStatsSchemaGenerator.populationStatsFields(),
                    "required", PopulationStatsSchemaGenerator.populationStatsRequired()
                )
            ))
        );

        return Map.of(
            "type", "object",
            "properties", Map.of(
                "superpopulations", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", fields,
                        "required", List.of("superpopulationCode", "superpopulation", "sampleCount", "maleCount",
                            "femaleCount", "phase3Count", "trioCount", "populations")
                    )
                )
            ),
            "required", List.of("superpopulations")
        );
    }
}
