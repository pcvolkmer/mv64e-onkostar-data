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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenetikCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.VorbefundeCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.mtb.MolecularDiagnosticReportCoding;
import dev.pcvolkmer.mv64e.mtb.MolecularDiagnosticReportCodingCode;
import dev.pcvolkmer.mv64e.mtb.PriorDiagnosticReport;
import dev.pcvolkmer.mv64e.mtb.Reference;
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
class KpaVorbefundeDataMapperTest {

  VorbefundeCatalogue catalogue;
  MolekulargenetikCatalogue molekulargenetikCatalogue;
  PropertyCatalogue propertyCatalogue;

  KpaVorbefundeDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock VorbefundeCatalogue catalogue,
      @Mock MolekulargenetikCatalogue molekulargenetikCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {
    this.catalogue = catalogue;
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper =
        new KpaVorbefundeDataMapper(catalogue, molekulargenetikCatalogue, propertyCatalogue);
  }

  @Test
  void shouldMapResultSet() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        DateColumn.name("erstellungsdatum").value("2000-07-06"),
                        Column.name("befundnummer").value("X/2025/1234"),
                        Column.name("ergebnisse").value("Befundtext"),
                        PropcatColumn.name("artderdiagnostik").value("panel"))))
        .when(catalogue)
        .getAllByParentId(anyInt());

    doAnswer(invocationOnMock -> ResultSet.from(Map.of("id", 1, "einsendenummer", "X/2025/1234")))
        .when(molekulargenetikCatalogue)
        .getByEinsendenummer(anyString());

    doAnswer(
            invocationOnMock -> {
              var testPropertyData =
                  Map.of("panel", new PropertyCatalogue.Entry("panel", "Panel", "Panel"));
              var code = invocationOnMock.getArgument(0, String.class);
              return testPropertyData.get(code);
            })
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).hasSize(1);

    var actual = actualList.get(0);
    assertThat(actual).isInstanceOf(PriorDiagnosticReport.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());
    assertThat(actual.getIssuedOn())
        .isEqualTo(new java.sql.Date(Date.from(Instant.parse("2000-07-06T00:00:00Z")).getTime()));
    assertThat(actual.getSpecimen())
        .isEqualTo(Reference.builder().id("1").type("Specimen").build());
    assertThat(actual.getType())
        .isEqualTo(
            MolecularDiagnosticReportCoding.builder()
                .code(MolecularDiagnosticReportCodingCode.PANEL)
                .display("Panel")
                .build());
    assertThat(actual.getResults()).containsExactly("Befundtext");
  }

  @Test
  void shouldReturnEmptyListOnDataAccessErrorRequestingByParentId() {
    when(catalogue.getAllByParentId(anyInt())).thenThrow(new DataAccessException("Test"));

    var actualList = this.dataMapper.getByParentId(1);
    assertThat(actualList).isEmpty();
  }
}
