# 1000 Genomes Project Dataset MCP Server

Natural language access to _**1000 Genomes Project dataset**_, hosted online in
_[Dnaerys variant store](https://dnaerys.org/)_
 

Dataset is sequenced & aligned to GRCh38 by _New York Genome Center_
- 2504 unrelated samples from the phase three panel
- additional 698 samples from 602 family trios
    - 3202 samples total (1598 males, 1604 females)
- [dataset details](https://www.internationalgenome.org/data-portal/data-collection/30x-grch38) 

### Key Features

- _real-time_ access to _138 044 724_ unique variants and about _442 billion_ individual genotypes in 3202 samples

- variant, sample, and genotype selection based on coordinates, annotations, zygosity

- filtering by VEP, ClinVar, gnomAD AF and AlphaMissense annotations
  
- filtering by inheritance model (de novo, heterozygous dominant, homozygous recessive)

## Deployments

Remote MCP service is available online via _Streamable HTTP:_

- http://db.dnaerys.org:80/mcp
- https://db.dnaerys.org:443/mcp

For local builds with _stdio_ and _http_ transports see details [below](#installation)

## Architecture

MCP Server is implemented as a Java EE service, accessing _1KGP dataset_ via gRPC calls to public Dnaerys variant store service.

- service implementation is based on [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/)
- provides MCP over _Streamable HTTP_, _HTTP/SSE_ and _STDIO_ transports


## Examples

_Sonnet 4.5_ answered most of the questions below, unless specified otherwise. Some answers are from _[multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)_,
some with _extended thinking mode_, and some from a single-agent system in normal mode.

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

#### Structural Intolerance

> _Which regions in XXXX gene are most likely disease-critical, with strong purifying selection, based on available
variation patterns across functional domains in KGP ? Do statistical evaluation._

- results for [CACNA1A](https://claude.ai/public/artifacts/ac0f76a9-b311-46ef-a377-5387f3da8085?fullscreen=true),
[SCN1A](https://claude.ai/public/artifacts/7ce01d30-9bbc-4823-9be5-4d20f51137e1?fullscreen=true),
[SCN2A](https://claude.ai/public/artifacts/9be55bf0-c38f-4aba-bfd3-4361463b713b?fullscreen=true) ([chart](https://github.com/user-attachments/assets/9c1d6c6b-55b7-41a2-be85-b0f9fde08c68)), 
[POLR2A](https://claude.ai/public/artifacts/eeaa2483-a7ca-4f4e-81c3-e9ea435bd438?fullscreen=true) ([chart](https://github.com/user-attachments/assets/7df15795-351b-4066-8a81-fd3f98134f08)), 
[RPL5](https://claude.ai/public/artifacts/75f05627-75e2-4826-a2f0-13d2bd05e7e7?fullscreen=true) ([interactive vis](https://claude.ai/public/artifacts/a7baecef-83e7-463f-a2cf-0dfee441e24a)),
[RPL11](https://claude.ai/public/artifacts/f3fe732b-671b-48f9-956a-439d2d46698b?fullscreen=true),
[RPS26](https://claude.ai/public/artifacts/50c82a35-427e-494c-ab30-8b03b8dafc7c?fullscreen=true)


> _In what cardiac related genes, e.g. ion channels, variants in KGP dataset near catalytic residues or
ligand-binding pockets show strong depletion compared to flanking residues (±20 amino acids) ?_
   
- [interactive app](https://claude.ai/public/artifacts/e81fa694-7de5-4fed-b903-e6cb23d02dd9?fullscreen=true)


#### Reclassification & AlphaMissense Integration

> _Retrieve all variants in KGP dataset in the voltage-gated sodium channel gene family (SCN1A, SCN2A, SCN5A)
currently classified as 'VUS' in ClinVar. Correlate their 'Likely Pathogenic' AlphaMissense classification
with their frequency in this healthy cohort. Synthesize a reasoned argument to reclassify a subset of these
as 'Likely Benign' based on the logic that pathogenic predictions by AlphaMissense are incompatible with the
observed allele frequency in this healthy population._

- [summary](https://claude.ai/public/artifacts/9a602e37-6795-4979-8b8c-48b052b33501?fullscreen=true),
[report](https://claude.ai/public/artifacts/3888c6d2-9b6d-435c-88f3-fde159e5b241?fullscreen=true),
[variants](https://claude.ai/public/artifacts/f9b6557b-c1e1-414f-ae1c-d62d256cb46a?fullscreen=true)


#### Oligogenic Burden

> _Calculate the 'Ciliary Mutational Load' for every individual in the KGP dataset. Aggregate all rare, non-synonymous variants
across the entire Bardet-Biedl Syndrome (BBS) gene panel (BBS1 through BBS21). Is there a clear 'cliff' or maximum mutational
burden observed in healthy individuals ? Determine if the healthy cohort contains any 'triallelic' carriers (homozygous at one
locus, heterozygous at another) and model why they do not display the BBS phenotype._

- [report](https://claude.ai/public/artifacts/15fabda9-8a4a-4f0c-9605-ec80298e66f3?fullscreen=true),
[vis](https://github.com/user-attachments/assets/3d42b9da-3d23-48c7-9703-e9599d8d59b0)


#### Protein-Protein Interactions

> _Analyze samples in the KGP dataset with missense variants located at the 'hinge' or 'head' domains in Cohesin complex genes
(SMC1A, SMC3, RAD21). Perform a 'co-evolution' analysis - do samples with a destabilizing mutation in the SMC1A head domain
tend to carry a complementary variant in the SMC3 head domain that restores electrostatic compatibility (e.g., a charge swap
from Glu->Lys in one and Lys->Glu in the other) ?_

- results might be _[some](https://claude.ai/public/artifacts/ea605022-296d-446d-989f-a9e7bae5ab6b)_

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


_More examples [here](./examples/README.md)_

## Available Tools

Description for 30 tools and parameters can be found [here](./src/main/java/org/dnaerys/mcp/OneKGPMCPServer.java)

## Installation

Project can be run locally with MCP over _stdio_ and/or _http_ transports

#### Option A - build & run locally

- build the project and package it as a single _über-jar_:
    - jar is located in `target/onekgpd-mcp-runner.jar` and includes all dependencies

```shell script
./mvnw package -DskipTests -Dquarkus.package.jar.type=uber-jar
```

- run it locally with _dev profile_
    - both _stdio_ and _http_ transports are enabled 
    - http transport is on quarkus [http.port](./src/main/resources/application.properties)
    - project expects _JRE 21_ to be available at runtime

```shell script
java -Dquarkus.profile=dev -jar <full path>/onekgpd-mcp-runner.jar
```

#### Option B - build & run in docker

- in order to run in docker, _stdio_ transport needs to be disabled to prevent application from stopping itself
due to closed stdio in containers
    - it's already configured in _prod profile_
    - it's the default configuration overall
 
- build with _prod profile_

```shell script
docker build -f Dockerfile -t onekgpd-mcp .
```

- run as you prefer, e.g.

```shell script
docker run -p 9000:9000 --name onekgpd-mcp --rm onekgpd-mcp
```

#### Option C - pull from Docker Hub

- pull prebuilt image; _stdio_ transport disabled, _http_ transport on port 9000

```shell script
docker pull dnaerys/onekgpd-mcp:latest
```

- run

```shell script
docker run -p 9000:9000 --name onekgpd-mcp --rm onekgpd-mcp
```

---

#### Connecting with MCP clients

- to connect via _http_ transport, _remote or local_, simply direct the client to an appropriate destination,
_e.g._ `http://localhost:9000/mcp` or `https://db.dnaerys.org:443/mcp`

- to connect via _stdio_ transport, MCP client should start application with _dev profile_ and with a full path to the jar file 
    - e.g. for _Claude Desktop_ and _stdio_ transport add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "OneKGPd": {
      "command": "java",
      "args": ["-Dquarkus.profile=dev", "-jar", "/full/path/onekgpd-mcp-runner.jar"]
    }
  }
}
```

#### Verification

> How many variants exist in 1000 Genome Project ?


## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](./LICENSE) file for details.
