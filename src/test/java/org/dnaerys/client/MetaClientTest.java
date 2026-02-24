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

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.dnaerys.client.entity.PopulationInfo;
import org.dnaerys.client.entity.PopulationStats;
import org.dnaerys.client.entity.SuperpopulationInfo;
import org.dnaerys.client.entity.SuperpopulationSummary;
import org.dnaerys.client.entity.SampleMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MetaClient Tests")
@QuarkusTest
class MetaClientTest {

    @Inject
    MetaClient metaClient;

    @Nested
    @DisplayName("selectSamplesByPopulation Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Both params null throws RuntimeException")
        void bothNull() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation(null, null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("At least one parameter");
        }

        @Test
        @DisplayName("Both params blank throws RuntimeException")
        void bothBlank() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation("  ", "  ", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("At least one parameter");
        }

        @Test
        @DisplayName("Unrecognised population throws RuntimeException")
        void unrecognisedPopulation() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation("INVALID_POP", null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unrecognised population");
        }

        @Test
        @DisplayName("Unrecognised superpopulation throws RuntimeException")
        void unrecognisedSuperpopulation() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation(null, "INVALID_REG", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unrecognised superpopulation");
        }
    }

    @Nested
    @DisplayName("Population Query Tests")
    class PopulationQueryTests {

        @Test
        @DisplayName("Short code 'GBR' returns samples")
        void shortCodeGBR() {
            List<String> samples = metaClient.selectSamplesByPopulation("GBR", null, null, null);
            assertThat(samples).isNotEmpty();
            assertThat(samples).contains("HG00096");
        }

        @Test
        @DisplayName("Full name 'British in England and Scotland' returns samples")
        void fullNameGBR() {
            List<String> samples = metaClient.selectSamplesByPopulation(
                "British in England and Scotland", null, null, null);
            assertThat(samples).isNotEmpty();
        }

        @Test
        @DisplayName("Short code and full name return same results with same pagination")
        void shortCodeAndFullNameMatch() {
            List<String> byCode = metaClient.selectSamplesByPopulation("GBR", null, null, null);
            List<String> byName = metaClient.selectSamplesByPopulation(
                "British in England and Scotland", null, null, null);
            assertThat(byCode).isEqualTo(byName);
        }
    }

    @Nested
    @DisplayName("Superpopulation Query Tests")
    class SuperpopulationQueryTests {

        @Test
        @DisplayName("Short code 'EUR' returns samples")
        void shortCodeEUR() {
            List<String> samples = metaClient.selectSamplesByPopulation(null, "EUR", null, null);
            assertThat(samples).isNotEmpty();
        }

        @Test
        @DisplayName("Full name 'Europe' returns samples")
        void fullNameEurope() {
            List<String> samples = metaClient.selectSamplesByPopulation(null, "Europe", null, null);
            assertThat(samples).isNotEmpty();
        }

        @Test
        @DisplayName("Superpopulation contains all populations within it")
        void superpopulationContainsPopulations() {
            List<String> eurSamples = metaClient.selectSamplesByPopulation(null, "EUR", 0, 3202);
            List<String> gbrSamples = metaClient.selectSamplesByPopulation("GBR", null, 0, 3202);
            assertThat(eurSamples).containsAll(gbrSamples);
        }
    }

    @Nested
    @DisplayName("Combined Query Tests")
    class CombinedQueryTests {

        @Test
        @DisplayName("Population + matching superpopulation returns same as population alone")
        void populationAndMatchingSuperpopulation() {
            List<String> samples = metaClient.selectSamplesByPopulation("GBR", "EUR", 0, 3202);
            List<String> popOnly = metaClient.selectSamplesByPopulation("GBR", null, 0, 3202);
            assertThat(samples).isEqualTo(popOnly);
        }

        @Test
        @DisplayName("Mismatched population + superpopulation returns empty list")
        void mismatchedPopulationSuperpopulation() {
            List<String> samples = metaClient.selectSamplesByPopulation("CHS", "EUR", null, null);
            assertThat(samples).isEmpty();
        }
    }

    @Nested
    @DisplayName("Result Ordering Tests")
    class OrderingTests {

        @Test
        @DisplayName("Results are sorted by externalID")
        void resultsSorted() {
            List<String> samples = metaClient.selectSamplesByPopulation("GBR", null, null, null);
            assertThat(samples).isSorted();
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Default pagination returns at most 50 results")
        void defaultPaginationLimit() {
            List<String> samples = metaClient.selectSamplesByPopulation("GBR", null, null, null);
            assertThat(samples).hasSizeLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("Explicit skip + limit returns correct window")
        void explicitSkipAndLimit() {
            List<String> allSamples = metaClient.selectSamplesByPopulation("GBR", null, 0, 3202);
            List<String> page = metaClient.selectSamplesByPopulation("GBR", null, 5, 10);
            assertThat(page).hasSize(10);
            assertThat(page).isEqualTo(allSamples.subList(5, 15));
        }

        @Test
        @DisplayName("Skip beyond total result count returns empty list")
        void skipBeyondTotal() {
            List<String> samples = metaClient.selectSamplesByPopulation("GBR", null, 9999, 50);
            assertThat(samples).isEmpty();
        }

        @Test
        @DisplayName("limit > 3202 throws RuntimeException")
        void limitTooHigh() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation("GBR", null, 0, 3203))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("'limit' must be between 1 and 3202");
        }

        @Test
        @DisplayName("skip < 0 throws RuntimeException")
        void skipNegative() {
            assertThatThrownBy(() -> metaClient.selectSamplesByPopulation("GBR", null, -1, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("'skip' must be >= 0");
        }

        @Test
        @DisplayName("Known population GBR has more than 50 total samples")
        void gbrHasMoreThan50() {
            List<String> allGBR = metaClient.selectSamplesByPopulation("GBR", null, 0, 3202);
            assertThat(allGBR).hasSizeGreaterThan(50);
        }
    }

    @Nested
    @DisplayName("getSampleMeta Tests")
    class GetSampleMetaTests {

        @Test
        @DisplayName("Trio father HG00427 has all fields including child")
        void trioFather() {
            List<SampleMeta> metas = metaClient.getSampleMeta(List.of("HG00427"));
            assertThat(metas).hasSize(1);
            SampleMeta m = metas.get(0);
            assertThat(m.sampleId()).isEqualTo("HG00427");
            assertThat(m.familyId()).isEqualTo("SH009");
            assertThat(m.gender()).isEqualTo("male");
            assertThat(m.relationship()).isEqualTo("father");
            assertThat(m.paternalId()).isNull();
            assertThat(m.maternalId()).isNull();
            assertThat(m.children()).isNotNull().contains("HG00429");
            assertThat(m.populationCode()).isEqualTo("CHS");
            assertThat(m.superpopulationCode()).isEqualTo("EAS");
            assertThat(m.population()).isEqualTo("Southern Han Chinese, China");
            assertThat(m.superpopulation()).isEqualTo("East Asia");
        }

        @Test
        @DisplayName("Non-trio sample HG00097 has nullable fields as null")
        void nonTrioSample() {
            List<SampleMeta> metas = metaClient.getSampleMeta(List.of("HG00097"));
            assertThat(metas).hasSize(1);
            SampleMeta m = metas.get(0);
            assertThat(m.sampleId()).isEqualTo("HG00097");
            assertThat(m.gender()).isEqualTo("female");
            assertThat(m.paternalId()).isNull();
            assertThat(m.maternalId()).isNull();
            assertThat(m.relationship()).isNull();
            assertThat(m.children()).isNull();
            assertThat(m.populationCode()).isEqualTo("GBR");
            assertThat(m.superpopulationCode()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("Multiple samples returns all requested")
        void multipleSamples() {
            List<SampleMeta> metas = metaClient.getSampleMeta(List.of("HG00427", "HG00097"));
            assertThat(metas).hasSize(2);
            assertThat(metas).extracting(SampleMeta::sampleId)
                .containsExactly("HG00097", "HG00427");
        }

        @Test
        @DisplayName("Unknown sample ID throws RuntimeException")
        void unknownSampleId() {
            assertThatThrownBy(() -> metaClient.getSampleMeta(List.of("NONEXISTENT")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unknown sample IDs");
        }

        @Test
        @DisplayName("Empty list throws RuntimeException")
        void emptyList() {
            assertThatThrownBy(() -> metaClient.getSampleMeta(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must not be null or empty");
        }

        @Test
        @DisplayName("Child sample HG00429 has parental IDs set")
        void childSample() {
            List<SampleMeta> metas = metaClient.getSampleMeta(List.of("HG00429"));
            assertThat(metas).hasSize(1);
            SampleMeta m = metas.get(0);
            assertThat(m.sampleId()).isEqualTo("HG00429");
            assertThat(m.paternalId()).isEqualTo("HG00427");
            assertThat(m.maternalId()).isEqualTo("HG00428");
            assertThat(m.relationship()).isEqualTo("child");
        }
    }

    @Nested
    @DisplayName("listPopulations Tests")
    class ListPopulationsTests {

        @Test
        @DisplayName("Returns non-empty list")
        void returnsNonEmpty() {
            List<PopulationInfo> pops = metaClient.listPopulations();
            assertThat(pops).isNotEmpty();
        }

        @Test
        @DisplayName("Known population CHS present with correct fields")
        void knownPopulationCHS() {
            List<PopulationInfo> pops = metaClient.listPopulations();
            PopulationInfo chs = pops.stream()
                .filter(p -> "CHS".equals(p.populationCode()))
                .findFirst().orElseThrow();
            assertThat(chs.population()).isEqualTo("Southern Han Chinese, China");
            assertThat(chs.superpopulationCode()).isEqualTo("EAS");
            assertThat(chs.superpopulation()).isEqualTo("East Asia");
            assertThat(chs.sampleCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("All entries have sampleCount > 0")
        void allPositiveSampleCount() {
            List<PopulationInfo> pops = metaClient.listPopulations();
            assertThat(pops).allMatch(p -> p.sampleCount() > 0);
        }

        @Test
        @DisplayName("Ordered by superpopulationCode then populationCode")
        void ordering() {
            List<PopulationInfo> pops = metaClient.listPopulations();
            for (int i = 1; i < pops.size(); i++) {
                PopulationInfo prev = pops.get(i - 1);
                PopulationInfo curr = pops.get(i);
                int cmp = prev.superpopulationCode().compareTo(curr.superpopulationCode());
                if (cmp == 0) {
                    assertThat(prev.populationCode()).isLessThanOrEqualTo(curr.populationCode());
                } else {
                    assertThat(cmp).isLessThan(0);
                }
            }
        }
    }

    @Nested
    @DisplayName("listSuperpopulations Tests")
    class ListSuperpopulationsTests {

        @Test
        @DisplayName("Returns exactly 5 entries")
        void exactlyFiveSuperpopulations() {
            List<SuperpopulationInfo> superpops = metaClient.listSuperpopulations();
            assertThat(superpops).hasSize(5);
        }

        @Test
        @DisplayName("Known superpopulation EAS has correct fields and contains CHS")
        void knownSuperpopulationEAS() {
            List<SuperpopulationInfo> superpops = metaClient.listSuperpopulations();
            SuperpopulationInfo eas = superpops.stream()
                .filter(r -> "EAS".equals(r.superpopulationCode()))
                .findFirst().orElseThrow();
            assertThat(eas.superpopulation()).isEqualTo("East Asia");
            assertThat(eas.sampleCount()).isGreaterThan(0);
            assertThat(eas.populations()).contains("CHS");
        }

        @Test
        @DisplayName("All population codes in superpopulations are valid")
        void allPopulationCodesValid() {
            List<SuperpopulationInfo> superpops = metaClient.listSuperpopulations();
            List<PopulationInfo> allPops = metaClient.listPopulations();
            Set<String> validCodes = allPops.stream()
                .map(PopulationInfo::populationCode)
                .collect(Collectors.toSet());

            for (SuperpopulationInfo superpop : superpops) {
                assertThat(validCodes).containsAll(superpop.populations());
            }
        }

        @Test
        @DisplayName("Total sampleCount across all superpopulations is 3202")
        void totalSampleCount() {
            List<SuperpopulationInfo> superpops = metaClient.listSuperpopulations();
            int total = superpops.stream().mapToInt(SuperpopulationInfo::sampleCount).sum();
            assertThat(total).isEqualTo(3202);
        }
    }

    @Nested
    @DisplayName("getPopulationStats Tests")
    class GetPopulationStatsTests {

        @Test
        @DisplayName("Single population CHS has consistent counts")
        void singlePopulationCHS() {
            List<PopulationStats> stats = metaClient.getPopulationStats(List.of("CHS"));
            assertThat(stats).hasSize(1);
            PopulationStats s = stats.get(0);
            assertThat(s.populationCode()).isEqualTo("CHS");
            assertThat(s.population()).isEqualTo("Southern Han Chinese, China");
            assertThat(s.superpopulationCode()).isEqualTo("EAS");
            assertThat(s.maleCount() + s.femaleCount()).isEqualTo(s.sampleCount());
            assertThat(s.trioCount()).isLessThanOrEqualTo(s.sampleCount());
            assertThat(s.phase3Count()).isLessThanOrEqualTo(s.sampleCount());
        }

        @Test
        @DisplayName("Multiple populations in one call")
        void multiplePopulations() {
            List<PopulationStats> stats = metaClient.getPopulationStats(List.of("CHS", "GBR"));
            assertThat(stats).hasSize(2);
            assertThat(stats).extracting(PopulationStats::populationCode)
                .containsExactly("CHS", "GBR");
        }

        @Test
        @DisplayName("Null list throws RuntimeException")
        void nullList() {
            assertThatThrownBy(() -> metaClient.getPopulationStats(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must not be null or empty");
        }

        @Test
        @DisplayName("Empty list throws RuntimeException")
        void emptyList() {
            assertThatThrownBy(() -> metaClient.getPopulationStats(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must not be null or empty");
        }

        @Test
        @DisplayName("Unknown population code throws RuntimeException")
        void unknownPopulation() {
            assertThatThrownBy(() -> metaClient.getPopulationStats(List.of("INVALID")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unrecognised population values");
        }

        @Test
        @DisplayName("Mix of valid + invalid throws RuntimeException")
        void mixedValidInvalid() {
            assertThatThrownBy(() -> metaClient.getPopulationStats(List.of("CHS", "INVALID")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unrecognised population values")
                .hasMessageContaining("INVALID");
        }
    }

    @Nested
    @DisplayName("getSuperpopulationSummary Tests")
    class GetSuperpopulationSummaryTests {

        @Test
        @DisplayName("Single superpopulation EAS has consistent top-level and nested counts")
        void singleSuperpopulationEAS() {
            List<SuperpopulationSummary> summaries = metaClient.getSuperpopulationSummary(List.of("EAS"));
            assertThat(summaries).hasSize(1);
            SuperpopulationSummary s = summaries.get(0);
            assertThat(s.superpopulationCode()).isEqualTo("EAS");
            assertThat(s.superpopulation()).isEqualTo("East Asia");
            assertThat(s.populations()).isNotEmpty();

            int popSampleSum = s.populations().stream().mapToInt(PopulationStats::sampleCount).sum();
            int popMaleSum = s.populations().stream().mapToInt(PopulationStats::maleCount).sum();
            int popFemaleSum = s.populations().stream().mapToInt(PopulationStats::femaleCount).sum();
            int popPhase3Sum = s.populations().stream().mapToInt(PopulationStats::phase3Count).sum();
            int popTrioSum = s.populations().stream().mapToInt(PopulationStats::trioCount).sum();

            assertThat(s.sampleCount()).isEqualTo(popSampleSum);
            assertThat(s.maleCount()).isEqualTo(popMaleSum);
            assertThat(s.femaleCount()).isEqualTo(popFemaleSum);
            assertThat(s.phase3Count()).isEqualTo(popPhase3Sum);
            assertThat(s.trioCount()).isEqualTo(popTrioSum);
        }

        @Test
        @DisplayName("Multiple superpopulations")
        void multipleSuperpopulations() {
            List<SuperpopulationSummary> summaries = metaClient.getSuperpopulationSummary(List.of("EAS", "EUR"));
            assertThat(summaries).hasSize(2);
            assertThat(summaries).extracting(SuperpopulationSummary::superpopulationCode)
                .containsExactly("EAS", "EUR");
        }

        @Test
        @DisplayName("Nested populations list is non-empty and ordered")
        void nestedPopulationsOrdered() {
            List<SuperpopulationSummary> summaries = metaClient.getSuperpopulationSummary(List.of("EAS"));
            List<PopulationStats> pops = summaries.get(0).populations();
            assertThat(pops).isNotEmpty();
            assertThat(pops).extracting(PopulationStats::populationCode).isSorted();
        }

        @Test
        @DisplayName("Null list throws RuntimeException")
        void nullList() {
            assertThatThrownBy(() -> metaClient.getSuperpopulationSummary(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must not be null or empty");
        }

        @Test
        @DisplayName("Empty list throws RuntimeException")
        void emptyList() {
            assertThatThrownBy(() -> metaClient.getSuperpopulationSummary(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must not be null or empty");
        }

        @Test
        @DisplayName("Unknown superpopulation throws RuntimeException")
        void unknownSuperpopulation() {
            assertThatThrownBy(() -> metaClient.getSuperpopulationSummary(List.of("INVALID")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unrecognised superpopulation values");
        }

        @Test
        @DisplayName("Total sampleCount across all 5 superpopulations equals 3202")
        void totalAcrossAllSuperpopulations() {
            List<SuperpopulationSummary> summaries = metaClient.getSuperpopulationSummary(
                List.of("AFR", "AMR", "EAS", "EUR", "SAS"));
            int total = summaries.stream().mapToInt(SuperpopulationSummary::sampleCount).sum();
            assertThat(total).isEqualTo(3202);
        }
    }
}
