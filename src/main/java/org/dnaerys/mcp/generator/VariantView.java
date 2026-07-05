package org.dnaerys.mcp.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.dnaerys.cluster.grpc.Chromosome;
import org.dnaerys.cluster.grpc.Variant;

import java.io.IOException;

//    @JsonPropertyOrder({ "chr", "pos", "ref", "alt", "AF", "AC", "AN", "HetSamplesCount", "HomSamplesCount", "gnomADe", "gnomADg", "AlphaMissense", "HGVSp" })
@JsonSerialize(using = VariantView.VariantSerializer.class)
public record VariantView(
    int chrIdx,
    int pos,
    String ref,
    String alt,
    float AF,
    int AC,
    int AN,
    int HetSamplesCount,
    int HomSamplesCount,
    float gnomADe,
    float gnomADg,
    float AlphaMissense,
    String HGVSp
) {

    /**
     * Factory method for clean mapping from gRPC.
     */
    public static VariantView fromGrpc(Variant v) {
        return new VariantView(
            v.getChrValue(),
            v.getStart(),
            v.getRef(),
            v.getAlt(),
            v.getAf(),
            (int) v.getAc(),
            v.getAn(),
            v.getHetSamples(),
            v.getHomSamples(),
            v.getGnomADe(),
            v.getGnomADg(),
            v.getAmScore(),
            v.getAminoAcids().isBlank() ? "" : v.getAminoAcids().split(",")[0]
        );
    }

    /**
     * The Manual Serializer: no reflection, minimal overhead, skips defaults.
     */
    public static class VariantSerializer extends JsonSerializer<VariantView> {
        @Override
        public void serialize(VariantView v, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            // Required Fields
            gen.writeStringField("chr", mapChr(v.chrIdx()));
            gen.writeNumberField("pos", v.pos());
            gen.writeStringField("ref", v.ref());
            gen.writeStringField("alt", v.alt());
            gen.writeNumberField("AF", v.AF);
            gen.writeNumberField("AC", v.AC);
            gen.writeNumberField("AN", v.AN);
            gen.writeNumberField("HetSamplesCount", v.HetSamplesCount);
            gen.writeNumberField("HomSamplesCount", v.HomSamplesCount);

            // Optional (NON_DEFAULT logic)
            if (v.gnomADe != 0.0f) gen.writeNumberField("gnomADe", v.gnomADe);
            if (v.gnomADg != 0.0f) gen.writeNumberField("gnomADg", v.gnomADg);
            if (v.AlphaMissense != 0.0f) gen.writeNumberField("AlphaMissense", v.AlphaMissense);

            if (v.HGVSp != null && !v.HGVSp.isEmpty()) {
                gen.writeStringField("HGVSp", v.HGVSp);
            }

            gen.writeEndObject();
        }

        private String mapChr(int chrIdx) {
            Chromosome chr = Chromosome.forNumber(chrIdx);
            if (chr == null) return "0";
            return switch (chr) {
                case CHRX -> "X";
                case CHRY -> "Y";
                case CHRMT -> "MT";
                case CHROMOSOME_UNSPECIFIED, UNRECOGNIZED -> "0";
                default -> String.valueOf(chr.getNumber());
            };
        }
    }
}