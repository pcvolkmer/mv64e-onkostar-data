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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.PatientCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.Address;
import dev.pcvolkmer.mv64e.mtb.GenderCodingCode;
import dev.pcvolkmer.mv64e.mtb.Patient;
import java.time.Instant;
import java.util.Date;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
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
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value("20001234"),
                    Column.name("geschlecht").value("M"),
                    DateColumn.name("geburtsdatum").value("2000-01-01"),
                    Column.name("GKZ").value("06634022")))
        .when(patientCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("20001234");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.MALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T00:00:00Z")));
    assertThat(actual.getDateOfDeath()).isNull();
    assertThat(actual.getAddress()).isEqualTo(Address.builder().municipalityCode("06634").build());
  }

  @Test
  void shouldCreatePatientDead() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value("20001234"),
                    Column.name("geschlecht").value("M"),
                    DateColumn.name("geburtsdatum").value("2000-01-01"),
                    DateColumn.name("sterbedatum").value("2024-06-19"),
                    Column.name("GKZ").value("06634022")))
        .when(patientCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Patient.class);
    assertThat(actual.getId()).isEqualTo("20001234");
    assertThat(actual.getGender().getCode()).isEqualTo(GenderCodingCode.MALE);
    assertThat(actual.getBirthDate()).isEqualTo(Date.from(Instant.parse("2000-01-01T00:00:00Z")));
    assertThat(actual.getDateOfDeath()).isEqualTo(Date.from(Instant.parse("2024-06-19T00:00:00Z")));
    assertThat(actual.getAddress()).isEqualTo(Address.builder().municipalityCode("06634").build());
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      excludeColumns = {Column.PATIENTEN_ID},
      maxNullColumns = 2)
  @MockitoSettings(strictness = Strictness.LENIENT)
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(patientCatalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNotNull();
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        Column.name(Column.PATIENTEN_ID).value("20001234"),
        Column.name("geschlecht").value("M"),
        DateColumn.name("geburtsdatum").value("2000-01-01"),
        DateColumn.name("sterbedatum").value("2024-06-19"),
        Column.name("GKZ").value("06634022"));
  }
}
