
∆ƒ
dnaerys.protoorg.dnaerys.cluster.grpc"
HealthRequest"(
HealthResponse
status (	Rstatus"
ClusterNodesRequest"†
ClusterNodesResponse!
active_nodes (	RactiveNodes%
inactive_nodes (	RinactiveNodes
total_nodes (R
totalNodes

elapsed_ms (R	elapsedMs"F
DatasetInfoRequest0
return_samples_names (RreturnSamplesNames"ó
DatasetInfoResponse:
cohorts (2 .org.dnaerys.cluster.grpc.CohortRcohorts#
samples_total (RsamplesTotal#
females_total (RfemalesTotal
males_total (R
malesTotal%
variants_total (RvariantsTotalA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly
rto (Rrto/
prs (2.org.dnaerys.cluster.grpc.PRSRprs
	timestamp	 (	R	timestamp
data_format
 (R
dataFormat
notes (	Rnotes
rings_total (R
ringsTotal

elapsed_ms (R	elapsedMs
node_id (	RnodeId"é
Cohort
cohort_name (	R
cohortName#
samples_count (RsamplesCount!
female_count (RfemaleCount

male_count (R	maleCount0
female_samples_names (	RfemaleSamplesNames,
male_samples_names (	RmaleSamplesNames
	synthetic (R	synthetic"O
PRS
name (	Rname
desc (	Rdesc 
cardinality (Rcardinality"∞	
AnnotationsH
variant_type (2%.org.dnaerys.cluster.grpc.VariantTypeRvariantTypeH
feature_type (2%.org.dnaerys.cluster.grpc.FeatureTypeRfeatureType<
bio_type (2!.org.dnaerys.cluster.grpc.BioTypeRbioTypeG
consequence (2%.org.dnaerys.cluster.grpc.ConsequenceRconsequence8
impact (2 .org.dnaerys.cluster.grpc.ImpactRimpactD
clinsgn (2*.org.dnaerys.cluster.grpc.ClinSignificanceRclinsgn
af_lt (RafLt
af_gt (RafGt-
gnomad_exomes_af_lt	 (RgnomadExomesAfLt-
gnomad_exomes_af_gt
 (RgnomadExomesAfGt/
gnomad_genomes_af_lt (RgnomadGenomesAfLt/
gnomad_genomes_af_gt (RgnomadGenomesAfGt2
sift (2.org.dnaerys.cluster.grpc.SIFTRsift>
polyphen (2".org.dnaerys.cluster.grpc.PolyPhenRpolyphen
cadd_raw_lt (R	caddRawLt
cadd_raw_gt (R	caddRawGt"
cadd_phred_lt (RcaddPhredLt"
cadd_phred_gt (RcaddPhredGt
am_score_lt (R	amScoreLt
am_score_gt (R	amScoreGtB
am_class (2'.org.dnaerys.cluster.grpc.AlphaMissenseRamClass$
biallelicOnly (RbiallelicOnly*
multiallelicOnly (RmultiallelicOnly"
excludeMales (RexcludeMales&
excludeFemales (RexcludeFemales"æ
AllelesInRegionRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"É
AllelesInBracketRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
	start_min (RstartMin
	start_max (RstartMax
end_min (RendMin
end_max (RendMax
ref (	Rref
alt (	Ralt
hom (Rhom
het	 (Rhet7
ann
 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"¶
 AllelesInBracketInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
	start_min (RstartMin
	start_max (RstartMax
end_min (RendMin
end_max (RendMax
ref (	Rref
alt (	Ralt
hom (Rhom
het	 (Rhet7
ann
 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
samples (	Rsamples
skip (Rskip
limit (Rlimit"·
AllelesInRegionInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly
samples
 (	Rsamples*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"ƒ
AllelesInMultiRegionsRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"Á
%AllelesInMultiRegionsInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly
samples
 (	Rsamples*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"˜
AllelesResponse=
variants (2!.org.dnaerys.cluster.grpc.VariantRvariants-
incomplete_cluster (RincompleteCluster
affected (Raffected

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"É
Variant6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
af (Raf
ac (Rac
an (Ran
homc	 (Rhomc
hetc
 (Rhetc
misc (Rmisc
homfc (Rhomfc
hetfc (Rhetfc
misfc (Rmisfc
gnomADe (RgnomADe
gnomADg (RgnomADg
cadd_raw (RcaddRaw

cadd_phred (R	caddPhred
am_score (RamScore
amino_acids (	R
aminoAcids
	biallelic (R	biallelic"â
AllelesWithStatsResponseF
variants (2*.org.dnaerys.cluster.grpc.VariantWithStatsRvariants-
incomplete_cluster (RincompleteCluster
affected (Raffected

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"≠
VariantWithStats;
variant (2!.org.dnaerys.cluster.grpc.VariantRvariant
vaf (Rvaf
vac (Rvac
van (Rvan
vhomc (Rvhomc
vhetc (Rvhetc
vhomfc (Rvhomfc
vhetfc (Rvhetfc
phwe	 (Rphwe
pchi2
 (Rpchi2
or (Ror
ibc (Ribc"K
TopNchi2Request
n (Rn
samples (	Rsamples
seq (Rseq"0
TopNHWERequest
n (Rn
seq (Rseq"æ
SamplesInRegionRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"ƒ
SamplesInMultiRegionsRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"◊
SamplesHomRefRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
position (RpositionA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly
skip (Rskip
limit (Rlimit"“
SamplesResponse
samples (	Rsamples-
incomplete_cluster (RincompleteCluster
affected (Raffected

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"ﬂ
DeNovoRequest
parent1 (	Rparent1
parent2 (	Rparent2
proband (	Rproband6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt7
ann	 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly
 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"ì
HetDominantRequest'
affected_parent (	RaffectedParent+
unaffected_parent (	RunaffectedParent%
affected_child (	RaffectedChild6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt7
ann	 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly
 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"ú
HomRecessiveRequest-
unaffected_parent1 (	RunaffectedParent1-
unaffected_parent2 (	RunaffectedParent2%
affected_child (	RaffectedChild6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt7
ann	 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly
 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
skip (Rskip
limit (Rlimit"ú

PRSRequest
prs_name (	RprsName
cohort_name (	R
cohortName
samples (	Rsamples
dominant (Rdominant
	recessive (R	recessive"‚
PRSResponse
prs_name (	RprsNameJ
sample_scores (2%.org.dnaerys.cluster.grpc.SampleScoreRsampleScores
dominant (Rdominant
	recessive (R	recessive'
prs_cardinality (RprsCardinality-
incomplete_cluster (RincompleteCluster

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id	 (	RnodeId"Ê
SampleScore
sample (	Rsample

scores_sum (R	scoresSum-
hethom_cardinality (RhethomCardinality'
ref_cardinality (RrefCardinality'
mis_cardinality (RmisCardinality
imputed_sum (R
imputedSum"Ù
FstatXRequest
cohort_name (	R
cohortName
samples (	Rsamples#
aaf_threshold (RaafThreshold)
female_threshold (RfemaleThreshold%
male_threshold (RmaleThreshold
include_par (R
includePar
seq (Rseq"æ
SexMismatchResponseK
mismatch_males (2$.org.dnaerys.cluster.grpc.SampleStatRmismatchMalesO
mismatch_females (2$.org.dnaerys.cluster.grpc.SampleStatRmismatchFemales-
incomplete_cluster (RincompleteCluster

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"Å

SampleStat
sample (	Rsample!
reported_sex (	RreportedSex!
observed_sex (	RobservedSex
f_stat (RfStat"ó
FstatXResponse:
males (2$.org.dnaerys.cluster.grpc.SampleStatRmales>
females (2$.org.dnaerys.cluster.grpc.SampleStatRfemales-
incomplete_cluster (RincompleteCluster

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"º
KinshipRequest
cohort_name (	R
cohortName
samples (	Rsamples?
degree (2'.org.dnaerys.cluster.grpc.KinshipDegreeRdegree
	threshold (R	threshold
seq (Rseq"Y
KinshipDuoRequest
sample1 (	Rsample1
sample2 (	Rsample2
seq (Rseq"”
KinshipTrioRequest
sample1 (	Rsample1
sample2 (	Rsample2
sample3 (	Rsample3?
degree (2'.org.dnaerys.cluster.grpc.KinshipDegreeRdegree
	threshold (R	threshold
seq (Rseq"’
KinshipResponse7
rel (2%.org.dnaerys.cluster.grpc.RelatednessRrel-
incomplete_cluster (RincompleteCluster

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"õ
Relatedness
sample1 (	Rsample1
sample2 (	Rsample2?
degree (2'.org.dnaerys.cluster.grpc.KinshipDegreeRdegree
phi_bwf (RphiBwf"«
SampleKinshipRequest

sample_vcf (	R	sampleVcf
cohort_name (	R
cohortName?
degree (2'.org.dnaerys.cluster.grpc.KinshipDegreeRdegree
	threshold (R	threshold
seq (Rseq"â
SampleKinshipResponse@
rel (2..org.dnaerys.cluster.grpc.RelatednessPerSampleRrel#
accepted_snvs (RacceptedSnvs-
incomplete_cluster (RincompleteCluster

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"ç
RelatednessPerSample
sample (	Rsample?
degree (2'.org.dnaerys.cluster.grpc.KinshipDegreeRdegree
phi_bwf (RphiBwf
common_loci (R
commonLoci
nHetS1 (RnHetS1
nHetS2 (RnHetS2
nHetS1S2 (RnHetS1S2
nHomOp (RnHomOp"ô
CountAllelesInRegionRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength"º
$CountAllelesInRegionInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength
samples (	Rsamples"ﬁ
CountAllelesInBracketRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
	start_min (RstartMin
	start_max (RstartMax
end_min (RendMin
end_max (RendMax
ref (	Rref
alt (	Ralt
hom (Rhom
het	 (Rhet7
ann
 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength"Å
%CountAllelesInBracketInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
	start_min (RstartMin
	start_max (RstartMax
end_min (RendMin
end_max (RendMax
ref (	Rref
alt (	Ralt
hom (Rhom
het	 (Rhet7
ann
 (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength (RvariantMinLength*
variantMaxLength (RvariantMaxLength
samples (	Rsamples"ü
!CountAllelesInMultiRegionsRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength"¬
*CountAllelesInMultiRegionsInSamplesRequest6
chr (2$.org.dnaerys.cluster.grpc.ChromosomeRchr
start (Rstart
end (Rend
ref (	Rref
alt (	Ralt
hom (Rhom
het (Rhet7
ann (2%.org.dnaerys.cluster.grpc.AnnotationsRannA
assembly	 (2%.org.dnaerys.cluster.grpc.RefAssemblyRassembly*
variantMinLength
 (RvariantMinLength*
variantMaxLength (RvariantMaxLength
samples (	Rsamples"”
CountAllelesResponse
count (Rcount-
incomplete_cluster (RincompleteCluster
affected (Raffected

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId"”
CountSamplesResponse
count (Rcount-
incomplete_cluster (RincompleteCluster
affected (Raffected

elapsed_ms (R	elapsedMs"
elapsed_db_ms (RelapsedDbMs
node_id (	RnodeId*?
RefAssembly
ASSEMBLY_UNSPECIFIED 

GRCh37

GRCh38*…

Chromosome
CHROMOSOME_UNSPECIFIED 	
CHR_1	
CHR_2	
CHR_3	
CHR_4	
CHR_5	
CHR_6	
CHR_7	
CHR_8	
CHR_9	

CHR_10


CHR_11

CHR_12

CHR_13

CHR_14

CHR_15

CHR_16

CHR_17

CHR_18

CHR_19

CHR_20

CHR_21

CHR_22	
CHR_X	
CHR_Y

CHR_MT*√
VariantType
VARIANTTYPE_UNSPECIFIED 
SNV
	INSERTION
DELETION	
INDEL
SUBSTITUTION
	INVERSION
TRANSLOCATION
DUPLICATION
ALU_INSERTION	!
COMPLEX_STRUCTURAL_ALTERATION

COMPLEX_SUBSTITUTION
COPY_NUMBER_GAIN
COPY_NUMBER_LOSS
COPY_NUMBER_VARIATION
INTERCHROMOSOMAL_BREAKPOINT"
INTERCHROMOSOMAL_TRANSLOCATION
INTRACHROMOSOMAL_BREAKPOINT"
INTRACHROMOSOMAL_TRANSLOCATION
LOSS_OF_HETEROZYGOSITY
MOBILE_ELEMENT_DELETION
MOBILE_ELEMENT_INSERTION
NOVEL_SEQUENCE_INSERTION!
SHORT_TANDEM_REPEAT_VARIATION
TANDEM_DUPLICATION	
PROBE
ALU_DELETION
HERV_DELETION
HERV_INSERTION
LINE1_DELETION
LINE1_INSERTION
SVA_DELETION
SVA_INSERTION %
!COMPLEX_CHROMOSOMAL_REARRANGEMENT!
SEQUENCE_ALTERATION"*c
FeatureType
FEATURETYPE_UNSPECIFIED 

TRANSCRIPT
REGULATORYFEATURE
MOTIFFEATURE*·
BioType
BIOTYPE_UNSPECIFIED 
PROCESSED_TRANSCRIPT

LNCRNA
	ANTISENSE
MACRO_LNCRNA

NON_CODING
RETAINED_INTRON
SENSE_INTRONIC
SENSE_OVERLAPPING
LINCRNA		
NCRNA
	
MIRNA
MISCRNA	
PIRNA
RRNA	
SIRNA	
SNRNA

SNORNA
TRNA
VAULTRNA
PROTEIN_CODING

PSEUDOGENE
IG_PSEUDOGENE
POLYMORPHIC_PSEUDOGENE
PROCESSED_PSEUDOGENE
TRANSCRIBED_PSEUDOGENE
TRANSLATED_PSEUDOGENE
UNITARY_PSEUDOGENE
UNPROCESSED_PSEUDOGENE
READTHROUGH
STOP_CODON_READTHROUGH
TEC
TR_GENE 
	TR_C_GENE!
	TR_D_GENE"
	TR_J_GENE#
	TR_V_GENE$
IG_GENE%
	IG_C_GENE&
	IG_D_GENE'
	IG_J_GENE(
	IG_V_GENE)
NONSENSE_MEDIATED_DECAY*
PROMOTER+
PROMOTER_FLANKING_REGION,
ENHANCER-
CTCF_BINDING_SITE.
OPEN_CHROMATIN_REGION/*ã	
Consequence
CONSEQUENCE_UNSPECIFIED 
TRANSCRIPT_ABLATION
SPLICE_ACCEPTOR_VARIANT
SPLICE_DONOR_VARIANT
STOP_GAINED
FRAMESHIFT_VARIANT
	STOP_LOST

START_LOST
TRANSCRIPT_AMPLIFICATION
INFRAME_INSERTION	
INFRAME_DELETION

MISSENSE_VARIANT
PROTEIN_ALTERING_VARIANT
SPLICE_REGION_VARIANT%
!INCOMPLETE_TERMINAL_CODON_VARIANT
START_RETAINED_VARIANT
STOP_RETAINED_VARIANT
SYNONYMOUS_VARIANT
CODING_SEQUENCE_VARIANT
MATURE_MIRNA_VARIANT
FIVE_PRIME_UTR_VARIANT
THREE_PRIME_UTR_VARIANT&
"NON_CODING_TRANSCRIPT_EXON_VARIANT
INTRON_VARIANT
NMD_TRANSCRIPT_VARIANT!
NON_CODING_TRANSCRIPT_VARIANT
UPSTREAM_GENE_VARIANT
DOWNSTREAM_GENE_VARIANT
TFBS_ABLATION
TFBS_AMPLIFICATION
TF_BINDING_SITE_VARIANT
REGULATORY_REGION_ABLATION#
REGULATORY_REGION_AMPLIFICATION 
FEATURE_ELONGATION!
REGULATORY_REGION_VARIANT"
FEATURE_TRUNCATION#
INTERGENIC_VARIANT$'
#SPLICE_POLYPYRIMIDINE_TRACT_VARIANT%!
SPLICE_DONOR_5TH_BASE_VARIANT&
SPLICE_DONOR_REGION_VARIANT'
CODING_TRANSCRIPT_VARIANT(
SEQUENCE_VARIANT)*O
Impact
IMPACT_UNSPECIFIED 
HIGH
MODERATE
LOW
MODIFIER*<
SIFT
SIFT_UNSPECIFIED 
	TOLERATED
DELETERIOUS*k
PolyPhen
POLYPHEN_UNSPECIFIED 

BENIGN
POSSIBLY_DAMAGING
PROBABLY_DAMAGING
UNKNOWN*—
ClinSignificance
CLNSIG_UNSPECIFIED 
CLNSIG_BENIGN
LIKELY_BENIGN
UNCERTAIN_SIGNIFICANCE
LIKELY_PATHOGENIC

PATHOGENIC
DRUG_RESPONSE
ASSOCIATION
RISK_FACTOR

PROTECTIVE	
AFFECTS

CONFERS_SENSITIVITY
CONFLICTING_INTERPRETATIONS
NOT_PROVIDED	
OTHER$
 LIKELY_PATHOGENIC_LOW_PENETRANCE
PATHOGENIC_LOW_PENETRANCE
UNCERTAIN_RISK_ALLELE
LIKELY_RISK_ALLELE
ESTABLISHED_RISK_ALLELE*e
AlphaMissense
AM_UNSPECIFIED 
AM_LIKELY_BENIGN
AM_LIKELY_PATHOGENIC
AM_AMBIGUOUS*Ö
KinshipDegree
KINSHIP_UNSPECIFIED 
TWINS_MONOZYGOTIC
FIRST_DEGREE
SECOND_DEGREE
THIRD_DEGREE
	UNRELATED2ª#
DnaerysService]
Health'.org.dnaerys.cluster.grpc.HealthRequest(.org.dnaerys.cluster.grpc.HealthResponse" o
ClusterNodes-.org.dnaerys.cluster.grpc.ClusterNodesRequest..org.dnaerys.cluster.grpc.ClusterNodesResponse" l
DatasetInfo,.org.dnaerys.cluster.grpc.DatasetInfoRequest-.org.dnaerys.cluster.grpc.DatasetInfoResponse" y
SelectVariantsInRegion0.org.dnaerys.cluster.grpc.AllelesInRegionRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0Ä
CountVariantsInRegion5.org.dnaerys.cluster.grpc.CountAllelesInRegionRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" ã
SelectVariantsInRegionInSamples9.org.dnaerys.cluster.grpc.AllelesInRegionInSamplesRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0í
CountVariantsInRegionInSamples>.org.dnaerys.cluster.grpc.CountAllelesInRegionInSamplesRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" ù
(SelectVariantsInRegionInSamplesWithStats9.org.dnaerys.cluster.grpc.AllelesInRegionInSamplesRequest2.org.dnaerys.cluster.grpc.AllelesWithStatsResponse" 0{
SelectVariantsInBracket1.org.dnaerys.cluster.grpc.AllelesInBracketRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0Ç
CountVariantsInBracket6.org.dnaerys.cluster.grpc.CountAllelesInBracketRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" ç
 SelectVariantsInBracketInSamples:.org.dnaerys.cluster.grpc.AllelesInBracketInSamplesRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0î
CountVariantsInBracketInSamples?.org.dnaerys.cluster.grpc.CountAllelesInBracketInSamplesRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" Ö
SelectVariantsInMultiRegions6.org.dnaerys.cluster.grpc.AllelesInMultiRegionsRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0å
CountVariantsInMultiRegions;.org.dnaerys.cluster.grpc.CountAllelesInMultiRegionsRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" ó
%SelectVariantsInMultiRegionsWithStats6.org.dnaerys.cluster.grpc.AllelesInMultiRegionsRequest2.org.dnaerys.cluster.grpc.AllelesWithStatsResponse" 0ó
%SelectVariantsInMultiRegionsInSamples?.org.dnaerys.cluster.grpc.AllelesInMultiRegionsInSamplesRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0û
$CountVariantsInMultiRegionsInSamplesD.org.dnaerys.cluster.grpc.CountAllelesInMultiRegionsInSamplesRequest..org.dnaerys.cluster.grpc.CountAllelesResponse" ©
.SelectVariantsInMultiRegionsInSamplesWithStats?.org.dnaerys.cluster.grpc.AllelesInMultiRegionsInSamplesRequest2.org.dnaerys.cluster.grpc.AllelesWithStatsResponse" 0i
TopNHWE(.org.dnaerys.cluster.grpc.TopNHWERequest2.org.dnaerys.cluster.grpc.AllelesWithStatsResponse" k
TopNchi2).org.dnaerys.cluster.grpc.TopNchi2Request2.org.dnaerys.cluster.grpc.AllelesWithStatsResponse" v
SelectSamplesInRegion0.org.dnaerys.cluster.grpc.SamplesInRegionRequest).org.dnaerys.cluster.grpc.SamplesResponse" z
CountSamplesInRegion0.org.dnaerys.cluster.grpc.SamplesInRegionRequest..org.dnaerys.cluster.grpc.CountSamplesResponse" Ç
SelectSamplesInMultiRegions6.org.dnaerys.cluster.grpc.SamplesInMultiRegionsRequest).org.dnaerys.cluster.grpc.SamplesResponse" Ü
CountSamplesInMultiRegions6.org.dnaerys.cluster.grpc.SamplesInMultiRegionsRequest..org.dnaerys.cluster.grpc.CountSamplesResponse" x
SelectSamplesHomReference..org.dnaerys.cluster.grpc.SamplesHomRefRequest).org.dnaerys.cluster.grpc.SamplesResponse" |
CountSamplesHomReference..org.dnaerys.cluster.grpc.SamplesHomRefRequest..org.dnaerys.cluster.grpc.CountSamplesResponse" f
SelectDeNovo'.org.dnaerys.cluster.grpc.DeNovoRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0p
SelectHetDominant,.org.dnaerys.cluster.grpc.HetDominantRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0r
SelectHomRecessive-.org.dnaerys.cluster.grpc.HomRecessiveRequest).org.dnaerys.cluster.grpc.AllelesResponse" 0T
Prs$.org.dnaerys.cluster.grpc.PRSRequest%.org.dnaerys.cluster.grpc.PRSResponse" l
SexMismatchCheck'.org.dnaerys.cluster.grpc.FstatXRequest-.org.dnaerys.cluster.grpc.SexMismatchResponse" ]
FstatX'.org.dnaerys.cluster.grpc.FstatXRequest(.org.dnaerys.cluster.grpc.FstatXResponse" `
Kinship(.org.dnaerys.cluster.grpc.KinshipRequest).org.dnaerys.cluster.grpc.KinshipResponse" f

KinshipDuo+.org.dnaerys.cluster.grpc.KinshipDuoRequest).org.dnaerys.cluster.grpc.KinshipResponse" h
KinshipTrio,.org.dnaerys.cluster.grpc.KinshipTrioRequest).org.dnaerys.cluster.grpc.KinshipResponse" r
SampleKinship..org.dnaerys.cluster.grpc.SampleKinshipRequest/.org.dnaerys.cluster.grpc.SampleKinshipResponse" BBDnaerysProtoPbproto3