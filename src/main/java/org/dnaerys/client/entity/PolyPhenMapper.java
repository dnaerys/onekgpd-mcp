package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.PolyPhen;

public class PolyPhenMapper {
    public static PolyPhen fromString(String polyPhen) {
        if (polyPhen == null) return PolyPhen.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = polyPhen
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return PolyPhen.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return PolyPhen.UNRECOGNIZED;
        }
    }
}