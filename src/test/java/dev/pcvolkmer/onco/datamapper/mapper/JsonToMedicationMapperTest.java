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

package dev.pcvolkmer.onco.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pcvolkmer.mv64e.mtb.RequestedMedicationSystem;
import org.junit.jupiter.api.Test;

class JsonToMedicationMapperTest {

  // See example in:
  // https://github.com/pcvolkmer/onkostar-plugin-dnpm/blob/master/sql/sql-queries.md
  @Test
  void shouldMapTherapielinieJson() {
    var json =
        "[\n"
            + "    {\"system\":\"other\",\"code\":\"Gemcitabin\",\"substance\":\"Gemcitabin (dFdC)\"},\n"
            + "    {\"system\":\"other\",\"code\":\"Cisplatin\",\"substance\":\"Cisplatin (CDDP)\"}\n"
            + "]";

    var actual = JsonToMedicationMapper.map(json);

    assertThat(actual).hasSize(2);

    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo("Gemcitabin");
    assertThat(elem0.getDisplay()).isEqualTo("Gemcitabin (dFdC)");
    assertThat(elem0.getSystem()).isEqualTo(RequestedMedicationSystem.UNDEFINED);
    assertThat(elem0.getVersion()).isNull();

    var elem1 = actual.get(1);
    assertThat(elem1.getCode()).isEqualTo("Cisplatin");
    assertThat(elem1.getDisplay()).isEqualTo("Cisplatin (CDDP)");
    assertThat(elem0.getSystem()).isEqualTo(RequestedMedicationSystem.UNDEFINED);
    assertThat(elem0.getVersion()).isNull();
  }

  // See example in:
  // https://github.com/pcvolkmer/onkostar-plugin-dnpm/blob/master/sql/sql-queries.md
  @Test
  void shouldMapEinzelempfehlungJson() {
    var json =
        "[\n"
            + "    {\"code\":\"\",\"name\":\"PARP-Inhibierung\",\"system\":\"UNREGISTERED\"}\n"
            + "]";

    var actual = JsonToMedicationMapper.map(json);

    assertThat(actual).hasSize(1);

    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEmpty();
    assertThat(elem0.getDisplay()).isEqualTo("PARP-Inhibierung");
    assertThat(elem0.getSystem()).isEqualTo(RequestedMedicationSystem.UNDEFINED);
    assertThat(elem0.getVersion()).isNull();
  }
}
