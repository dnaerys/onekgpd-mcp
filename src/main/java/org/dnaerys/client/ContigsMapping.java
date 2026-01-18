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

import org.dnaerys.cluster.grpc.Chromosome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

public class ContigsMapping {
    static final Map<String, Chromosome> GRPC_CHR = Map.ofEntries(
        entry("1", Chromosome.CHR_1),
        entry("2", Chromosome.CHR_2),
        entry("3", Chromosome.CHR_3),
        entry("4", Chromosome.CHR_4),
        entry("5", Chromosome.CHR_5),
        entry("6", Chromosome.CHR_6),
        entry("7", Chromosome.CHR_7),
        entry("8", Chromosome.CHR_8),
        entry("9", Chromosome.CHR_9),
        entry("10", Chromosome.CHR_10),
        entry("11", Chromosome.CHR_11),
        entry("12", Chromosome.CHR_12),
        entry("13", Chromosome.CHR_13),
        entry("14", Chromosome.CHR_14),
        entry("15", Chromosome.CHR_15),
        entry("16", Chromosome.CHR_16),
        entry("17", Chromosome.CHR_17),
        entry("18", Chromosome.CHR_18),
        entry("19", Chromosome.CHR_19),
        entry("20", Chromosome.CHR_20),
        entry("21", Chromosome.CHR_21),
        entry("22", Chromosome.CHR_22),
        entry("X", Chromosome.CHR_X),
        entry("Y", Chromosome.CHR_Y),
        entry("MT", Chromosome.CHR_MT)
    );

    static Chromosome contigName2GrpcChr(String contig) {
        return GRPC_CHR.getOrDefault(contig, Chromosome.UNRECOGNIZED);
    }

    static List<Chromosome> contigName2GrpcChr(String[] contigs) {
        ArrayList<Chromosome> res = new ArrayList<>(contigs.length);
        for (String chr : contigs) {
            res.add(contigName2GrpcChr(chr));
        }
        return res;
    }
}
