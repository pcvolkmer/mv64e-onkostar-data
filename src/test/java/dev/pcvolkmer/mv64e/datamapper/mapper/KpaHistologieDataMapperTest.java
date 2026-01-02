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
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenetikCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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

    when(this.catalogue.getAllByParentId(anyInt()))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    Column.name("histologie").value(100),
                    DateColumn.name("erstellungsdatum").value("2000-01-01"),
                    Column.name("tumorzellgehalt").value(80))));

    when(this.molekulargenetikCatalogue.getById(anyInt()))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(100),
                Column.name(Column.PATIENTEN_ID).value(42),
                Column.name("histologie").value(100),
                DateColumn.name("datum").value("2000-01-01")));
  }

  @Test
  void shouldMapResultSet() {
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
    when(this.catalogue.getAllByParentId(anyInt()))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    Column.name("histologie").value(100),
                    DateColumn.name("erstellungsdatum").value("2000-01-01"))));

    when(this.molekulargenetikCatalogue.getById(anyInt()))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(100),
                Column.name(Column.PATIENTEN_ID).value(42),
                Column.name("histologie").value(100),
                DateColumn.name("datum").value("2000-01-01")));

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

  @Test
  void shouldReturnNullIfColumnHistologieMissing() {
    when(catalogue.getById(anyInt()))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1),
                Column.name(Column.PATIENTEN_ID).value(42),
                DateColumn.name("erstellungsdatum").value("2000-01-01"),
                Column.name("tumorzellgehalt").value(80)));

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNull();
  }

  // Note: If column "erstellungsdatum" missing, this should fail in DNPM:DIP
  @FuzzNullTest(
      initMethod = "fuzzInitData",
      includeColumns = {"tumorzellgehalt", "erstellungsdatum"})
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(HistologyReport.class);
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        Column.name(Column.PATIENTEN_ID).value(42),
        Column.name("histologie").value(100),
        DateColumn.name("erstellungsdatum").value("2000-01-01"),
        Column.name("tumorzellgehalt").value(80));
  }
}
