package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.mtb.LevelOfEvidenceGradingCoding;
import dev.pcvolkmer.mv64e.mtb.LevelOfEvidenceGradingCodingCode;
import dev.pcvolkmer.mv64e.mtb.PublicationReference;
import dev.pcvolkmer.mv64e.mtb.PublicationSystem;
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
class EinzelempfehlungStudieDataMapperTest {

  EinzelempfehlungCatalogue catalogue;
  EinzelempfehlungStudieDataMapper mapper;

  @BeforeEach
  void setUp(
      @Mock EinzelempfehlungCatalogue catalogue,
      @Mock TherapieplanCatalogue therapieplanCatalogue) {
    this.catalogue = catalogue;
    this.mapper = new EinzelempfehlungStudieDataMapper(catalogue, therapieplanCatalogue);

    // Care Plan
    doAnswer(
            invocationOnMock ->
                ResultSet.from(
                    Map.of(
                        "datum",
                        new java.sql.Date(
                            Date.from(Instant.parse("2025-07-11T12:00:00Z")).getTime()),
                        "ref_dnpm_klinikanamnese",
                        "4711")))
        .when(therapieplanCatalogue)
        .getById(anyInt());
  }

  @Test
  void shouldMapEinzelempfehlungEvenWithoutEvidenzlevel() {
    Map<String, Object> testData =
        Map.of("id", 1, "hauptprozedur_id", 100, "patienten_id", 42, "prio", 1);
    var resultSet = ResultSet.from(testData);

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
  }

  @Test
  void shouldMapEvidenzlevel() {
    Map<String, Object> testData =
        Map.of(
            "id",
            1,
            "hauptprozedur_id",
            100,
            "patienten_id",
            42,
            "prio",
            1,
            "evidenzlevel",
            "3",
            "evidenzlevel_publication",
            "12345678\n12.2024/123",
            "evidenzlevel_propcat_version",
            42);
    var resultSet = ResultSet.from(testData);

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getLevelOfEvidence())
        .satisfies(
            levelOfEvidence -> {
              assertThat(levelOfEvidence).isNotNull();
              assertThat(levelOfEvidence.getGrading())
                  .satisfies(
                      grading -> {
                        assertThat(grading)
                            .isEqualTo(
                                LevelOfEvidenceGradingCoding.builder()
                                    .code(LevelOfEvidenceGradingCodingCode.M1C)
                                    .display("m1C")
                                    .system("dnpm-dip/mtb/level-of-evidence/grading")
                                    .build());
                      });
              assertThat(levelOfEvidence.getPublications())
                  .satisfies(
                      publicationReferences -> {
                        assertThat(publicationReferences).hasSize(2);
                        Iterable expectedPublicationReferences =
                            List.of(
                                PublicationReference.builder()
                                    .id("12345678")
                                    .system(PublicationSystem.PUBMED_NCBI_NLM_NIH_GOV)
                                    .type("Publication")
                                    .build(),
                                PublicationReference.builder()
                                    .id("12.2024/123")
                                    .system(PublicationSystem.DOI_ORG)
                                    .type("Publication")
                                    .build());
                        assertThat(publicationReferences)
                            .containsAll(expectedPublicationReferences);
                      });
            });
  }

  @Test
  void shouldMapEvidenzlevelWithoutPublications() {
    Map<String, Object> testData =
        Map.of(
            "id",
            1,
            "hauptprozedur_id",
            100,
            "patienten_id",
            42,
            "prio",
            1,
            "evidenzlevel",
            "3",
            "evidenzlevel_propcat_version",
            42);
    var resultSet = ResultSet.from(testData);

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getLevelOfEvidence())
        .satisfies(
            levelOfEvidence -> {
              assertThat(levelOfEvidence).isNotNull();
              assertThat(levelOfEvidence.getGrading())
                  .satisfies(
                      grading -> {
                        assertThat(grading)
                            .isEqualTo(
                                LevelOfEvidenceGradingCoding.builder()
                                    .code(LevelOfEvidenceGradingCodingCode.M1C)
                                    .display("m1C")
                                    .system("dnpm-dip/mtb/level-of-evidence/grading")
                                    .build());
                      });
              assertThat(levelOfEvidence.getPublications()).isNull();
            });
  }

  @Test
  void shouldMapIssuedOn() {
    Map<String, Object> testData =
        Map.of("id", 1, "hauptprozedur_id", 100, "patienten_id", 42, "prio", 1);
    var resultSet = ResultSet.from(testData);

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getIssuedOn()).isEqualTo(Date.from(Instant.parse("2025-07-11T00:00:00Z")));
  }
}
