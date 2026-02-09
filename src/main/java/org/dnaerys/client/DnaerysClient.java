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

package org.dnaerys.client;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dnaerys.client.entity.*;
import org.dnaerys.cluster.grpc.DnaerysServiceGrpc;
import org.dnaerys.cluster.grpc.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.grpc.GrpcClient;
import org.dnaerys.mcp.OneKGPdMCPServer.SelectByAnnotations;
import org.dnaerys.mcp.OneKGPdMCPServer.GenomicRegion;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DnaerysClient {

    private static final Logger LOG = Logger.getLogger(DnaerysClient.class);

    @GrpcClient("dnaerys")
    DnaerysServiceGrpc.DnaerysServiceBlockingStub blockingStub;

    @ConfigProperty(name = "quarkus.grpc.clients.dnaerys.host")
    String host;

    @ConfigProperty(name = "quarkus.grpc.clients.dnaerys.port")
    int port;

    @PostConstruct
    void init() {
        LOG.infof("gRPC client initialized. Connecting to: %s:%d", host, port);
    }

    private static final Integer MAX_RETURNED_ITEMS = 50;
    private static final Integer MAX_RECEIVED_ITEMS = 5000;
    private static final Integer TOTAL_SAMPLES = 3202;

    public enum Gender { MALE, FEMALE, BOTH }

    public record DatasetInfo(int variantsTotal, int samplesTotal, int samplesMaleCount, int samplesFemaleCount) {}

    public record AlphaMissenseAvg(double alphaMissenseMean, double alphaMissenseDeviation, int variantCount) {}
    public record VariantBurden(String histogram, String highestBurdenSamples, String secondHighestBurdenSamples) {}

    Annotations composeAnnotations(SelectByAnnotations sbn) {
        if (sbn == null) return Annotations.getDefaultInstance();

        Annotations.Builder builder = Annotations.newBuilder();

        if (sbn.afLessThan() != null && sbn.afLessThan() > 0) {
            builder.setAfLt(sbn.afLessThan());
        } else if (sbn.afLessThan() != null && sbn.afLessThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.afLessThan());
        }

        if (sbn.afGreaterThan() != null && sbn.afGreaterThan() > 0) {
            builder.setAfGt(sbn.afGreaterThan());
        } else if (sbn.afGreaterThan() != null && sbn.afGreaterThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.afGreaterThan());
        }

        if (sbn.gnomadGenomeAfLessThan() != null && sbn.gnomadGenomeAfLessThan() > 0) {
            builder.setGnomadGenomesAfLt(sbn.gnomadGenomeAfLessThan());
        } else if (sbn.gnomadGenomeAfLessThan() != null && sbn.gnomadGenomeAfLessThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.gnomadGenomeAfLessThan());
        }

        if (sbn.gnomadGenomeAfGreaterThan() != null && sbn.gnomadGenomeAfGreaterThan() > 0) {
            builder.setGnomadGenomesAfGt(sbn.gnomadGenomeAfGreaterThan());
        } else if (sbn.gnomadGenomeAfGreaterThan() != null && sbn.gnomadGenomeAfGreaterThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.gnomadGenomeAfGreaterThan());
        }

        if (sbn.gnomadExomeAfLessThan() != null && sbn.gnomadExomeAfLessThan() > 0) {
            builder.setGnomadExomesAfLt(sbn.gnomadExomeAfLessThan());
        } else if (sbn.gnomadExomeAfLessThan() != null && sbn.gnomadExomeAfLessThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.gnomadExomeAfLessThan());
        }

        if (sbn.gnomadExomeAfGreaterThan() != null && sbn.gnomadExomeAfGreaterThan() > 0) {
            builder.setGnomadExomesAfGt(sbn.gnomadExomeAfGreaterThan());
        } else if (sbn.gnomadExomeAfGreaterThan() != null && sbn.gnomadExomeAfGreaterThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.gnomadExomeAfGreaterThan());
        }

        if (sbn.vepImpact() != null && !sbn.vepImpact().isEmpty()) {
            for (String token : sbn.vepImpact().split(",")) {
                Impact element = ImpactMapper.fromString(token);
                if (element != Impact.UNRECOGNIZED) {
                    builder.addImpact(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.vepBiotype() != null && !sbn.vepBiotype().isEmpty()) {
            for (String token : sbn.vepBiotype().split(",")) {
                BioType element = BiotypeMapper.fromString(token);
                if (element != BioType.UNRECOGNIZED) {
                    builder.addBioType(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.vepFeature() != null && !sbn.vepFeature().isEmpty()) {
            for (String token : sbn.vepFeature().split(",")) {
                FeatureType element = FeatureTypeMapper.fromString(token);
                if (element != FeatureType.UNRECOGNIZED) {
                    builder.addFeatureType(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.vepVariantType() != null && !sbn.vepVariantType().isEmpty()) {
            for (String token : sbn.vepVariantType().split(",")) {
                VariantType element = VariantTypeMapper.fromString(token);
                if (element != VariantType.UNRECOGNIZED) {
                    builder.addVariantType(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.vepConsequences() != null && !sbn.vepConsequences().isEmpty()) {
            for (String token : sbn.vepConsequences().split(",")) {
                Consequence element = ConsequencesMapper.fromString(token);
                if (element != Consequence.UNRECOGNIZED) {
                    builder.addConsequence(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.clinSignificance() != null && !sbn.clinSignificance().isEmpty()) {
            for (String token : sbn.clinSignificance().split(",")) {
                ClinSignificance element = ClinSigMapper.fromString(token);
                if (element != ClinSignificance.UNRECOGNIZED) {
                    builder.addClinsgn(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.alphaMissenseClass() != null && !sbn.alphaMissenseClass().isEmpty()) {
            for (String token : sbn.alphaMissenseClass().split(",")) {
                AlphaMissense element = AlphaMissenseMapper.fromString(token);
                if (element != AlphaMissense.UNRECOGNIZED) {
                    builder.addAmClass(element);
                } else {
                    throw new RuntimeException("Invalid parameter: " + token);
                }
            }
        }

        if (sbn.alphaMissenseScoreLessThan() != null && sbn.alphaMissenseScoreLessThan() > 0) {
            builder.setAmScoreLt(sbn.alphaMissenseScoreLessThan());
        } else if (sbn.alphaMissenseScoreLessThan() != null && sbn.alphaMissenseScoreLessThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.alphaMissenseScoreLessThan());
        }

        if (sbn.alphaMissenseScoreGreaterThan() != null && sbn.alphaMissenseScoreGreaterThan() > 0) {
            builder.setAmScoreGt(sbn.alphaMissenseScoreGreaterThan());
        } else if (sbn.alphaMissenseScoreGreaterThan() != null && sbn.alphaMissenseScoreGreaterThan() < 0) {
            throw new RuntimeException("Invalid parameter: " + sbn.alphaMissenseScoreGreaterThan());
        }

        if (sbn.biallelicOnly() != null && sbn.biallelicOnly()) {
            builder.setBiallelicOnly(sbn.biallelicOnly());
        }

        if (sbn.multiallelicOnly() != null && sbn.multiallelicOnly()) {
            builder.setMultiallelicOnly(sbn.multiallelicOnly());
        }

        if (sbn.excludeMales() != null && sbn.excludeMales()) {
            builder.setExcludeMales(sbn.excludeMales());
        }

        if (sbn.excludeFemales() != null && sbn.excludeFemales()) {
            builder.setExcludeFemales(sbn.excludeFemales());
        }

        return builder.build();
    }

    public DatasetInfo getDatasetInfo() {
        DatasetInfoRequest request = DatasetInfoRequest.newBuilder()
            .setReturnSamplesNames(false)
            .build();
        DatasetInfoResponse response = blockingStub.datasetInfo(request);
        return new DatasetInfo(
            response.getVariantsTotal(),
            response.getSamplesTotal(),
            response.getMalesTotal(),
            response.getFemalesTotal()
        );
    }

    public List<String> getSampleIds(Gender gender) {
        DatasetInfoRequest request = DatasetInfoRequest.newBuilder()
            .setReturnSamplesNames(true)
            .build();

        DatasetInfoResponse response = blockingStub.datasetInfo(request);
        List<org.dnaerys.cluster.grpc.Cohort> cohorts = response.getCohortsList();

        return cohorts.stream()
            .flatMap(c -> switch (gender) {
                case FEMALE -> c.getFemaleSamplesNamesList().stream();
                case MALE   -> c.getMaleSamplesNamesList().stream();
                case BOTH   -> Stream.concat(
                    c.getFemaleSamplesNamesList().stream(),
                    c.getMaleSamplesNamesList().stream());
            })
            .toList();
    }

    public long countVariantsInMultiRegions(List<GenomicRegion> regions, boolean selectHom, boolean selectHet,
                                            SelectByAnnotations sbn) {
        paramValidation(regions, sbn);
        Annotations annotations = composeAnnotations(sbn);

        var builder = CountAllelesInMultiRegionsRequest.newBuilder();

        regions.forEach(r -> {
            builder.addChr(ContigsMapping.contigName2GrpcChr(r.chromosome()));
            builder.addStart(r.start());
            builder.addEnd(r.end());
            builder.addRef(r.refAllele() == null ? "" : r.refAllele());
            builder.addAlt(r.altAllele() == null ? "" : r.altAllele());
        });

        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());

        CountAllelesInMultiRegionsRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        return blockingStub.countVariantsInMultiRegions(request).getCount();
    }

    public long countVariantsInMultiRegionsInSample(List<GenomicRegion> regions, String sample, boolean selectHom,
                                                    boolean selectHet, SelectByAnnotations sbn) {
        return countVariantsInMultiRegionsInSample(regions, sample, selectHom, selectHet, sbn, true);
    }

    public long countVariantsInMultiRegionsInSample(List<GenomicRegion> regions, String sample, boolean selectHom,
                                                    boolean selectHet, SelectByAnnotations sbn, boolean validateParameters) {
        if (validateParameters) {
            if (sample == null || sample.isEmpty()) {
                throw new RuntimeException("Sample ID must not be empty");
            } else {
                List<String> allSamples = getSampleIds(DnaerysClient.Gender.BOTH);
                if (!allSamples.contains(sample)) {
                    throw new RuntimeException(String.format(
                        "Invalid parameter: sample '%s' does not exist", sample));
                }
            }
            paramValidation(regions, sbn);
        }

        Annotations annotations = composeAnnotations(sbn);

        var builder = CountAllelesInMultiRegionsInSamplesRequest.newBuilder();

        regions.forEach(r -> {
            builder.addChr(ContigsMapping.contigName2GrpcChr(r.chromosome()));
            builder.addStart(r.start());
            builder.addEnd(r.end());
            builder.addRef(r.refAllele() == null ? "" : r.refAllele());
            builder.addAlt(r.altAllele() == null ? "" : r.altAllele());
        });

        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());

        CountAllelesInMultiRegionsInSamplesRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .addSamples(sample)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        return blockingStub.countVariantsInMultiRegionsInSamples(request).getCount();
    }

    public List<Variant> selectVariantsInRegion(GenomicRegion region, boolean selectHom, boolean selectHet,
                                                SelectByAnnotations sbn, Integer skip, Integer limit) {
        paramValidation(region, sbn == null ? null : sbn.minVariantLengthBp(), sbn == null ? null : sbn.maxVariantLengthBp(), skip, limit);
        Annotations annotations = composeAnnotations(sbn);
        Chromosome chr = ContigsMapping.contigName2GrpcChr(region.chromosome());

        var builder = AllelesInRegionRequest.newBuilder();

        if (region.refAllele() != null) builder.setRef(region.refAllele());
        if (region.altAllele() != null) builder.setAlt(region.altAllele());
        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());
        if (skip != null) builder.setSkip(skip);
        if (limit != null) {
            builder.setLimit(limit);
        } else {
            builder.setLimit(MAX_RETURNED_ITEMS);
        }

        AllelesInRegionRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(region.start())
            .setEnd(region.end())
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        Set<Variant> results = new HashSet<>();
        Iterator<AllelesResponse> responseStream = blockingStub.selectVariantsInRegion(request);

        while (responseStream.hasNext()) {
            results.addAll(responseStream.next().getVariantsList());
        }

        return new ArrayList<>(results);
    }

    public List<Variant> selectVariantsInRegionInSample(GenomicRegion region, String sample, boolean selectHom,
                                                        boolean selectHet, SelectByAnnotations sbn, Integer skip, Integer limit) {
        if (sample == null || sample.isEmpty())
            throw new RuntimeException("Sample ID must not be empty");

        paramValidation(region, sbn == null ? null : sbn.minVariantLengthBp(), sbn == null ? null : sbn.maxVariantLengthBp(), skip, limit);
        Annotations annotations = composeAnnotations(sbn);
        Chromosome chr = ContigsMapping.contigName2GrpcChr(region.chromosome());

        var builder = AllelesInRegionInSamplesRequest.newBuilder();

        if (region.refAllele() != null) builder.setRef(region.refAllele());
        if (region.altAllele() != null) builder.setAlt(region.altAllele());
        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());
        if (skip != null) builder.setSkip(skip);
        if (limit != null) {
            builder.setLimit(limit);
        } else {
            builder.setLimit(MAX_RETURNED_ITEMS);
        }

        AllelesInRegionInSamplesRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(region.start())
            .setEnd(region.end())
            .addSamples(sample)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        Set<Variant> results = new HashSet<>();
        Iterator<AllelesResponse> responseStream = blockingStub.selectVariantsInRegionInSamples(request);

        while (responseStream.hasNext()) {
            results.addAll(responseStream.next().getVariantsList());
        }

        return new ArrayList<>(results);
    }

    public long countSamplesInMultiRegions(List<GenomicRegion> regions, boolean selectHom, boolean selectHet,
                                           SelectByAnnotations sbn) {
        paramValidation(regions, sbn);
        Annotations annotations = composeAnnotations(sbn);

        var builder = SamplesInMultiRegionsRequest.newBuilder();

        regions.forEach(r -> {
            builder.addChr(ContigsMapping.contigName2GrpcChr(r.chromosome()));
            builder.addStart(r.start());
            builder.addEnd(r.end());
            builder.addRef(r.refAllele() == null ? "" : r.refAllele());
            builder.addAlt(r.altAllele() == null ? "" : r.altAllele());
        });

        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());

        SamplesInMultiRegionsRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        return blockingStub.countSamplesInMultiRegions(request).getCount();
    }

    public List<String> selectSamplesInRegion(GenomicRegion region, boolean selectHom, boolean selectHet, SelectByAnnotations sbn) {

        paramValidation(region, sbn == null ? null : sbn.minVariantLengthBp(), sbn == null ? null : sbn.maxVariantLengthBp(), 0, 0);
        Annotations annotations = composeAnnotations(sbn);
        Chromosome chr = ContigsMapping.contigName2GrpcChr(region.chromosome());

        var builder = SamplesInRegionRequest.newBuilder();

        if (region.refAllele() != null) builder.setRef(region.refAllele());
        if (region.altAllele() != null) builder.setAlt(region.altAllele());
        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());

        SamplesInRegionRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(region.start())
            .setEnd(region.end())
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        return blockingStub.selectSamplesInRegion(request).getSamplesList();
    }

    public List<String> selectSamplesInMultiRegions(List<GenomicRegion> regions, boolean selectHom, boolean selectHet,
                                                    SelectByAnnotations sbn) {
        paramValidation(regions, sbn);
        Annotations annotations = composeAnnotations(sbn);

        var builder = SamplesInMultiRegionsRequest.newBuilder();

        regions.forEach(r -> {
            builder.addChr(ContigsMapping.contigName2GrpcChr(r.chromosome()));
            builder.addStart(r.start());
            builder.addEnd(r.end());
            builder.addRef(r.refAllele() == null ? "" : r.refAllele());
            builder.addAlt(r.altAllele() == null ? "" : r.altAllele());
        });

        if (sbn != null && sbn.minVariantLengthBp() != null) builder.setVariantMinLength(sbn.minVariantLengthBp());
        if (sbn != null && sbn.maxVariantLengthBp() != null) builder.setVariantMaxLength(sbn.maxVariantLengthBp());

        SamplesInMultiRegionsRequest request = builder
            .setAssembly(RefAssembly.GRCh38)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        return blockingStub.selectSamplesInMultiRegions(request).getSamplesList();
    }

    public long countSamplesHomozygousReference(String chromosome, int position) {
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED))
            throw new RuntimeException("Invalid Chromosome");

        if (position <= 0)
            throw new RuntimeException("Invalid parameter: 'position' must be >= 0");

        SamplesHomRefRequest request = SamplesHomRefRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setPosition(position)
            .build();

        return blockingStub.countSamplesHomReference(request).getCount();
    }

    public List<String> selectSamplesHomozygousReference(String chromosome, int position) {
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED))
            throw new RuntimeException("Invalid Chromosome");

        if (position <= 0)
            throw new RuntimeException("Invalid parameter: 'position' must be >= 0");

        SamplesHomRefRequest request = SamplesHomRefRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setPosition(position)
            .build();

        return blockingStub.selectSamplesHomReference(request).getSamplesList();
    }

    public String kinship(String sample1, String sample2) {
        // Parameters validation
        List<String> allSamples = getSampleIds(DnaerysClient.Gender.BOTH);
        if (!allSamples.contains(sample1)) {
            throw new RuntimeException("Sample '" + sample1 + "' does not exist");
        }
        if (!allSamples.contains(sample2)) {
            throw new RuntimeException("Sample '" + sample2 + "' does not exist");
        }

        KinshipDuoRequest request =
            KinshipDuoRequest
                .newBuilder()
                .setSample1(sample1)
                .setSample2(sample2)
                .setSeq(true)
                .build();
        List<Relatedness> response = blockingStub.kinshipDuo(request).getRelList();
        return response.getFirst().getDegree().toString();
    }

    public AlphaMissenseAvg computeAlphaMissenseAvg(List<GenomicRegion> regions) {
        // count vars
        boolean selectHom = true;
        boolean selectHet = true;
        SelectByAnnotations sbn = SelectByAnnotations.withAlphaMissenseScore(42f);

        long amTotal = countVariantsInMultiRegions(regions, selectHom, selectHet, sbn);
        if (amTotal > MAX_RECEIVED_ITEMS) {
            throw new RuntimeException(String.format(
                "Total number of selected variants exceeds %s. Try to reduce the number of regions or ranges.",
                MAX_RECEIVED_ITEMS));
        }

        Annotations annotations = composeAnnotations(sbn);

        // collect vars
        int pageNumber = 0;
        final int pageSize = 100;
        int lastSize = 0;
        boolean keepGoing = true;
        Set<Variant> results = new HashSet<>((int)amTotal);

        while (keepGoing) {
            int skip = pageNumber * pageSize;

            var builder = AllelesInMultiRegionsRequest.newBuilder();
            regions.forEach(r -> {
                builder.addChr(ContigsMapping.contigName2GrpcChr(r.chromosome()));
                builder.addStart(r.start());
                builder.addEnd(r.end());
                builder.addRef(r.refAllele() == null ? "" : r.refAllele());
                builder.addAlt(r.altAllele() == null ? "" : r.altAllele());
            });

            AllelesInMultiRegionsRequest request = builder
                .setAssembly(RefAssembly.GRCh38)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAnn(annotations)
                .setSkip(skip)
                .setLimit(pageSize)
                .build();

            Iterator<AllelesResponse> responseStream = blockingStub.selectVariantsInMultiRegions(request);

            while (responseStream.hasNext()) {
                // somehow responseStream returns duplicated elements, hence Set
                results.addAll(responseStream.next().getVariantsList());
            }

            if (results.size() > lastSize) {
                lastSize = results.size();
            } else {
                keepGoing = false; // no new variants in the last query --> we've retrieved all variants from all nodes
                if (lastSize > amTotal) {
                    LOG.errorf("Internal data inconsistency. Counted AM vars: %d, retrieved AM vars: %d", amTotal, lastSize);
                }
            }
            pageNumber++;
        }

        return alphaMissenseAvg(results);
    }

    private AlphaMissenseAvg alphaMissenseAvg(Set<Variant> variants) {
        if (variants.isEmpty()) {
            return new AlphaMissenseAvg(0d, 0d, 0);
        }

        // Calculate mean
        int count = 0;
        double sum = 0.0;

        for (Variant variant : variants) {
            sum += variant.getAmScore();
            count++;
        }

        double mean = sum / count;

        // Calculate sum of squared differences
        double sumSquaredDiff = 0.0;

        for (Variant variant : variants) {
            double diff = variant.getAmScore() - mean;
            sumSquaredDiff += diff * diff;
        }

        // Population standard deviation
        double variance = sumSquaredDiff / count;
        double stdDev = Math.sqrt(variance);

        return new AlphaMissenseAvg(mean, stdDev, count);
    }

    public VariantBurden computeVariantBurden(List<GenomicRegion> regions, List<String> samples,
                                              Boolean selectHom, Boolean selectHet, SelectByAnnotations sbn) {
        Instant startTimestamp = Instant.now();
        Integer zeroVarSamples = 0;

        paramValidation(regions, sbn);

        if (samples == null || samples.isEmpty()) {
            // Default case - all samples in the dataset with matching variants
            samples = selectSamplesInMultiRegions(regions, selectHom, selectHet, sbn);
            zeroVarSamples = TOTAL_SAMPLES - samples.size();
        } else {
            // Parameters validation
            for (String sample : samples) {
                List<String> allSamples = getSampleIds(DnaerysClient.Gender.BOTH);
                if (!allSamples.contains(sample)) {
                    throw new RuntimeException(String.format(
                        "Invalid parameter: sample '%s' does not exist", sample));
                }
            }
        }

        // Calculate variant burden for each sample
        Map<String, Integer> sampleBurdens = new HashMap<>();

        for (String sample : samples) {
            int variantCount = (int) countVariantsInMultiRegionsInSample(regions, sample, selectHom, selectHet, sbn, false);
            sampleBurdens.put(sample, variantCount);
        }

        // Generate histogram of variant counts
        String histogram = generateHistogram(sampleBurdens, zeroVarSamples);

        // Find samples with highest and second-highest burden
        String highestBurdenSamplesJson = "";
        String secondHighestBurdenSamplesJson = "";

        if (!sampleBurdens.isEmpty()) {
            List<Integer> sortedUniqueBurdens = sampleBurdens.values().stream()
                .distinct()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());

            if (!sortedUniqueBurdens.isEmpty()) {
                int highestBurden = sortedUniqueBurdens.get(0);

                List<String> highestBurdenSamples = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : sampleBurdens.entrySet()) {
                    if (entry.getValue() == highestBurden) {
                        highestBurdenSamples.add(entry.getKey());
                    }
                }
                highestBurdenSamplesJson = createBurdenSamplesJson(highestBurden, highestBurdenSamples);

                if (sortedUniqueBurdens.size() > 1) {
                    int secondHighestBurden = sortedUniqueBurdens.get(1);
                    List<String> secondHighestBurdenSamples = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : sampleBurdens.entrySet()) {
                        if (entry.getValue() == secondHighestBurden) {
                            secondHighestBurdenSamples.add(entry.getKey());
                        }
                    }
                    secondHighestBurdenSamplesJson = createBurdenSamplesJson(secondHighestBurden, secondHighestBurdenSamples);
                }
            }
        }

        Instant endTimestamp = Instant.now();
        Duration timeElapsed = Duration.between(startTimestamp, endTimestamp);
        LOG.debugf("VariantBurden duration: %s ms", timeElapsed.toMillis());

        return new VariantBurden(histogram, highestBurdenSamplesJson, secondHighestBurdenSamplesJson);
    }

    private String generateHistogram(Map<String, Integer> sampleBurdens, Integer zeroVarSamples) {
        // Count frequency of each burden value
        Map<Integer, Integer> burdenFrequency = new HashMap<>(); // Map(variantBurden, sampleCount)
        burdenFrequency.put(0, zeroVarSamples);
        for (Integer burden : sampleBurdens.values()) {
            burdenFrequency.merge(burden, 1, Integer::sum);
        }

        // Format as JSON array string
        StringBuilder histogram = new StringBuilder();
        histogram.append("[");

        List<Integer> sortedBurdens = burdenFrequency.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        for (int i = 0; i < sortedBurdens.size(); i++) {
            Integer burden = sortedBurdens.get(i);
            Integer frequency = burdenFrequency.get(burden);
            if (i > 0) {
                histogram.append(",");
            }
            histogram
                .append("{\"variantCount\":\"").append(burden)
                .append("\",\"samples\":\"").append(frequency).append("\"}");
        }

        histogram.append("]");
        return histogram.toString();
    }

    private String createBurdenSamplesJson(int variantCount, List<String> samples) {
        StringBuilder json = new StringBuilder();
        json.append("{\"variantCount\":\"").append(variantCount).append("\",\"samples\":[");

        for (int i = 0; i < samples.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(samples.get(i)).append("\"");
        }

        json.append("]}");
        return json.toString();
    }

    private void paramValidation(GenomicRegion region, Integer varMinLength, Integer varMaxLength, Integer skip, Integer limit) {
        Chromosome chr = ContigsMapping.contigName2GrpcChr(region.chromosome());
        if (chr.equals(Chromosome.UNRECOGNIZED)) throw new RuntimeException("Invalid Chromosome: " + region.chromosome());

        if (region.start() < 0 || region.end() < region.start()) {
            throw new RuntimeException(String.format(
                "Invalid genomic region: %s:%d-%d. Start must be >= 0 and end must be >= start.",
                region.chromosome(), region.start(), region.end()));
        }

        if (skip != null && skip < 0) throw new RuntimeException("Invalid parameter: 'skip' must be >= 0.");
        if (limit != null && (limit < 0 || limit > MAX_RETURNED_ITEMS)) {
            throw new RuntimeException("Invalid parameter: 'limit' must be >= 0 and <= " + MAX_RETURNED_ITEMS);
        }

        if (varMinLength != null && varMinLength < 0) throw new RuntimeException("Invalid parameter: 'minVariantLengthBp' must be >= 0.");
        if (varMaxLength != null && varMaxLength < 0) throw new RuntimeException("Invalid parameter: 'maxVariantLengthBp' must be >= 0.");
        if (varMinLength != null && varMaxLength != null && varMaxLength < varMinLength) {
            throw new RuntimeException("Invalid parameter: 'minVariantLengthBp' must be <= 'maxVariantLengthBp'.");
        }
    }

    private void paramValidation(List<GenomicRegion> regions, SelectByAnnotations sbn) {
        if (regions == null || regions.isEmpty()) {
            throw new RuntimeException("The 'regions' list cannot be empty.");
        }
        Integer varMinLength = sbn == null ? null : sbn.minVariantLengthBp();
        Integer varMaxLength = sbn == null ? null : sbn.maxVariantLengthBp();
        for (var region : regions) {
            paramValidation(region, varMinLength, varMaxLength, 0, 0);
        }
    }
}
