package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Output schema for tools that return a list of sample IDs selected by a criterion
 * (findSamples, findSamplesHomozygousReference, findSamplesByPopulation).
 * Wrapper object: totalCount is always present; the samples array is present
 * only when countOnly=false.
 */
@ApplicationScoped
public class SamplesResultSchemaGenerator implements OutputSchemaGenerator {

    private static final String TOTAL_COUNT_DESC =
        "Total number of samples matching the criteria that exist in the database. " +
        "If it is greater than the number of returned samples, use skip/limit pagination to retrieve the rest. " +
        "For homozygous-reference queries, -1 means no variant exists at the position in the database.";

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "totalCount", Map.of("type", "integer", "description", TOTAL_COUNT_DESC),
                "samples", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string")
                )
            ),
            "required", List.of("totalCount")
        );
    }
}
