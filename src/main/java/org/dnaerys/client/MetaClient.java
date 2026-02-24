/*
 * Copyright © 2026 Dmitry Degrave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dnaerys.client;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.dnaerys.db.DuckDBConnectionProducer;
import org.jboss.logging.Logger;

import org.dnaerys.client.entity.PopulationInfo;
import org.dnaerys.client.entity.PopulationStats;
import org.dnaerys.client.entity.SuperpopulationInfo;
import org.dnaerys.client.entity.SuperpopulationSummary;
import org.dnaerys.client.entity.SampleMeta;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MetaClient {

    private static final Logger LOG = Logger.getLogger(MetaClient.class);
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 3202;

    @Inject
    DuckDBConnectionProducer duckDB;

    private Connection connection;

    @PostConstruct
    void init() {
        connection = duckDB.getConnection();
        LOG.info("MetaClient initialized with DuckDB connection");
    }

    public List<String> selectSamplesByPopulation(String population, String region,
                                                  Integer skip, Integer limit) {
        boolean hasPop = population != null && !population.isBlank();
        boolean hasReg = region != null && !region.isBlank();

        if (!hasPop && !hasReg) {
            throw new RuntimeException(
                "At least one parameter ('population' or 'region') must be provided");
        }

        String popTrimmed = hasPop ? population.trim() : null;
        String regTrimmed = hasReg ? region.trim() : null;

        int effectiveSkip = (skip != null) ? skip : 0;
        int effectiveLimit = (limit != null) ? limit : DEFAULT_LIMIT;

        if (effectiveSkip < 0) {
            throw new RuntimeException("Invalid parameter: 'skip' must be >= 0, actual: " + effectiveSkip);
        }
        if (effectiveLimit < 1 || effectiveLimit > MAX_LIMIT) {
            throw new RuntimeException(
                "Invalid parameter: 'limit' must be between 1 and " + MAX_LIMIT + ",  actual: " + effectiveLimit);
        }

        validatePopulationRegionInputs(popTrimmed, regTrimmed);

        StringBuilder sql = new StringBuilder("SELECT externalIDs FROM sample_meta WHERE ");
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (hasPop) {
            conditions.add("(pop = ? OR \"Population\" = ?)");
            params.add(popTrimmed);
            params.add(popTrimmed);
        }

        if (hasReg) {
            conditions.add("(reg = ? OR region = ?)");
            params.add(regTrimmed);
            params.add(regTrimmed);
        }

        sql.append(String.join(" AND ", conditions));
        sql.append(" ORDER BY externalIDs LIMIT ? OFFSET ?");
        params.add(effectiveLimit);
        params.add(effectiveSkip);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else {
                    ps.setInt(i + 1, (Integer) p);
                }
            }

            ResultSet rs = ps.executeQuery();
            List<String> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rs.getString("externalIDs"));
            }

            LOG.debugf("selectSamplesByPopulation: found %d samples", results.size());
            return results;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    public List<SampleMeta> getSampleMeta(List<String> sampleIds) {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw new RuntimeException("Parameter 'sampleIds' must not be null or empty");
        }

        List<String> trimmedIds = sampleIds.stream()
            .map(String::trim)
            .toList();

        validateSampleIds(trimmedIds);

        String placeholders = trimmedIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));

        String sql =
            "SELECT s.externalIDs, s.familyId, s.gender, s.pid, s.mid, " +
            "s.\"Relationship\", s.pop, s.reg, s.\"Population\", s.region, s.phase3, " +
            "LIST(c.externalIDs) AS children " +
            "FROM sample_meta s " +
            "LEFT JOIN sample_meta c ON (c.pid = s.externalIDs OR c.mid = s.externalIDs) " +
            "AND c.pid != '0' AND c.mid != '0' " +
            "WHERE s.externalIDs IN (" + placeholders + ") " +
            "GROUP BY s.externalIDs, s.familyId, s.gender, s.pid, s.mid, " +
            "s.\"Relationship\", s.pop, s.reg, s.\"Population\", s.region, s.phase3 " +
            "ORDER BY s.externalIDs";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < trimmedIds.size(); i++) {
                ps.setString(i + 1, trimmedIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            List<SampleMeta> results = new ArrayList<>();
            while (rs.next()) {
                String pid = rs.getString("pid");
                String mid = rs.getString("mid");
                String relationship = rs.getString("Relationship");
                String familyId = rs.getString("familyId");

                Array childrenArray = rs.getArray("children");
                List<String> childList = null;
                if (childrenArray != null) {
                    Object[] arr = (Object[]) childrenArray.getArray();
                    List<String> nonNull = new ArrayList<>();
                    for (Object o : arr) {
                        if (o != null) nonNull.add(o.toString());
                    }
                    if (!nonNull.isEmpty()) {
                        Collections.sort(nonNull);
                        childList = nonNull;
                    }
                }

                results.add(new SampleMeta(
                    rs.getString("externalIDs"),
                    nullIfEmpty(familyId),
                    rs.getString("gender"),
                    nullIfAbsent(pid),
                    nullIfAbsent(mid),
                    nullIfEmpty(relationship),
                    childList,
                    rs.getString("pop"),
                    rs.getString("reg"),
                    rs.getString("Population"),
                    rs.getString("region"),
                    rs.getString("phase3")
                ));
            }

            LOG.debugf("getSampleMeta: found %d samples", results.size());
            return results;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    public List<PopulationInfo> listPopulations() {
        String sql =
            "SELECT pop, \"Population\", reg, region, COUNT(*) AS cnt " +
            "FROM sample_meta " +
            "GROUP BY pop, \"Population\", reg, region " +
            "ORDER BY reg, pop ";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<PopulationInfo> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new PopulationInfo(
                    rs.getString("pop"),
                    rs.getString("Population"),
                    rs.getString("reg"),
                    rs.getString("region"),
                    rs.getInt("cnt")
                ));
            }

            LOG.debugf("listPopulations: returned %d entries", results.size());
            return results;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    public List<SuperpopulationInfo> listSuperpopulations() {
        String sql =
            "SELECT reg, region, COUNT(*) AS cnt, LIST(DISTINCT pop ORDER BY pop) AS pops " +
            "FROM sample_meta " +
            "GROUP BY reg, region " +
            "ORDER BY reg";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<SuperpopulationInfo> results = new ArrayList<>();
            while (rs.next()) {
                Array popsArray = rs.getArray("pops");
                List<String> populations = new ArrayList<>();
                if (popsArray != null) {
                    Object[] arr = (Object[]) popsArray.getArray();
                    for (Object o : arr) {
                        if (o != null) populations.add(o.toString());
                    }
                }

                results.add(new SuperpopulationInfo(
                    rs.getString("reg"),
                    rs.getString("region"),
                    rs.getInt("cnt"),
                    populations
                ));
            }

            LOG.debugf("listSuperpopulations: returned %d entries", results.size());
            return results;

        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    public List<PopulationStats> getPopulationStats(List<String> populations) {
        if (populations == null || populations.isEmpty()) {
            throw new RuntimeException("Parameter 'populations' must not be null or empty");
        }

        List<String> trimmed = populations.stream().map(String::trim).toList();
        validatePopulationValues(trimmed);

        String placeholders = trimmed.stream().map(p -> "?").collect(Collectors.joining(", "));

        String sql =
            "SELECT pop, \"Population\", reg, region, " +
            "COUNT(*) AS cnt, " +
            "COUNT(CASE WHEN gender = 'male' THEN 1 END) AS male_cnt, " +
            "COUNT(CASE WHEN gender = 'female' THEN 1 END) AS female_cnt, " +
            "COUNT(CASE WHEN phase3 = 'TRUE' THEN 1 END) AS phase3_cnt, " +
            "COUNT(CASE WHEN pid IS NOT NULL AND pid != '0' AND pid != '' " +
            "AND mid IS NOT NULL AND mid != '0' AND mid != '' THEN 1 END) AS trio_cnt " +
            "FROM sample_meta " +
            "WHERE pop IN (" + placeholders + ") OR \"Population\" IN (" + placeholders + ") " +
            "GROUP BY pop, \"Population\", reg, region " +
            "ORDER BY pop";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < trimmed.size(); i++) {
                ps.setString(i + 1, trimmed.get(i));
                ps.setString(i + 1 + trimmed.size(), trimmed.get(i));
            }

            return readPopulationStats(ps);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    public List<SuperpopulationSummary> getSuperpopulationSummary(List<String> superpopulations) {
        if (superpopulations == null || superpopulations.isEmpty()) {
            throw new RuntimeException("Parameter 'superpopulations' must not be null or empty");
        }

        List<String> trimmed = superpopulations.stream().map(String::trim).toList();
        validateSuperpopulationValues(trimmed);

        String placeholders = trimmed.stream().map(r -> "?").collect(Collectors.joining(", "));

        // Get per-population stats for all populations within the requested superpopulations
        String sql =
            "SELECT pop, \"Population\", reg, region, " +
            "COUNT(*) AS cnt, " +
            "COUNT(CASE WHEN gender = 'male' THEN 1 END) AS male_cnt, " +
            "COUNT(CASE WHEN gender = 'female' THEN 1 END) AS female_cnt, " +
            "COUNT(CASE WHEN phase3 = 'TRUE' THEN 1 END) AS phase3_cnt, " +
            "COUNT(CASE WHEN pid IS NOT NULL AND pid != '0' AND pid != '' " +
            "AND mid IS NOT NULL AND mid != '0' AND mid != '' THEN 1 END) AS trio_cnt " +
            "FROM sample_meta " +
            "WHERE reg IN (" + placeholders + ") OR region IN (" + placeholders + ") " +
            "GROUP BY pop, \"Population\", reg, region " +
            "ORDER BY reg, pop";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < trimmed.size(); i++) {
                ps.setString(i + 1, trimmed.get(i));
                ps.setString(i + 1 + trimmed.size(), trimmed.get(i));
            }

            List<PopulationStats> popStats = readPopulationStats(ps);

            // Group by superpopulation in Java
            Map<String, List<PopulationStats>> bySuperpop = new LinkedHashMap<>();
            for (PopulationStats ps2 : popStats) {
                bySuperpop.computeIfAbsent(ps2.superpopulationCode(), k -> new ArrayList<>()).add(ps2);
            }

            List<SuperpopulationSummary> results = new ArrayList<>();
            for (var entry : bySuperpop.entrySet()) {
                List<PopulationStats> pops = entry.getValue();
                int sampleCount = pops.stream().mapToInt(PopulationStats::sampleCount).sum();
                int maleCount = pops.stream().mapToInt(PopulationStats::maleCount).sum();
                int femaleCount = pops.stream().mapToInt(PopulationStats::femaleCount).sum();
                int phase3Count = pops.stream().mapToInt(PopulationStats::phase3Count).sum();
                int trioCount = pops.stream().mapToInt(PopulationStats::trioCount).sum();

                results.add(new SuperpopulationSummary(
                    entry.getKey(),
                    pops.get(0).superpopulation(),
                    sampleCount, maleCount, femaleCount, phase3Count, trioCount,
                    pops
                ));
            }

            LOG.debugf("getSuperpopulationSummary: returned %d superpopulations", results.size());
            return results;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB query failed: " + e.getMessage(), e);
        }
    }

    private List<PopulationStats> readPopulationStats(PreparedStatement ps) throws Exception {
        ResultSet rs = ps.executeQuery();
        List<PopulationStats> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new PopulationStats(
                rs.getString("pop"),
                rs.getString("Population"),
                rs.getString("reg"),
                rs.getString("region"),
                rs.getInt("cnt"),
                rs.getInt("male_cnt"),
                rs.getInt("female_cnt"),
                rs.getInt("phase3_cnt"),
                rs.getInt("trio_cnt")
            ));
        }
        LOG.debugf("readPopulationStats: returned %d entries", results.size());
        return results;
    }

    private void validatePopulationValues(List<String> populations) {
        String placeholders = populations.stream().map(p -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT DISTINCT pop, \"Population\" FROM sample_meta " +
            "WHERE pop IN (" + placeholders + ") OR \"Population\" IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < populations.size(); i++) {
                ps.setString(i + 1, populations.get(i));
                ps.setString(i + 1 + populations.size(), populations.get(i));
            }

            ResultSet rs = ps.executeQuery();
            Set<String> found = new HashSet<>();
            while (rs.next()) {
                found.add(rs.getString("pop"));
                found.add(rs.getString("Population"));
            }

            List<String> unknown = populations.stream()
                .filter(p -> !found.contains(p))
                .toList();

            if (!unknown.isEmpty()) {
                throw new RuntimeException("Unrecognised population values: " + unknown);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB validation query failed: " + e.getMessage(), e);
        }
    }

    private void validateSuperpopulationValues(List<String> superpopulations) {
        String placeholders = superpopulations.stream().map(r -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT DISTINCT reg, region FROM sample_meta " +
            "WHERE reg IN (" + placeholders + ") OR region IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < superpopulations.size(); i++) {
                ps.setString(i + 1, superpopulations.get(i));
                ps.setString(i + 1 + superpopulations.size(), superpopulations.get(i));
            }

            ResultSet rs = ps.executeQuery();
            Set<String> found = new HashSet<>();
            while (rs.next()) {
                found.add(rs.getString("reg"));
                found.add(rs.getString("region"));
            }

            List<String> unknown = superpopulations.stream()
                .filter(r -> !found.contains(r))
                .toList();

            if (!unknown.isEmpty()) {
                throw new RuntimeException("Unrecognised superpopulation values: " + unknown);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB validation query failed: " + e.getMessage(), e);
        }
    }

    private void validateSampleIds(List<String> sampleIds) {
        String placeholders = sampleIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));

        String sql = "SELECT DISTINCT externalIDs FROM sample_meta WHERE externalIDs IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < sampleIds.size(); i++) {
                ps.setString(i + 1, sampleIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            Set<String> found = new HashSet<>();
            while (rs.next()) {
                found.add(rs.getString("externalIDs"));
            }

            List<String> unknown = sampleIds.stream()
                .filter(id -> !found.contains(id))
                .toList();

            if (!unknown.isEmpty()) {
                throw new RuntimeException("Unknown sample IDs: " + unknown);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB validation query failed: " + e.getMessage(), e);
        }
    }

    private void validatePopulationRegionInputs(String population, String region) {
        try {
            if (population != null) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT 1 FROM sample_meta WHERE pop = ? OR \"Population\" = ? LIMIT 1")) {
                    ps.setString(1, population);
                    ps.setString(2, population);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException(
                            "Unrecognised population: '" + population + "'");
                    }
                }
            }
            if (region != null) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT 1 FROM sample_meta WHERE reg = ? OR region = ? LIMIT 1")) {
                    ps.setString(1, region);
                    ps.setString(2, region);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException(
                            "Unrecognised superpopulation: '" + region + "'");
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DuckDB validation query failed: " + e.getMessage(), e);
        }
    }

    private static String nullIfAbsent(String value) {
        return (value == null || value.isEmpty() || "0".equals(value)) ? null : value;
    }

    private static String nullIfEmpty(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
