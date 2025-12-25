package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.FeatureType;

public class FeatureTypeMapper {
    public static FeatureType fromString(String feature) {
        if (feature == null) return FeatureType.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = feature
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return FeatureType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return FeatureType.UNRECOGNIZED;
        }
    }
}