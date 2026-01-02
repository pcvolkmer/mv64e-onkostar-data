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
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorausbreitungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.TumorStaging;
import dev.pcvolkmer.mv64e.mtb.TumorStagingMethodCoding;
import dev.pcvolkmer.mv64e.mtb.TumorStagingMethodCodingCode;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class KpaTumorausbreitungDataMapperTest {

  TumorausbreitungCatalogue catalogue;

  KpaTumorausbreitungDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock TumorausbreitungCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new KpaTumorausbreitungDataMapper(catalogue);
  }

  @Test
  void shouldMapResultSet() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        DateColumn.name("zeitpunkt").value("2000-01-01"),
                        PropcatColumn.name("typ").value("pathologic"),
                        PropcatColumn.name("wert").value("tumor-free"),
                        PropcatColumn.name("tnmtprefix").value("p"),
                        PropcatColumn.name("tnmt").value("0"),
                        PropcatColumn.name("tnmnprefix").value("p"),
                        PropcatColumn.name("tnmn").value("0"),
                        PropcatColumn.name("tnmmprefix").value("p"),
                        PropcatColumn.name("tnmm").value("0"))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(TumorStaging.class);
    assertThat(actual.getDate())
        .isEqualTo(new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()));
    assertThat(actual.getMethod())
        .isEqualTo(
            TumorStagingMethodCoding.builder()
                .code(TumorStagingMethodCodingCode.PATHOLOGIC)
                .system("dnpm-dip/mtb/tumor-staging/method")
                .build());
    assertThat(actual.getOtherClassifications()).hasSize(1);
    assertThat(actual.getOtherClassifications().get(0).getCode()).isEqualTo("tumor-free");
    assertThat(actual.getTnmClassification().getTumor().getCode()).isEqualTo("pT0");
    assertThat(actual.getTnmClassification().getNodes().getCode()).isEqualTo("pN0");
    assertThat(actual.getTnmClassification().getMetastasis().getCode()).isEqualTo("pM0");
  }

  @Test
  void shouldNotUseNullTnmForUnusableValue() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        DateColumn.name("zeitpunkt").value("2000-01-01"),
                        PropcatColumn.name("typ").value("pathologic"),
                        PropcatColumn.name("wert").value("tumor-free"),
                        PropcatColumn.name("tnmtprefix").value("p"),
                        PropcatColumn.name("tnmt").value(""), // <- Invalid empty value
                        PropcatColumn.name("tnmnprefix").value("p"),
                        PropcatColumn.name("tnmn").value("0"),
                        PropcatColumn.name("tnmmprefix").value("p"),
                        PropcatColumn.name("tnmm").value("0"))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(TumorStaging.class);
    assertThat(actual.getDate())
        .isEqualTo(new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()));
    assertThat(actual.getMethod())
        .isEqualTo(
            TumorStagingMethodCoding.builder()
                .code(TumorStagingMethodCodingCode.PATHOLOGIC)
                .system("dnpm-dip/mtb/tumor-staging/method")
                .build());
    // No T value available in DNPM:DIP for code "4e"
    assertThat(actual.getTnmClassification().getTumor()).isNull();
    assertThat(actual.getTnmClassification().getNodes().getCode()).isEqualTo("pN0");
    assertThat(actual.getTnmClassification().getMetastasis().getCode()).isEqualTo("pM0");
  }

  @FuzzNullTest(initMethod = "fuzzInitData")
  @MockitoSettings(strictness = Strictness.LENIENT)
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.dataMapper.getByParentId(1);
    assertThat(actual).isNotNull();
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        DateColumn.name("zeitpunkt").value("2000-01-01"),
        PropcatColumn.name("typ").value("pathologic"),
        PropcatColumn.name("wert").value("tumor-free"),
        PropcatColumn.name("tnmtprefix").value("p"),
        PropcatColumn.name("tnmt").value("0"),
        PropcatColumn.name("tnmnprefix").value("p"),
        PropcatColumn.name("tnmn").value("0"),
        PropcatColumn.name("tnmmprefix").value("p"),
        PropcatColumn.name("tnmm").value("0"));
  }
}
