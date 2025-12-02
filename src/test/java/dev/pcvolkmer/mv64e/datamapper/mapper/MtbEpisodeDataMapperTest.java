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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.mtb.MtbEpisodeOfCare;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtbEpisodeDataMapperTest {

  KpaCatalogue kpaCatalogue;
  PropertyCatalogue propertyCatalogue;

  MtbEpisodeDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock KpaCatalogue kpaCatalogue, @Mock PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper = new MtbEpisodeDataMapper(kpaCatalogue, propertyCatalogue);
  }

  @Test
  void shouldMapData() {
    final Map<String, Object> kpaData =
        Map.of(
            "id", 4711,
            "patienten_id", 42,
            "anmeldedatummtb",
                new java.sql.Date(Date.from(Instant.parse("2025-06-28T12:00:00Z")).getTime()));

    doAnswer(invocationOnMock -> ResultSet.from(kpaData)).when(kpaCatalogue).getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbEpisodeOfCare.class);
    assertThat(actual.getId()).isEqualTo("4711");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());

    assertThat(actual.getDiagnoses()).hasSize(1);
    assertThat(actual.getDiagnoses().get(0))
        .isEqualTo(Reference.builder().id("4711").type("Diagnose").build());

    assertThat(actual.getPeriod())
        .isEqualTo(
            // UTC day start!
            PeriodDate.builder().start(Date.from(Instant.parse("2025-06-28T00:00:00Z"))).build());
  }
}
