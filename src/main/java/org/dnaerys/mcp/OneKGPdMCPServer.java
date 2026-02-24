/*
 * Copyright © 2026 Dmitry Degrave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dnaerys.mcp;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import org.dnaerys.client.DnaerysClient;
import org.dnaerys.client.MetaClient;
import org.dnaerys.client.entity.PopulationInfo;
import org.dnaerys.client.entity.PopulationStats;
import org.dnaerys.client.entity.SuperpopulationInfo;
import org.dnaerys.client.entity.SuperpopulationSummary;
import org.dnaerys.client.entity.SampleMeta;
import org.dnaerys.cluster.grpc.Variant;
import org.dnaerys.mcp.generator.*;
import org.dnaerys.mcp.util.McpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@ApplicationScoped
public class OneKGPdMCPServer {

    @Inject
    McpResponse mcpResponse;

    @Inject
    DnaerysClient client;

    @Inject
    MetaClient metaClient;

    private static final String CHROMOSOME_DESC =
        "chromosome, values: 1,2,...,22,X,Y";
    private static final String START_DESC =
        "start position in base pairs, 1-based, GRCh38";
    private static final String END_DESC =
        "end position in base pairs, 1-based, GRCh38";
    private static final String POSITION_DESC =
        "position in base pairs, 1-based, GRCh38";

    private static final String HET_DESC =
        "include HETEROZYGOUS variants (0/1 genotypes)";
    private static final String HOM_DESC =
        "include HOMOZYGOUS variants (1/1 genotypes)";

    private static final String REF_DESC =
        "reference allele bases (REF)";
    private static final String ALT_DESC =
        "alternative allele bases (ALT)";

    private static final String MINLEN_DESC =
        "minimal alternative variant length in base pairs";
    private static final String MAXLEN_DESC =
        "maximal alternative variant length in base pairs";

    private static final String BIONLY_DESC =
        "select biallelic variants only";
    private static final String MULTONLY_DESC =
        "select multiallelic variants only";

    private static final String EXCLUDE_MALE_DESC =
        "exclude variants in males";
    private static final String EXCLUDE_FEMALE_DESC =
        "exclude variants in females";

    private static final String AFLT_DESC =
        "KGP AF < afLessThan";
    private static final String AFGT_DESC =
        "KGP AF > afGreaterThan";

    private static final String GNE_AFLT_DESC =
        "gnomAD Exome AF < gnomadAfExLessThan";
    private static final String GNE_AFGT_DESC =
        "gnomAD Exome AF > gnomadAfExGreaterThan";

    private static final String GNG_AFLT_DESC =
        "gnomAD Genome AF < gnomadAfGenLessThan";
    private static final String GNG_AFGT_DESC =
        "gnomAD Genome AF > gnomadAfGenGreaterThan";

    private static final String IMPACT_DESC =
        "CSV with VEP impact terms. Values: " +
        "HIGH,MODERATE,LOW,MODIFIER";

    private static final String BIOTYPE_DESC =
        "CSV with VEP biotypes terms. Values: " +
        "PROCESSED_TRANSCRIPT,LNCRNA,RETAINED_INTRON,MIRNA,RRNA," +
        "SNRNA,SNORNA,PROTEIN_CODING,IG_PSEUDOGENE,TEC";

    private static final String VARIANTTYPE_DESC =
        "CSV with Sequence Ontology Variant Classes terms. Values: " +
        "SNV,DELETION,INSERTION";

    private static final String FEATURETYPE_DESC =
        "CSV with VEP feature types terms. Values: " +
        "TRANSCRIPT,REGULATORYFEATURE,MOTIFFEATURE";

    private static final String CONSEQ_DESC =
        "CSV with Sequence Ontology variant consequences. Values: " +
        "TRANSCRIPT_ABLATION,SPLICE_ACCEPTOR_VARIANT,SPLICE_DONOR_VARIANT,STOP_GAINED,FRAMESHIFT_VARIANT," +
        "STOP_LOST,START_LOST,INFRAME_INSERTION,INFRAME_DELETION,MISSENSE_VARIANT," +
        "PROTEIN_ALTERING_VARIANT,SPLICE_REGION_VARIANT,INCOMPLETE_TERMINAL_CODON_VARIANT,START_RETAINED_VARIANT," +
        "STOP_RETAINED_VARIANT,SYNONYMOUS_VARIANT,CODING_SEQUENCE_VARIANT,MATURE_MIRNA_VARIANT," +
        "NON_CODING_TRANSCRIPT_EXON_VARIANT,INTRON_VARIANT," +
        "NON_CODING_TRANSCRIPT_VARIANT,UPSTREAM_GENE_VARIANT,DOWNSTREAM_GENE_VARIANT," +
        "REGULATORY_REGION_ABLATION,REGULATORY_REGION_VARIANT,INTERGENIC_VARIANT,SPLICE_POLYPYRIMIDINE_TRACT_VARIANT," +
        "SPLICE_DONOR_5TH_BASE_VARIANT,SPLICE_DONOR_REGION_VARIANT";

    private static final String AM_DESC =
        "CSV with AlphaMissense Classes. Values: " +
        "LIKELY_BENIGN,LIKELY_PATHOGENIC,AMBIGUOUS";

    private static final String AMLT_DESC =
        "AlphaMissense Score < alphaMissenseScoreLT";

    private static final String AMGT_DESC =
        "AlphaMissense Score > alphaMissenseScoreGT";

    private static final String CLIN_DESC =
        "CSV with ClinVar Clinical Significance annotations. Values: " +
        "BENIGN,LIKELY_BENIGN,UNCERTAIN_SIGNIFICANCE,LIKELY_PATHOGENIC,PATHOGENIC," +
        "DRUG_RESPONSE,ASSOCIATION,RISK_FACTOR,PROTECTIVE,AFFECTS,CONFERS_SENSITIVITY," +
        "UNCERTAIN_RISK_ALLELE,LIKELY_RISK_ALLELE,ESTABLISHED_RISK_ALLELE";

    private static final String SKIP_DESC =
        "items to skip";

    private static final String LIM_DESC =
        "items limit";

    public record GenomicRegion(
        @ToolArg(description = CHROMOSOME_DESC) String chromosome,
        @ToolArg(description = START_DESC) int start,
        @ToolArg(description = END_DESC) int end,
        @ToolArg(description = REF_DESC, required = false) String refAllele,
        @ToolArg(description = ALT_DESC, required = false) String altAllele
    ) {}

    public record SelectByAnnotations(
        @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
        @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
        @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
        @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
        @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
        @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
        @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
        @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
        @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
        @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
        @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
        @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
        @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
        @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
        @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
        @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
        @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
        @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
        @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
        @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
        @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp
    ) {
        public static SelectByAnnotations empty() {
            return new SelectByAnnotations(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
            );
        }

        public static SelectByAnnotations withAlphaMissenseScore(Float amScore) {
            return new SelectByAnnotations(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, amScore, null, null, null, null, null, null, null
            );
        }
    }

    // Tools

    @Startup
    void init() { Log.info("Starting Dnaerys OneKGPd MCP server..."); }

    // Dataset stats & constants

    @Tool(
        title = "getDatasetInfo",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getDatasetInfo",
            readOnlyHint = true,
            destructiveHint = false, // required to override Quarkus' defaults
            idempotentHint = true,  // required to override Quarkus' defaults
            openWorldHint = false
        ),
        description = "Retrieve the number of variants and samples in 1000 Genomes Project.\n\n" +
            "RETURNS: total number of variants, total number of samples, number of male and female samples.\n" +
            "Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = DnaerysClient.DatasetInfo.class,
            generator = DatasetInfoSchemaGenerator.class
        )
    )
    public ToolResponse getDatasetInfo() {
        try {
            DnaerysClient.DatasetInfo counts = client.getDatasetInfo();
            return mcpResponse.success(counts);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "countVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT variants which exist in ANY genomic region provided, in 1000 Genomes.\n" +
            "Returns: Integer count of variants matching criteria in ANY region.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. Use this tool FIRST: to assess result size before calling selectVariants\n" +
            "2. If count is manageable, call selectVariants with same filters if variant details are required\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public ToolResponse countVariants(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );
            Integer count = client.countVariants(regions, selectHom, selectHet, annotations);
            return mcpResponse.success(Map.of("count", count));
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "selectVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT variants which exist in ANY genomic region provided, in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, cohort-wide stats matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countVariants first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public ToolResponse selectVariants(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );
            List<Variant> variants = client.selectVariants(regions, selectHom, selectHet, annotations, skip, limit);
            List<VariantView> vv = variants.stream()
                .map(VariantView::fromGrpc)
                .toList();
            Map<String, Object> structured = Map.of("variants", vv);
            return mcpResponse.success(structured, vv);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "countVariantsInSamples",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countVariantsInSamples",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT variants which exist in ANY genomic region provided in the specified SAMPLES.\n" +
            "Returns: Integer count of variants matching criteria in the specified samples in ANY region.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. Use this tool FIRST to get variant count for the sample\n" +
            "2. If count is manageable, call selectVariantsInSamples with same filters if variant details are required\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public ToolResponse countVariantsInSamples(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = "List of samples") List<String> samples,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );
            Integer count = client.countVariantsInSamples(regions, samples, selectHom, selectHet, annotations);
            return mcpResponse.success(Map.of("count", count));
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "selectVariantsInSamples",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectVariantsInSamples",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT variants which exist in ANY genomic region provided in the specified SAMPLES.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, cohort-wide stats matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countVariantsInSamples first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Array of variants for each sample",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantMapSchemaGenerator.class  // Changed generator
        )
    )
    public ToolResponse selectVariantsInSamples(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = "List of samples") List<String> samples,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );

            Map<String, Set<Variant>> variantsBySample = client.selectVariantsInSamples(regions, samples, selectHom, selectHet, annotations, skip, limit);

            // Convert to array of {sample, variants} objects
            List<Map<String, Object>> arrayFormat = variantsBySample.entrySet().stream()
                .map(entry -> Map.of(
                    "sample", (Object) entry.getKey(),
                    "variants", (Object) entry.getValue().stream()
                        .map(VariantView::fromGrpc)
                        .toList()
                ))
                .toList();

            Map<String, Object> structured = Map.of("variantsBySample", arrayFormat);

            // For rawData, keep the map structure for stringify
            Map<String, List<VariantView>> viewsBySample = variantsBySample.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(VariantView::fromGrpc)
                        .toList()
                ));

            return mcpResponse.success(structured, viewsBySample);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "countSamples",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countSamples",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT SAMPLES with specific variants which exist in ANY genomic region provided, in 1000 Genomes.\n" +
            "Returns: Integer count of unique samples having variants matching criteria in ANY region.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include samples with HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include samples with HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need samples with homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need samples with HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need samples with HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. Use this tool FIRST: to assess result size before calling selectSamples\n" +
            "2. If count is manageable, call selectSamples with same filters if sample IDs are required\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public ToolResponse countSamples(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );

            Integer count = client.countSamples(regions, selectHom, selectHet, annotations);
            return mcpResponse.success(Map.of("count", count));
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "selectSamples",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamples",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT SAMPLES with specific variants which exist in ANY genomic region provided, in 1000 Genomes.\n" +
            "Returns: unique sample IDs having variants matching criteria in ANY region.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include samples with HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include samples with HOMOZYGOUS variants (1/1 genotypes)\n" +
            "Examples:\n" +
            "- Use selectHet=true AND selectHom=true: when need samples with homozygous OR heterozygous variants or uncertain\n" +
            "- Use selectHet=true AND selectHom=false: when need samples with HETEROZYGOUS variants ONLY (0/1 genotypes)\n" +
            "- Use selectHet=false AND selectHom=true: when need samples with HOMOZYGOUS variants ONLY (1/1 genotypes)\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countSamples first to assess result size\n" +
            "2. Apply this tool with the same filters\n\n" +

            "PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public ToolResponse selectSamples(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );
            List<String> samples = client.selectSamples(regions, selectHom, selectHet, annotations);
            Map<String, Object> structured = Map.of("samples", samples);
            return mcpResponse.success(structured, samples);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "countSamplesHomozygousReference",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countSamplesHomozygousReference",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT unique samples with HOMOZYGOUS REFERENCE variants (0/0 genotypes) at genomic position.\n" +
            "RETURNS: Number of samples or -1 if no variants exist at the position in the database. " +
            "Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public ToolResponse countSamplesHomozygousReference(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = POSITION_DESC) int position) {
        try {
            Integer count = client.countSamplesHomozygousReference(chromosome, position);
            return mcpResponse.success(Map.of("count", count));
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "selectSamplesHomozygousReference",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamplesHomozygousReference",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT unique samples with HOMOZYGOUS REFERENCE variants (0/0 genotypes) at genomic position.\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public ToolResponse selectSamplesHomozygousReference(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = POSITION_DESC) int position) {
        try {
            List<String> samples = client.selectSamplesHomozygousReference(chromosome, position);
            Map<String, Object> structured = Map.of("samples", samples);
            return mcpResponse.success(structured, samples);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    public record KinshipResult(String degree) {}

    @Tool(
        title = "getKinshipDegree",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getKinshipDegree",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "Retrieve degree of relatedness (kinship) between two samples in 1000 Genomes Project.\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = KinshipResult.class,
            generator = KinshipSchemaGenerator.class
        )
    )
    public ToolResponse getKinshipDegree(
                @ToolArg(description = "First sample ID (e.g., HG00404)") String sample1,
                @ToolArg(description = "Second sample ID (e.g., HG00405)") String sample2) {
        try {
            String degree = client.kinship(sample1, sample2);
            KinshipResult result = new KinshipResult(degree);
            return mcpResponse.success(result);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "computeAlphaMissenseAvg",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "computeAlphaMissenseAvg",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description = "COMPUTE AlphaMissense Score Mean value across multiple regions.\n\n" +
            "RETURNS: AlphaMissense Score Mean value, Standard Deviation.\n" +
            "Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = DnaerysClient.AlphaMissenseAvg.class,
            generator = AlphaMissenseAvgSchemaGenerator.class
        )
    )
    public ToolResponse computeAlphaMissenseAvg(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, null, null);
            DnaerysClient.AlphaMissenseAvg result = client.computeAlphaMissenseAvg(regions);
            return mcpResponse.success(result);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "computeVariantBurden",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "computeVariantBurden",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description = "COMPUTE variant burden across multiple regions for samples for selected variants.\n\n" +

            "ZYGOSITY Parameters Logic:\n" +
            "- Use selectHet=true: to include samples with HETEROZYGOUS variants (0/1 genotypes)\n" +
            "- Use selectHom=true: to include samples with HOMOZYGOUS variants (1/1 genotypes)\n" +

            "ANNOTATION PARAMETERS Logic:\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' selects variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS:\n" +
            "- Histogram: number of samples having certain number of variants.\n" +
            "- List of samples with maximum burden and 2nd highest burden.",
        outputSchema = @Tool.OutputSchema(
            from = DnaerysClient.VariantBurden.class,
            generator = VariantBurdenSchemaGenerator.class
        )
    )
    @Blocking
    public ToolResponse computeVariantBurden(
                @ToolArg(description = CHROMOSOME_DESC) List<String> chromosome,
                @ToolArg(description = START_DESC) List<Integer> start,
                @ToolArg(description = END_DESC) List<Integer> end,
                @ToolArg(description = REF_DESC, required = false) List<String> refAllele,
                @ToolArg(description = ALT_DESC, required = false) List<String> altAllele,
                @ToolArg(description = HET_DESC) Boolean selectHet,
                @ToolArg(description = HOM_DESC) Boolean selectHom,
                @ToolArg(description = "List of samples. Empty = ALL samples in KGP") List<String> samples,
                @ToolArg(description = AFLT_DESC, required = false) Float afLessThan,
                @ToolArg(description = AFGT_DESC, required = false) Float afGreaterThan,
                @ToolArg(description = GNE_AFLT_DESC, required = false) Float gnomadExomeAfLessThan,
                @ToolArg(description = GNE_AFGT_DESC, required = false) Float gnomadExomeAfGreaterThan,
                @ToolArg(description = GNG_AFLT_DESC, required = false) Float gnomadGenomeAfLessThan,
                @ToolArg(description = GNG_AFGT_DESC, required = false) Float gnomadGenomeAfGreaterThan,
                @ToolArg(description = CLIN_DESC, required = false) String clinSignificance,
                @ToolArg(description = IMPACT_DESC, required = false) String vepImpact,
                @ToolArg(description = FEATURETYPE_DESC, required = false) String vepFeature,
                @ToolArg(description = BIOTYPE_DESC, required = false) String vepBiotype,
                @ToolArg(description = VARIANTTYPE_DESC, required = false) String vepVariantType,
                @ToolArg(description = CONSEQ_DESC, required = false) String vepConsequences,
                @ToolArg(description = AM_DESC, required = false) String alphaMissenseClass,
                @ToolArg(description = AMLT_DESC, required = false) Float alphaMissenseScoreLessThan,
                @ToolArg(description = AMGT_DESC, required = false) Float alphaMissenseScoreGreaterThan,
                @ToolArg(description = BIONLY_DESC, required = false) Boolean biallelicOnly,
                @ToolArg(description = MULTONLY_DESC, required = false) Boolean multiallelicOnly,
                @ToolArg(description = EXCLUDE_MALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXCLUDE_FEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        try {
            List<GenomicRegion> regions = getGenomicRegions(chromosome, start, end, refAllele, altAllele);
            SelectByAnnotations annotations = new SelectByAnnotations (
                afLessThan, afGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, gnomadGenomeAfLessThan,
                gnomadGenomeAfGreaterThan, clinSignificance, vepImpact, vepFeature, vepBiotype, vepVariantType,
                vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, minVariantLengthBp, maxVariantLengthBp
            );
            DnaerysClient.VariantBurden result = client.computeVariantBurden(regions, samples, selectHom, selectHet, annotations);
            return mcpResponse.success(result);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    public List<GenomicRegion> getGenomicRegions(List<String> chromosome, List<Integer> start, List<Integer> end,
                                                 List<String> refAllele, List<String> altAllele) {
        if (chromosome.size() != start.size() || chromosome.size() != end.size()) {
            throw new RuntimeException("Invalid parameter: number of elements in 'chromosome', 'start' and 'end' lists should be equal");
        }
        if (refAllele != null && !refAllele.isEmpty() && refAllele.size() != chromosome.size()) {
            throw new RuntimeException("Invalid parameter: number of elements in 'refAllele' should be equal to number of regions");
        }
        if (altAllele != null && !altAllele.isEmpty() && altAllele.size() != chromosome.size()) {
            throw new RuntimeException("Invalid parameter: number of elements in 'altAllele' should be equal to number of regions");
        }
        List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
        for (int i = 0; i < chromosome.size(); i++) {
            var ref = refAllele != null && !refAllele.isEmpty() ? refAllele.get(i) : null;
            var alt = altAllele != null && !altAllele.isEmpty() ? altAllele.get(i) : null;
            regions.add(new GenomicRegion(chromosome.get(i), start.get(i), end.get(i), ref, alt));
        }
        return regions;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Tool(
        title = "getSampleMetadata",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getSampleMetadata",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "GET sample metadata for given sample IDs from 1000 Genomes Project.\n" +
            "Returns: family info, gender, parental IDs, children, population, superpopulation, phase3 status.",
        outputSchema = @Tool.OutputSchema(
            from = SampleMeta.class,
            generator = SampleMetaSchemaGenerator.class
        )
    )
    public ToolResponse getSampleMetadata(
                @ToolArg(description = "list of sample IDs (e.g. HG00096, HG00427)") List<String> sampleIds) {
        try {
            List<SampleMeta> metas = metaClient.getSampleMeta(sampleIds);
            Map<String, Object> structured = Map.of("samples", metas);
            return mcpResponse.success(structured, metas);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "listPopulations",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "listPopulations",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "LIST all known populations in 1000 Genomes Project with their superpopulation and sample count.\n" +
            "Use this to discover valid population/region values before calling selectSamplesByPopulation.",
        outputSchema = @Tool.OutputSchema(
            from = PopulationInfo.class,
            generator = PopulationInfoSchemaGenerator.class
        )
    )
    public ToolResponse listPopulations() {
        try {
            List<PopulationInfo> populations = metaClient.listPopulations();
            Map<String, Object> structured = Map.of("populations", populations);
            return mcpResponse.success(structured, populations);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "listSuperpopulations",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "listSuperpopulations",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "LIST all known superpopulations in 1000 Genomes Project with sample count and constituent populations.\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = SuperpopulationInfo.class,
            generator = SuperpopulationInfoSchemaGenerator.class
        )
    )
    public ToolResponse listSuperpopulations() {
        try {
            List<SuperpopulationInfo> superpopulations = metaClient.listSuperpopulations();
            Map<String, Object> structured = Map.of("superpopulations", superpopulations);
            return mcpResponse.success(structured, superpopulations);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "getPopulationStats",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getPopulationStats",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "GET detailed statistics for one or more populations: gender split, phase3 count, trio membership.\n" +
            "Accepts population short codes (e.g. CHS) or full names.\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = PopulationStats.class,
            generator = PopulationStatsSchemaGenerator.class
        )
    )
    public ToolResponse getPopulationStats(
                @ToolArg(description = "list of population codes or full names (e.g. CHS, GBR)") List<String> populations) {
        try {
            List<PopulationStats> stats = metaClient.getPopulationStats(populations);
            Map<String, Object> structured = Map.of("populations", stats);
            return mcpResponse.success(structured, stats);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "getSuperpopulationSummary",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getSuperpopulationSummary",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "GET summary statistics for one or more superpopulations with per-population breakdown.\n" +
            "Accepts superpopulation short codes (e.g. EAS) or full names.\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = SuperpopulationSummary.class,
            generator = SuperpopulationSummarySchemaGenerator.class
        )
    )
    public ToolResponse getSuperpopulationSummary(
                @ToolArg(description = "list of superpopulation codes or full names (e.g. EAS, EUR)") List<String> superpopulations) {
        try {
            List<SuperpopulationSummary> summaries = metaClient.getSuperpopulationSummary(superpopulations);
            Map<String, Object> structured = Map.of("superpopulations", summaries);
            return mcpResponse.success(structured, summaries);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }

    @Tool(
        title = "selectSamplesByPopulation",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamplesByPopulation",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
                "SELECT samples from 1000 Genomes by population and/or superpopulation.\n" +
                "Accepts short codes (population: CHS, superpopulation: EAS) or full names " +
                "(population: 'Southern Han Chinese, China', superpopulation: 'East Asia').\n" +

                "At least one parameter must be provided. When both are given, results are intersected.\n" +
                "Supports pagination via skip/limit (default: skip=0, limit=50, max limit=3202).\n" +

                "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public ToolResponse selectSamplesByPopulation(
                @ToolArg(description = "population short code (e.g. CHS, GBR) or full name (e.g. 'Southern Han Chinese, China')", required = false) String population,
                @ToolArg(description = "superpopulation short code (e.g. EAS, EUR) or full name (e.g. 'East Asia')", required = false) String region,
                @ToolArg(description = "number of results to skip (default 0)", required = false) Integer skip,
                @ToolArg(description = "maximum number of results to return (default 50, max 3202)", required = false) Integer limit) {
        try {
            List<String> samples = metaClient.selectSamplesByPopulation(population, region, skip, limit);
            Map<String, Object> structured = Map.of("samples", samples);
            return mcpResponse.success(structured, samples);
        } catch (Exception e) {
            throw McpResponse.handle(e);
        }
    }
}
