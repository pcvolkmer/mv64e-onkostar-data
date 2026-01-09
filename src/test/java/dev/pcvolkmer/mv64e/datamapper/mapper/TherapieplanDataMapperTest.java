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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.RebiopsieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.ReevaluationCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.mtb.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
  void shouldCreateCarePlan() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name(Column.ID).value(1),
                        Column.name(Column.HAUPTPROZEDUR_ID).value(100),
                        Column.name(Column.PATIENTEN_ID).value(42),
                        Column.name("wirkstoffe_json")
                            .value(
                                "[{\"code\":\"\",\"name\":\"PARP-Inhibierung\",\"system\":\"UNREGISTERED\"}]"),
                        PropcatColumn.name("empfehlungskategorie").value("systemisch"))))
        .when(einzelempfehlungCatalogue)
        .getAllByParentId(anyInt());

    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(100),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    DateColumn.name("datum").value("2025-07-11"),
                    Column.name("ref_dnpm_klinikanamnese").value(4711),
                    Column.name("protokollauszug").value("Das ist ein Protokollauszug"),
                    Column.name("mit_einzelempfehlung").value(true)))
        .when(this.therapieplanCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("100");
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
  void shouldNotSetRecommendationMissingReasonAndNoSequencingPerformedReason() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1), Column.name(Column.PATIENTEN_ID).value(42)))
        .when(therapieplanCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");

    assertThat(actual.getRecommendationsMissingReason()).isNull();
    assertThat(actual.getNoSequencingPerformedReason()).isNull();
  }

  @Test
  void shouldSetRecommendationMissingReasonAndNoSequencingPerformedReason() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    PropcatColumn.name("target").value("KT"),
                    PropcatColumn.name("status_begruendung").value("non-genetic-cause")))
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

    assertThat(actual.getNoSequencingPerformedReason())
        .isEqualTo(
            CarePlanNoSequencingPerformedReasonCoding.builder()
                .code(NoSequencingPerformedReasonCode.NON_GENETIC_CAUSE)
                .build());
  }

  @Test
  void shouldSetRecommendationsMissingReason() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    PropcatColumn.name("target").value("KT")))
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

  @ParameterizedTest
  @ValueSource(strings = {"TG", "U"})
  void shouldNotSetRecommendationsMissingReason(String targetFoundCode) {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    PropcatColumn.name("target").value(targetFoundCode)))
        .when(therapieplanCatalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(MtbCarePlan.class);
    assertThat(actual.getId()).isEqualTo("1");

    assertThat(actual.getRecommendationsMissingReason()).isNull();
    assertThat(actual.getNoSequencingPerformedReason()).isNull();
  }

  @Test
  void shouldSetNoSequencingPerformedReason() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    PropcatColumn.name("status_begruendung").value("non-genetic-cause")))
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
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(1),
                    Column.name(Column.PATIENTEN_ID).value(42),
                    Column.name("humangen_beratung").value(1),
                    PropcatColumn.name("humangen_ber_grund").value("other")))
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
