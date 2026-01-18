package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.AlphaMissense;

public class AlphaMissenseMapper {
    public static AlphaMissense fromString(String am) {
        if (am == null) return AlphaMissense.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = am
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        String prefix = "AM_";
        String amNormalized = prefix + normalized;

        try {
            return AlphaMissense.valueOf(amNormalized);
        } catch (IllegalArgumentException e) {
            return AlphaMissense.UNRECOGNIZED;
        }
    }
}
