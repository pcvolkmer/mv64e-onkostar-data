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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.mtb.EcogCoding;
import dev.pcvolkmer.mv64e.mtb.EcogCodingCode;
import dev.pcvolkmer.mv64e.mtb.PerformanceStatus;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.EcogCatalogue;
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
class KpaEcogDataMapperTest {

  EcogCatalogue catalogue;

  KpaEcogDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock EcogCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new KpaEcogDataMapper(catalogue);
  }

  @Test
  void shouldMapResultSet(@Mock ResultSet resultSet) {
    var testData =
        Map.of(
            "id", "1",
            "patienten_id", "42",
            "datum", new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()),
            "ecog", "1");

    doAnswer(
            invocationOnMock ->
                Reference.builder()
                    .id(testData.get("patienten_id").toString())
                    .type("Patient")
                    .build())
        .when(resultSet)
        .getPatientReference();

    doAnswer(
            invocationOnMock -> {
              var columnName = invocationOnMock.getArgument(0, String.class);
              return testData.get(columnName);
            })
        .when(resultSet)
        .getString(anyString());

    doAnswer(
            invocationOnMock -> {
              var columnName = invocationOnMock.getArgument(0, String.class);
              return testData.get(columnName);
            })
        .when(resultSet)
        .getDate(anyString());

    when(resultSet.getId()).thenReturn(1);

    doAnswer(invocationOnMock -> List.of(resultSet)).when(catalogue).getAllByParentId(anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(PerformanceStatus.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());
    assertThat(actual.getEffectiveDate())
        .isEqualTo(new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()));
    assertThat(actual.getValue())
        .isEqualTo(
            EcogCoding.builder()
                .code(EcogCodingCode.CODE_1)
                .display("ECOG 1")
                .system("ECOG-Performance-Status")
                .build());
  }
}
