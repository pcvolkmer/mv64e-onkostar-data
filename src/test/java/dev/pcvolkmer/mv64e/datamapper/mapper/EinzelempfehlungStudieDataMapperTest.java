package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
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
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(100),
                    DateColumn.name("datum").value("2025-07-11"),
                    Column.name("ref_dnpm_klinikanamnese").value("4711")))
        .when(therapieplanCatalogue)
        .getById(anyInt());
  }

  @Test
  void shouldMapEinzelempfehlungEvenWithoutEvidenzlevel() {
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(1));

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
  }

  @Test
  void shouldMapEvidenzlevel() {
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(1),
            PropcatColumn.name("evidenzlevel").value("3"),
            Column.name("evidenzlevel_publication").value("12345678\n12.2024/123"));

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
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(1),
            PropcatColumn.name("evidenzlevel").value("3"));

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
  void shouldNotMapEmptyEvidenzlevel() {
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(1));

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getLevelOfEvidence()).isNull();
  }

  @Test
  void shouldMapIssuedOn() {
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(1));

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getIssuedOn()).isEqualTo(Date.from(Instant.parse("2025-07-11T00:00:00Z")));
  }

  @Test
  void shouldMapDefaultLowestPrio() {
    var resultSet =
        TestResultSet.withColumns(
            Column.name(Column.ID).value(1),
            Column.name(Column.HAUPTPROZEDUR_ID).value(100),
            Column.name(Column.PATIENTEN_ID).value(42),
            Column.name("prio").value(99));

    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getIssuedOn()).isEqualTo(Date.from(Instant.parse("2025-07-11T00:00:00Z")));
    assertThat(actual.getPriority().getCode()).isEqualTo(RecommendationPriorityCodingCode.CODE_4);
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      excludeColumns = {Column.PATIENTEN_ID, Column.HAUPTPROZEDUR_ID})
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var actual = this.mapper.getById(1);
    assertThat(actual).isNotNull();
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      includeColumns = {Column.PATIENTEN_ID, Column.HAUPTPROZEDUR_ID})
  @MockitoSettings(strictness = Strictness.LENIENT)
  void fuzzTestNullColumnsThrowsDataAccessException(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);

    var ex = assertThrows(DataAccessException.class, () -> this.mapper.getById(1));
    assertThat(ex.getMessage()).isIn("No patient id found", "Cannot fetch 'Therapieplan'");
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        Column.name("hauptprozedur_id").value(100),
        Column.name("patienten_id").value(42),
        Column.name("prio").value(1),
        PropcatColumn.name("evidenzlevel").value("1"),
        Column.name("evidenzlevel_publication").value("12345678\n12.2024/123"));
  }
}
