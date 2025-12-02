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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.mtb.*;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpaPatientDataMapperTest {

  KpaCatalogue kpaCatalogue;
  PropertyCatalogue propertyCatalogue;

  KpaPatientDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock KpaCatalogue kpaCatalogue, @Mock PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper = new KpaPatientDataMapper(kpaCatalogue, propertyCatalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreatePatientAlive(@Mock ResultSet resultSet) {
    var testData =
        Map.of(
            "patient_id", "1",
            "geschlecht", "m",
            "geburtsdatum",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()),
            "todesdatum",
                new java.sql.Date(Date.from(Instant.parse("2024-06-19T12:00:00Z")).getTime()),
            "krankenkasse", "12345678",
            "artderkrankenkasse", "GKV");

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

    doAnswer(invocationOnMock -> resultSet).when(kpaCatalogue).getById(anyInt());

    doAnswer(
            invocationOnMock ->
                new PropertyCatalogue.Entry(
                    "GKV", "Gesetzliche Krankenversicherung", "Gesetzliche Krankenversicherung"))
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.MALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T12:00:00Z")));
    assertThat(actual.getDateOfDeath()).isEqualTo(Date.from(Instant.parse("2024-06-19T12:00:00Z")));
    assertThat(actual.getHealthInsurance())
        .isEqualTo(
            HealthInsurance.builder()
                .reference(
                    Reference.builder()
                        .id("12345678")
                        .system("https://www.dguv.de/arge-ik")
                        .type("HealthInsurance")
                        .build())
                .type(
                    HealthInsuranceCoding.builder()
                        .code(HealthInsuranceCodingCode.GKV)
                        .display("Gesetzliche Krankenversicherung")
                        .system("http://fhir.de/CodeSystem/versicherungsart-de-basis")
                        .build())
                .build());
  }

  @Test
  void shouldCreatePatientDead(@Mock ResultSet resultSet) {
    var testData =
        Map.of(
            "patient_id", "1",
            "geschlecht", "w",
            "geburtsdatum",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T12:00:00Z")).getTime()),
            "krankenkasse", "12345678",
            "artderkrankenkasse", "PKV");

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

    doAnswer(invocationOnMock -> resultSet).when(kpaCatalogue).getById(anyInt());

    doAnswer(
            invocationOnMock ->
                new PropertyCatalogue.Entry(
                    "PKV", "Private Krankenversicherung", "Private Krankenversicherung"))
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.FEMALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T12:00:00Z")));
    assertThat(actual.getDateOfDeath()).isNull();
    assertThat(actual.getHealthInsurance())
        .isEqualTo(
            HealthInsurance.builder()
                .reference(
                    Reference.builder()
                        .id("12345678")
                        .system("https://www.dguv.de/arge-ik")
                        .type("HealthInsurance")
                        .build())
                .type(
                    HealthInsuranceCoding.builder()
                        .code(HealthInsuranceCodingCode.PKV)
                        .display("Private Krankenversicherung")
                        .system("http://fhir.de/CodeSystem/versicherungsart-de-basis")
                        .build())
                .build());
  }
}
