# 1000 Genomes Project Dataset MCP Server

Natural language access to _**1000 Genomes Project dataset**_, hosted online in _[Dnaerys variant store](https://dnaerys.org/)_

Sequenced & aligned by _New York Genome Center_ (_GRCh38_). _3202 samples_: 2504 unrelated samples from phase
three panel + 698 samples from 602 family trios - [dataset details](https://www.internationalgenome.org/data-portal/data-collection/30x-grch38)

### Key Features

- _real-time_ access to _138 044 723_ unique variants and _~442 billion_ individual genotypes

- variant, sample and genotype selection based on coordinates, annotations, zygosity

- filtering by VEP (impact, biotype, feature type, variant class, consequences), ClinVar Clinical Significance (202502),
gnomADe + gnomADg 4.1, AlphaMissense Score & AlphaMissense Class annotations

  - [full annotation composition](./docs/annotations.md)

- returned variants annotated with _HGVSp_, _gnomADe + gnomADg_, _AlphaMissense score_ + cohort-wide statistics

## Online Service

Remote MCP service via _Streamable HTTP:_

- http://db.dnaerys.org/mcp
- https://db.dnaerys.org/mcp

## Examples

#### Macromolecular structural complexes

> _The MCM2-7 Complex (The "DNA Helicase Motor") is a molecular masterpiece. It’s a heterohexameric ring where each subunit is
a distinct "gear" in the DNA-unzipping motor. Unlike homomeric rings (where every subunit is the same), this complex is asymmetric.
Each interface between subunits is unique, and they don't all burn ATP at the same rate. The MCM2/5 interface is the "gate" that
must physically open to allow DNA to enter the ring and then snap shut. This is a high-stress mechanical point._
>
> _Identify individuals in the KGP cohort carrying missense variants at the MCM2/5 interface. Specifically, look for
'charge-reversal' variants (e.g., Aspartate to Lysine). In these specific samples, analyze the 'compensatory coupling':
do they carry a secondary, reciprocal charge-reversal variant on the opposing subunit interface that restores the
electrostatic 'latch' ?_
>
> _Identify individuals in the KGP cohort who carry high-pathogenicity variants in the Walker A or Walker B motifs
(the ATP-burning heart) of any MCM subunit in MCM2-7 Complex. For these individuals, perform a 'Systemic Flux' analysis:
look at their variants in the leading-strand polymerase (POLE) and the sliding clamp (PCNA). Do you detect a signature of
'Coordinated Deceleration' where the motor, the clamp, and the polymerase all carry variants that suggest a slower but
highly-accurate replication fork ?_

#### Macromolecular structural complexes

> _The human RNA Exosome (Exo-9 core) is a "dead machine" that acts as a scaffold. In lower organisms the ring itself can degrade RNA.
In humans, the 9-subunit ring has lost all its catalytic teeth and is purely a structural tunnel that guides RNA into the catalytic
subunits (DIS3 or EXOSC10) attached at the bottom. Since RNA is a highly negatively charged polymer, the residues lining this pore
are typically positively charged (Lysine, Arginine), but not too "sticky" or RNA will jam. So, to reach the "shredder" at the bottom
it must slide through a narrow pore formed by the Exo-9 ring._
>
> _The task: analyse all missense variants in the KGP cohort that map to the internal pore-lining residues of the Exo-9 ring.
Look for 'charge-swap' variants where a positive residue (K, R) is replaced by a negative one (D, E). If an individual is healthy
despite having a 'negative patch' in the tunnel that should repel RNA, do they carry a compensatory variant in the cap subunits
(EXOSC1, 2, 3) that widens the entrance? Use a 3D electrostatic surface map to determine if the 'healthy' cohort maintains a specific
electrostatic gradient._

#### Synergistic Epistasis in Redox Homeostasis

> _Cellular redox homeostasis is maintained by two parallel antioxidant systems: the glutathione system
and the thioredoxin system. Complete loss of either GSR or TXNRD1 is incompatible with mammalian development, yet population
databases contain individuals carrying variants predicted to impair enzyme function._
>

> _Identify clusters of individuals in the KGP cohort who carry multiple 'Moderate' impact VEP variants across both systems.
Reasoning through the AlphaMissense structural implications, can you detect a 'balancing act' where a loss of efficiency
in Glutathione reductase is consistently paired with high-confidence benign or potentially activating variants in the
Thioredoxin system ? Synthesize a model of 'Redox Robustness' based on the co-occurrence of these variants across the cohort._

#### Macromolecular structural complexes

> _Treat the 26S Proteasome as a mechanically redundant 3D machine and map every missense variant from the KGP individuals
across all 33 subunits. Perform a spatial analysis to determine if pathogenic variation is statistically partitioned toward
the distal 'Lid' (Zone C) rather than the more evolutionary constrained 'Core' (Zone A) or 'Gating' (Zone B) interfaces.
Identify individuals with a high cumulative burden (2+ 'Likely Pathogenic' variants) to investigate inter-subunit compensation,
searching for paired 'weakening' and 'stabilizing' mutations at protein-protein hinges. Finally, define the 'mechanical
tolerance' of the proteasome by establishing the maximum cumulative structural disruption observed in a single healthy
individual based on AlphaMissense scores and calculated ΔΔG values._

_[More examples](./examples/README.md)_

---

## Architecture

Implemented as a Java EE service, accessing _KGP dataset_ via gRPC calls to public Dnaerys variant store service.

- provides MCP over _Streamable HTTP_, _HTTP/SSE_ and _STDIO_ transports
- service implementation is based on [Quarkus MCP Server framework](https://docs.quarkiverse.io/quarkus-mcp-server/dev/)
- tools: _computeAlphaMissenseAvg, computeVariantBurden, countSamples, countSamplesHomozygousReference, countVariants,
  countVariantsInSamples, getDatasetInfo, getKinshipDegree, selectSamples, selectSamplesHomozygousReference,
  selectVariants, selectVariantsInSamples_
  - [implementation](./src/main/java/org/dnaerys/mcp/OneKGPdMCPServer.java)

## Installation

Project can be run locally with MCP over _stdio_ and/or _http_ transports

#### Option A - build & run locally

- build the project and package it as a single _über-jar_:
    - jar is located in `target/onekgpd-mcp-runner.jar` and includes all dependencies

```shell script
./mvnw clean package -DskipTests -Dquarkus.package.jar.type=uber-jar
```

with skipping test compilation

```shell script
./mvnw  clean package -Dmaven.test.skip=true -Dquarkus.package.jar.type=uber-jar
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

- to connect via _http_ transport, _remote or local_, simply direct the client to a destination,
_e.g._ `http://localhost:9000/mcp` or `https://db.dnaerys.org:443/mcp`
    - _NB:_ _Claude Desktop_ won't work with `http://localhost:9000/mcp` option (e.g. when running MCP server in a docker container).
    This option is for clients like _Goose_.

- to connect via _stdio_ transport, MCP client should start application with _dev profile_ and with a full path to the jar file 
    - e.g. for _Claude Desktop_ add to config files (e.g. `claude_desktop_config.json`):

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

### Test Coverage Status

| Component | Type | Tests | Status |
|-----------|------|-------|--------|
| Entity Mappers (9 classes) | Unit | 314 | ✅ Complete |
| DnaerysClient | Unit | 58 (7 disabled) | ✅ Complete |
| DnaerysClient | Integration | 5 (1 disabled) | ✅ Complete |
| OneKGPdMCPServer | Unit | 26 | ✅ Complete |
| OneKGPdMCPServer | Integration | 5 | ✅ Complete |
| Other | Unit | 1 | ✅ Complete |
| Other | Integration | 1 | ✅ Complete |
| **Total** | | **410 tests** | **402 passing, 8 disabled** |

**Test Breakdown:**
- Unit tests: 399 (7 disabled, 392 passing)
- Integration tests: 11 (1 disabled, 10 passing)

**Disabled Tests:**
- 7 DnaerysClient unit tests (PaginationTests, streaming gRPC limitation - `wiremock-grpc-extension:0.11.0` cannot mock streaming RPCs yet)
- 1 DnaerysClient integration test (PaginationLogicTests, streaming gRPC limitation - `wiremock-grpc-extension:0.11.0` cannot mock streaming RPCs yet)

### Running Tests

```bash
# Unit tests only (no server required)
./mvnw test

# Integration tests (requires db.dnaerys.org access)
./mvnw verify -DskipITs=false

# Update test baselines after data changes
./mvnw verify -DskipITs=false -DupdateBaseline=true
```

See [TEST_SPECIFICATION.md](./docs/TEST_SPECIFICATION.md) for detailed test documentation.

---

_Test part of this project is written by Claude. Fun part is written by humans._

---

## Privacy Policy

OneKGPd MCP Server operates as a read-only interface layer for 1000 Genomes Project dataset.
Server does not collect, store, or transmit any user data. No conversation data is recorded.
No personal information is collected. No cookies, tracking mechanisms or authentication are used.

## Support

For issues, questions, or feedback: https://github.com/dnaerys/onekgpd-mcp/issues

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](./LICENSE) file for details.
