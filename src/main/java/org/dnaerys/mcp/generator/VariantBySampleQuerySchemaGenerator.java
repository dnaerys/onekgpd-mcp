package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Output schema for in-samples variant queries (findVariantsInSamples).
 * Wrapper object: totalCount is always present; the variantsBySample array
 * (variants grouped per sample) is present only when countOnly=false.
 */
@ApplicationScoped
public class VariantBySampleQuerySchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "totalCount", Map.of("type", "integer", "description", VariantQuerySchemaGenerator.TOTAL_COUNT_DESC),
                "variantsBySample", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "sample", Map.of("type", "string", "description", "Sample name"),
                            "variants", Map.of(
                                "type", "array",
                                "items", Map.of(
                                    "type", "object",
                                    "properties", VariantQuerySchemaGenerator.variantFields(),
                                    "required", VariantQuerySchemaGenerator.REQUIRED_VARIANT_FIELDS
                                )
                            )
                        ),
                        "required", List.of("sample", "variants")
                    )
                )
            ),
            "required", List.of("totalCount")
        );
    }
}
