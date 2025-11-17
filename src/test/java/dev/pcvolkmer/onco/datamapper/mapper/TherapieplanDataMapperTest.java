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

import dev.pcvolkmer.mv64e.mtb.*;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.RebiopsieCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ReevaluationCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.TherapieplanCatalogue;
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
class TherapieplanDataMapperTest {

  TherapieplanCatalogue therapieplanCatalogue;
  EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  RebiopsieCatalogue rebiopsieCatalogue;
  ReevaluationCatalogue reevaluationCatalogue;
  PropertyCatalogue propertyCatalogue;

  TherapieplanDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock TherapieplanCatalogue therapieplanCatalogue,
      @Mock RebiopsieCatalogue rebiopsieCatalogue,
      @Mock ReevaluationCatalogue reevaluationCatalogue,
      @Mock EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.rebiopsieCatalogue = rebiopsieCatalogue;
    this.reevaluationCatalogue = reevaluationCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    this.dataMapper =
        new TherapieplanDataMapper(
            therapieplanCatalogue,
            rebiopsieCatalogue,
            reevaluationCatalogue,
            einzelempfehlungCatalogue,
            propertyCatalogue);
  }

  @Test
  void shouldCreateCarePlan(@Mock ResultSet resultSet) {
    final var testData =
        Map.of(
            "id", "1",
            "patienten_id", "42",
            "wirkstoffe_json",
                "[{\"code\":\"\",\"name\":\"PARP-Inhibierung\",\"system\":\"UNREGISTERED\"}]",
            "protokollauszug", "Das ist ein Protokollauszug",
            "mit_einzelempfehlung", true,
            "empfehlungskategorie", "systemisch");

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
        .isTrue(anyString());

    doAnswer(invocationOnMock -> resultSet).when(therapieplanCatalogue).getById(anyInt());

    doAnswer(invocationOnMock -> List.of(resultSet))
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock ->
                ResultSet.from(
                    Map.of(
                        "datum",
                        new java.sql.Date(
                            Date.from(Instant.parse("2025-07-11T12:00:00Z")).getTime()),
                        "ref_dnpm_klinikanamnese",
                        "4711")))
        .when(this.einzelempfehlungCatalogue)
        .getParentById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");
    assertThat(actual.getPatient()).isEqualTo(Reference.builder().id("42").type("Patient").build());

    assertThat(actual.getMedicationRecommendations())
        .satisfies(
            recommendations -> {
              assertThat(recommendations).hasSize(1);
              assertThat(recommendations.get(0))
                  .satisfies(
                      recommendation -> {
                        assertThat(recommendation.getId()).isEqualTo("1");
                        assertThat(recommendation.getIssuedOn())
                            .isEqualTo(Date.from(Instant.parse("2025-07-11T00:00:00Z")));
                      });
            });

    assertThat(actual.getNotes()).hasSize(1);
    assertThat(actual.getNotes().get(0)).isEqualTo("Das ist ein Protokollauszug");
  }

  @Test
  void shouldSetRecommendationsMissingReason() {
    final Map<String, Object> testData =
        Map.of(
            "id", 1,
            "patienten_id", 42,
            "status_begruendung", "no-target");

    doAnswer(invocationOnMock -> ResultSet.from(testData))
        .when(therapieplanCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");

    assertThat(actual.getRecommendationsMissingReason())
        .isEqualTo(
            MtbCarePlanRecommendationsMissingReasonCoding.builder()
                .code(MtbCarePlanRecommendationsMissingReasonCodingCode.NO_TARGET)
                .build());

    assertThat(actual.getNoSequencingPerformedReason()).isNull();
  }

  @Test
  void shouldSetNoSequencingPerformedReason() {
    final Map<String, Object> testData =
        Map.of(
            "id", 1,
            "patienten_id", 42,
            "status_begruendung", "non-genetic-cause");

    doAnswer(invocationOnMock -> ResultSet.from(testData))
        .when(therapieplanCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");

    assertThat(actual.getRecommendationsMissingReason()).isNull();

    assertThat(actual.getNoSequencingPerformedReason())
        .isEqualTo(
            CarePlanNoSequencingPerformedReasonCoding.builder()
                .code(NoSequencingPerformedReasonCode.NON_GENETIC_CAUSE)
                .build());
  }

  @Test
  void shouldMapHumGenBeratung() {
    final Map<String, Object> testData =
        Map.of(
            "id", 1,
            "patienten_id", 42,
            "humangen_beratung", 1,
            "humangen_ber_grund", "other",
            "humangen_ber_grund_propcat_version", 1234);

    doAnswer(invocationOnMock -> ResultSet.from(testData))
        .when(therapieplanCatalogue)
        .getById(anyInt());

    doAnswer(
            invocationOnMock -> {
              var testPropertyData =
                  Map.of("other", new PropertyCatalogue.Entry("other", "Andere", "Andere"));

              var code = invocationOnMock.getArgument(0, String.class);
              return testPropertyData.get(code);
            })
        .when(propertyCatalogue)
        .getByCodeAndVersion(anyString(), anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");

    assertThat(actual.getRecommendationsMissingReason()).isNull();

    assertThat(actual.getGeneticCounselingRecommendation())
        .isEqualTo(
            GeneticCounselingRecommendation.builder()
                .id("1")
                .patient(Reference.builder().id("42").type("Patient").build())
                .reason(
                    GeneticCounselingRecommendationReasonCoding.builder()
                        .code(GeneticCounselingRecommendationReasonCodingCode.OTHER)
                        .display("Andere")
                        .system("dnpm-dip/mtb/recommendation/genetic-counseling/reason")
                        .build())
                .build());
  }
}
