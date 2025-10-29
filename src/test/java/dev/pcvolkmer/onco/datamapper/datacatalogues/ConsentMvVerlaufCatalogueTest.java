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

package dev.pcvolkmer.onco.datamapper.datacatalogues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ConsentMvVerlaufCatalogueTest {

  JdbcTemplate jdbcTemplate;
  ConsentMvVerlaufCatalogue catalogue;

  @BeforeEach
  void setUp(@Mock JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.catalogue = ConsentMvVerlaufCatalogue.create(jdbcTemplate);
  }

  @Test
  void shouldUseCorrectQuery(@Mock Map<String, Object> resultSet) {
    doAnswer(invocationOnMock -> List.of(resultSet))
        .when(jdbcTemplate)
        .queryForList(anyString(), anyInt());

    this.catalogue.getById(1);

    var captor = ArgumentCaptor.forClass(String.class);
    verify(this.jdbcTemplate).queryForList(captor.capture(), anyInt());

    assertThat(captor.getValue())
        .isEqualTo(
            "SELECT patient.patienten_id, dk_dnpm_uf_consentmvverlauf.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM dk_dnpm_uf_consentmvverlauf JOIN prozedur ON (prozedur.id = dk_dnpm_uf_consentmvverlauf.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND prozedur.id = ?");
  }

  @Test
  void shouldUseCorrectSubformQuery(@Mock Map<String, Object> resultSet) {
    doAnswer(invocationOnMock -> List.of(resultSet))
        .when(jdbcTemplate)
        .queryForList(anyString(), anyInt());

    this.catalogue.getAllByParentId(1);

    var captor = ArgumentCaptor.forClass(String.class);
    verify(this.jdbcTemplate).queryForList(captor.capture(), anyInt());

    assertThat(captor.getValue())
        .isEqualTo(
            "SELECT patient.patienten_id, dk_dnpm_uf_consentmvverlauf.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM dk_dnpm_uf_consentmvverlauf JOIN prozedur ON (prozedur.id = dk_dnpm_uf_consentmvverlauf.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND hauptprozedur_id = ?");
  }
}
