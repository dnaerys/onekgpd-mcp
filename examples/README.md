## Examples

### Table of Content
- [Models](#models)
- [Incomplete Penetrance & Genetic Resilience](#incomplete-penetrance--genetic-resilience)
- [Structural Intolerance](#structural-intolerance)
- [Reclassification & AlphaMissense Integration](#reclassification--alphamissense-integration)
- [Oligogenic Signatures](#oligogenic-signatures)
- [Oligogenic Burden](#oligogenic-burden)
- [Protein-Protein Interactions](#protein-protein-interactions)
- [Macromolecular structural complexes](#macromolecular-structural-complexes)
- [Regulatory Variant Impact on Known Disease Genes](#regulatory-variant-impact-on-known-disease-genes)


#### Models

_Opus 4.5’s_ safety filters flagged many questions below, hence _Sonnet 4.5_ answered most of them unless specified otherwise.
Some answers are from _[multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)_,
some with _extended thinking mode_, and some from a single-agent system in normal mode.

---

#### Incomplete Penetrance & Genetic Resilience

> _Titin acts as a molecular spring that gives our heart muscles their elasticity. Certain mutations in TTN, known as
Truncating Variants (TTNtv), are the leading genetic cause of Dilated Cardiomyopathy - a condition where the heart
stretches and fails to pump blood effectively. One in 500 healthy people carries one of these "deadly" mutations but
shows no signs of heart disease. We are searching for the genetic 'shield' that allows healthy individuals to harbor
known pathogenic TTN mutations without developing cardiomyopathy. Locate established pathogenic alleles (specifically
TTNtv in the A-band or high-PSI exons) documented in ClinVar or cardiac literature, that appear in the KGP healthy cohort.
For every healthy carrier identified, scan a 100kb window (the haplotype block) around the pathogenic site. We are looking
for Modifier Variants - secondary missense or regulatory changes - that consistently 'travel' with the pathogenic allele
on the same chromosome. Evaluate statistical significance and biological mechanisms of compensation._

- [vis](https://claude.ai/public/artifacts/a4385d67-fc4d-4224-9593-e298dfb7d65c?fullscreen=true),
[report](https://claude.ai/public/artifacts/bc48355a-eecd-4924-96cd-2e29596b2241?fullscreen=true),
[suppl](https://claude.ai/public/artifacts/3706f27a-fd34-41cd-bab0-de5cfde449a0?fullscreen=true) _(Opus 4.5)_

> _Identify samples in the KGP dataset that are homozygous for variants classified as 'Pathogenic' in ClinVar for severe
autosomal recessive metabolic disorders. For these specific samples, scan their exomes for enrichment of variants in known
suppressor genes or alternative metabolic pathways that might compensate for the primary defect. Propose a mechanism of
compensation based on pathway analysis._

- _reports for_ _[AATD - SERPINA1 - PiZZ](https://claude.ai/public/artifacts/3231b371-a021-479a-8648-5913660968fa?fullscreen=true)_
_and_ _[Cystic Fibrosis & Sickle Cell Disease](https://claude.ai/public/artifacts/4dfe5e97-a30e-4a93-9ca2-bdf0eb40e64c?fullscreen=true)_


> _Select samples carrying known dominant-negative variants in KRT5 or KRT14 genes (Epidermolysis Bullosa) in the KGP.
Search for potential cis- or trans-acting rescue modifiers. Specifically, check if these samples carry variants that promote
the upregulation of the homologous KRT6 or KRT16 genes (paralog compensation). Can you detect a statistically significant
enrichment of 'paralog-boosting' promoter variants in these resilient carriers ?_

- [summary](https://claude.ai/public/artifacts/7077c890-2967-4451-8eec-57197959c314?fullscreen=true), 
[report](https://claude.ai/public/artifacts/dfb9ebd9-cf18-4e69-b4d3-0b825024f5fe?fullscreen=true)


---

#### Structural Intolerance

> _Which regions in XXXX gene are most likely disease-critical, with strong purifying selection, based on available
variation patterns across functional domains in KGP ? Do statistical evaluation._

- results for [CACNA1A](https://claude.ai/public/artifacts/9707df35-ceea-4cb1-a54d-0c392db3c38c?fullscreen=true) ([chart](https://github.com/user-attachments/assets/0e568973-5690-4334-abb9-833808d1fce9)),
[SCN1A](https://claude.ai/public/artifacts/7ce01d30-9bbc-4823-9be5-4d20f51137e1?fullscreen=true),
[SCN2A](https://claude.ai/public/artifacts/9be55bf0-c38f-4aba-bfd3-4361463b713b?fullscreen=true) ([chart](https://github.com/user-attachments/assets/9c1d6c6b-55b7-41a2-be85-b0f9fde08c68)), 
[POLR2A](https://claude.ai/public/artifacts/eeaa2483-a7ca-4f4e-81c3-e9ea435bd438?fullscreen=true) ([chart](https://github.com/user-attachments/assets/7df15795-351b-4066-8a81-fd3f98134f08)), 
[RPL5](https://claude.ai/public/artifacts/75f05627-75e2-4826-a2f0-13d2bd05e7e7?fullscreen=true) ([interactive vis](https://claude.ai/public/artifacts/a7baecef-83e7-463f-a2cf-0dfee441e24a)),
[RPL11](https://claude.ai/public/artifacts/f3fe732b-671b-48f9-956a-439d2d46698b?fullscreen=true),
[RPS26](https://claude.ai/public/artifacts/50c82a35-427e-494c-ab30-8b03b8dafc7c?fullscreen=true)

> _Identify variants in the Trigger Loop of POLR2A. Since this loop must move precisely to add nucleotides,
does the healthy population exclusively carry variants that preserve the 'flexibility' of the loop while
the 'catalytic' residues remain untouched ?_

- almost, albeit not exactly - [informal summary](https://claude.ai/public/artifacts/8cf126fb-d74b-4774-b60f-874268dacfca?fullscreen=true),
[vis](https://github.com/user-attachments/assets/043cdfd3-caf3-463c-83cd-6ae4f5eb1c94)

> _Analyze variants in the CCT/TRiC Chaperonin (The Folding Cage) - a barrel-shaped folding machine that helps proteins
like actin and tubulin reach their final shape. It consists of two rings of eight different subunits (CCT1-CCT8).
Is there a difference in variant pattern in the healthy KGP cohort in these subunits ? E.g., map variants to the
ATP-binding pocket vs. the Substrate-binding pocket across all 8 subunits. Do we see a pattern where 'Healthy'
variation is allowed in the substrate pocket (allowing the cage to adapt to new proteins) but strictly forbidden
in the ATP pocket (the power source)?_

- [report](https://claude.ai/public/artifacts/f38892ce-fb98-415d-ba29-817c6c995854?fullscreen=true) and
[interactive representation](https://claude.ai/public/artifacts/8a3604bb-e6f3-49dc-80ad-0858cae4e5a5?fullscreen=true)

> _Examine the GBA gene (Gaucher disease). Map all missense variants found in the healthy KGP cohort to the Glucocerebrosidase
active site (residues E235 and E340 and the surrounding pocket). Calculate the minimum distance of each variant to the catalytic
glutamate residues. Is there a defined 'sphere of intolerance' (e.g., < 5 Angstroms) around the active site where zero variants
are observed ? At what radial distance does the variant density return to the background rate ?_

- [report](https://claude.ai/public/artifacts/e707ecbc-fec2-4fad-9456-89d2fc34ecae?fullscreen=true),
[variants](https://claude.ai/public/artifacts/91d502e5-1419-42a2-acc6-49c56024dcb3),
[vis](https://github.com/user-attachments/assets/cc35e1d0-8f8c-4d01-9b36-4740e180dc50)

> _In what cardiac related genes, e.g. ion channels, variants in KGP dataset near catalytic residues or
ligand-binding pockets show strong depletion compared to flanking residues (±20 amino acids) ?_
   
- results might be [some](https://claude.ai/public/artifacts/e81fa694-7de5-4fed-b903-e6cb23d02dd9?fullscreen=true)

---

#### Reclassification & AlphaMissense Integration

> _Retrieve all variants in KGP dataset in the voltage-gated sodium channel gene family (SCN1A, SCN2A, SCN5A)
currently classified as 'VUS' in ClinVar. Correlate their 'Likely Pathogenic' AlphaMissense classification
with their frequency in this healthy cohort. Synthesize a reasoned argument to reclassify a subset of these
as 'Likely Benign' based on the logic that pathogenic predictions by AlphaMissense are incompatible with the
observed allele frequency in this healthy population._

- [summary](https://claude.ai/public/artifacts/9a602e37-6795-4979-8b8c-48b052b33501?fullscreen=true),
[report](https://claude.ai/public/artifacts/3888c6d2-9b6d-435c-88f3-fde159e5b241?fullscreen=true),
[variants](https://claude.ai/public/artifacts/f9b6557b-c1e1-414f-ae1c-d62d256cb46a?fullscreen=true)

> _Analyze the distribution of missense variants in the BRCA1 and BRCA2 genes found in the KGP dataset.
Map these variants to the 3D protein structures. Identify specific structural domains (e.g. the BRCT domain vs.
unstructured loops) that tolerate a high density of AlphaMissense-predicted 'ambiguous' variants in this
healthy cohort, effectively creating a 'map of benign tolerance' for future clinical reference._

- [summary](https://claude.ai/public/artifacts/b06a5d2a-45cf-4a55-b045-f453d52b47c7?fullscreen=true),
[report](https://claude.ai/public/artifacts/004375b5-76c6-43b1-9793-05f75ca993b2?fullscreen=true),
[variants](https://claude.ai/public/artifacts/fda1f207-0002-4a22-be11-37f2634f9f75?fullscreen=true),
[ASCII art](https://claude.ai/public/artifacts/36818f98-19fe-4df4-8067-87ee33cac703?fullscreen=true)

> _Retrieve all variants in the KGP dataset annotated as 'High Impact' by VEP but 'Benign' by AlphaMissense.
Analyze the Sequence Ontology context of these variants. Are they predominantly located in alternatively
spliced exons that are non-essential for the canonical transcript ? Synthesize a report on which specific
gene isoforms are dispensable for health based on this discordance._

- [summary](https://claude.ai/public/artifacts/8b0502a8-107c-43fc-a603-01e6c160225b?fullscreen=true),
[report](https://claude.ai/public/artifacts/fcc30868-60c6-4297-a0f9-65ea40fc0f18?fullscreen=true),
[suppl](https://claude.ai/public/artifacts/6405a394-f59a-4421-bbfe-37199cf9f74b?fullscreen=true)


---

#### Oligogenic Signatures

> _Are there patterns of variation in KGP dataset that suggest digenic or oligogenic interactions for Bardet-Biedl syndrome ?
Check variety of combinations and zygosity patterns._

---

#### Oligogenic Burden

> _Calculate the 'Ciliary Mutational Load' for every individual in the KGP dataset. Aggregate all rare, non-synonymous variants
across the entire Bardet-Biedl Syndrome (BBS) gene panel (BBS1 through BBS21). Is there a clear 'cliff' or maximum mutational
burden observed in healthy individuals ? Determine if the healthy cohort contains any 'triallelic' carriers (homozygous at one
locus, heterozygous at another) and model why they do not display the BBS phenotype._

- [report](https://claude.ai/public/artifacts/15fabda9-8a4a-4f0c-9605-ec80298e66f3?fullscreen=true),
[vis](https://github.com/user-attachments/assets/3d42b9da-3d23-48c7-9703-e9599d8d59b0)

---

#### Protein-Protein Interactions

> _Analyze samples in the KGP dataset with missense variants located at the 'hinge' or 'head' domains in Cohesin complex genes
(SMC1A, SMC3, RAD21). Perform a 'co-evolution' analysis - do samples with a destabilizing mutation in the SMC1A head domain
tend to carry a complementary variant in the SMC3 head domain that restores electrostatic compatibility (e.g., a charge swap
from Glu->Lys in one and Lys->Glu in the other) ?_

- results might be _[some](https://claude.ai/public/artifacts/ea605022-296d-446d-989f-a9e7bae5ab6b)_

> _Analyze the Actin-Myosin motor complex. For variants in ACTA1 that map to the interface with Myosin compare the frequency
of these variants against variants that map to the internal structure of the actin monomer. Do the interface variants show a
higher constraint (lower frequency) than internal variants, suggesting that disrupting the force-generating interaction is
less tolerable than minor changes to the monomer's stability ?_

- [report](https://claude.ai/public/artifacts/0218ff12-28f6-427b-a755-e3b7570d88bc?fullscreen=true),
[vis](https://github.com/user-attachments/assets/3fbbf520-9d94-437d-81d3-d21a7ed22a93),
[summary table](https://github.com/user-attachments/assets/65aa5775-1f15-49d9-864b-1f5a6373dd62)

> _Focus on the RAS-RAF interaction in the MAPK signaling pathway. Query KGP dataset for variants in the KRAS effector-binding
loop. For samples carrying variants here, check for 'compensatory' variants in the RAS-binding domain (RBD) of BRAF, ARAF, or CRAF.
Use structural modeling to infer if these variants represent co-evolutionary 'charge-swaps' that maintain a stable binding energy
(ΔG) despite the primary mutation._

- results might be _[none](https://claude.ai/public/artifacts/4ceba8c2-a1c9-4d4a-a9a7-efaa45190ca0?fullscreen=true)_,
[vis](https://github.com/user-attachments/assets/55b67dad-6066-46fc-90c1-4693faa0279c)

---

#### Macromolecular structural complexes

> _The human RNA Exosome (Exo-9 core) is a "dead machine" that acts as a scaffold. In lower organisms the ring itself can degrade RNA.
In humans, the 9-subunit ring has lost all its catalytic teeth and is purely a structural tunnel that guides RNA into the catalytic
subunits (DIS3 or EXOSC10) attached at the bottom. Since RNA is a highly negatively charged polymer, the residues lining this pore
are typically positively charged (Lysine, Arginine), but not too "sticky" or RNA will jam. So, to reach the "shredder" at the bottom
it must slide through a narrow pore formed by the Exo-9 ring._

> _The task: analyse all missense variants in the healthy KGP cohort that map to the internal pore-lining residues of the Exo-9 ring.
Look for 'charge-swap' variants where a positive residue (K, R) is replaced by a negative one (D, E). If an individual is healthy
despite having a 'negative patch' in the tunnel that should repel RNA, do they carry a compensatory variant in the cap subunits
(EXOSC1, 2, 3) that widens the entrance? Use a 3D electrostatic surface map to determine if the 'healthy' cohort maintains a specific
electrostatic gradient._

- [report](https://claude.ai/public/artifacts/b8ce0f2b-6adb-4763-9be9-9c323866a691?fullscreen=true) _(Opus 4.5)_

> _Investigate the genes involved in the Desmosome complex (DSG, DSC, PKP, DSP). For samples in the KGP with a heterozygous LoF
variant in one key component (e.g., DSP), are they depleted of rare missense variants in the interacting components (e.g., PKP1)
compared to the general population ? This would suggest that even a single hit requires the rest of the complex to be "pristine"
when one component is already compromised to maintain mechanical integrity._

- [report](https://claude.ai/public/artifacts/5a9b552f-6acf-45dd-b28e-c1cec8613785?fullscreen=true),
[suppl](https://claude.ai/public/artifacts/c4c5fe5e-efed-42bc-aa89-53bb2fd75b32?fullscreen=true)

> _Treat the 26S Proteasome as a 3D physical object. Map every missense variant in the healthy KGP cohort across all 33+ subunits.
Perform a spatial flux analysis: Are the variants in this healthy population significantly more likely to occur at the 'distal edges'
of the regulatory lid rather than the 'catalytic core' or the 'gating interfaces' ? More importantly, find individuals with a
'high-burden' (3+ rare variants) across the complex. Do you detect inter-subunit compensation ? For example, if a variant in PSMD1
is predicted to weaken a hinge, is there a correlated 'strengthening' variant in the interacting PSMD2 subunit ?
Define the 'mechanical tolerance' of the proteasome based on the maximum cumulative ΔΔG observed in a single healthy individual._

- [report](https://claude.ai/public/artifacts/aea55d37-c8d5-4612-b88c-148629d0b050?fullscreen=true),
[vis](https://github.com/user-attachments/assets/6e8bbec3-e499-4167-96e9-e9b60c25e720) _(Opus 4.5)_

> _The MCM2-7 Complex (The "DNA Helicase Motor") is a molecular masterpiece. It’s a heterohexameric ring where each subunit is
a distinct "gear" in the DNA-unzipping motor. Unlike homomeric rings (where every subunit is the same), this complex is asymmetric.
Each interface between subunits is unique, and they don't all burn ATP at the same rate. The MCM2/5 interface is the "gate" that
must physically open to allow DNA to enter the ring and then snap shut. This is a high-stress mechanical point. Identify individuals
in the healthy KGP cohort carrying missense variants at the MCM2/5 interface. Specifically, look for 'charge-reversal' variants
(e.g., Aspartate to Lysine). In these specific samples, analyze the 'compensatory coupling': do they carry a secondary, reciprocal
charge-reversal variant on the opposing subunit interface that restores the electrostatic 'latch' ?_

- [report](https://claude.ai/public/artifacts/8e875116-9aca-4b70-ac09-e07c4f1b5207?fullscreen=true) _(Opus 4.5)_

- > _Identify individuals in the healthy KGP cohort who carry high-pathogenicity variants in the Walker A or Walker B motifs
(the ATP-burning heart) of any MCM subunit in MCM2-7 Complex. For these individuals, perform a 'Systemic Flux' analysis:
look at their variants in the leading-strand polymerase (POLE) and the sliding clamp (PCNA). Do you detect a signature of
'Coordinated Deceleration' where the motor, the clamp, and the polymerase all carry variants that suggest a slower but
highly-accurate replication fork ?_

    - [report](https://claude.ai/public/artifacts/4ef3cf50-e209-4fd7-9ec3-25c6c8549de7)


#### Regulatory Variant Impact on Known Disease Genes

> _Identify healthy individuals in the KGP dataset carrying ClinVar-validated pathogenic variants in SCN5A, KCNH2, or LDLR.
For each carrier, conduct a cis-regulatory scan within a 70kb window to identify variants in high linkage disequilibrium
that are statistically associated with Haplotype-Specific Expression (HSE) or Allelic Imbalance. Analyze if these secondary
variants disrupt Transcription Factor Binding Sites (TFBS) in proximal promoters, alter uORF-mediated translation kinetics,
or modify mRNA stability motifs in the 3'UTR. Evaluate the hypothesis that resilience is achieved through a "Transcriptional
Damping" mechanism, where the pathogenic allele is preferentially silenced or the wild-type allele is hyper-activated,
ensuring the total pool of functional protein remains above the critical phenotypic threshold._

- SCN5A [transcriptional damping report](https://claude.ai/public/artifacts/8c470f0b-46ac-4cb4-9768-d77a95030a06?fullscreen=true) _(Opus 4.5)_

- > _Building on the SCN5A damping findings, the next phase should investigate if resilience in 150 heterozygous carriers
    is also mediated by trans-acting chaperones that stabilize the functional 50% of sodium channels. Task Statement:
    Identify healthy SCN5A pathogenic carriers from the previous "Transcriptional Damping" cohort and perform a genome-wide
    scan for Trans-acting Proteostatic Modifiers. Search for enrichment of gain-of-function variants or high-expression
    eQTLs in cardiac-specific ion channel chaperones and trafficking regulators, specifically SNTA1 (Syntrophin-alpha 1),
    GPD1L, and RANGRF (MOG1)._
    
    - trans-acting proteostatic modifiers [report](https://claude.ai/public/artifacts/9c4f8fbf-8fe6-4efe-a94f-4ce0ba759da4?fullscreen=true) _(Opus 4.5)_

- similar study for _[KCNH2 and LDLR](https://claude.ai/public/artifacts/cae6d6bf-3f57-489d-976e-c6286983fa7a?fullscreen=true)_  _(Opus 4.5)_


##### Transcriptional damping & friends

> _Transthyretin (TTR) is a critical transport protein that must maintain a stable homotetramer configuration to function;
its dissociation into monomers is the rate-limiting step in systemic amyloidosis, a devastating protein-molding disease.
How do healthy heterozygous carriers of ClinVar-pathogenic TTR interface variants (e.g., V30M equivalents) in KGP cohort
avoid systemic amyloidosis ? Specifically, is this genetic resilience driven by Kinetic Heterodimer Stabilization, where
the mixed wild-type/mutant tetramer is biochemically "locked" into a functional state ? Or is it driven by a Proteostatic
Assembly Checkpoint that selectively degrades unstable tetramers before they can dissociate into amyloidogenic monomers ?_

- [report](https://claude.ai/public/artifacts/0ea73bd5-bc4b-4e78-81e9-0117d0c6dcc8?fullscreen=true),
[suppl](https://claude.ai/public/artifacts/ba98b791-98d8-49bb-8bcf-caee4a9ba805?fullscreen=true),
[fig1](https://github.com/user-attachments/assets/bddf41e4-1211-444d-8c59-09c703c92de8),
[fig2](https://github.com/user-attachments/assets/6c1351a5-2753-42ec-92a1-9a25e7b06271),
[fig3](https://github.com/user-attachments/assets/cd859704-3548-42ae-b2dc-ed6d7bf781d1) 

---

_Feel free to open a PR with your favorite prompts_
