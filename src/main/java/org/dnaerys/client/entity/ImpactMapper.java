package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.Impact;

public class ImpactMapper {
    public static Impact fromString(String impact) {
        if (impact == null) return Impact.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = impact
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return Impact.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return Impact.UNRECOGNIZED;
        }
    }
}