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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KeimbahndiagnoseCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorausbreitungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorgradingCatalogue;
import dev.pcvolkmer.mv64e.mtb.MtbDiagnosis;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpaDiagnosisDataMapperTest {

  KpaCatalogue kpaCatalogue;
  HistologieCatalogue histologieCatalogue;
  TumorausbreitungCatalogue tumorausbreitungCatalogue;
  TumorgradingCatalogue tumorgradingCatalogue;
  KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue;
  PropertyCatalogue propertyCatalogue;

  KpaDiagnosisDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock KpaCatalogue kpaCatalogue,
      @Mock HistologieCatalogue histologieCatalogue,
      @Mock TumorausbreitungCatalogue tumorausbreitungCatalogue,
      @Mock TumorgradingCatalogue tumorgradingCatalogue,
      @Mock KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.histologieCatalogue = histologieCatalogue;
    this.tumorausbreitungCatalogue = tumorausbreitungCatalogue;
    this.tumorgradingCatalogue = tumorgradingCatalogue;
    this.keimbahndiagnoseCatalogue = keimbahndiagnoseCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper =
        new KpaDiagnosisDataMapper(
            kpaCatalogue,
            histologieCatalogue,
            tumorausbreitungCatalogue,
            tumorgradingCatalogue,
            keimbahndiagnoseCatalogue,
            propertyCatalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreateDiagnosis(@Mock ResultSet resultSet) {
    doAnswer(
            invocationOnMock ->
                Reference.builder()
                    .id(testData().get("patienten_id").toString())
                    .type("Patient")
                    .build())
        .when(resultSet)
        .getPatientReference();

    doAnswer(
            invocationOnMock -> {
              var columnName = invocationOnMock.getArgument(0, String.class);
              return testData().get(columnName);
            })
        .when(resultSet)
        .getString(anyString());

    doAnswer(invocationOnMock -> resultSet).when(kpaCatalogue).getById(anyInt());

    doAnswer(
            invocationOnMock ->
                new PropertyCatalogue.Entry(
                    "C00.0",
                    "Bösartige Neubildung: Äußere Oberlippe",
                    "Bösartige Neubildung: Äußere Oberlippe"))
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    doAnswer(
            invocationOnMock ->
                List.of(
                    ResultSet.from(
                        Map.of(
                            "id", 1,
                            "icd10", "C00.0",
                            "icd10_propcat_version", 42))))
        .when(keimbahndiagnoseCatalogue)
        .getAllByParentId(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbDiagnosis.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());
    assertThat(actual.getCode().getCode()).isEqualTo("F79.9");

    assertThat(actual.getGermlineCodes()).hasSize(1);
    assertThat(actual.getGermlineCodes().get(0).getCode()).isEqualTo("C00.0");
  }

  private static Map<String, Object> testData() {
    return Map.of(
        "id", "1",
        "icd10", "F79.9",
        "patienten_id", "42");
  }
}
