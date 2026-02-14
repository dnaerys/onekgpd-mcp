package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VariantMapSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        // Properties dictionary tells the LLM what each field means
        Map<String, Object> variantFields = Map.ofEntries(
            Map.entry("chr", Map.of("type", "string", "description", "Chromosome (1-22, X, Y)")),
            Map.entry("pos", Map.of("type", "integer", "description", "GRCh38 Position")),
            Map.entry("ref", Map.of("type", "string")),
            Map.entry("alt", Map.of("type", "string")),
            Map.entry("AF", Map.of("type", "number", "description", "Allele Frequency in 1000 Genomes dataset")),
            Map.entry("AC", Map.of("type", "integer", "description", "Allele Count in 1000 Genomes dataset")),
            Map.entry("AN", Map.of("type", "integer", "description", "Total number of alleles in 1000 Genomes dataset")),
            Map.entry("het", Map.of("type", "integer", "description", "Heterozygous allele count in 1000 Genomes dataset")),
            Map.entry("hom", Map.of("type", "integer", "description", "Homozygous allele count in 1000 Genomes dataset")),
            Map.entry("gnomADe", Map.of("type", "number", "description", "gnomAD Exomes AF")),
            Map.entry("gnomADg", Map.of("type", "number", "description", "gnomAD Genomes AF")),
            Map.entry("AlphaMissense", Map.of("type", "number", "description", "AlphaMissense score")),
            Map.entry("HGVSp", Map.of("type", "string", "description", "HGVSp notation"))
        );

        return Map.of(
            "type", "object",
            "properties", Map.of(
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
                                    "properties", variantFields,
                                    "required", List.of("chr", "pos", "ref", "alt", "AF", "AC", "AN", "het", "hom")
                                )
                            )
                        ),
                        "required", List.of("sample", "variants")
                    )
                )
            ),
            "required", List.of("variantsBySample")
        );
    }
}