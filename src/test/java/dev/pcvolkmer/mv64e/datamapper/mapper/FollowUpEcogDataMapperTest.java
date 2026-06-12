/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026  Paul-Christian Volkmer
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class FollowUpEcogDataMapperTest {

  FollowUpCatalogue catalogue;

  FollowUpEcogDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new FollowUpEcogDataMapper(catalogue);
  }

  @Test
  void shouldMapResultSet() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(11),
                    Column.name(Column.PATIENT_ID).value(42),
                    Column.name(Column.PATIENTEN_ID).value(2000000042),
                    Column.name("linktherapieempfehlung").value(1),
                    DateColumn.name("datumfollowup").value("2026-04-01"),
                    DateColumn.name("datumletzterkontakt").value("2026-01-01"),
                    Column.name("ecog").value("1")))
        .when(catalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(PerformanceStatus.class);
    assertThat(actual.getPatient())
        .isEqualTo(Reference.builder().id("2000000042").type("Patient").build());
    assertThat(actual.getValue())
        .isEqualTo(
            EcogCoding.builder()
                .code(EcogCodingCode.CODE_1)
                .display("ECOG 1")
                .system("ECOG-Performance-Status")
                .build());
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      includeColumns = {"ecog", "datum"})
  void shouldReturnNullResultIfEcogOrDateIsNull(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnNullResultIfEcogIsBlank() {
    when(catalogue.getById(anyInt()))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1),
                Column.name(Column.PATIENTEN_ID).value(42),
                DateColumn.name("datum").value("2000-01-01"),
                Column.name("ecog").value("")));

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNull();
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      includeColumns = {Column.PATIENTEN_ID, Column.HAUPTPROZEDUR_ID})
  void fuzzTestNullColumnsThrowsDataAccessException(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var ex = assertThrows(DataAccessException.class, () -> this.dataMapper.getById(1));
    assertThat(ex.getMessage()).isIn("No patient id found", "Cannot fetch 'Therapieplan'");
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        Column.name(Column.PATIENTEN_ID).value(42),
        DateColumn.name("datum").value("2000-01-01"),
        Column.name("ecog").value("1"));
  }
}
