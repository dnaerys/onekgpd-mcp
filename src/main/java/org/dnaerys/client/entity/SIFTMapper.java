package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.SIFT;

public class SIFTMapper {
    public static SIFT fromString(String sift) {
        if (sift == null) return SIFT.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = sift
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return SIFT.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return SIFT.UNRECOGNIZED;
        }
    }
}