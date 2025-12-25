package org.dnaerys.client.entity;

import org.dnaerys.cluster.grpc.ClinSignificance;

public class ClinSigMapper {
    public static ClinSignificance fromString(String clinSig) {
        if (clinSig == null) return ClinSignificance.UNRECOGNIZED;

        // Normalize input: uppercase, underscores instead of spaces or dashes
        String normalized = clinSig
            .trim()
            .toUpperCase()
            .replace(' ', '_')
            .replace('-', '_');

        try {
            return ClinSignificance.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ClinSignificance.UNRECOGNIZED;
        }
    }
}