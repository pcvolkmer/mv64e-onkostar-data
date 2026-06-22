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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.PathologiebefundCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.mtb.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class PathologiebefundToSpecimenDataMapperTest {

  PathologiebefundCatalogue catalogue;
  HistologieCatalogue histologieCatalogue;

  PathologiebefundToSpecimenDataMapper mapper;

  @BeforeEach
  void setUp(
      @Mock PathologiebefundCatalogue catalogue, @Mock HistologieCatalogue histologieCatalogue) {
    this.catalogue = catalogue;
    this.histologieCatalogue = histologieCatalogue;

    this.mapper = new PathologiebefundToSpecimenDataMapper(catalogue, histologieCatalogue);
  }

  @Test
  void shouldFetchAllRelatedSpecimens() {
    // Mock DNPM UF Histologie
    doAnswer(
            invocation ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(40)),
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(41)),
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(42))))
        .when(histologieCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Pathologiebefund
    when(this.catalogue.isAvailable(anyInt())).thenReturn(true);
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("Praeparat").value("B"),
                  PropcatColumn.name("EntnahmestellederBiopsie").value("T"));
            })
        .when(catalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(3);

    assertThat(actual.get(0).getId()).isEqualTo("40");
    assertThat(actual.get(1).getId()).isEqualTo("41");
    assertThat(actual.get(2).getId()).isEqualTo("42");

    assertThat(actual.get(0).getPatient())
        .isEqualTo(Reference.builder().id("4711").type("Patient").build());
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void shouldIgnoreNonRelatedHistologies() {
    // Mock DNPM UF Histologie
    doAnswer(
            invocation ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(41)),
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(42))))
        .when(histologieCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Pathologiebefund - only "42" is available
    doAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Integer.class) == 42)
        .when(catalogue)
        .isAvailable(anyInt());

    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              if (id == 42) {
                return TestResultSet.withColumns(
                    Column.name(Column.ID).value(id),
                    Column.name(Column.PATIENTEN_ID).value(4711),
                    PropcatColumn.name("Praeparat").value("B"),
                    PropcatColumn.name("EntnahmestellederBiopsie").value("T"));
              }
              throw new DataAccessException("Unexpected test id: " + id);
            })
        .when(catalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);

    assertThat(actual.get(0).getId()).isEqualTo("42");

    assertThat(actual.get(0).getPatient())
        .isEqualTo(Reference.builder().id("4711").type("Patient").build());
  }

  @ParameterizedTest
  @MethodSource("specimenMethodTestData")
  void shouldReturnExpectedSpecimenMethod(
      String value, TumorSpecimenCollectionMethodCoding coding) {
    // Mock DNPM UF Histologie
    doAnswer(
            invocation ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(42))))
        .when(histologieCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Pathologiebefund
    when(this.catalogue.isAvailable(anyInt())).thenReturn(true);
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("Praeparat").value(value),
                  PropcatColumn.name("EntnahmestellederBiopsie").value("T"));
            })
        .when(catalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getCollection().getMethod()).isEqualTo(coding);
  }

  // Returns all available Onkostar values and - best effort - expected mapping
  // See property catalogue OS.MolDiagEntnahmemethode and
  // https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783/ line 84
  static Stream<Arguments> specimenMethodTestData() {
    return Stream.of(
        Arguments.of(
            "B",
            TumorSpecimenCollectionMethodCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/method")
                .code(TumorSpecimenCollectionMethodCodingCode.BIOPSY)
                .display("Biopsie")
                .build()),
        Arguments.of(
            "R",
            TumorSpecimenCollectionMethodCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/method")
                .code(TumorSpecimenCollectionMethodCodingCode.RESECTION)
                .display("Resektat")
                .build()),
        Arguments.of(
            "U",
            TumorSpecimenCollectionMethodCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/method")
                .code(TumorSpecimenCollectionMethodCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("specimenLocalizationTestData")
  void shouldReturnExpectedSpecimenLocalization(
      String value, TumorSpecimenCollectionLocalizationCoding coding) {
    // Mock DNPM UF Histologie
    doAnswer(
            invocation ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(42))))
        .when(histologieCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Pathologiebefund
    when(this.catalogue.isAvailable(anyInt())).thenReturn(true);
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("Praeparat").value("B"),
                  PropcatColumn.name("EntnahmestellederBiopsie").value(value));
            })
        .when(catalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getCollection().getLocalization()).isEqualTo(coding);
  }

  @Test
  void shouldReturnExpectedSpecimenDate() {
    // Mock DNPM UF Histologie
    doAnswer(
            invocation ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1), Column.name("histologie").value(42))))
        .when(histologieCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Pathologiebefund
    when(this.catalogue.isAvailable(anyInt())).thenReturn(true);
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("Praeparat").value("B"),
                  DateColumn.name("HistologieDatum").value("2025-06-28"),
                  PropcatColumn.name("EntnahmestellederBiopsie").value("T"));
            })
        .when(catalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getCollection().getDate())
        .isEqualTo(Date.from(Instant.parse("2025-06-28T00:00:00Z")));
  }

  // Returns all available Onkostar values and - best effort - expected mapping
  // See property catalogue OS.Probenmaterial and
  // https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783/ line 82
  static Stream<Arguments> specimenLocalizationTestData() {
    return Stream.of(
        Arguments.of(
            "P",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.PRIMARY_TUMOR)
                .display("Primärtumor")
                .build()),
        Arguments.of(
            "R",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "M",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
                .display("Metastase")
                .build()),
        Arguments.of(
            "B",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "L",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.REGIONAL_LYMPH_NODES)
                .display("Lymphknoten")
                .build()),
        Arguments.of(
            "U",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "S",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()));
  }
}
