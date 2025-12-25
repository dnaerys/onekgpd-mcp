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

package org.dnaerys.client;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import org.dnaerys.cluster.grpc.*;
import org.dnaerys.client.entity.*;

public class DnaerysClient {

    final Integer MAX_RETURNED_ITEMS = 100;

    Annotations composeAnnotations(Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                    String impact, String biotype, String feature, String variantType, String consequences, String alphaMissense,
                    String clinSignificance, Boolean biallelicOnly) {

        Annotations.Builder builder = Annotations.newBuilder();

        if (afLessThan != null && afLessThan > 0) {
            builder.setAfLt(afLessThan);
        }

        if (afGreaterThan != null && afGreaterThan > 0) {
            builder.setAfGt(afGreaterThan);
        }

        if (gnomadAfLessThan != null && gnomadAfLessThan > 0) {
            builder.setGnomadAfLt(gnomadAfLessThan);
        }

        if (gnomadAfGreaterThan != null && gnomadAfGreaterThan > 0) {
            builder.setGnomadAfGt(gnomadAfGreaterThan);
        }

        if (impact != null && !impact.isEmpty()) {
            for (String token : impact.split(",")) {
                Impact element = ImpactMapper.fromString(token);
                if (element != Impact.UNRECOGNIZED) {
                    builder.addImpact(element);
                }
            }
        }

        if (biotype != null && !biotype.isEmpty()) {
            for (String token : biotype.split(",")) {
                BioType element = BiotypeMapper.fromString(token);
                if (element != BioType.UNRECOGNIZED) {
                    builder.addBtypes(element);
                }
            }
        }

        if (feature != null && !feature.isEmpty()) {
            for (String token : feature.split(",")) {
                FeatureType element = FeatureTypeMapper.fromString(token);
                if (element != FeatureType.UNRECOGNIZED) {
                    builder.addFtypes(element);
                }
            }
        }

        if (variantType != null && !variantType.isEmpty()) {
            for (String token : variantType.split(",")) {
                VariantType element = VariantTypeMapper.fromString(token);
                if (element != VariantType.UNRECOGNIZED) {
                    builder.addVtypes(element);
                }
            }
        }

        if (consequences != null && !consequences.isEmpty()) {
            for (String token : consequences.split(",")) {
                Consequence element = ConsequencesMapper.fromString(token);
                if (element != Consequence.UNRECOGNIZED) {
                    builder.addConsequences(element);
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

        if (clinSignificance != null && !clinSignificance.isEmpty()) {
            for (String token : clinSignificance.split(",")) {
                ClinSignificance element = ClinSigMapper.fromString(token);
                if (element != ClinSignificance.UNRECOGNIZED) {
                    builder.addClnsgn(element);
                }
            }
        }

        if (biallelicOnly != null && biallelicOnly) {
            builder.setBiallelicOnly(biallelicOnly);
        }

        return builder.build();
    }

    public long variantsTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getVariantsTotal();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public long countSamplesTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getSamplesTotal();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public long countFemaleSamplesTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getFemalesTotal();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public long countMaleSamplesTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getMalesTotal();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public long nodesTotal() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = false;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();
            return channel.getBlockingStub().datasetInfo(request).getRingsTotal();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public List<String> samplesIds() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = true;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();

            org.dnaerys.cluster.grpc.DatasetInfoResponse response = channel.getBlockingStub().datasetInfo(request);

            int samplesTotal = response.getSamplesTotal();
            List<org.dnaerys.cluster.grpc.Cohort> cohorts = response.getCohortsList();
            List<String> samplesNames = new ArrayList<>(samplesTotal);

            for (Cohort c : cohorts) {
                samplesNames.addAll(c.getFemaleSamplesNamesList());
            }
            for (Cohort c : cohorts) {
                samplesNames.addAll(c.getMaleSamplesNamesList());
            }

            return samplesNames;
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return List.of("{}"); // default
    }

    public List<String> femaleSamplesIds() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = true;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();

            org.dnaerys.cluster.grpc.DatasetInfoResponse response = channel.getBlockingStub().datasetInfo(request);

            int femaleTotal = response.getFemalesTotal();
            List<org.dnaerys.cluster.grpc.Cohort> cohorts = response.getCohortsList();
            List<String> femaleNames = new ArrayList<>(femaleTotal);

            for (Cohort c : cohorts) {
                femaleNames.addAll(c.getFemaleSamplesNamesList());
            }

            return femaleNames;
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return List.of("{}"); // default
    }

    public List<String> maleSamplesIds() {
        try {
            GrpcChannel channel = GrpcChannel.getInstance();
            final Boolean withSamplesNames = true;
            DatasetInfoRequest request =
                DatasetInfoRequest
                    .newBuilder()
                    .setReturnSamplesNames(withSamplesNames)
                    .build();

            org.dnaerys.cluster.grpc.DatasetInfoResponse response = channel.getBlockingStub().datasetInfo(request);

            int maleTotal = response.getMalesTotal();
            List<org.dnaerys.cluster.grpc.Cohort> cohorts = response.getCohortsList();
            List<String> maleNames = new ArrayList<>(maleTotal);

            for (Cohort c : cohorts) {
                maleNames.addAll(c.getMaleSamplesNamesList());
            }

            return maleNames;
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return List.of("{}"); // default
    }

    public long countVariantsInRegion(String chromosome, int start, int end, boolean selectHom, boolean selectHet,
                    String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
                    Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan, String impact,
                    String biotype, String feature, String variantType, String consequences, String alphaMissense,
                    String clinSignificance) {
        if (start < 0) return 0L;
        if (end < start) return 0L;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength ) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            CountAllelesInRegionRequest request =
                CountAllelesInRegionRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .build();

            return channel.getBlockingStub().countVariantsInRegion(request).getCount();

        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        return 0L; // default
    }

    public long countVariantsInRegionInSample(String chromosome, int start, int end, String sample, boolean selectHom,
                    boolean selectHet, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
                    Boolean biallelicOnly, Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                    String impact, String biotype, String feature, String variantType, String consequences, String alphaMissense,
                    String clinSignificance) {
        if (start < 0) return 0L;
        if (end < start) return 0L;
        if (sample == null || sample.isEmpty()) return 0L;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength ) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            CountAllelesInRegionInSamplesRequest request =
                CountAllelesInRegionInSamplesRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .addSamples(sample)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .build();

            return channel.getBlockingStub().countVariantsInRegionInSamples(request).getCount();

        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        return 0L; // default
    }


