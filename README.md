# 1000 Genomes Project Dataset MCP Server

Natural language access to _**1000 Genomes Project dataset**_, hosted online in _[Dnaerys variant store](https://dnaerys.org/)_

Sequenced & aligned by _New York Genome Center_ (_GRCh38_). _3202 samples_: 2504 unrelated samples from phase
three panel + 698 samples from 602 family trios ([dataset details](https://www.internationalgenome.org/data-portal/data-collection/30x-grch38))

### Key Features

- _real-time_ access to _138 044 723_ unique variants and _~442 billion_ individual genotypes

- variant, sample and genotype selection based on coordinates, annotations, zygosity

- filtering by VEP (impact, biotype, feature type, variant class, consequences), ClinVar Clinical Significance (202502),
gnomADe + gnomADg 4.1, AlphaMissense Score & AlphaMissense Class annotations
  
- filtering by inheritance model (de novo, heterozygous dominant, homozygous recessive)

- returned variants annotated with _HGVSp_, _gnomADe + gnomADg_, _AlphaMissense score_ + cohort-wide statistics

## Online Service

Remote MCP service via _Streamable HTTP:_

- http://db.dnaerys.org:80/mcp
- https://db.dnaerys.org:443/mcp

## Architecture

Implemented as a Java EE service, accessing _KGP dataset_ via gRPC calls to public Dnaerys variant store service.

- provides MCP over _Streamable HTTP_, _HTTP/SSE_ and _STDIO_ transports
- service implementation is based on [Quarkus MCP Server framework](https://docs.quarkiverse.io/quarkus-mcp-server/dev/)

## Available Tools

MCP tools and parameters are [here](./src/main/java/org/dnaerys/mcp/OneKGPdMCPServer.java)

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

- to connect via _http_ transport, _remote or local_, simply direct the client to a destination,
_e.g._ `http://localhost:9000/mcp` or `https://db.dnaerys.org:443/mcp`

- to connect via _stdio_ transport, MCP client should start application with _dev profile_ and with a full path to the jar file 
    - e.g. add to _Claude_ config files (e.g. `claude_desktop_config.json`):

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

## Test Coverage

| Component | Type | Tests | Status |
|-----------|------|-------|--------|
| Entity Mappers (9 classes) | Unit | 314 | ✅ Complete |
| DnaerysClient | Unit | 61 | ✅ Complete |
| DnaerysClient | Integration | 5 | ✅ Complete |
| OneKGPdMCPServer | Unit | 31 | ✅ Complete |
| OneKGPdMCPServer | Integration | 7 | ✅ Complete |

**Total: 419 tests (407 unit + 12 integration)**

### Running Tests

```bash
# Unit tests only (no server required)
./mvnw test

# Integration tests (requires db.dnaerys.org access)
./mvnw verify -DskipIntegrationTests=false

# Update test baselines after data changes
./mvnw verify -DskipIntegrationTests=false -DupdateBaseline=true
```

See [TEST_SPECIFICATION.md](./docs/TEST_SPECIFICATION.md) for detailed test documentation.

---

_Test part of this project is written by Claude. Fun part is written by humans._

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](./LICENSE) file for details.
