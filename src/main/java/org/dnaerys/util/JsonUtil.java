package org.dnaerys.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dnaerys.cluster.grpc.Chromosome;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtil {
    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());

    // Configured to omit nulls and default zeros to save tokens
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * Maps gRPC Chromosome enum to LLM-optimal string representation "1"-"22", "X", "Y", "MT".
     */
    public static String mapChr(Chromosome chr) {
        if (chr == null) return "0";
        return switch (chr) {
            case CHR_X -> "X";
            case CHR_Y -> "Y";
            case CHR_MT -> "MT";
            case UNRECOGNIZED, CHROMOSOME_UNSPECIFIED -> "0";
            default -> chr.getNumber() + "";
        };
    }

    /**
     * Converts a single object to a JSON string.
     */
    public static String stringify(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "JSON stringify failed", e);
            return "{}";
        }
    }

    /**
     * The core requirement: returns a single String containing a JSON array.
     */
    public static <T> String toJsonArray(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) return "[]";
        return collection.stream()
            .map(JsonUtil::stringify)
            .collect(Collectors.joining(",", "[", "]"));
    }
}