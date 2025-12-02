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

package dev.pcvolkmer.mv64e.datamapper.datacatalogues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
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
class MolekulargenuntersuchungCatalogueTest {

  JdbcTemplate jdbcTemplate;
  MolekulargenuntersuchungCatalogue catalogue;

  @BeforeEach
  void setUp(@Mock JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.catalogue = MolekulargenuntersuchungCatalogue.create(jdbcTemplate);
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
            "SELECT patient.patienten_id, dk_molekulargenuntersuchung.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM dk_molekulargenuntersuchung JOIN prozedur ON (prozedur.id = dk_molekulargenuntersuchung.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND prozedur.id = ?");
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
            "SELECT patient.patienten_id, dk_molekulargenuntersuchung.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM dk_molekulargenuntersuchung JOIN prozedur ON (prozedur.id = dk_molekulargenuntersuchung.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND hauptprozedur_id = ?");
  }

  @Test
  void shouldUseCorrectMerkmalQuery(@Mock Map<String, Object> resultSet) {
    when(resultSet.get(anyString())).thenReturn(Map.of("feldname", "name", "feldwert", "wert"));

    doAnswer(invocationOnMock -> List.of(resultSet))
        .when(jdbcTemplate)
        .queryForList(anyString(), anyInt());

    this.catalogue.getMerkmaleById(1);

    var captor = ArgumentCaptor.forClass(String.class);
    verify(this.jdbcTemplate).queryForList(captor.capture(), anyInt());

    assertThat(captor.getValue())
        .isEqualTo(
            "SELECT feldname, feldwert FROM dk_molekulargenuntersuchung_merkmale WHERE eintrag_id = ?");
  }

  @Test
  void shouldUseMerkmalList() {
    doAnswer(
            invocationOnMock -> {
              var sql = invocationOnMock.getArgument(0, String.class);
              ArrayList<Map<String, Object>> result = new ArrayList<>();
              if (sql.startsWith("SELECT feldname")) {
                result.add(Map.of("feldname", "name", "feldwert", "wert1"));
                result.add(Map.of("feldname", "name", "feldwert", "wert2"));
              } else {
                var map = new HashMap<String, Object>();
                map.put("id", 1);
                map.put("name", "x");
                result.add(map);
              }
              return result;
            })
        .when(jdbcTemplate)
        .queryForList(anyString(), anyInt());

    var result = this.catalogue.getById(1);

    assertThat(result.getInteger("id")).isEqualTo(1);
    assertThat(result.getMerkmalList("name")).isEqualTo(List.of("wert1", "wert2"));
  }
}
