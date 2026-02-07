package org.dnaerys.testdata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for managing test baselines.
 *
 * Baselines allow integration tests to detect server-side data changes.
 * On first run, actual values are captured. On subsequent runs, actual
 * values are compared against baselines with configurable thresholds.
 *
 * Usage:
 * - Run tests normally: ./mvnw verify -DskipITs=false
 * - Update baselines: ./mvnw verify -DskipITs=false -DupdateBaseline=true
 */
public class TestBaselines {

    private static final Logger LOGGER = Logger.getLogger(TestBaselines.class.getName());

    private static final String BASELINE_FILE = "test-baselines.properties";
    private static final String BASELINE_RESOURCE_PATH = "/test-baselines.properties";
    private static final double WARN_THRESHOLD = 0.005;  // 0.5%
    private static final double FAIL_THRESHOLD = 0.01;  // 1%

    private static Properties baselines;
    private static boolean baselinesDirty = false;

    /**
     * Result of comparing actual value against baseline.
     */
    public enum BaselineResult {
        /** Within 5% deviation - test passes */
        PASS,
        /** 0.5-1% deviation - test passes with warning logged */
        WARN,
        /** >1% deviation - test fails */
        FAIL,
        /** No baseline exists - value captured for first run */
        NO_BASELINE
    }

    /**
     * Comparison result with details.
     */
    public record ComparisonResult(
            BaselineResult result,
            String key,
            long actual,
            Long baseline,
            Double deviationPercent,
            String message
    ) {}

    /**
     * Check if running in baseline update mode.
     * Usage: ./mvnw verify -DskipITs=false -DupdateBaseline=true
     */
    public static boolean isUpdateMode() {
        return Boolean.getBoolean("updateBaseline");
    }

