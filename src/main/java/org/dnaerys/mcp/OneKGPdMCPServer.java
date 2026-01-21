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
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import org.dnaerys.client.DnaerysClient;
import org.dnaerys.cluster.grpc.Variant;
import org.dnaerys.mcp.generator.*;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@ApplicationScoped
public class OneKGPdMCPServer {

    private final DnaerysClient client = new DnaerysClient();

    private static final String CHROMOSOME_DESC =
        "chromosome ID, values: 1, 2, ..., 22, X, Y (no MT/scaffolds)";
    private static final String START_DESC =
        "start position in base pairs, 1-based, GRCh38 coordinates";
    private static final String END_DESC =
        "end position in base pairs, 1-based, GRCh38 coordinates";

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

    private static final String EXMALE_DESC =
        "exclude variants in males";
    private static final String EXFEMALE_DESC =
        "exclude variants in females";

    private static final String AFLT_DESC =
        "select variants with cohort AF < afLessThan";
    private static final String AFGT_DESC =
        "select variants with cohort AF > afGreaterThan";

    private static final String GNE_AFLT_DESC =
        "select variants with gnomAD Exome AF < gnomadAfExLessThan";
    private static final String GNE_AFGT_DESC =
        "select variants with gnomAD Exome AF > gnomadAfExGreaterThan";

    private static final String GNG_AFLT_DESC =
        "select variants with gnomAD Genome AF < gnomadAfGenLessThan";
    private static final String GNG_AFGT_DESC =
        "select variants with gnomAD Genome AF > gnomadAfGenGreaterThan";

    private static final String IMPACT_DESC =
        "CSV with VEP impact terms. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "HIGH, MODERATE, LOW, MODIFIER";

    private static final String BIOTYPE_DESC =
        "CSV with VEP biotypes terms. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
        "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
        "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
        "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY";

    private static final String VARIANTTYPE_DESC =
        "CSV with Sequence Ontology Variant Classes terms. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION";

    private static final String FEATURETYPE_DESC =
        "CSV with VEP feature types terms. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE";

    private static final String CONSEQ_DESC =
        "CSV with Sequence Ontology variant consequences. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
        "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
        "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
        "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
        "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
        "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
        "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
        "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
        "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT";

    private static final String AM_DESC =
        "CSV with AlphaMissense classes. " +
        "Relation between values in CSV is logical disjunction. Values:" +
        "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS";

    private static final String AMLT_DESC =
        "select variants with AlphaMissense Score < alphaMissenseScoreLT";

    private static final String AMGT_DESC =
        "select variants with AlphaMissense Score > alphaMissenseScoreGT";

    private static final String CLIN_DESC =
        "CSV with ClinVar Clinical Significance annotations. " +
        "Relation between values in CSV is logical disjunction. Values: " +
        "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
        "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
        "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
        "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE";

    private static final String SKIP_DESC =
        "number of items to be skipped in returned result";

    private static final String LIM_DESC =
        "limit items in returned result";

    // Tools

    @Startup
    void init() { Log.info("Starting Dnaerys OneKGPd MCP server..."); }

    // Dataset stats & constants

