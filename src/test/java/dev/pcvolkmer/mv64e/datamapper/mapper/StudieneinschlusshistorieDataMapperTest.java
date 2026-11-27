/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
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
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.StudieneinschlussCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.model.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class StudieneinschlusshistorieDataMapperTest {

  StudieneinschlussCatalogue studieneinschlussCatalogue;
  TherapieplanCatalogue therapieplanCatalogue;
  EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  FollowUpCatalogue followUpCatalogue;
  PropertyCatalogue propertyCatalogue;
  StudieneinschlusshistorieDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock StudieneinschlussCatalogue studieneinschlussCatalogue,
      @Mock TherapieplanCatalogue therapieplanCatalogue,
      @Mock EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      @Mock FollowUpCatalogue followUpCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {

    this.studieneinschlussCatalogue = studieneinschlussCatalogue;
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.followUpCatalogue = followUpCatalogue;
    this.propertyCatalogue = propertyCatalogue;

    this.dataMapper =
        new StudieneinschlusshistorieDataMapper(
            studieneinschlussCatalogue,
            therapieplanCatalogue,
            einzelempfehlungCatalogue,
            followUpCatalogue,
            propertyCatalogue);
  }

  @Test
  void testShouldMapFollowUpFormToSystemicTherapy() {

    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1001));

    when(einzelempfehlungCatalogue.getAllByParentId(eq(1001)))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(2001),
                    Column.name(Column.HAUPTPROZEDUR_ID).value(1001),
                    PropcatColumn.name("empfehlungskategorie").value("systemisch")),
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(2002),
                    Column.name(Column.HAUPTPROZEDUR_ID).value(1001),
                    PropcatColumn.name("empfehlungskategorie").value("op"))));

    when(followUpCatalogue.getByRecommendationId(eq(2001))).thenReturn(List.of(3001));

    when(studieneinschlussCatalogue.getAllByParentId(eq(3001)))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(4001),
                    Column.name(Column.PATIENTEN_ID).value(1),
                    DateColumn.name("beginn").value("2026-11-01"),
                    DateColumn.name("ende").value("2026-12-31"),
                    DateColumn.name("erfassungsdatum").value("2026-11-26"),
                    PropcatColumn.name("status").value("on-going"),
                    PropcatColumn.name("statusgrund").value("other"),
                    Column.name("wirkstoffe_json").value("[]"),
                    Column.name("studie").value("NCT1234"),
                    Column.name("studiensystem").value("NCT"),
                    Column.name("ref_einzelempfehlung").value(2001))));

    when(propertyCatalogue.getByCodeAndVersion(eq("on-going"), anyInt()))
        .thenReturn(new PropertyCatalogue.Entry("on-going", "Laufend", "Laufend"));

    when(propertyCatalogue.getByCodeAndVersion(eq("other"), anyInt()))
        .thenReturn(new PropertyCatalogue.Entry("other", "other", "other"));

    final var actual = this.dataMapper.getById(1001);

    assertThat(actual).size().isEqualTo(1);
    assertThat(actual.get(0).getHistory()).size().isEqualTo(1);

    assertThat(actual.get(0).getHistory().get(0))
        .satisfies(
            studieneinschluss -> {
              assertThat(studieneinschluss.getId()).isEqualTo("4001");
              assertThat(studieneinschluss.getPatient())
                  .isEqualTo(Reference.builder().id("1").type("Patient").build());
              assertThat(studieneinschluss.getPeriod())
                  .isEqualTo(
                      PeriodDate.builder()
                          .start(Date.from(Instant.parse("2026-11-01T00:00:00Z")))
                          .end(Date.from(Instant.parse("2026-12-31T00:00:00Z")))
                          .build());
              assertThat(studieneinschluss.getRecordedOn())
                  .isEqualTo(Date.from(Instant.parse("2026-11-26T00:00:00Z")));
              assertThat(studieneinschluss.getStudy())
                  .isEqualTo(
                      List.of(
                          StudyReference.builder()
                              .id("NCT1234")
                              .system(StudyReference.SystemEnum.NCT)
                              .type("Study")
                              .build()));
              assertThat(studieneinschluss.getStatus())
                  .isEqualTo(
                      TherapyStatusCoding.builder()
                          .code(TherapyStatusCoding.CodeEnum.ON_GOING)
                          .display("Laufend")
                          .system("dnpm-dip/therapy/status")
                          .build());
              assertThat(studieneinschluss.getStatusReason())
                  .isEqualTo(
                      MtbTherapyStatusReasonCoding.builder()
                          .code(MtbTherapyStatusReasonCoding.CodeEnum.OTHER)
                          .display("other")
                          .system("dnpm-dip/therapy/status-reason")
                          .build());
              assertThat(studieneinschluss.getReason())
                  .isEqualTo(Reference.builder().id("1001").type("MTBDiagnosis").build());
              assertThat(studieneinschluss.getBasedOn())
                  .isEqualTo(
                      Reference.builder()
                          .id("2001")
                          .type("MTBStudyEnrollmentRecommendation")
                          .build());
            });
  }
}
