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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.dnaerys.cluster.grpc.*;
import org.dnaerys.client.entity.*;

public class DnaerysClient {

    private static final Integer MAX_RETURNED_ITEMS = 50;
    private static final Logger LOGGER = Logger.getLogger(DnaerysClient.class.getName());

    public enum Gender { MALE, FEMALE, BOTH }

    public record SampleCounts(int total, int male, int female) {}

    Annotations composeAnnotations(Float afLessThan, Float afGreaterThan,
                                   Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
                                   Float gnomadAfExLessThan, Float gnomadAfExGreaterThan,
                                   String impact, String bioType, String featureType, String variantType,
                                   String consequences, String clinSignificance,
                                   String alphaMissense, Float alphaMissenseScoreLT, Float alphaMissenseScoreGT,
                                   Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales) {

        Annotations.Builder builder = Annotations.newBuilder();

        if (afLessThan != null && afLessThan > 0) {
            builder.setAfLt(afLessThan);
        }

        if (afGreaterThan != null && afGreaterThan > 0) {
            builder.setAfGt(afGreaterThan);
        }

        if (gnomadAfGenLessThan != null && gnomadAfGenLessThan > 0) {
            builder.setGnomadGenomesAfLt(gnomadAfGenLessThan);
        }

        if (gnomadAfGenGreaterThan != null && gnomadAfGenGreaterThan > 0) {
            builder.setGnomadGenomesAfGt(gnomadAfGenGreaterThan);
        }

        if (gnomadAfExLessThan != null && gnomadAfExLessThan > 0) {
            builder.setGnomadExomesAfLt(gnomadAfExLessThan);
        }

        if (gnomadAfExGreaterThan != null && gnomadAfExGreaterThan > 0) {
            builder.setGnomadExomesAfGt(gnomadAfExGreaterThan);
        }

        if (impact != null && !impact.isEmpty()) {
            for (String token : impact.split(",")) {
                Impact element = ImpactMapper.fromString(token);
                if (element != Impact.UNRECOGNIZED) {
                    builder.addImpact(element);
                }
            }
        }

        if (bioType != null && !bioType.isEmpty()) {
            for (String token : bioType.split(",")) {
                BioType element = BiotypeMapper.fromString(token);
                if (element != BioType.UNRECOGNIZED) {
                    builder.addBioType(element);
                }
            }
        }

        if (featureType != null && !featureType.isEmpty()) {
            for (String token : featureType.split(",")) {
                FeatureType element = FeatureTypeMapper.fromString(token);
                if (element != FeatureType.UNRECOGNIZED) {
                    builder.addFeatureType(element);
                }
            }
        }

        if (variantType != null && !variantType.isEmpty()) {
            for (String token : variantType.split(",")) {
                VariantType element = VariantTypeMapper.fromString(token);
                if (element != VariantType.UNRECOGNIZED) {
                    builder.addVariantType(element);
                }
            }
        }

        if (consequences != null && !consequences.isEmpty()) {
            for (String token : consequences.split(",")) {
                Consequence element = ConsequencesMapper.fromString(token);
                if (element != Consequence.UNRECOGNIZED) {
                    builder.addConsequence(element);
                }
            }
        }

        if (clinSignificance != null && !clinSignificance.isEmpty()) {
            for (String token : clinSignificance.split(",")) {
                ClinSignificance element = ClinSigMapper.fromString(token);
                if (element != ClinSignificance.UNRECOGNIZED) {
                    builder.addClinsgn(element);
                }
            }
        }

        if (alphaMissense != null && !alphaMissense.isEmpty()) {
            for (String token : alphaMissense.split(",")) {
                AlphaMissense element = AlphaMissenseMapper.fromString(token);
                if (element != AlphaMissense.UNRECOGNIZED) {
                    builder.addAmClass(element);
                }
            }
        }

        if (alphaMissenseScoreLT != null && alphaMissenseScoreLT > 0) {
            builder.setAmScoreLt(alphaMissenseScoreLT);
        }

        if (alphaMissenseScoreGT != null && alphaMissenseScoreGT > 0) {
            builder.setAmScoreGt(alphaMissenseScoreGT);
        }

        if (biallelicOnly != null && biallelicOnly) {
            builder.setBiallelicOnly(biallelicOnly);
        }

        if (multiallelicOnly != null && multiallelicOnly) {
            builder.setMultiallelicOnly(multiallelicOnly);
        }

        if (excludeMales != null && excludeMales) {
            builder.setExcludeMales(excludeMales);
        }

        if (excludeFemales != null && excludeFemales) {
            builder.setExcludeFemales(excludeFemales);
        }

        return builder.build();
    }

