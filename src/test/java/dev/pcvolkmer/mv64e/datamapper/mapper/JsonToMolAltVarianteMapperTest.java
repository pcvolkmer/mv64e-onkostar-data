/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pcvolkmer.mv64e.mtb.Coding;
import dev.pcvolkmer.mv64e.mtb.GeneAlterationReference;
import dev.pcvolkmer.mv64e.mtb.Reference;
import org.junit.jupiter.api.Test;

class JsonToMolAltVarianteMapperTest {

  @Test
  void shouldMapJson() {
    var json =
        "[{\"id\":22641112,\"ergebnis\":\"Einfache Variante (Mutation)\",\"gen\":\"BRAF\",\"exon\":\"-\",\"pathogenitaetsklasse\":\"-\"}]";

    var actual = JsonToMolAltVarianteMapper.map(json);

    assertThat(actual).hasSize(1);

    var variant = actual.get(0);
    assertThat(variant)
        .isEqualTo(
            GeneAlterationReference.builder()
                .gene(
                    Coding.builder()
                        .code("HGNC:1097")
                        .display("BRAF")
                        .system("https://www.genenames.org/")
                        .build())
                .variant(Reference.builder().id("22641112").type("Variant").build())
                .build());
  }
}
