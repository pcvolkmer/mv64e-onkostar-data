/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
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
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.model.RecistCoding;
import dev.pcvolkmer.mv64e.model.Reference;
import dev.pcvolkmer.mv64e.model.Response;
import dev.pcvolkmer.mv64e.model.ResponseMethodCoding;
import java.time.Instant;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class FollowUpResponseBefundMapperTest {

  FollowUpCatalogue catalogue;
  FollowUpResponseBefundMapper dataMapper;

  @BeforeEach
  void setUp(@Mock FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new FollowUpResponseBefundMapper(catalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreateFollowUp() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(11),
                    Column.name(Column.PATIENT_ID).value(42),
                    Column.name(Column.PATIENTEN_ID).value(2000000042),
                    DateColumn.name("datumfollowup").value("2026-04-01"),
                    PropcatColumn.name("beurteilungsmethode").value("RECIST"),
                    PropcatColumn.name("bestresponse").value("CR")))
        .when(catalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(11);

    assertThat(actual).isInstanceOf(Response.class);
    assertThat(actual.getId()).isEqualTo("11");
    assertThat(actual.getPatient())
        .isEqualTo(Reference.builder().id("2000000042").type("Patient").build());
    assertThat(actual.getTherapy())
        .isEqualTo(Reference.builder().id("11").type("MTBSystemicTherapy").build());
    assertThat(actual.getEffectiveDate()).isEqualTo(Instant.parse("2026-04-01T00:00:00Z"));
    assertThat(actual.getMethod())
        .isEqualTo(
            ResponseMethodCoding.builder()
                .code(ResponseMethodCoding.CodeEnum.RECIST)
                .display("RECIST")
                .system("dnpm-dip/mtb/response/method")
                .build());
    assertThat(actual.getValue())
        .isEqualTo(
            RecistCoding.builder()
                .code(RecistCoding.CodeEnum.CR)
                .display("CR")
                .system("RECIST")
                .build());
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      excludeColumns = {Column.PATIENTEN_ID, "datumfollowup", "beurteilungsmethode"})
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);
    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNotNull();
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      includeColumns = {"datumfollowup", "beurteilungsmethode"})
  void shouldThrowIgnorableExceptionIfMissingRequiredValues(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var ex =
        assertThrows(
            IgnorableMappingException.class,
            () -> {
              this.dataMapper.getById(1);
            });

    assertThat(ex)
        .isInstanceOf(IgnorableMappingException.class)
        .hasMessageMatching("Missing required response method or date");
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(11),
        Column.name(Column.PATIENT_ID).value(42),
        Column.name(Column.PATIENTEN_ID).value(2000000042),
        DateColumn.name("datumfollowup").value("2026-04-01"),
        PropcatColumn.name("beurteilungsmethode").value("RECIST"),
        PropcatColumn.name("bestresponse").value("CR"));
  }
}
