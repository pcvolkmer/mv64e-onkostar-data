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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.PatientCatalogue;
import dev.pcvolkmer.mv64e.mtb.Address;
import dev.pcvolkmer.mv64e.mtb.GenderCodingCode;
import dev.pcvolkmer.mv64e.mtb.Patient;
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
class PatientDataMapperTest {

  PatientCatalogue patientCatalogue;

  PatientDataMapper dataMapper;

  @BeforeEach
  void setUp(@Mock PatientCatalogue patientCatalogue) {
    this.patientCatalogue = patientCatalogue;
    this.dataMapper = new PatientDataMapper(patientCatalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreatePatientAlive() {
    Map<String, Object> testData =
        Map.of(
            "id", "1",
            "patienten_id", "20001234",
            "geschlecht", "M",
            "geburtsdatum",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()),
            "sterbedatum",
                new java.sql.Date(Date.from(Instant.parse("2024-06-19T00:00:00Z")).getTime()),
            "GKZ", "06634022");

    doAnswer(invocationOnMock -> ResultSet.from(testData)).when(patientCatalogue).getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("20001234");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.MALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T00:00:00Z")));
    assertThat(actual.getDateOfDeath()).isEqualTo(Date.from(Instant.parse("2024-06-19T00:00:00Z")));
    assertThat(actual.getAddress()).isEqualTo(Address.builder().municipalityCode("06634").build());
  }

  @Test
  void shouldCreatePatientDead() {
    Map<String, Object> testData =
        Map.of(
            "id", "1",
            "patienten_id", "20001234",
            "geschlecht", "M",
            "geburtsdatum",
                new java.sql.Date(Date.from(Instant.parse("2000-01-01T00:00:00Z")).getTime()),
            "GKZ", "06634022");

    doAnswer(invocationOnMock -> ResultSet.from(testData)).when(patientCatalogue).getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("20001234");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.MALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T00:00:00Z")));
    assertThat(actual.getDateOfDeath()).isNull();
    assertThat(actual.getAddress()).isEqualTo(Address.builder().municipalityCode("06634").build());
  }
}
