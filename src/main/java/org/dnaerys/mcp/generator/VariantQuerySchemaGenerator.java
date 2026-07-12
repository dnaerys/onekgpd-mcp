package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Output schema for cohort-wide variant queries (findVariants).
 * Wrapper object: totalCount is always present; the variants array is
 * present only when countOnly=false.
 */
@ApplicationScoped
public class VariantQuerySchemaGenerator implements OutputSchemaGenerator {

    static final String TOTAL_COUNT_DESC =
        "Total number of variants matching the criteria that exist in the database. " +
        "If it is greater than the number of returned variants, use skip/limit pagination to retrieve the rest.";

    // Field dictionary shared with VariantBySampleQuerySchemaGenerator; keys match VariantView's serializer.
    static Map<String, Object> variantFields() {
        return Map.ofEntries(
            Map.entry("chr", Map.of("type", "string", "description", "Chromosome (1-22, X, Y)")),
            Map.entry("pos", Map.of("type", "integer", "description", "GRCh38 Position")),
            Map.entry("ref", Map.of("type", "string")),
            Map.entry("alt", Map.of("type", "string")),
            Map.entry("AF", Map.of("type", "number", "description", "Allele Frequency in 1000 Genomes dataset")),
            Map.entry("AC", Map.of("type", "integer", "description", "Allele Count in 1000 Genomes dataset")),
            Map.entry("AN", Map.of("type", "integer", "description", "Total number of alleles in 1000 Genomes dataset")),
            Map.entry("HetSamplesCount", Map.of("type", "integer", "description", "Number of samples heterozygous for the ALT allele in 1000 Genomes dataset")),
            Map.entry("HomSamplesCount", Map.of("type", "integer", "description", "Number of samples homozygous for the ALT allele in 1000 Genomes dataset")),
            Map.entry("gnomADe", Map.of("type", "number", "description", "gnomAD Exomes AF")),
            Map.entry("gnomADg", Map.of("type", "number", "description", "gnomAD Genomes AF")),
            Map.entry("AlphaMissense", Map.of("type", "number", "description", "AlphaMissense score")),
            Map.entry("HGVSp", Map.of("type", "string", "description", "HGVSp notation"))
        );
    }

    static final List<String> REQUIRED_VARIANT_FIELDS =
        List.of("chr", "pos", "ref", "alt", "AF", "AC", "AN", "HetSamplesCount", "HomSamplesCount");

    @Override
    public Map<String, Object> generate(Class<?> type) {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "totalCount", Map.of("type", "integer", "description", TOTAL_COUNT_DESC),
                "variants", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", variantFields(),
                        "required", REQUIRED_VARIANT_FIELDS
                    )
                )
            ),
            "required", List.of("totalCount")
        );
    }
}