    public List<String> selectVariantsInRegion(String chromosome, int start, int end, boolean selectHom, boolean selectHet,
                            String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
                            Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan, String impact,
                            String biotype, String feature, String variantType, String consequences, String alphaMissense,
                            String clinSignificance, Integer skip, Integer limit) {
        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");

        if (skip == null || skip < 0) skip = 0;
        if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) limit = MAX_RETURNED_ITEMS;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        List<Variant> variants = new ArrayList<>();
        List<String> alleles = new ArrayList<>();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            AllelesInRegionRequest request =
                AllelesInRegionRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();

            Iterator<AllelesResponse> response = channel.getBlockingStub().selectVariantsInRegion(request);
            while (response.hasNext()) {
                variants.addAll(response.next().getAllelesList());
            }

            Gson gson = new Gson();

            for (Variant allele : variants) {
                alleles.add(gson.toJson(allele));
            }
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        if (alleles.isEmpty()) {
            alleles.add("{}");
        }

        return alleles;
    }

    public List<String> selectVariantsInRegionInSample(String chromosome, int start, int end, String sample, boolean selectHom,
                            boolean selectHet, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
                            Boolean biallelicOnly, Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                            String impact, String biotype, String feature, String variantType, String consequences, String alphaMissense,
                            String clinSignificance, Integer skip, Integer limit) {
        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");
        if (sample == null || sample.isEmpty()) return List.of("{}");

        if (skip == null || skip < 0) skip = 0;
        if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) limit = MAX_RETURNED_ITEMS;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        List<Variant> variants = new ArrayList<>();
        List<String> alleles = new ArrayList<>();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            AllelesInRegionInSamplesRequest request =
                AllelesInRegionInSamplesRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .addSamples(sample)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();

            Iterator<AllelesResponse> response = channel.getBlockingStub().selectVariantsInRegionInSamples(request);
            while (response.hasNext()) {
                variants.addAll(response.next().getAllelesList());
            }

            Gson gson = new Gson();
            for (Variant allele : variants) {
                alleles.add(gson.toJson(allele));
            }
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        if (alleles.isEmpty()) {
            alleles.add("{}");
        }

        return alleles;
    }

    public long countSamplesInRegion(String chromosome, int start, int end, boolean selectHom, boolean selectHet,
                    String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
                    Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                    String impact, String biotype, String feature, String variantType, String consequences,
                    String alphaMissense, String clinSignificance) {
        if (start < 0) return 0L;
        if (end < start) return 0L;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return 0L;

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            SamplesInRegionRequest request =
                SamplesInRegionRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .build();

            return channel.getBlockingStub().countSamplesInRegion(request).getCount();
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }
        return 0L; // default
    }

    public List<String> selectSamplesInRegion(String chromosome, int start, int end, boolean selectHom, boolean selectHet,
                            String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
                            Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                            String impact, String biotype, String feature, String variantType, String consequences,
                            String alphaMissense, String clinSignificance) {
        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            SamplesInRegionRequest request =
                SamplesInRegionRequest
                    .newBuilder()
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setHom(selectHom)
                    .setHet(selectHet)
                    .setAnn(annotations)
                    .build();

            List<String> samples = new ArrayList<>();
            samples.addAll(channel.getBlockingStub().selectSamplesInRegion(request).getSamplesList());
            return samples;
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        return List.of("{}"); // default
    }

    public List<String> selectDeNovo(String parent1, String parent2, String proband, String chromosome, int start, int end,
                            String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength, Boolean biallelicOnly,
                            Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                            String impact, String biotype, String feature, String variantType, String consequences,
                            String alphaMissense, String clinSignificance, Integer skip, Integer limit) {
        if (parent1 == null || parent1.isEmpty()) return List.of("{}");
        if (parent2 == null || parent2.isEmpty()) return List.of("{}");
        if (proband == null || proband.isEmpty()) return List.of("{}");

        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");

        if (skip == null || skip < 0) skip = 0;
        if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) limit = MAX_RETURNED_ITEMS;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        List<Variant> variants = new ArrayList<>();
        List<String> alleles = new ArrayList<>();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            DeNovoRequest request =
                DeNovoRequest
                    .newBuilder()
                    .setParent1(parent1)
                    .setParent2(parent2)
                    .setProband(proband)
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setAnn(annotations)
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();

            Iterator<AllelesResponse> response = channel.getBlockingStub().selectDeNovo(request);
            while (response.hasNext()) {
                variants.addAll(response.next().getAllelesList());
            }

            Gson gson = new Gson();
            for (Variant allele : variants) {
                alleles.add(gson.toJson(allele));
            }
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        if (alleles.isEmpty()) {
            alleles.add("{}");
        }

        return alleles;
    }

    public List<String> selectHetDominant(String affectedParent, String unaffectedParent, String proband, String chromosome,
                            int start, int end, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
                            Boolean biallelicOnly, Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                            String impact, String biotype, String feature, String variantType, String consequences,
                            String alphaMissense, String clinSignificance, Integer skip, Integer limit) {
        if (affectedParent == null || affectedParent.isEmpty()) return List.of("{}");
        if (unaffectedParent == null || unaffectedParent.isEmpty()) return List.of("{}");
        if (proband == null || proband.isEmpty()) return List.of("{}");

        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");

        if (skip == null || skip < 0) skip = 0;
        if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) limit = MAX_RETURNED_ITEMS;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        List<Variant> variants = new ArrayList<>();
        List<String> alleles = new ArrayList<>();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            HetDominantRequest request =
                HetDominantRequest
                    .newBuilder()
                    .setAffectedParent(affectedParent)
                    .setUnaffectedParent(unaffectedParent)
                    .setAffectedChild(proband)
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setAnn(annotations)
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();

            Iterator<AllelesResponse> response = channel.getBlockingStub().selectHetDominant(request);
            while (response.hasNext()) {
                variants.addAll(response.next().getAllelesList());
            }

            Gson gson = new Gson();
            for (Variant allele : variants) {
                alleles.add(gson.toJson(allele));
            }
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        if (alleles.isEmpty()) {
            alleles.add("{}");
        }

        return alleles;
    }

    public List<String> selectHomRecessive(String unaffectedParent1, String unaffectedParent2, String proband, String chromosome,
                            int start, int end, String refAllele, String altAllele, Integer varMinLength, Integer varMaxLength,
                            Boolean biallelicOnly, Float afLessThan, Float afGreaterThan, Float gnomadAfLessThan, Float gnomadAfGreaterThan,
                            String impact, String biotype, String feature, String variantType, String consequences,
                            String alphaMissense, String clinSignificance, Integer skip, Integer limit) {
        if (unaffectedParent1 == null || unaffectedParent1.isEmpty()) return List.of("{}");
        if (unaffectedParent2 == null || unaffectedParent2.isEmpty()) return List.of("{}");
        if (proband == null || proband.isEmpty()) return List.of("{}");

        if (start < 0) return List.of("{}");
        if (end < start) return List.of("{}");

        if (skip == null || skip < 0) skip = 0;
        if (limit == null || limit < 0 || limit > MAX_RETURNED_ITEMS) limit = MAX_RETURNED_ITEMS;

        RefAssembly assembly = RefAssembly.GRCh38;
        Chromosome chr = ContigsMapping.contigName2GrpcChr(chromosome);

        if (chr.equals(Chromosome.UNRECOGNIZED)) return List.of("{}");

        String referenceBases = refAllele == null ? "" : refAllele;
        String alternateBases = altAllele == null ? "" : altAllele;
        Integer variantMinLength = varMinLength == null || varMinLength <= 0 ? 0 : varMinLength;
        Integer variantMaxLength = varMaxLength == null || varMaxLength <= 0 ? 0 : varMaxLength;

        if (variantMaxLength < variantMinLength) { // fall back to defaults
            variantMinLength = 0;
            variantMaxLength = Integer.MAX_VALUE;
        }

        Annotations annotations = composeAnnotations(afLessThan, afGreaterThan, gnomadAfLessThan, gnomadAfGreaterThan, impact, biotype, feature,
                                                     variantType, consequences, alphaMissense, clinSignificance, biallelicOnly);

        List<Variant> variants = new ArrayList<>();
        List<String> alleles = new ArrayList<>();

        try {
            GrpcChannel channel = GrpcChannel.getInstance();

            HomRecessiveRequest request =
                HomRecessiveRequest
                    .newBuilder()
                    .setUnaffectedParent1(unaffectedParent1)
                    .setUnaffectedParent2(unaffectedParent2)
                    .setAffectedChild(proband)
                    .setAssembly(assembly)
                    .setChr(chr)
                    .setStart(start)
                    .setEnd(end)
                    .setAlt(alternateBases)
                    .setRef(referenceBases)
                    .setVariantMinLength(variantMinLength)
                    .setVariantMaxLength(variantMaxLength)
                    .setAnn(annotations)
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();

            Iterator<AllelesResponse> response = channel.getBlockingStub().selectHomRecessive(request);
            while (response.hasNext()) {
                variants.addAll(response.next().getAllelesList());
            }

            Gson gson = new Gson();
            for (Variant allele : variants) {
                alleles.add(gson.toJson(allele));
            }
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        if (alleles.isEmpty()) {
            alleles.add("{}");
        }

        return alleles;
    }

    public String kinship(String sample1, String sample2) {
        if (sample1 == null || sample1.isEmpty()) return "";
        if (sample2 == null || sample2.isEmpty()) return "";

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
        } catch (Throwable th) {
            th.printStackTrace();
            Logger.getLogger(DnaerysClient.class.getName()).log(Level.SEVERE, th.getMessage());
        }

        return ""; // default
    }
}