    @Tool(
        title = "getSampleCounts",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getSampleCounts",
            readOnlyHint = true,
            destructiveHint = false, // required to override Quarkus' defaults
            idempotentHint = true,  // required to override Quarkus' defaults
            openWorldHint = false
        ),
        description =
            "Retrieve the total, male, and female SAMPLE COUNTS for 1000 Genomes Project\n\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = DnaerysClient.SampleCounts.class,
            generator = SampleCountsSchemaGenerator.class
        )
    )
    public DnaerysClient.SampleCounts getSampleCounts() {
        return client.getSampleCounts();
    }

    @Tool(
        title = "getSampleIds",
        structuredContent = true, // We now know this is required for Schema validation
        annotations = @Tool.Annotations(
            title = "getSampleIds",
            readOnlyHint = true,
            destructiveHint = false, // required to override Quarkus' defaults
            idempotentHint = true,  // required to override Quarkus' defaults
            openWorldHint = false
        ),
        description =
            "Retrieve ALL Sample ID in 1000 Genomes Project\n\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            from = Map.class, // Change this to Map
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> getSampleIds() {
        return Map.of("samples", client.getSampleIds(DnaerysClient.Gender.BOTH));
    }

    @Tool(
        title = "getFemaleSamplesIds",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getFemaleSamplesIds",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "Retrieve all FEMALE Sample IDs in the 1000 Genomes Project\n\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> getFemaleSamplesIds() {
        return Map.of("samples", client.getSampleIds(DnaerysClient.Gender.FEMALE));
    }

    @Tool(
        title = "getMaleSamplesIds",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getMaleSamplesIds",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "Retrieve all MALE Sample IDs in the 1000 Genomes Project\n\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> getMaleSamplesIds() {
        return Map.of("samples", client.getSampleIds(DnaerysClient.Gender.MALE));
    }

    @Tool(
        title = "getVariantsTotal",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "getVariantsTotal",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "Retrieve TOTAL number of variants in 1000 Genomes Project\n\n" +
            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> getVariantsTotal() {
        return Map.of("count", client.variantsTotal());
    }

    // Count/Select Variants in Region

    @Tool(
        title = "countVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT ALL variants (Homozygous + Heterozygous) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectVariantsInRegion\n" +
            "- Use countHomozygousVariantsInRegion: to count 1/1 genotypes only\n" +
            "- Use countHeterozygousVariantsInRegion: to count 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get variant count\n" +
            "2. If count is manageable, call selectVariantsInRegion with same filters if variant details are required\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = true;
        Long count = client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact,
                        vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countHomozygousVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countHomozygousVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT HOMOZYGOUS variants ONLY (1/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of homozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectHomozygousVariantsInRegion\n" +
            "- Use countVariantsInRegion: to count both 0/1 and 1/1 genotypes\n" +
            "- Use countHeterozygousVariantsInRegion: to count 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get homozygous variant count\n" +
            "2. If count is manageable, call selectHomozygousVariantsInRegion with same filters if variant details are required\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countHomozygousVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = false;
        Long count = client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact,
                        vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countHeterozygousVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countHeterozygousVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT HETEROZYGOUS variants ONLY (0/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of heterozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectHeterozygousVariantsInRegion\n" +
            "- Use countVariantsInRegion: to count both 0/1 and 1/1 genotypes\n" +
            "- Use countHomozygousVariantsInRegion: to count 1/1 genotypes only\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get heterozygous variant count\n" +
            "2. If count is manageable, call selectHeterozygousVariantsInRegion with same filters if variant details are required\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countHeterozygousVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = false;
        boolean selectHet = true;
        Long count = client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact,
                        vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "selectVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT ALL variants (Homozygous + Heterozygous) in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, cohort-wide stats.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this PRIMARY/DEFAULT tool: when need both homozygous AND heterozygous variants or uncertain\n" +
            "- Use selectHomozygousVariantsInRegion: when need 1/1 genotypes only\n" +
            "- Use selectHeterozygousVariantsInRegion: when need 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countVariantsInRegion first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = true;

        List<Variant> variants = client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele,
            minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
            gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
            vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
            clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "selectHomozygousVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectHomozygousVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HOMOZYGOUS variants ONLY (1/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, cohort-wide stats.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectVariantsInRegion if you need both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countHomozygousVariantsInRegion first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectHomozygousVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = false;

        List<Variant> variants = client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele,
            minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
            gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
            vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
            clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "selectHeterozygousVariantsInRegion",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectHeterozygousVariantsInRegion",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HETEROZYGOUS variants ONLY (0/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, cohort-wide stats.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectVariantsInRegion if you need both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countHeterozygousVariantsInRegion first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectHeterozygousVariantsInRegion(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = false;
        boolean selectHet = true;

        List<Variant> variants = client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele,
            minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
            gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
            vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
            clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    // Count/Select Variants in Samples

    @Tool(
        title = "countVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT ALL variants (Homozygous + Heterozygous) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of variants matching criteria in the specified sample.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectVariantsInRegionInSample\n" +
            "- Use countHomozygousVariantsInRegionInSample: to count 1/1 genotypes only\n" +
            "- Use countHeterozygousVariantsInRegionInSample: to count 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get variant count for the sample\n" +
            "2. If count is manageable, call selectVariantsInRegionInSample with same filters if variant details are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = true;
        Long count = client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countHomozygousVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countHomozygousVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT HOMOZYGOUS variants ONLY (1/1 genotypes) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of homozygous variants matching criteria in the specified sample.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectHomozygousVariantsInRegionInSample\n" +
            "- Use countVariantsInRegionInSample: to count both 0/1 and 1/1 genotypes\n" +
            "- Use countHeterozygousVariantsInRegionInSample: to count 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get homozygous variant count for the sample\n" +
            "2. If count is manageable, call selectHomozygousVariantsInRegionInSample with same filters if variant details are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countHomozygousVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = false;
        Long count = client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countHeterozygousVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countHeterozygousVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT HETEROZYGOUS variants ONLY (0/1 genotypes) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of heterozygous variants matching criteria in the specified sample.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectHeterozygousVariantsInRegionInSample\n" +
            "- Use countVariantsInRegionInSample: to count both 0/1 and 1/1 genotypes\n" +
            "- Use countHomozygousVariantsInRegionInSample: to count 1/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get heterozygous variant count for the sample\n" +
            "2. If count is manageable, call selectHeterozygousVariantsInRegionInSample with same filters if variant details are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countHeterozygousVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = false;
        boolean selectHet = true;
        Long count = client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "selectVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT ALL variants (Homozygous + Heterozygous) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, sample genotype.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this PRIMARY/DEFAULT tool: when need both homozygous AND heterozygous variants or uncertain\n" +
            "- Use selectHomozygousVariantsInRegionInSample: when need 1/1 genotypes only\n" +
            "- Use selectHeterozygousVariantsInRegionInSample: when need 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countVariantsInRegionInSample first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = true;

        List<Variant> variants = client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "selectHomozygousVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectHomozygousVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HOMOZYGOUS variants ONLY (1/1 genotypes) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, sample genotype.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectVariantsInRegionInSample if you need both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countHomozygousVariantsInRegionInSample first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectHomozygousVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = false;

        List<Variant> variants = client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "selectHeterozygousVariantsInRegionInSample",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectHeterozygousVariantsInRegionInSample",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HETEROZYGOUS variants ONLY (0/1 genotypes) for a specific SAMPLE in genomic region in 1000 Genomes.\n" +
            "Returns: variants with gnomADe/gnomADg AF, AlphaMissense score, HGVSp, sample genotype.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectVariantsInRegionInSample if you need both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countHeterozygousVariantsInRegionInSample first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates), sampleId\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> selectHeterozygousVariantsInRegionInSample(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = "sample id") String sampleId,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {
        boolean selectHom = false;
        boolean selectHet = true;

        List<Variant> variants = client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    // Count/Select Samples

    @Tool(
        title = "countSamplesWithVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countSamplesWithVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT SAMPLES with ANY variants (Homozygous + Heterozygous) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of unique samples having variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectSamplesWithVariants\n" +
            "- Use countSamplesWithHomVariants: to count samples with 1/1 genotypes only\n" +
            "- Use countSamplesWithHetVariants: to count samples with 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get sample count\n" +
            "2. If count is manageable, call selectSamplesWithVariants with same filters if sample IDs are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countSamplesWithVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = true;
        Long count = client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
                        vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                        clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countSamplesWithHomVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countSamplesWithHomVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT SAMPLES with HOMOZYGOUS variants ONLY (1/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of unique samples having homozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectSamplesWithHomVariants\n" +
            "- Use countSamplesWithVariants: to count samples with both 0/1 and 1/1 genotypes\n" +
            "- Use countSamplesWithHetVariants: to count samples with 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get sample count\n" +
            "2. If count is manageable, call selectSamplesWithHomVariants with same filters if sample IDs are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countSamplesWithHomVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = false;
        Long count = client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
                        vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                        clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "countSamplesWithHetVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "countSamplesWithHetVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "COUNT SAMPLES with HETEROZYGOUS variants ONLY (0/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: Integer count of unique samples having heterozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this tool FIRST: to assess result size before calling selectSamplesWithHetVariants\n" +
            "- Use countSamplesWithVariants: to count samples with both 0/1 and 1/1 genotypes\n" +
            "- Use countSamplesWithHomVariants: to count samples with 1/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. Call this tool to get sample count\n" +
            "2. If count is manageable, call selectSamplesWithHetVariants with same filters if sample IDs are required\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions.",
        outputSchema = @Tool.OutputSchema(
            generator = CountSchemaGenerator.class
        )
    )
    public Map<String, Long> countSamplesWithHetVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = false;
        boolean selectHet = true;
        Long count = client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
                        vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                        clinSignificance);
        return Map.of("count", count);
    }

    @Tool(
        title = "selectSamplesWithVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamplesWithVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT SAMPLES with ANY variants (Homozygous + Heterozygous) in genomic region in 1000 Genomes.\n" +
            "Returns: unique sample IDs having variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "TOOL SELECTION:\n" +
            "- Use this PRIMARY/DEFAULT tool: when need samples with both homozygous AND heterozygous variants or uncertain\n" +
            "- Use selectSamplesWithHomVariants: when need samples with 1/1 genotypes only\n" +
            "- Use selectSamplesWithHetVariants: when need samples with 0/1 genotypes only\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countSamplesWithVariants first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> selectSamplesWithVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = true;
        List<String> samples = client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
                        vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                        clinSignificance);
        return Map.of("samples", samples);
    }

    @Tool(
        title = "selectSamplesWithHomVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamplesWithHomVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT SAMPLES with HOMOZYGOUS variants ONLY (1/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: unique sample IDs having homozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectSamplesWithVariants if you need samples with both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countSamplesWithHomVariants first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> selectSamplesWithHomVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = true;
        boolean selectHet = false;
        List<String> samples = client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact, vepBiotype,
                        vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan, alphaMissenseScoreGreaterThan,
                        clinSignificance);
        return Map.of("samples", samples);
    }

    @Tool(
        title = "selectSamplesWithHetVariants",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "selectSamplesWithHetVariants",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT SAMPLES with HETEROZYGOUS variants ONLY (0/1 genotypes) in genomic region in 1000 Genomes.\n" +
            "Returns: unique sample IDs having heterozygous variants matching criteria.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "ALTERNATIVE: Use selectSamplesWithVariants if you need samples with both 0/1 and 1/1 variants.\n\n" +

            "WORKFLOW:\n" +
            "1. ALWAYS call countSamplesWithHetVariants first to assess result size\n" +
            "2. Apply this tool with appropriate filters\n\n" +

            "PARAMETERS:\n" +
            "- Required: chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            generator = SampleIdArraySchemaGenerator.class
        )
    )
    public Map<String, List<String>> selectSamplesWithHetVariants(
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp) {
        boolean selectHom = false;
        boolean selectHet = true;
        List<String> samples = client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance);
        return Map.of("samples", samples);
    }

    @Tool(
        title = "deNovoInTrio",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "deNovoInTrio",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT DE NOVO variants in proband from a trio analysis in 1000 Genomes.\n" +
            "Returns: variants present in proband but absent in both parents (new mutations).\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "INHERITANCE MODEL: De novo - variants not inherited from either parent.\n\n" +

            "WORKFLOW:\n" +
            "1. Provide sample IDs for both parents and proband\n" +
            "2. Apply filters to narrow down candidate variants\n\n" +

            "PARAMETERS:\n" +
            "- Required: parent1, parent2, proband (sample IDs), chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> deNovoInTrio(
                @ToolArg(description = "sample id for parent 1") String parent1,
                @ToolArg(description = "sample id for parent 2") String parent2,
                @ToolArg(description = "sample id for proband") String proband,
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {

        List<Variant> variants = client.selectDeNovo(parent1, parent2, proband, chromosome, start, end, refAllele, altAllele, minVariantLengthBp,
                        maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan, afGreaterThan,
                        gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan, vepImpact,
                        vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "hetDominantInTrio",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "hetDominantInTrio",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HETEROZYGOUS DOMINANT variants in affected child from a trio analysis in 1000 Genomes.\n" +
            "Returns: heterozygous variants shared between affected parent and proband, absent in unaffected parent.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "INHERITANCE MODEL: Autosomal dominant - variant inherited from affected parent.\n\n" +

            "WORKFLOW:\n" +
            "1. Provide sample IDs for affected parent, unaffected parent, and proband\n" +
            "2. Apply filters to narrow down candidate variants\n\n" +

            "PARAMETERS:\n" +
            "- Required: affectedParent, unaffectedParent, proband (sample IDs), chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> hetDominantInTrio(
                @ToolArg(description = "sample id for affected parent") String affectedParent,
                @ToolArg(description = "sample id for unaffected parent") String unaffectedParent,
                @ToolArg(description = "sample id for proband") String proband,
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {

        List<Variant> variants = client.selectHetDominant(affectedParent, unaffectedParent, proband, chromosome, start, end, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
    }

    @Tool(
        title = "homRecessiveInTrio",
        structuredContent = true,
        annotations = @Tool.Annotations(
            title = "homRecessiveInTrio",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        ),
        description =
            "SELECT HOMOZYGOUS RECESSIVE variants in affected child from a trio analysis in 1000 Genomes.\n" +
            "Returns: homozygous variants in proband where both unaffected parents are heterozygous carriers.\n" +
            "Filters: REF/ALT, AF (KGP/gnomAD), VEP impact/biotype/consequences, variant type, AlphaMissense class/score, ClinVar significance.\n\n" +

            "INHERITANCE MODEL: Autosomal recessive - both parents are carriers (0/1), child is homozygous (1/1).\n\n" +

            "WORKFLOW:\n" +
            "1. Provide sample IDs for both unaffected parents and proband\n" +
            "2. Apply filters to narrow down candidate variants\n\n" +

            "PARAMETERS:\n" +
            "- Required: unaffectedParent1, unaffectedParent2, proband (sample IDs), chromosome (1-22, X, Y), start, end (GRCh38 coordinates)\n" +
            "- Filters: ALL filters are combined with AND logic\n" +
            "- CSV parameters: OR logic. Example: impact='HIGH,MODERATE' returns variants with HIGH OR MODERATE impact\n" +
            "- Pagination: skip, limit (max=50)\n\n" +

            "RETURNS: Refer to the Output Schema for field definitions. Empty array [] if no matches.",
        outputSchema = @Tool.OutputSchema(
            from = VariantView.class,
            generator = VariantArraySchemaGenerator.class
        )
    )
    public Map<String, List<VariantView>> homRecessiveInTrio(
                @ToolArg(description = "sample id for unaffected parent 1") String unaffectedParent1,
                @ToolArg(description = "sample id for unaffected parent 2") String unaffectedParent2,
                @ToolArg(description = "sample id for proband") String proband,
                @ToolArg(description = CHROMOSOME_DESC) String chromosome,
                @ToolArg(description = START_DESC) int start,
                @ToolArg(description = END_DESC) int end,
                @ToolArg(description = REF_DESC, required = false) String refAllele,
                @ToolArg(description = ALT_DESC, required = false) String altAllele,
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
                @ToolArg(description = EXMALE_DESC, required = false) Boolean excludeMales,
                @ToolArg(description = EXFEMALE_DESC, required = false) Boolean excludeFemales,
                @ToolArg(description = MINLEN_DESC, required = false) Integer minVariantLengthBp,
                @ToolArg(description = MAXLEN_DESC, required = false) Integer maxVariantLengthBp,
                @ToolArg(description = SKIP_DESC, required = false) Integer skip,
                @ToolArg(description = LIM_DESC, required = false) Integer limit) {

        List<Variant> variants = client.selectHomRecessive(unaffectedParent1, unaffectedParent2, proband, chromosome, start, end, refAllele, altAllele,
                        minVariantLengthBp, maxVariantLengthBp, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales, afLessThan,
                        afGreaterThan, gnomadGenomeAfLessThan, gnomadGenomeAfGreaterThan, gnomadExomeAfLessThan, gnomadExomeAfGreaterThan,
                        vepImpact, vepBiotype, vepFeature, vepVariantType, vepConsequences, alphaMissenseClass, alphaMissenseScoreLessThan,
                        alphaMissenseScoreGreaterThan, clinSignificance, skip, limit);

        List<VariantView> vv = variants.stream()
            .map(VariantView::fromGrpc)
            .toList();

        return Map.of("variants", vv);
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
    public KinshipResult getKinshipDegree(
        @ToolArg(description = "First sample ID") String sample1,
        @ToolArg(description = "Second sample ID") String sample2 ) {

        return new KinshipResult(client.kinship(sample1, sample2));
    }
}
