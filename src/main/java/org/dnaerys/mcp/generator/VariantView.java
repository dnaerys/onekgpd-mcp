package org.dnaerys.mcp.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.dnaerys.cluster.grpc.Chromosome;
import org.dnaerys.cluster.grpc.Variant;

import java.io.IOException;

//    @JsonPropertyOrder({ "chr", "pos", "ref", "alt", "AF", "AC", "AN", "het", "hom", "gnomADe", "gnomADg", "AlphaMissense", "HGVSp" })
@JsonSerialize(using = VariantView.VariantSerializer.class)
public record VariantView(
    int chrIdx,
    int pos,
    String ref,
    String alt,
    float AF,
    int AC,
    int AN,
    int het,
    int hom,
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
            // Counters logic: only include for autosomes to avoid misleading LLM on sex chromosomes
            v.getChr().getNumber() > 22 ? 0 : v.getHetc(), // counters in sex chr are split between males/females
            v.getChr().getNumber() > 22 ? 0 : v.getHomc(), // counters in sex chr are split between males/females
            v.getGnomADe(),
            v.getGnomADg(),
            v.getAmScore(),
            v.getAminoAcids()
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
            gen.writeNumberField("het", v.het);
            gen.writeNumberField("hom", v.hom);

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
                case CHR_X -> "X";
                case CHR_Y -> "Y";
                case CHR_MT -> "MT";
                case CHROMOSOME_UNSPECIFIED, UNRECOGNIZED -> "0";
                default -> String.valueOf(chr.getNumber());
            };
        }
    }
}