    /**
     * Load baselines from properties file.
     */
    private static synchronized Properties loadBaselines() {
        if (baselines != null) {
            return baselines;
        }

        baselines = new Properties();

        // Try to load from classpath (src/test/resources)
        try (InputStream is = TestBaselines.class.getResourceAsStream(BASELINE_RESOURCE_PATH)) {
            if (is != null) {
                baselines.load(is);
                LOGGER.info("Loaded " + baselines.size() + " baselines from " + BASELINE_FILE);
            } else {
                LOGGER.info("No baseline file found - will capture baselines on first run");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load baselines", e);
        }

        return baselines;
    }

    /**
     * Compare actual value against baseline.
     *
     * @param key Baseline key (e.g., "total.samples", "brca1.variant.count")
     * @param actual Actual value from test
     * @return Comparison result with details
     */
    public static ComparisonResult compare(String key, long actual) {
        Properties props = loadBaselines();

        // In update mode, always capture the new value
        if (isUpdateMode()) {
            update(key, actual);
            return new ComparisonResult(
                    BaselineResult.NO_BASELINE,
                    key,
                    actual,
                    null,
                    null,
                    "Baseline updated: " + key + " = " + actual
            );
        }

        String baselineStr = props.getProperty(key);

        // No baseline exists - capture it
        if (baselineStr == null || baselineStr.isEmpty()) {
            update(key, actual);
            return new ComparisonResult(
                    BaselineResult.NO_BASELINE,
                    key,
                    actual,
                    null,
                    null,
                    "First run - captured baseline: " + key + " = " + actual
            );
        }

        // Parse baseline and compare
        try {
            long baseline = Long.parseLong(baselineStr.trim());

            // Handle zero baseline specially
            if (baseline == 0) {
                if (actual == 0) {
                    return new ComparisonResult(
                            BaselineResult.PASS,
                            key,
                            actual,
                            baseline,
                            0.0,
                            "Baseline match (both zero): " + key
                    );
                } else {
                    // Baseline was 0 but now we have data - always warn
                    return new ComparisonResult(
                            BaselineResult.WARN,
                            key,
                            actual,
                            baseline,
                            100.0,
                            "Baseline was 0, now " + actual + " for: " + key
                    );
                }
            }

            // Calculate deviation percentage
            double deviation = Math.abs((double)(actual - baseline) / baseline);
            double deviationPercent = deviation * 100;

            BaselineResult result;
            String message;

            if (deviation <= WARN_THRESHOLD) {
                result = BaselineResult.PASS;
                message = String.format("Baseline check PASSED: %s (actual=%d, baseline=%d, deviation=%.2f%%)",
                        key, actual, baseline, deviationPercent);
            } else if (deviation <= FAIL_THRESHOLD) {
                result = BaselineResult.WARN;
                message = String.format("Baseline check WARNING: %s (actual=%d, baseline=%d, deviation=%.2f%% > %.0f%%)",
                        key, actual, baseline, deviationPercent, WARN_THRESHOLD * 100);
                LOGGER.warning(message);
            } else {
                result = BaselineResult.FAIL;
                message = String.format("Baseline check FAILED: %s (actual=%d, baseline=%d, deviation=%.2f%% > %.0f%%)",
                        key, actual, baseline, deviationPercent, FAIL_THRESHOLD * 100);
                LOGGER.severe(message);
            }

            return new ComparisonResult(result, key, actual, baseline, deviationPercent, message);

        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid baseline value for " + key + ": " + baselineStr);
            update(key, actual);
            return new ComparisonResult(
                    BaselineResult.NO_BASELINE,
                    key,
                    actual,
                    null,
                    null,
                    "Invalid baseline - recaptured: " + key + " = " + actual
            );
        }
    }

    /**
     * Update baseline value.
     *
     * @param key Baseline key
     * @param value New baseline value
     */
    public static synchronized void update(String key, long value) {
        Properties props = loadBaselines();
        props.setProperty(key, String.valueOf(value));
        baselinesDirty = true;
        LOGGER.fine("Updated baseline: " + key + " = " + value);
    }

    /**
     * Save baselines to file (call at end of test class).
     * Only writes if baselines were modified.
     */
    public static synchronized void saveBaselines() {
        if (!baselinesDirty || baselines == null || baselines.isEmpty()) {
            return;
        }

        // Find the test resources directory
        Path resourcesDir = findTestResourcesDir();
        if (resourcesDir == null) {
            LOGGER.warning("Could not find src/test/resources directory - baselines not saved");
            return;
        }

        Path baselinePath = resourcesDir.resolve(BASELINE_FILE);

        try (OutputStream os = Files.newOutputStream(baselinePath)) {
            String header = "Test Baselines - Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    "\nDataset: 1000 Genomes Phase 3 (GRCh38)";
            baselines.store(os, header);
            LOGGER.info("Saved baselines to: " + baselinePath);
            baselinesDirty = false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save baselines to: " + baselinePath, e);
        }
    }

    /**
     * Find the src/test/resources directory.
     */
    private static Path findTestResourcesDir() {
        // Try relative to current working directory
        Path[] candidates = {
                Path.of("src/test/resources"),
                Path.of("../src/test/resources"),
                Path.of("../../src/test/resources")
        };

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        // Try to find from classpath resource location
        try {
            java.net.URL url = TestBaselines.class.getResource("/application.properties");
            if (url != null && "file".equals(url.getProtocol())) {
                Path propsPath = Path.of(url.toURI());
                Path resourcesDir = propsPath.getParent();
                if (Files.isDirectory(resourcesDir)) {
                    return resourcesDir;
                }
            }
        } catch (Exception e) {
            LOGGER.fine("Could not determine resources dir from classpath: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get baseline value for a key (for informational purposes).
     *
     * @param key Baseline key
     * @return Baseline value or null if not set
     */
    public static Long getBaseline(String key) {
        Properties props = loadBaselines();
        String value = props.getProperty(key);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Check if a baseline exists for a key.
     */
    public static boolean hasBaseline(String key) {
        return getBaseline(key) != null;
    }
}
