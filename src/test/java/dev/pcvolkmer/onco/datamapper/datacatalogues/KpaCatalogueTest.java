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

import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpaCatalogueTest {

    JdbcTemplate jdbcTemplate;
    KpaCatalogue catalogue;

    @BeforeEach
    void setUp(@Mock JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogue = KpaCatalogue.create(jdbcTemplate);
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
                .isEqualTo("SELECT patient.patienten_id, dk_dnpm_kpa.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM dk_dnpm_kpa JOIN prozedur ON (prozedur.id = dk_dnpm_kpa.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND prozedur.id = ?");
    }

    @Test
    void shouldThrowExceptionIfNoKpaProcedureFound() {
        doAnswer(invocationOnMock -> List.of())
                .when(jdbcTemplate).query(anyString(), any(RowMapper.class), anyString());

        var ex = assertThrows(DataAccessException.class, () -> catalogue.getProcedureIdByCaseId("16000123"));
        assertThat(ex).hasMessage("No record found for case: 16000123");
    }

    @Test
    void shouldUseCorrectMerkmalQuery(@Mock Map<String, Object> resultSet) {
        when(resultSet.get(anyString()))
                .thenReturn(Map.of("feldname", "name", "feldwert", "wert"));

        doAnswer(invocationOnMock -> List.of(resultSet))
                .when(jdbcTemplate)
                .queryForList(anyString(), anyInt());

        this.catalogue.getMerkmaleById(1);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(this.jdbcTemplate).queryForList(captor.capture(), anyInt());

        assertThat(captor.getValue())
                .isEqualTo("SELECT feldname, feldwert FROM dk_dnpm_kpa_merkmale WHERE eintrag_id = ?");
    }

    @Test
    void shouldUseMerkmalList() {
        doAnswer(invocationOnMock -> {
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
