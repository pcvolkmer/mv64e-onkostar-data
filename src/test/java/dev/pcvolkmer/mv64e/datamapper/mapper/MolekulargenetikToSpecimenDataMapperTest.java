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

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.*;
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

@ExtendWith(MockitoExtension.class)
class MolekulargenetikToSpecimenDataMapperTest {

  MolekulargenetikCatalogue molekulargenetikCatalogue;
  TherapieplanCatalogue therapieplanCatalogue;
  RebiopsieCatalogue rebiopsieCatalogue;
  ReevaluationCatalogue reevaluationCatalogue;
  EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  VorbefundeCatalogue vorbefundeCatalogue;
  HistologieCatalogue histologieCatalogue;

  MolekulargenetikToSpecimenDataMapper mapper;

  @BeforeEach
  void setUp(
      @Mock MolekulargenetikCatalogue molekulargenetikCatalogue,
      @Mock TherapieplanCatalogue therapieplanCatalogue,
      @Mock RebiopsieCatalogue rebiopsieCatalogue,
      @Mock ReevaluationCatalogue reevaluationCatalogue,
      @Mock EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      @Mock VorbefundeCatalogue vorbefundeCatalogue,
      @Mock HistologieCatalogue histologieCatalogue) {
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.rebiopsieCatalogue = rebiopsieCatalogue;
    this.reevaluationCatalogue = reevaluationCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.vorbefundeCatalogue = vorbefundeCatalogue;
    this.histologieCatalogue = histologieCatalogue;

    this.mapper =
        new MolekulargenetikToSpecimenDataMapper(
            molekulargenetikCatalogue,
            therapieplanCatalogue,
            rebiopsieCatalogue,
            reevaluationCatalogue,
            einzelempfehlungCatalogue,
            vorbefundeCatalogue,
            histologieCatalogue);
  }

  @Test
  void shouldFetchAllRelatedSpecimens() {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Rebiopsien - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(40)));
            })
        .when(rebiopsieCatalogue)
        .getAllByParentId(anyInt());

    // Mock Reevaluationen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(41)));
            })
        .when(reevaluationCatalogue)
        .getAllByParentId(anyInt());

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
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
  void shouldNotFetchSpecimensForUnknownEinsendenummer() {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Vorbefund
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42),
                      Column.name("befundnummer").value("H/2025/1234")));
            })
        .when(vorbefundeCatalogue)
        .getAllByParentId(anyInt());

    when(molekulargenetikCatalogue.getByEinsendenummer(anyString()))
        .thenThrow(new DataAccessException("Test"));

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldNotFetchRelatedSpecimensTwice() {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Rebiopsien - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(40)));
            })
        .when(rebiopsieCatalogue)
        .getAllByParentId(anyInt());

    // Mock Reevaluationen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(40)));
            })
        .when(reevaluationCatalogue)
        .getAllByParentId(anyInt());

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(2);

    assertThat(actual.get(0).getId()).isEqualTo("40");
    assertThat(actual.get(1).getId()).isEqualTo("42");
  }

  @ParameterizedTest
  @MethodSource("specimenTypeTestData")
  void shouldReturnExpectedSpecimenType(String value, TumorSpecimenCoding coding) {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("materialfixierung").value(value),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getType()).isEqualTo(coding);
  }

  // Returns all available Onkostar values and - best effort - expected mapping
  // See property catalogue OS.Material and
  // https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783/ line 80
  static Stream<Arguments> specimenTypeTestData() {
    return Stream.of(
        Arguments.of(
            "0",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "1",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "2",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.CRYO_FROZEN)
                .display("Cryo-frozen")
                .build()),
        Arguments.of(
            "3",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.FFPE)
                .display("FFPE")
                .build()),
        Arguments.of(
            "4",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "9",
            TumorSpecimenCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/type")
                .code(TumorSpecimenCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("specimenMethodTestData")
  void shouldReturnExpectedSpecimenMethod(
      String value, TumorSpecimenCollectionMethodCoding coding) {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value(value),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
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
            "LB",
            TumorSpecimenCollectionMethodCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/method")
                .code(TumorSpecimenCollectionMethodCodingCode.LIQUID_BIOPSY)
                .display("Liquid Biopsy")
                .build()),
        Arguments.of(
            "Z",
            TumorSpecimenCollectionMethodCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/method")
                .code(TumorSpecimenCollectionMethodCodingCode.CYTOLOGY)
                .display("Zytologie")
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

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value(value));
            })
        .when(molekulargenetikCatalogue)
        .getById(anyInt());

    var actual = this.mapper.getAllByKpaId(1, Reference.builder().build());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getCollection().getLocalization()).isEqualTo(coding);
  }

  @Test
  void shouldReturnExpectedSpecimenDate() {

    // Mock Einzelempfehlungen ID
    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1, 2));

    // Mock Einzelempfehlungen - two referencing the same OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name("ref_molekulargenetik").value(42)));
            })
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    // Mock OS.Molekulargenetik
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  DateColumn.name("entnahmedatum").value("2025-06-28"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
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
            "T",
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
            "LK",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
                .display("Metastase")
                .build()),
        Arguments.of(
            "M",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
                .display("Metastase")
                .build()),
        Arguments.of(
            "ITM",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
                .display("Metastase")
                .build()),
        Arguments.of(
            "SM",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
                .display("Metastase")
                .build()),
        Arguments.of(
            "KM",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "NG",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "AS",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
                .build()),
        Arguments.of(
            "PLERG",
            TumorSpecimenCollectionLocalizationCoding.builder()
                .system("dnpm-dip/mtb/tumor-specimen/collection/localization")
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
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
                .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
                .display("Unbekannt")
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
