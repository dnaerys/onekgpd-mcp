/*
 * Copyright © 2025 Dmitry Degrave
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

import org.dnaerys.client.DnaerysClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@SuppressWarnings("unused")
@ApplicationScoped
public class OneKGPMCPServer {

    private final DnaerysClient client = new DnaerysClient();

    @Startup
    void init() { Log.info("Starting Dnaerys OneKGPd MCP server..."); }

    @Tool(description = "Returns number of samples in 1000 Genomes Project")
    public Long countSamplesTotal() {
        return client.countSamplesTotal();
    }

    @Tool(description = "Returns number of female samples in 1000 Genomes Project")
    public Long countFemaleSamplesTotal() {
        return client.countFemaleSamplesTotal();
    }

    @Tool(description = "Returns number of male samples in 1000 Genomes Project")
    public Long countMaleSamplesTotal() {
        return client.countMaleSamplesTotal();
    }

    @Tool(description = "Returns all sample ID in 1000 Genomes Project")
    public List<String> sampleIds() {
        return client.samplesIds();
    }

    @Tool(description = "Returns all female samples ID in 1000 Genomes Project")
    public List<String> femaleSamplesIds() {
        return client.femaleSamplesIds();
    }

    @Tool(description = "Returns all male samples ID in 1000 Genomes Project")
    public List<String> maleSamplesIds() {
        return client.maleSamplesIds();
    }

    @Tool(description = "Returns number of variants in 1000 Genomes Project")
    public Long variantsTotal() {
        return client.variantsTotal();
    }

    @Tool(description = "Returns number of nodes in database cluster")
    public Long nodesTotal() {
        return client.nodesTotal();
    }

    @Tool(description = "Returns number of variants in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact,
                        biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of Homozygous variants in a region 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countHomozygousVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact,
                        biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of Heterozygous variants in a region 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countHeterozygousVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.countVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact,
                        biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of variants in sample in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of Homozygous variants in sample in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countHomozygousVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of Heterozygous variants in sample in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countHeterozygousVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.countVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns only Homozygous variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectHomozygousVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns only Heterozygous variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectHeterozygousVariantsInRegion(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.selectVariantsInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns variants in sample in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense,
                        clinSignificance, skip, limit);
    }

    @Tool(description = "Returns only Homozygous variants in sample in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectHomozygousVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns only Heterozygous variants in sample in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Sample is defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> selectHeterozygousVariantsInRegionInSample(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "sample id") String sampleId,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.selectVariantsInRegionInSample(chromosome, start, end, sampleId, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns number of samples which have Homozygous or Heterozygous variants in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countSamplesWithVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of samples which have Homozygous variants in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countSamplesWithHomVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns number of samples which have Heterozygous variants in a region in 1000 Genomes Project. " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public Long countSamplesWithHetVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.countSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns unique samples which have Homozygous or Heterozygous variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no samples are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public List<String> selectSamplesWithVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = true;
        return client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns unique samples which have Homozygous variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no samples are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public List<String> selectSamplesWithHomVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = true;
        boolean selectHet = false;
        return client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns unique samples which have Heterozygous variants in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no samples are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction.")
    public List<String> selectSamplesWithHetVariants(
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance) {
        boolean selectHom = false;
        boolean selectHet = true;
        return client.selectSamplesInRegion(chromosome, start, end, selectHom, selectHet, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance);
    }

    @Tool(description = "Returns De Novo variants in a proband in trio in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Parents and proband samples are defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> deNovoInTrio(
                            @ToolArg(description = "sample id for parent 1") String parent1,
                            @ToolArg(description = "sample id for parent 2") String parent2,
                            @ToolArg(description = "sample id for proband") String proband,
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        return client.selectDeNovo(parent1, parent2, proband, chromosome, start, end, refAllele, altAllele, variantMinLength,
                        variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype,
                        feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns heterozygous dominant variants in affected child in a trio in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Parents and proband samples are defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> hetDominantInTrio(
                            @ToolArg(description = "sample id for affected parent") String affectedParent,
                            @ToolArg(description = "sample id for unaffected parent") String unaffectedParent,
                            @ToolArg(description = "sample id for proband") String proband,
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        return client.selectHetDominant(affectedParent, unaffectedParent, proband, chromosome, start, end, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns homozygous recessive variants in affected child in a trio in a region in 1000 Genomes Project. " +
                        "Returns an empty json if no variants are found (empty json is NOT an error). " +
                        "A region is defined by chromosome ID, start and end coordinates in GRCh38 assembly. " +
                        "Chromosome ID is in a form of 1, 2, ..., 22, X, Y. " +
                        "Parents and proband samples are defined by sample ID. " +
                        "Optional ALT and REF alleles can be provided as selection parameters. " +
                        "Optional filtering by 1000 Genomes Project AF. " +
                        "Optional filtering by gnomAD AF. " +
                        "Optional filtering by VEP impact terms. " +
                        "Optional filtering by VEP biotypes terms. " +
                        "Optional filtering by VEP feature types terms. " +
                        "Optional filtering by Sequence Ontology Variant Classes (types) terms. " +
                        "Optional filtering by Sequence Ontology variant consequences. " +
                        "Optional filtering by AlphaMissense class. " +
                        "Optional filtering by ClinVar Clinical Significance annotations. " +
                        "Optional filtering biallelic variants. " +
                        "If more than one filtering criteria of different types is provided, relation between them is logical conjunction. " +
                        "Use 'skip' and 'limit' parameters for pagination if needed. The Max value for limit = 100.")
    public List<String> homRecessiveInTrio(
                            @ToolArg(description = "sample id for unaffected parent 1") String unaffectedParent1,
                            @ToolArg(description = "sample id for unaffected parent 2") String unaffectedParent2,
                            @ToolArg(description = "sample id for proband") String proband,
                            @ToolArg(description = "chromosome ID, in a form of 1, 2, ..., 22, X, Y, MT") String chromosome,
                            @ToolArg(description = "start of region") int start,
                            @ToolArg(description = "end of region") int end,
                            @ToolArg(description = "reference allele bases (REF)", required = false) String refAllele,
                            @ToolArg(description = "alternative allele bases (ALT)", required = false) String altAllele,
                            @ToolArg(description = "minimal variant length", required = false) Integer variantMinLength,
                            @ToolArg(description = "maximal variant length", required = false) Integer variantMaxLength,
                            @ToolArg(description = "select biallelic variants only", required = false) Boolean biallelicOnly,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF < afLessThan",
                                required = false) Float afLessThan,
                            @ToolArg(description = "select variants with 1000 Genomes Project AF > afGreaterThan",
                                required = false) Float afGreaterThan,
                            @ToolArg(description = "select variants with gnomAD AF < gnomadAfLessThan",
                                required = false) Float gnomadAfLessThan,
                            @ToolArg(description = "select variants with gnomAD AF > gnomadAfGreaterThan",
                                required = false) Float gnomadAfGreaterThan,
                            @ToolArg(description = "A comma separated list of VEP impact terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "HIGH, MODERATE, LOW, MODIFIER",
                                required = false) String impact,
                            @ToolArg(description = "A comma separated list of VEP biotypes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "PROCESSED_TRANSCRIPT, LNCRNA, ANTISENSE, MACRO_LNCRNA, NON_CODING, RETAINED_INTRON, " +
                                "SENSE_INTRONIC, SENSE_OVERLAPPING, LINCRNA, NCRNA, MIRNA, MISCRNA, PIRNA, RRNA, SIRNA, " +
                                "SNRNA, SNORNA, TRNA, VAULTRNA, PROTEIN_CODING, PSEUDOGENE, IG_PSEUDOGENE, READTHROUGH, " +
                                "STOP_CODON_READTHROUGH, TEC, TR_GENE, IG_GENE, NONSENSE_MEDIATED_DECAY",
                                required = false) String biotype,
                            @ToolArg(description = "A comma separated list of VEP feature types terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT, REGULATORYFEATURE, MOTIFFEATURE",
                                required = false) String feature,
                            @ToolArg(description = "A comma separated list of Sequence Ontology Variant Classes terms. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "SNV, INSERTION, DELETION, INDEL, SUBSTITUTION, INVERSION, TRANSLOCATION, DUPLICATION, SEQUENCE_ALTERATION",
                                required = false) String variantType,
                            @ToolArg(description = "A comma separated list of Sequence Ontology variant consequences. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "TRANSCRIPT_ABLATION, SPLICE_ACCEPTOR_VARIANT, SPLICE_DONOR_VARIANT, STOP_GAINED, FRAMESHIFT_VARIANT, " +
                                "STOP_LOST, START_LOST, TRANSCRIPT_AMPLIFICATION, INFRAME_INSERTION, INFRAME_DELETION, MISSENSE_VARIANT, " +
                                "PROTEIN_ALTERING_VARIANT, SPLICE_REGION_VARIANT, INCOMPLETE_TERMINAL_CODON_VARIANT, START_RETAINED_VARIANT, " +
                                "STOP_RETAINED_VARIANT, SYNONYMOUS_VARIANT, CODING_SEQUENCE_VARIANT, MATURE_MIRNA_VARIANT, FIVE_PRIME_UTR_VARIANT, " +
                                "THREE_PRIME_UTR_VARIANT, NON_CODING_TRANSCRIPT_EXON_VARIANT, INTRON_VARIANT, NMD_TRANSCRIPT_VARIANT, " +
                                "NON_CODING_TRANSCRIPT_VARIANT, UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT, TFBS_ABLATION, TFBS_AMPLIFICATION, " +
                                "TF_BINDING_SITE_VARIANT, REGULATORY_REGION_ABLATION, REGULATORY_REGION_AMPLIFICATION, FEATURE_ELONGATION, " +
                                "REGULATORY_REGION_VARIANT, FEATURE_TRUNCATION, INTERGENIC_VARIANT, SPLICE_POLYPYRIMIDINE_TRACT_VARIANT, " +
                                "SPLICE_DONOR_5TH_BASE_VARIANT, SPLICE_DONOR_REGION_VARIANT, CODING_TRANSCRIPT_VARIANT, SEQUENCE_VARIANT",
                                required = false) String consequences,
                            @ToolArg(description = "A comma separated list of AlphaMissense classes. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "LIKELY_BENIGN, LIKELY_PATHOGENIC, AMBIGUOUS",
                                required = false) String alphaMissense,
                            @ToolArg(description = "A comma separated list of ClinVar Clinical Significance annotations. " +
                                "If more than one value provided, relation between them is logical disjunction, " +
                                "i.e. selects variants which have ANY of annotations provided. Possible values: " +
                                "CLNSIG_BENIGN, LIKELY_BENIGN, UNCERTAIN_SIGNIFICANCE, LIKELY_PATHOGENIC, PATHOGENIC, " +
                                "DRUG_RESPONSE, ASSOCIATION, RISK_FACTOR, PROTECTIVE, AFFECTS, CONFERS_SENSITIVITY, " +
                                "CONFLICTING_INTERPRETATIONS, LIKELY_PATHOGENIC_LOW_PENETRANCE, PATHOGENIC_LOW_PENETRANCE, " +
                                "UNCERTAIN_RISK_ALLELE, LIKELY_RISK_ALLELE, ESTABLISHED_RISK_ALLELE",
                                required = false) String clinSignificance,
                            @ToolArg(description = "number of items to be skipped in returned result", required = false) Integer skip,
                            @ToolArg(description = "limit items in returned result", required = false) Integer limit) {
        return client.selectHomRecessive(unaffectedParent1, unaffectedParent2, proband, chromosome, start, end, refAllele, altAllele,
                        variantMinLength, variantMaxLength, biallelicOnly, afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan,
                        impact, biotype, feature, variantType, consequences, alphaMissense, clinSignificance, skip, limit);
    }

    @Tool(description = "Returns degree of relatedness (kinship) between samples in 1000 Genomes Project. " +
                        "Samples are defined by sample ID.")
    public String kinship(  @ToolArg(description = "sample id 1") String sample1,
                            @ToolArg(description = "sample id 2") String sample2 ) {
        return client.kinship(sample1, sample2);
    }
}
