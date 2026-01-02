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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.ProzedurCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class KpaProzedurDataMapperTest {

  ProzedurCatalogue catalogue;
  PropertyCatalogue propertyCatalogue;

  KpaProzedurDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock ProzedurCatalogue catalogue, @Mock PropertyCatalogue propertyCatalogue) {
    this.catalogue = catalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper = new KpaProzedurDataMapper(catalogue, propertyCatalogue);
  }

  @Test
  void shouldGetProceduresWithoutReason() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        DateColumn.name("beginn").value("2000-01-01"),
                        DateColumn.name("ende").value("2024-06-19"),
                        DateColumn.name("erfassungsdatum").value("2024-06-19"),
                        PropcatColumn.name("intention").value("S"),
                        PropcatColumn.name("status").value("stopped"),
                        PropcatColumn.name("statusgrund").value("patient-death"),
                        PropcatColumn.name("typ").value("surgery"),
                        Column.name("therapielinie").value(1L))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock -> List.of(TestResultSet.withColumns(Column.name(Column.ID).value(1))))
        .when(catalogue)
        .getDiseases(anyInt());

    when(this.propertyCatalogue.getByCodeAndVersion(anyString(), anyInt()))
        .thenReturn(new PropertyCatalogue.Entry("1", "Version 1", "Version 1"));

    var actual = dataMapper.getByParentId(1);

    assertThat(actual).hasSize(1);
  }

  @Test
  void shouldNotGetProceduresWithoutStart() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        // No "beginn" column
                        DateColumn.name("ende").value("2024-06-19"),
                        DateColumn.name("erfassungsdatum").value("2024-06-19"),
                        PropcatColumn.name("intention").value("S"),
                        PropcatColumn.name("status").value("stopped"),
                        PropcatColumn.name("statusgrund").value("patient-death"),
                        PropcatColumn.name("typ").value("surgery"),
                        Column.name("therapielinie").value(1L))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock -> List.of(TestResultSet.withColumns(Column.name(Column.ID).value(1))))
        .when(catalogue)
        .getDiseases(anyInt());

    var actual = dataMapper.getByParentId(1);

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldNotGetProceduresWithoutErfassungsdatum() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        DateColumn.name("beginn").value("2000-01-01"),
                        DateColumn.name("ende").value("2024-06-19"),
                        // No column "erfassungsdatum"
                        PropcatColumn.name("intention").value("S"),
                        PropcatColumn.name("status").value("stopped"),
                        PropcatColumn.name("statusgrund").value("patient-death"),
                        PropcatColumn.name("typ").value("surgery"),
                        Column.name("therapielinie").value(1L))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock -> List.of(TestResultSet.withColumns(Column.name(Column.ID).value(1))))
        .when(catalogue)
        .getDiseases(anyInt());

    var actual = dataMapper.getByParentId(1);

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldMapResultSet() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        Column.name("ref_einzelempfehlung").value(999),
                        DateColumn.name("beginn").value("2000-01-01"),
                        DateColumn.name("ende").value("2024-06-19"),
                        DateColumn.name("erfassungsdatum").value("2024-06-19"),
                        PropcatColumn.name("intention").value("S"),
                        PropcatColumn.name("status").value("stopped"),
                        PropcatColumn.name("statusgrund").value("patient-death"),
                        PropcatColumn.name("typ").value("surgery"),
                        Column.name("therapielinie").value(1L))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock -> List.of(TestResultSet.withColumns(Column.name(Column.ID).value(1))))
        .when(catalogue)
        .getDiseases(anyInt());

    doAnswer(
            invocationOnMock -> {
              var testPropertyData =
                  Map.of(
                      "S", new PropertyCatalogue.Entry("S", "Sonstiges", "Sonstiges"),
                      "stopped",
                          new PropertyCatalogue.Entry("stopped", "Abgebrochen", "Abgebrochen"),
                      "patient-death", new PropertyCatalogue.Entry("patient-death", "Tod", "Tod"),
                      "surgery", new PropertyCatalogue.Entry("surgery", "OP", "OP"));

              var code = invocationOnMock.getArgument(0, String.class);
              return testPropertyData.get(code);
            })
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(OncoProcedure.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());
    assertThat(actual.getPeriod())
        .isEqualTo(
            PeriodDate.builder()
                .start(Date.from(Instant.parse("2000-01-01T00:00:00Z")))
                .end(Date.from(Instant.parse("2024-06-19T00:00:00Z")))
                .build());
    assertThat(actual.getRecordedOn()).isEqualTo(Date.from(Instant.parse("2024-06-19T00:00:00Z")));
    assertThat(actual.getIntent())
        .isEqualTo(
            MtbTherapyIntentCoding.builder()
                .code(MtbTherapyIntentCodingCode.S)
                .display("Sonstiges")
                .system("dnpm-dip/therapy/intent")
                .build());
    assertThat(actual.getStatus())
        .isEqualTo(
            TherapyStatusCoding.builder()
                .code(TherapyStatusCodingCode.STOPPED)
                .display("Abgebrochen")
                .system("dnpm-dip/therapy/status")
                .build());
    assertThat(actual.getStatusReason())
        .isEqualTo(
            MtbTherapyStatusReasonCoding.builder()
                .code(MtbTherapyStatusReasonCodingCode.PATIENT_DEATH)
                .display("Tod")
                .system("dnpm-dip/therapy/status-reason")
                .build());
    assertThat(actual.getTherapyLine()).isEqualTo(1);
    assertThat(actual.getCode())
        .isEqualTo(
            OncoProcedureCoding.builder()
                .code(OncoProcedureCodingCode.SURGERY)
                .display("OP")
                .system("dnpm-dip/therapy/type")
                .build());
    assertThat(actual.getBasedOn()).isEqualTo(Reference.builder().id("999").build());
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      excludeColumns = {Column.PATIENTEN_ID, Column.HAUPTPROZEDUR_ID},
      maxNullColumns = 2)
  @MockitoSettings(strictness = Strictness.LENIENT)
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getAllByParentId(anyInt())).thenReturn(List.of(resultSet));

    when(catalogue.getDiseases(anyInt()))
        .thenReturn(List.of(TestResultSet.withColumns(Column.name(Column.ID).value(1))));

    doAnswer(
            invocationOnMock -> {
              var testPropertyData =
                  Map.of(
                      "S",
                      new PropertyCatalogue.Entry("S", "Sonstiges", "Sonstiges"),
                      "stopped",
                      new PropertyCatalogue.Entry("stopped", "Abgebrochen", "Abgebrochen"),
                      "patient-death",
                      new PropertyCatalogue.Entry("patient-death", "Tod", "Tod"),
                      "surgery",
                      new PropertyCatalogue.Entry("surgery", "OP", "OP"));

              var code = invocationOnMock.getArgument(0, String.class);
              return testPropertyData.get(code);
            })
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actual = this.dataMapper.getByParentId(1);
    assertThat(actual).isNotNull();
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        Column.name(Column.PATIENTEN_ID).value(42),
        Column.name("ref_einzelempfehlung").value(999),
        DateColumn.name("beginn").value("2000-01-01"),
        DateColumn.name("ende").value("2024-06-19"),
        DateColumn.name("erfassungsdatum").value("2024-06-19"),
        PropcatColumn.name("intention").value("S"),
        PropcatColumn.name("status").value("stopped"),
        PropcatColumn.name("statusgrund").value("patient-death"),
        PropcatColumn.name("typ").value("surgery"),
        Column.name("therapielinie").value(1L));
  }
}
