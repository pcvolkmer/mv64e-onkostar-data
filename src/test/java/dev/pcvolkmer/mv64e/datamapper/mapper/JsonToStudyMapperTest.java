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

import dev.pcvolkmer.mv64e.mtb.StudySystem;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonToStudyMapperTest {

  // See example in:
  // https://github.com/pcvolkmer/onkostar-plugin-dnpm/blob/master/sql/sql-queries.md
  @Test
  void shouldMapEinzelempfehlungJson() {
    var json =
        "[\n"
            + "    {\"studie\":\"TestInhibitor\",\"system\":\"NCT\",\"id\":\"NCT12345678\",\"nct\":\"NCT12345678\",\"ort\":\"Teststadt\",\"internextern\":\"e\"}\n"
            + "]";

    var actual = JsonToStudyMapper.map(json);

    assertThat(actual).hasSize(1);

    var study = actual.get(0);
    assertThat(study.getId()).isEqualTo("NCT12345678");
    assertThat(study.getDisplay())
        .isEqualTo(
            "TestInhibitor"); // Datenmodell V2.1: Über den "display"-Wert an der Referenz kann der
    // Studien-Name gesetzt werden.
    assertThat(study.getSystem()).isEqualTo(StudySystem.NCT);
    assertThat(study.getType()).isEqualTo("Study");
  }

  @ParameterizedTest
  @MethodSource("studySystems")
  void shouldMapStudySystemInJson(String system, StudySystem expected) {
    var json =
        "[\n"
            + "    {\"studie\":\"TestInhibitor\",\"system\":\""
            + system
            + "\",\"id\":\"12345678\",\"nct\":\"12345678\",\"ort\":\"Teststadt\",\"internextern\":\"e\"}\n"
            + "]";

    var actual = JsonToStudyMapper.map(json);

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getSystem()).isEqualTo(expected);
  }

  private static Stream<Arguments> studySystems() {
    return Stream.of(
        Arguments.of("NCT", StudySystem.NCT),
        // Onkostar Property Catalogue
        Arguments.of("EudraCT", StudySystem.EUDRA_CT),
        // DNPM-Datamodel value
        Arguments.of("Eudra-CT", StudySystem.EUDRA_CT),
        Arguments.of("DRKS", StudySystem.DRKS),
        Arguments.of("EUDAMED", StudySystem.EUDAMED));
  }
}
