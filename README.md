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

#### Regulatory Variant Impact on Known Disease Genes

> _Identify healthy individuals in the KGP dataset carrying ClinVar-validated pathogenic variants in SCN5A, KCNH2, or LDLR.
For each carrier, conduct a cis-regulatory scan within a 70kb window to identify variants in high linkage disequilibrium
that are statistically associated with Haplotype-Specific Expression (HSE) or Allelic Imbalance. Analyze if these secondary
variants disrupt Transcription Factor Binding Sites (TFBS) in proximal promoters, alter uORF-mediated translation kinetics,
or modify mRNA stability motifs in the 3'UTR. Evaluate the hypothesis that resilience is achieved through a "Transcriptional
Damping" mechanism, where the pathogenic allele is preferentially silenced or the wild-type allele is hyper-activated,
ensuring the total pool of functional protein remains above the critical phenotypic threshold._

- _[SCN5A transcriptional damping analysis](https://claude.ai/public/artifacts/8c470f0b-46ac-4cb4-9768-d77a95030a06?fullscreen=true)_

- > _Building on the SCN5A damping findings, the next phase should investigate if resilience in 150 heterozygous carriers
    is also mediated by trans-acting chaperones that stabilize the functional 50% of sodium channels. Task Statement:
    Identify healthy SCN5A pathogenic carriers from the previous "Transcriptional Damping" cohort and perform a genome-wide
    scan for Trans-acting Proteostatic Modifiers. Search for enrichment of gain-of-function variants or high-expression
    eQTLs in cardiac-specific ion channel chaperones and trafficking regulators, specifically SNTA1 (Syntrophin-alpha 1),
    GPD1L, and RANGRF (MOG1)._
    
    - _[trans-acting proteostatic modifiers study](https://claude.ai/public/artifacts/9c4f8fbf-8fe6-4efe-a94f-4ce0ba759da4?fullscreen=true)_

- similar study for _[KCNH2 and LDLR](https://claude.ai/public/artifacts/cae6d6bf-3f57-489d-976e-c6286983fa7a?fullscreen=true)_ 


#### Metabolic Pathway Redundancy

> _Cellular redox homeostasis is maintained by two parallel antioxidant systems: the glutathione system
and the thioredoxin system. Complete loss of either GSR or TXNRD1 is incompatible with mammalian development, yet population
databases contain individuals carrying variants predicted to impair enzyme function. Identify clusters of individuals in the
KGP cohort who carry multiple 'Moderate' impact VEP variants across both systems. Reasoning through the AlphaMissense structural
implications, can you detect a 'balancing act' where a loss of efficiency in Glutathione reductase is consistently paired with
high-confidence benign or potentially activating variants in the Thioredoxin system ? Synthesize a model of 'Redox Robustness'
based on the co-occurrence of these variants across the cohort._

- _[Genomic architecture of redox resilience analysis](https://claude.ai/public/artifacts/13a47b08-3dd6-4bd2-ac2f-ddd5535393a4?fullscreen=true),
[fig 1](https://github.com/user-attachments/assets/6432bf5b-9649-4027-948a-664d1b84a35f),
[fig 2](https://github.com/user-attachments/assets/ad846701-cf92-42b3-8859-cc01f322cb5c),
[fig 3](https://github.com/user-attachments/assets/cebecf70-2e33-4f20-bf68-63ec9e9f48d1),
[fig 4](https://github.com/user-attachments/assets/518ee5a0-acd2-41c8-8ff3-7afe07f1ee76),
[fig 5](https://github.com/user-attachments/assets/657d9dd2-2285-43cd-99f6-70011a551020),
[fig 6](https://github.com/user-attachments/assets/982c34f4-66b4-496b-ba46-e06c8766901c), 
[suppl](https://claude.ai/public/artifacts/efbce3ed-99b9-4790-b010-3cb01bf19d5a?fullscreen=true)_


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

- _[RNA Exosome charge-swap analysis](https://claude.ai/public/artifacts/b8ce0f2b-6adb-4763-9be9-9c323866a691?fullscreen=true)_

#### Structural Intolerance

> _In what cardiac related genes, e.g. ion channels, variants in KGP dataset near catalytic residues or
ligand-binding pockets show strong depletion compared to flanking residues (±20 amino acids) ?_
   
- [interactive visualisation](https://claude.ai/public/artifacts/e81fa694-7de5-4fed-b903-e6cb23d02dd9?fullscreen=true)


_[More examples](./examples/README.md)_

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
