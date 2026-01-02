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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EcogCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.EcogCoding;
import dev.pcvolkmer.mv64e.mtb.EcogCodingCode;
import dev.pcvolkmer.mv64e.mtb.PerformanceStatus;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class KpaEcogDataMapperTest {

  EcogCatalogue catalogue;

  KpaEcogDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock EcogCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new KpaEcogDataMapper(catalogue);
  }

  @Test
  void shouldMapResultSet() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        DateColumn.name("datum").value("2000-01-01"),
                        Column.name("ecog").value("1"))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(PerformanceStatus.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());
    assertThat(actual.getEffectiveDate())
        .isEqualTo(new java.util.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()));
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
  void shouldReturnNullIfEcogOrDateIsNull(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(PerformanceStatus.class);
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
