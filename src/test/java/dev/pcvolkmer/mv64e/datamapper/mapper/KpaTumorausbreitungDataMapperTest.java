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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorausbreitungCatalogue;
import dev.pcvolkmer.mv64e.mtb.TumorStaging;
import dev.pcvolkmer.mv64e.mtb.TumorStagingMethodCoding;
import dev.pcvolkmer.mv64e.mtb.TumorStagingMethodCodingCode;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    Map<String, Object> testData =
        Map.of(
            "id", "1",
            "zeitpunkt",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()),
            "typ", "pathologic",
            "wert", "tumor-free",
            "tnmtprefix", "p",
            "tnmt", "0",
            "tnmnprefix", "p",
            "tnmn", "0",
            "tnmmprefix", "p",
            "tnmm", "0");

    doAnswer(invocationOnMock -> List.of(ResultSet.from(testData)))
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
  void shouldNotUseNullTnmForUnsableValue() {
    Map<String, Object> testData =
        Map.of(
            "id",
            "1",
            "zeitpunkt",
            new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()),
            "typ",
            "pathologic",
            "wert",
            "tumor-free",
            "tnmtprefix",
            "p",
            "tnmt",
            "4e",
            "tnmnprefix",
            "p",
            "tnmn",
            "0",
            "tnmmprefix",
            "p",
            "tnmm",
            "0");

    doAnswer(invocationOnMock -> List.of(ResultSet.from(testData)))
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

  @ParameterizedTest
  @CsvSource({
    "0,0",
    "1,1",
    "1a,1a",
    "1a1,1a(1)",
    "1a2,1a(2)",
    "1b,1b",
    "1b1,1b(1)",
    "1b2,1b(2)",
    "1b3,1b(3)",
    "1c,1c",
    "1c1,1c(1)",
    "1c2,1c(2)",
    "1c3,1c(3)",
    "1d,1d",
    "1mi,",
    "2,2",
    "2a,2a",
    "2a1,2a(1)",
    "2a2,2a(2)",
    "2b,2b",
    "2c,2c",
    "2d,2d",
    "3,3",
    "3a,3a",
    "3b,3b",
    "3c,3c",
    "3d,3d",
    "3e,",
    "4,4",
    "4a,4a",
    "4b,4b",
    "4c,4c",
    "4d,4d",
    "4e,",
    "a,",
    "is,is",
    "is(DCIS),is(DCIS)",
    "is(LAMN),is(LAMN)",
    "is(LCIS),is(LCIS)",
    "is(Paget),is(Paget)",
    "is(pd),is(pd)",
    "is(pu),is(pu)",
    "X,X",
  })
  void testValueSanitization(String input, String expected) {
    final var actual = KpaTumorausbreitungDataMapper.sanitizeTValue(input);
    assertThat(actual).isEqualTo(expected);
  }
}
