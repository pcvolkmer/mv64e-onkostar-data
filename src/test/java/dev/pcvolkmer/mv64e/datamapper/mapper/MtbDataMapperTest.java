/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026  Paul-Christian Volkmer
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.*;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class MtbDataMapperTest {

  JdbcTemplate jdbcTemplate;

  MtbDataMapper mtbDataMapper;

  @BeforeEach
  void setUp(@Mock JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.mtbDataMapper = new MtbDataMapper(jdbcTemplate);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Nested
  class InitializedMtbDataMapper {

    MtbDataMapper out;

    DataCatalogueFactory dataCatalogueFactory;

    KpaCatalogue kpaCatalogue;
    PatientCatalogue patientCatalogue;
    TherapieplanCatalogue therapieplanCatalogue;
    HistologieCatalogue histologieCatalogue;
    TumorgradingCatalogue tumorgradingCatalogue;
    TumorausbreitungCatalogue tumorausbreitungCatalogue;
    KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue;
    ProzedurCatalogue prozedurCatalogue;
    TherapielinieCatalogue therapielinieCatalogue;
    EcogCatalogue ecogCatalogue;
    EinzelempfehlungCatalogue einzelempfehlungCatalogue;
    RebiopsieCatalogue rebiopsieCatalogue;
    ReevaluationCatalogue reevaluationCatalogue;
    VorbefundeCatalogue vorbefundeCatalogue;
    MolekulargenetikCatalogue molekulargenetikCatalogue;
    MolekulargenuntersuchungCatalogue molekulargenuntersuchungCatalogue;
    MolekulargenMsiCatalogue molekulargenMsiCatalogue;
    VerwandteCatalogue verwandteCatalogue;
    ConsentMvCatalogue consentMvCatalogue;
    ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue;

    /* very large initialization of catalogues as mock objects */
    @BeforeEach
    void setup(
        @Mock DataCatalogueFactory dataCatalogueFactory,
        @Mock PropertyCatalogue propertyCatalogue,
        @Mock KpaCatalogue kpaCatalogue,
        @Mock PatientCatalogue patientCatalogue,
        @Mock TherapieplanCatalogue therapieplanCatalogue,
        @Mock HistologieCatalogue histologieCatalogue,
        @Mock TumorgradingCatalogue tumorgradingCatalogue,
        @Mock TumorausbreitungCatalogue tumorausbreitungCatalogue,
        @Mock KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue,
        @Mock ProzedurCatalogue prozedurCatalogue,
        @Mock TherapielinieCatalogue therapielinieCatalogue,
        @Mock EcogCatalogue ecogCatalogue,
        @Mock EinzelempfehlungCatalogue einzelempfehlungCatalogue,
        @Mock RebiopsieCatalogue rebiopsieCatalogue,
        @Mock ReevaluationCatalogue reevaluationCatalogue,
        @Mock VorbefundeCatalogue vorbefundeCatalogue,
        @Mock MolekulargenetikCatalogue molekulargenetikCatalogue,
        @Mock MolekulargenuntersuchungCatalogue molekulargenuntersuchungCatalogue,
        @Mock MolekulargenMsiCatalogue molekulargenMsiCatalogue,
        @Mock VerwandteCatalogue verwandteCatalogue,
        @Mock ConsentMvCatalogue consentMvCatalogue,
        @Mock ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue) {
      this.dataCatalogueFactory = dataCatalogueFactory;
      this.kpaCatalogue = kpaCatalogue;
      this.patientCatalogue = patientCatalogue;
      this.therapieplanCatalogue = therapieplanCatalogue;
      this.histologieCatalogue = histologieCatalogue;
      this.tumorgradingCatalogue = tumorgradingCatalogue;
      this.tumorausbreitungCatalogue = tumorausbreitungCatalogue;
      this.keimbahndiagnoseCatalogue = keimbahndiagnoseCatalogue;
      this.prozedurCatalogue = prozedurCatalogue;
      this.therapielinieCatalogue = therapielinieCatalogue;
      this.ecogCatalogue = ecogCatalogue;
      this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
      this.rebiopsieCatalogue = rebiopsieCatalogue;
      this.reevaluationCatalogue = reevaluationCatalogue;
      this.vorbefundeCatalogue = vorbefundeCatalogue;
      this.molekulargenetikCatalogue = molekulargenetikCatalogue;
      this.molekulargenuntersuchungCatalogue = molekulargenuntersuchungCatalogue;
      this.molekulargenMsiCatalogue = molekulargenMsiCatalogue;
      this.verwandteCatalogue = verwandteCatalogue;
      this.consentMvCatalogue = consentMvCatalogue;
      this.consentMvVerlaufCatalogue = consentMvVerlaufCatalogue;

      this.out =
          new MtbDataMapper(
              dataCatalogueFactory, propertyCatalogue, TumorCellContentMethodCodingCode.HISTOLOGIC);

      try (MockedStatic<DataCatalogueFactory> mock = mockStatic(DataCatalogueFactory.class)) {
        mock.when(DataCatalogueFactory::instance).thenReturn(dataCatalogueFactory);
      }

      when(propertyCatalogue.getByCodeAndVersion(anyString(), anyInt()))
          .thenReturn(new PropertyCatalogue.Entry("test", "Test", "Test"));

      when(dataCatalogueFactory.catalogue(KpaCatalogue.class)).thenReturn(kpaCatalogue);
      when(dataCatalogueFactory.catalogue(PatientCatalogue.class)).thenReturn(patientCatalogue);
      when(dataCatalogueFactory.catalogue(TherapieplanCatalogue.class))
          .thenReturn(therapieplanCatalogue);
      when(dataCatalogueFactory.catalogue(HistologieCatalogue.class))
          .thenReturn(histologieCatalogue);
      when(dataCatalogueFactory.catalogue(TumorgradingCatalogue.class))
          .thenReturn(tumorgradingCatalogue);
      when(dataCatalogueFactory.catalogue(TumorausbreitungCatalogue.class))
          .thenReturn(tumorausbreitungCatalogue);
      when(dataCatalogueFactory.catalogue(KeimbahndiagnoseCatalogue.class))
          .thenReturn(keimbahndiagnoseCatalogue);
      when(dataCatalogueFactory.catalogue(ProzedurCatalogue.class)).thenReturn(prozedurCatalogue);
      when(dataCatalogueFactory.catalogue(TherapielinieCatalogue.class))
          .thenReturn(therapielinieCatalogue);
      when(dataCatalogueFactory.catalogue(EcogCatalogue.class)).thenReturn(ecogCatalogue);
      when(dataCatalogueFactory.catalogue(EinzelempfehlungCatalogue.class))
          .thenReturn(einzelempfehlungCatalogue);
      when(dataCatalogueFactory.catalogue(RebiopsieCatalogue.class)).thenReturn(rebiopsieCatalogue);
      when(dataCatalogueFactory.catalogue(ReevaluationCatalogue.class))
          .thenReturn(reevaluationCatalogue);
      when(dataCatalogueFactory.catalogue(VorbefundeCatalogue.class))
          .thenReturn(vorbefundeCatalogue);
      when(dataCatalogueFactory.catalogue(MolekulargenetikCatalogue.class))
          .thenReturn(molekulargenetikCatalogue);
      when(dataCatalogueFactory.catalogue(MolekulargenuntersuchungCatalogue.class))
          .thenReturn(molekulargenuntersuchungCatalogue);
      when(dataCatalogueFactory.catalogue(MolekulargenMsiCatalogue.class))
          .thenReturn(molekulargenMsiCatalogue);
      when(dataCatalogueFactory.catalogue(VerwandteCatalogue.class)).thenReturn(verwandteCatalogue);
      when(dataCatalogueFactory.catalogue(ConsentMvCatalogue.class)).thenReturn(consentMvCatalogue);
      when(dataCatalogueFactory.catalogue(ConsentMvVerlaufCatalogue.class))
          .thenReturn(consentMvVerlaufCatalogue);

      when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of());
      when(tumorgradingCatalogue.getAllByParentId(anyInt())).thenReturn(List.of());
      when(keimbahndiagnoseCatalogue.getAllByParentId(anyInt())).thenReturn(List.of());
      when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of());
    }

    @Test
    void shouldInitializeMetadata() {
      when(kpaCatalogue.getById(anyInt()))
          .thenReturn(
              TestResultSet.withColumns(
                  Column.name(Column.ID).value(1),
                  Column.name(Column.PATIENTEN_ID).value(42),
                  Column.name("patient_id").value(42),
                  PropcatColumn.name("icd10").value("C00.0"),
                  PropcatColumn.name("icdo3lokalisation").value("8000/0")));

      when(patientCatalogue.getById(anyInt()))
          .thenReturn(TestResultSet.withColumns(Column.name(Column.ID).value(42)));

      var actual = out.getById(1);

      assertThat(actual).isInstanceOf(Mtb.class);
      assertThat(actual.getMetadata()).isInstanceOf(MvhMetadata.class);
    }

    @Test
    void shouldMapMvConsent() {
      when(kpaCatalogue.getById(anyInt()))
          .thenReturn(
              TestResultSet.withColumns(
                  Column.name(Column.ID).value(1),
                  Column.name(Column.PATIENTEN_ID).value(42),
                  Column.name("patient_id").value(42),
                  PropcatColumn.name("icd10").value("C00.0"),
                  PropcatColumn.name("icdo3lokalisation").value("8000/0"),
                  Column.name("consentmv64e").value(10)));

      when(patientCatalogue.getById(anyInt()))
          .thenReturn(TestResultSet.withColumns(Column.name(Column.ID).value(42)));

      when(consentMvVerlaufCatalogue.getAllByParentId(anyInt()))
          .thenReturn(
              List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(10),
                      Column.name("sequencing").value("permit"),
                      Column.name("caseidentification").value("deny"),
                      Column.name("reidentification").value("deny"))));

      var actual = out.getById(1);

      assertThat(actual.getMetadata())
          .satisfies(
              mvhMetadata -> {
                assertThat(mvhMetadata.getModelProjectConsent())
                    .isInstanceOf(ModelProjectConsent.class);

                assertThat(mvhMetadata.getModelProjectConsent().getProvisions())
                    .satisfies(
                        provisions -> {
                          assertThat(provisions).hasSize(3);

                          final var expectedProvisions =
                              List.of(
                                  Provision.builder()
                                      .purpose(ModelProjectConsentPurpose.SEQUENCING)
                                      .type(ConsentProvision.PERMIT)
                                      .build(),
                                  Provision.builder()
                                      .purpose(ModelProjectConsentPurpose.CASE_IDENTIFICATION)
                                      .type(ConsentProvision.DENY)
                                      .build(),
                                  Provision.builder()
                                      .purpose(ModelProjectConsentPurpose.REIDENTIFICATION)
                                      .type(ConsentProvision.DENY)
                                      .build());

                          assertThat(provisions).containsAll((Iterable) expectedProvisions);
                        });
              });
    }

    @Test
    void shouldMapReasonMissingResearchConsent() {
      when(kpaCatalogue.getById(anyInt()))
          .thenReturn(
              TestResultSet.withColumns(
                  Column.name(Column.ID).value(1),
                  Column.name(Column.PATIENTEN_ID).value(42),
                  Column.name("patient_id").value(42),
                  PropcatColumn.name("icd10").value("C00.0"),
                  PropcatColumn.name("icdo3lokalisation").value("8000/0"),
                  PropcatColumn.name("grundkeinbroadconsent").value("consent-not-returned")));

      when(patientCatalogue.getById(anyInt()))
          .thenReturn(TestResultSet.withColumns(Column.name(Column.ID).value(42)));

      var actual = out.getById(1);

      assertThat(actual.getMetadata())
          .satisfies(
              mvhMetadata ->
                  assertThat(mvhMetadata.getReasonResearchConsentMissing())
                      .isEqualTo(ResearchConsentReasonMissing.CONSENT_NOT_RETURNED));
    }
  }
}
