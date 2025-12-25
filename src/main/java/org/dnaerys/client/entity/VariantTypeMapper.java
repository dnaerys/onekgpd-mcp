package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.VariantType;

public class VariantTypeMapper {
    public static VariantType fromString(String vt) {
        if (vt == null) return VariantType.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = vt
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return VariantType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return VariantType.UNRECOGNIZED;
        }
    }
}