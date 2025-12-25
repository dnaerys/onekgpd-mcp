package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.Consequence;

public class ConsequencesMapper {
    public static Consequence fromString(String so) {
        if (so == null) return Consequence.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = so
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return Consequence.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return Consequence.UNRECOGNIZED;
        }
    }
}