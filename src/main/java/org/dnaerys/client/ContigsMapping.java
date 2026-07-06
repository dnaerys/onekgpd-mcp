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
        entry("1", Chromosome.CHR1),
        entry("2", Chromosome.CHR2),
        entry("3", Chromosome.CHR3),
        entry("4", Chromosome.CHR4),
        entry("5", Chromosome.CHR5),
        entry("6", Chromosome.CHR6),
        entry("7", Chromosome.CHR7),
        entry("8", Chromosome.CHR8),
        entry("9", Chromosome.CHR9),
        entry("10", Chromosome.CHR10),
        entry("11", Chromosome.CHR11),
        entry("12", Chromosome.CHR12),
        entry("13", Chromosome.CHR13),
        entry("14", Chromosome.CHR14),
        entry("15", Chromosome.CHR15),
        entry("16", Chromosome.CHR16),
        entry("17", Chromosome.CHR17),
        entry("18", Chromosome.CHR18),
        entry("19", Chromosome.CHR19),
        entry("20", Chromosome.CHR20),
        entry("21", Chromosome.CHR21),
        entry("22", Chromosome.CHR22),
        entry("X", Chromosome.CHRX),
        entry("Y", Chromosome.CHRY),
        entry("MT", Chromosome.CHRMT)
    );

    static Chromosome contigName2GrpcChr(String contig) {
        String chr = contig.toUpperCase().startsWith("CHR") ? contig.substring(3) : contig;
        return GRPC_CHR.getOrDefault(chr, Chromosome.UNRECOGNIZED);
    }

    static List<Chromosome> contigName2GrpcChr(String[] contigs) {
        ArrayList<Chromosome> res = new ArrayList<>(contigs.length);
        for (String chr : contigs) {
            res.add(contigName2GrpcChr(chr));
        }
        return res;
    }
}
