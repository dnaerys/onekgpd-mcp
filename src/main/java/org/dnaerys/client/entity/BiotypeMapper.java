package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.BioType;

public class BiotypeMapper {
    public static BioType fromString(String biotype) {
        if (biotype == null) return BioType.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = biotype
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return BioType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return BioType.UNRECOGNIZED;
        }
    }
}