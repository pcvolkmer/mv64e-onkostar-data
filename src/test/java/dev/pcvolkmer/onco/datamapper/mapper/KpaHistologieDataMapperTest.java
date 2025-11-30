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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.mtb.*;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.MolekulargenetikCatalogue;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpaHistologieDataMapperTest {

  HistologieCatalogue catalogue;
  MolekulargenetikCatalogue molekulargenetikCatalogue;
  PropertyCatalogue propertyCatalogue;

  KpaHistologieDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock HistologieCatalogue catalogue,
      @Mock MolekulargenetikCatalogue molekulargenetikCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {
    this.catalogue = catalogue;
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper =
        new KpaHistologieDataMapper(catalogue, molekulargenetikCatalogue, propertyCatalogue);

    Map<String, Object> histologieTestData =
        Map.of(
            "id",
            1,
            "patienten_id",
            42,
            "histologie",
            100,
            "erstellungsdatum",
            new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()),
            "tumorzellgehalt",
            80);

    Map<String, Object> molekulargenetikTestData =
        Map.of(
            "id", 100,
            "patienten_id", 42,
            "histologie", 100,
            "datum", new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()));

    when(this.catalogue.getAllByParentId(anyInt()))
        .thenReturn(List.of(ResultSet.from(histologieTestData)));
    when(this.molekulargenetikCatalogue.getById(anyInt()))
        .thenReturn(ResultSet.from(molekulargenetikTestData));
  }

  @Test
  void shouldMapResultSet() {
    Map<String, Object> histologieTestData =
        Map.of(
            "id", 1,
            "patienten_id", 42,
            "histologie", 100,
            "erstellungsdatum",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()),
            "tumorzellgehalt", 80);

    when(this.catalogue.getAllByParentId(anyInt()))
        .thenReturn(List.of(ResultSet.from(histologieTestData)));

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual)
        .isInstanceOf(HistologyReport.class)
        .satisfies(
            histologyReport -> {
              assertThat(histologyReport.getPatient().getId()).isEqualTo("42");
              assertThat(histologyReport.getResults().getTumorCellContent())
                  .isEqualTo(
                      TumorCellContent.builder()
                          .id("1")
                          .patient(Reference.builder().id("42").type("Patient").build())
                          .specimen(Reference.builder().id("100").type("Specimen").build())
                          .method(
                              TumorCellContentMethodCoding.builder()
                                  .code(TumorCellContentMethodCodingCode.HISTOLOGIC)
                                  .build())
                          .value(0.8)
                          .build());
            });
  }

  // See https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783/ - Line 130
  @Test
  void shouldMapResultSetWithoutTumorCellContent() {
    Map<String, Object> histologieTestData =
        Map.of(
            "id",
            1,
            "patienten_id",
            42,
            "histologie",
            100,
            "erstellungsdatum",
            new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()));

    when(this.catalogue.getAllByParentId(anyInt()))
        .thenReturn(List.of(ResultSet.from(histologieTestData)));

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual)
        .isInstanceOf(HistologyReport.class)
        .satisfies(
            histologyReport -> {
              assertThat(histologyReport.getPatient().getId()).isEqualTo("42");
              assertThat(histologyReport.getResults().getTumorCellContent()).isNull();
            });
  }
}
