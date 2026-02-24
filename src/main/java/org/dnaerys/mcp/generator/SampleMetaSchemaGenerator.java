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

package org.dnaerys.mcp.generator;

import io.quarkiverse.mcp.server.OutputSchemaGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SampleMetaSchemaGenerator implements OutputSchemaGenerator {

    @Override
    public Map<String, Object> generate(Class<?> type) {
        Map<String, Object> sampleFields = Map.ofEntries(
            Map.entry("sampleId", Map.of("type", "string", "description", "Sample external ID")),
            Map.entry("familyId", Map.of("type", "string", "description", "Family ID (present for trio members)")),
            Map.entry("gender", Map.of("type", "string", "description", "Gender (male/female)")),
            Map.entry("paternalId", Map.of("type", "string", "description", "Paternal sample ID (present for children in trios)")),
            Map.entry("maternalId", Map.of("type", "string", "description", "Maternal sample ID (present for children in trios)")),
            Map.entry("relationship", Map.of("type", "string", "description", "Family relationship (father/mother/child, present for trio members)")),
            Map.entry("children", Map.of("type", "array", "items", Map.of("type", "string"), "description", "IDs of this sample's children in the dataset")),
            Map.entry("populationCode", Map.of("type", "string", "description", "Population short code (e.g. GBR, CHS)")),
            Map.entry("superpopulationCode", Map.of("type", "string", "description", "Superpopulation short code (e.g. EUR, EAS)")),
            Map.entry("population", Map.of("type", "string", "description", "Full population name")),
            Map.entry("superpopulation", Map.of("type", "string", "description", "Full superpopulation name")),
            Map.entry("phase3", Map.of("type", "string", "description", "Whether sample was in 1000 Genomes Phase 3 (TRUE/FALSE)"))
        );

        return Map.of(
            "type", "object",
            "properties", Map.of(
                "samples", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", sampleFields,
                        "required", List.of("sampleId", "gender", "populationCode", "superpopulationCode", "population", "superpopulation", "phase3")
                    )
                )
            ),
            "required", List.of("samples")
        );
    }
}