    public long variantsTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getVariantsTotal();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return 0L; // default
    }

    public SampleCounts getSampleCounts() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            DatasetInfoRequest request = DatasetInfoRequest.newBuilder()
                .setReturnSamplesNames(false)
                .build();
            DatasetInfoResponse response = channel.getBlockingStub().datasetInfo(request);

            return new SampleCounts(
                response.getSamplesTotal(),
                response.getMalesTotal(),
                response.getFemalesTotal()
            );
       } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve sample counts", e);
            return new SampleCounts(0, 0, 0);
       }
    }

    public List<String> getSampleIds(Gender gender) {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            DatasetInfoRequest request = DatasetInfoRequest.newBuilder()
                .setReturnSamplesNames(true)
                .build();

            DatasetInfoResponse response = channel.getBlockingStub().datasetInfo(request);
            List<org.dnaerys.cluster.grpc.Cohort> cohorts = response.getCohortsList();

            List<String> result = cohorts.stream()
                .flatMap(c -> switch (gender) {
                    case FEMALE -> c.getFemaleSamplesNamesList().stream();
                    case MALE   -> c.getMaleSamplesNamesList().stream();
                    case BOTH   -> Stream.concat(
                        c.getFemaleSamplesNamesList().stream(),
                        c.getMaleSamplesNamesList().stream());
                })
                .toList();

            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve sample IDs for " + gender, e);
            return List.of();
        }
    }

    public long countVariantsInRegion(
        String chromosome, int start, int end, boolean selectHom, boolean selectHet,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance) {

        if (start < 0 || end < start) return 0L;

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        CountAllelesInRegionRequest request = CountAllelesInRegionRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            return channel.getBlockingStub().countVariantsInRegion(request).getCount();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "countVariantsInRegion gRPC call failed", e);
            return 0L;
        }
    }

    public long countVariantsInRegionInSample(
        String chromosome, int start, int end, String sample, boolean selectHom, boolean selectHet,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance) {

        if (start < 0 || end < start || sample == null || sample.isEmpty()) return 0L;

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        CountAllelesInRegionInSamplesRequest request = CountAllelesInRegionInSamplesRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .addSamples(sample)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            return channel.getBlockingStub().countVariantsInRegionInSamples(request).getCount();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Count gRPC call failed for sample: " + sample, e);
            return 0L;
        }
    }

    public List<Variant> selectVariantsInRegion(
            String chromosome, int start, int end, boolean selectHom, boolean selectHet,
            String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
            Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
            Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
            Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
            String featureType, String variantType, String consequences, String alphaMissense,
            Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance,
            Integer skip, Integer limit) {

        if (start < 0 || end < start) return List.of();

        int finalSkip = (skip == null || skip < 0) ? 0 : skip;
        int finalLimit = (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) ? MAX_RETURNED_ITEMS : limit;

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of();

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        AllelesInRegionRequest request = AllelesInRegionRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(varMinLength == null || varMinLength <= 0 ? 0 : varMinLength)
            .setVariantMaxLength(varMaxLength == null || varMaxLength <= 0 ? Integer.MAX_VALUE : varMaxLength)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .setLimit(finalLimit)
            .setSkip(finalSkip)
            .build();

        List<Variant> results = new ArrayList<>();
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            Iterator<AllelesResponse> responseStream = channel.getBlockingStub().selectVariantsInRegion(request);

            while (responseStream.hasNext()) {
                results.addAll(responseStream.next().getVariantsList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "gRPC Call Failed", e);
            return List.of();
        }
        return results;
    }

    public List<Variant> selectVariantsInRegionInSample(
        String chromosome, int start, int end, String sample, boolean selectHom, boolean selectHet,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance,
        Integer skip, Integer limit) {

        if (start < 0 || end < start || sample == null || sample.isEmpty()) List.of();

        int finalSkip = (skip == null || skip < 0) ? 0 : skip;
        int finalLimit = (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) ? MAX_RETURNED_ITEMS : limit;

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) List.of();

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        AllelesInRegionInSamplesRequest request = AllelesInRegionInSamplesRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .addSamples(sample)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(varMinLength == null || varMinLength <= 0 ? 0 : varMinLength)
            .setVariantMaxLength(varMaxLength == null || varMaxLength <= 0 ? Integer.MAX_VALUE : varMaxLength)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .setLimit(finalLimit)
            .setSkip(finalSkip)
            .build();

        List<Variant> results = new ArrayList<>();
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            Iterator<AllelesResponse> responseStream = channel.getBlockingStub().selectVariantsInRegionInSamples(request);

            while (responseStream.hasNext()) {
                results.addAll(responseStream.next().getVariantsList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "gRPC Call Failed for sample: " + sample, e);
            return List.of();
        }

        return results;
    }

    public long countSamplesInRegion(
        String chromosome, int start, int end, boolean selectHom, boolean selectHet,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance) {

        if (start < 0 || end < start) return 0L;

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        SamplesInRegionRequest request = SamplesInRegionRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            return channel.getBlockingStub().countSamplesInRegion(request).getCount();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Count samples gRPC call failed for region: " + chromosome + ":" + start + "-" + end, e);
            return 0L;
        }
    }

    public List<String> selectSamplesInRegion(
        String chromosome, int start, int end, boolean selectHom, boolean selectHet,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance) {

        if (start < 0 || end < start) return List.of();

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of();

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        SamplesInRegionRequest request = SamplesInRegionRequest.newBuilder()
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setHom(selectHom)
            .setHet(selectHet)
            .setAnn(annotations)
            .build();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            List<String> samples = channel.getBlockingStub().selectSamplesInRegion(request).getSamplesList();
            if (samples == null) return List.of();
            return samples;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Select samples gRPC call failed", e);
            return List.of();
        }
    }

    public List<Variant> selectDeNovo(
        String parent1, String parent2, String proband, String chromosome, int start, int end,
        String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
        Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales, Float afLessThan,
        Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan, Float gnomadAfExLessThan,
        Float gnomadAfExGreaterThan, String impact, String bioType, String featureType, String variantType,
        String consequences, String alphaMissense, Float alphaMissenseScoreLT, Float alphaMissenseScoreGT,
        String clinSignificance, Integer skip, Integer limit) {

        if (parent1 == null || parent1.isEmpty() || parent2 == null || parent2.isEmpty() || proband == null || proband.isEmpty()) {
            return List.of();
        }
        if (start < 0 || end < start) return List.of();

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of();

        int finalSkip = (skip == null || skip < 0) ? 0 : skip;
        int finalLimit = (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) ? MAX_RETURNED_ITEMS : limit;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        DeNovoRequest request = DeNovoRequest.newBuilder()
            .setParent1(parent1)
            .setParent2(parent2)
            .setProband(proband)
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setAnn(annotations)
            .setLimit(finalLimit)
            .setSkip(finalSkip)
            .build();

        List<Variant> variants = new ArrayList<>();
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            Iterator<AllelesResponse> responseStream = channel.getBlockingStub().selectDeNovo(request);

            while (responseStream.hasNext()) {
                variants.addAll(responseStream.next().getVariantsList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DeNovo gRPC call failed for trio: " + parent1 + ", " + parent2 + ", " + proband, e);
            List.of();
        }

        return variants;
    }

    public List<Variant> selectHetDominant(
        String affectedParent, String unaffectedParent, String proband, String chromosome,
        int start, int end, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance,
        Integer skip, Integer limit) {

        if (affectedParent == null || affectedParent.isEmpty() ||
            unaffectedParent == null || unaffectedParent.isEmpty() ||
            proband == null || proband.isEmpty()) {
            return List.of();
        }
        if (start < 0 || end < start) return List.of();

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of();

        int finalSkip = (skip == null || skip < 0) ? 0 : skip;
        int finalLimit = (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) ? MAX_RETURNED_ITEMS : limit;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        HetDominantRequest request = HetDominantRequest.newBuilder()
            .setAffectedParent(affectedParent)
            .setUnaffectedParent(unaffectedParent)
            .setAffectedChild(proband) // Mapping 'proband' to affected child
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setAnn(annotations)
            .setLimit(finalLimit)
            .setSkip(finalSkip)
            .build();

        List<Variant> variants = new ArrayList<>();
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            Iterator<AllelesResponse> responseStream = channel.getBlockingStub().selectHetDominant(request);

            while (responseStream.hasNext()) {
                variants.addAll(responseStream.next().getVariantsList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HetDominant gRPC call failed for trio: " + affectedParent + ", " + unaffectedParent + ", " + proband, e);
            List.of();
        }

        return variants;
    }

    public List<Variant> selectHomRecessive(
        String unaffectedParent1, String unaffectedParent2, String proband, String chromosome,
        int start, int end, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
        Boolean biallelicOnly, Boolean multiallelicOnly, Boolean excludeMales, Boolean excludeFemales,
        Float afLessThan, Float afGreaterThan, Float gnomadAfGenLessThan, Float gnomadAfGenGreaterThan,
        Float gnomadAfExLessThan, Float gnomadAfExGreaterThan, String impact, String bioType,
        String featureType, String variantType, String consequences, String alphaMissense,
        Float alphaMissenseScoreLT, Float alphaMissenseScoreGT, String clinSignificance,
        Integer skip, Integer limit) {

        if (unaffectedParent1 == null || unaffectedParent1.isEmpty() ||
            unaffectedParent2 == null || unaffectedParent2.isEmpty() ||
            proband == null || proband.isEmpty()) {
            return List.of();
        }
        if (start < 0 || end < start) return List.of();

        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);
        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of();

        int finalSkip = (skip == null || skip < 0) ? 0 : skip;
        int finalLimit = (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) ? MAX_RETURNED_ITEMS : limit;

        int minLen = (varMinLength == null || varMinLength < 0) ? 0 : varMinLength;
        int maxLen = (varMaxLength == null || varMaxLength < 0) ? 0 : varMaxLength;

        if (maxLen < minLen) {
            minLen = 0;
            maxLen = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfGenLessThan, gnomadAfGenGreaterThan,
            gnomadAfExLessThan, gnomadAfExGreaterThan, impact, bioType, featureType, variantType, consequences, clinSignificance,
            alphaMissense, alphaMissenseScoreLT, alphaMissenseScoreGT, biallelicOnly, multiallelicOnly, excludeMales, excludeFemales);

        HomRecessiveRequest request = HomRecessiveRequest.newBuilder()
            .setUnaffectedParent1(unaffectedParent1)
            .setUnaffectedParent2(unaffectedParent2)
            .setAffectedChild(proband)
            .setAssembly(RefAssembly.GRCh38)
            .setChr(chr)
            .setStart(start)
            .setEnd(end)
            .setAlt(altAllele == null ? "" : altAllele)
            .setRef(refAllele == null ? "" : refAllele)
            .setVariantMinLength(minLen)
            .setVariantMaxLength(maxLen)
            .setAnn(annotations)
            .setLimit(finalLimit)
            .setSkip(finalSkip)
            .build();

        List<Variant> variants = new ArrayList<>();
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            Iterator<AllelesResponse> responseStream = channel.getBlockingStub().selectHomRecessive(request);

            while (responseStream.hasNext()) {
                variants.addAll(responseStream.next().getVariantsList());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HomRecessive gRPC call failed for trio: " + unaffectedParent1 + ", " + unaffectedParent2 + ", " + proband, e);
            List.of();
        }

        return variants;
    }

    public String kinship(String sample1, String sample2) {
        if (sample1 == null || sample1.isEmpty() || sample2 == null || sample2.isEmpty()) return "";

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            KinshipDuoRequest request =
                KinshipDuoRequest
                    .newBuilder()
                    .setSample1(sample1)
                    .setSample2(sample2)
                    .setSeq(true)
                    .build();

            List<Relatedness> response = channel.getBlockingStub().kinshipDuo(request).getRelList();
            if (response.isEmpty()) return "";
            return response.getFirst().getDegree().toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return ""; // default
    }
}
