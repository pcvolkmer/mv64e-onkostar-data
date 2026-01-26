package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenetikCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenuntersuchungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MolekulargenetikNgsDataMapperTest {

  MolekulargenetikCatalogue molekulargenetikCatalogue;
  MolekulargenuntersuchungCatalogue molekulargenuntersuchungCatalogue;
  PropertyCatalogue propertyCatalogue;

  MolekulargenetikNgsDataMapper mapper;

  @BeforeEach
  void setup(
      @Mock MolekulargenetikCatalogue molekulargenetikCatalogue,
      @Mock MolekulargenuntersuchungCatalogue molekulargenuntersuchungCatalogue,
      @Mock PropertyCatalogue propertyCatalogue) {
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.molekulargenuntersuchungCatalogue = molekulargenuntersuchungCatalogue;
    this.propertyCatalogue = propertyCatalogue;
    mapper =
        new MolekulargenetikNgsDataMapper(
            molekulargenetikCatalogue,
            molekulargenuntersuchungCatalogue,
            propertyCatalogue,
            TumorCellContentMethodCodingCode.HISTOLOGIC);
  }

  @Test
  void shouldNotMapNgsReportIfNotOfSequencingType() {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(false);

    var actual = this.mapper.getById(1);

    assertThat(actual).isNull();
  }

  @Test
  void shouldMapNgsReport() {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("AnalyseMethoden").values("S"),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(true);

    var actual = this.mapper.getById(1);

    assertThat(actual).isInstanceOf(SomaticNgsReport.class);
  }

  @Test
  void shouldContainSimpleVariantWithDataAsIs() {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("AnalyseMethoden").values("S"),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(eq(1));

    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name(Column.PATIENTEN_ID).value(4711),
                      Column.name(Column.HAUPTPROZEDUR_ID).value(1),
                      PropcatColumn.name("ergebnis").value("P"),
                      Column.name("untersucht").value("BRAF"),
                      Column.name("EVStart").value(123),
                      Column.name("EVEnde").value(125),
                      Column.name("evaltnucleotide").value("C"),
                      Column.name("evrefnucleotide").value("A"),
                      // Not real data - just for testing purposes
                      Column.name("evhgncid").value("HGNC:1234"),
                      Column.name("evchromosom").value("chr1"),
                      Column.name("evensemblid").value("ENSG00000123456")));
            })
        .when(molekulargenuntersuchungCatalogue)
        .getAllByParentId(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(true);

    var actual = this.mapper.getById(1);

    assertThat(actual).isInstanceOf(SomaticNgsReport.class);
    assertThat(actual.getResults()).isNotNull();
    assertThat(actual.getResults())
        .satisfies(
            results -> {
              assertThat(results).isNotNull();
              assertThat(results.getSimpleVariants())
                  .satisfies(
                      simpleVariants -> {
                        assertThat(simpleVariants).hasSize(1);
                        assertThat(simpleVariants.get(0).getChromosome())
                            .isEqualTo(Chromosome.CHR1);
                        assertThat(simpleVariants.get(0).getGene().getCode())
                            .isEqualTo("HGNC:1234");
                        assertThat(simpleVariants.get(0).getGene().getDisplay()).isEqualTo("BRAF");
                        assertThat(simpleVariants.get(0).getGene().getSystem())
                            .isEqualTo("https://www.genenames.org/");
                        assertThat(simpleVariants.get(0).getTranscriptId())
                            .isEqualTo(
                                TranscriptId.builder()
                                    .value("ENSG00000123456")
                                    .system(TranscriptIdSystem.ENSEMBL_ORG)
                                    .build());
                        assertThat(simpleVariants.get(0).getPosition())
                            .satisfies(
                                position -> {
                                  assertThat(position.getStart()).isEqualTo(123);
                                  assertThat(position.getEnd()).isEqualTo(125);
                                });
                        assertThat(simpleVariants.get(0).getRefAllele()).isEqualTo("A");
                        assertThat(simpleVariants.get(0).getAltAllele()).isEqualTo("C");
                      });
            });
  }

  @Test
  void shouldContainSimpleVariantWithMissingDataFromGeneList() {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("AnalyseMethoden").values("S"),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(eq(1));

    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name(Column.PATIENTEN_ID).value(4711),
                      Column.name(Column.HAUPTPROZEDUR_ID).value(1),
                      PropcatColumn.name("ergebnis").value("P"),
                      Column.name("untersucht").value("BRAF"),
                      Column.name("EVStart").value(123),
                      Column.name("EVEnde").value(125),
                      Column.name("evaltnucleotide").value("C"),
                      Column.name("evrefnucleotide").value("A")
                      // Not more data - fetch Information from gene list
                      ));
            })
        .when(molekulargenuntersuchungCatalogue)
        .getAllByParentId(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(true);

    var actual = this.mapper.getById(1);

    assertThat(actual).isInstanceOf(SomaticNgsReport.class);
    assertThat(actual.getResults()).isNotNull();
    assertThat(actual.getResults())
        .satisfies(
            results -> {
              assertThat(results).isNotNull();
              assertThat(results.getSimpleVariants())
                  .satisfies(
                      simpleVariants -> {
                        assertThat(simpleVariants).hasSize(1);
                        assertThat(simpleVariants.get(0).getChromosome())
                            .isEqualTo(Chromosome.CHR7);
                        assertThat(simpleVariants.get(0).getGene().getCode())
                            .isEqualTo("HGNC:1097");
                        assertThat(simpleVariants.get(0).getGene().getDisplay()).isEqualTo("BRAF");
                        assertThat(simpleVariants.get(0).getGene().getSystem())
                            .isEqualTo("https://www.genenames.org/");
                        assertThat(simpleVariants.get(0).getTranscriptId())
                            .isEqualTo(
                                TranscriptId.builder()
                                    .value("ENSG00000157764")
                                    .system(TranscriptIdSystem.ENSEMBL_ORG)
                                    .build());
                        assertThat(simpleVariants.get(0).getPosition())
                            .satisfies(
                                position -> {
                                  assertThat(position.getStart()).isEqualTo(123);
                                  assertThat(position.getEnd()).isEqualTo(125);
                                });
                        assertThat(simpleVariants.get(0).getRefAllele()).isEqualTo("A");
                        assertThat(simpleVariants.get(0).getAltAllele()).isEqualTo("C");
                      });
            });
  }

  @Test
  void shouldContainSimpleVariantWithoutEndPosition() {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("AnalyseMethoden").values("S"),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(eq(1));

    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name(Column.PATIENTEN_ID).value(4711),
                      Column.name(Column.HAUPTPROZEDUR_ID).value(1),
                      PropcatColumn.name("ergebnis").value("P"),
                      Column.name("untersucht").value("BRAF"),
                      Column.name("EVStart").value(123),
                      Column.name("evaltnucleotide").value("C"),
                      Column.name("evrefnucleotide").value("A")));
            })
        .when(molekulargenuntersuchungCatalogue)
        .getAllByParentId(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(true);

    var actual = this.mapper.getById(1);

    assertThat(actual).isInstanceOf(SomaticNgsReport.class);
    assertThat(actual.getResults()).isNotNull();
    assertThat(actual.getResults())
        .satisfies(
            results -> {
              assertThat(results).isNotNull();
              assertThat(results.getSimpleVariants())
                  .satisfies(
                      simpleVariants -> {
                        assertThat(simpleVariants).hasSize(1);
                        assertThat(simpleVariants.get(0).getGene().getCode())
                            .isEqualTo("HGNC:1097");
                        assertThat(simpleVariants.get(0).getGene().getDisplay()).isEqualTo("BRAF");
                        assertThat(simpleVariants.get(0).getGene().getSystem())
                            .isEqualTo("https://www.genenames.org/");
                        assertThat(simpleVariants.get(0).getPosition())
                            .satisfies(
                                position -> {
                                  assertThat(position.getStart()).isEqualTo(123);
                                  assertThat(position.getEnd()).isNull();
                                });
                        assertThat(simpleVariants.get(0).getRefAllele()).isEqualTo("A");
                        assertThat(simpleVariants.get(0).getAltAllele()).isEqualTo("C");
                      });
            });
  }

  @ParameterizedTest
  @CsvSource({
    "p.F123G,p.Phe123Gly",
    "p.L123F,p.Leu123Phe",
    "p.S123L,p.Ser123Leu",
    "p.Y123S,p.Tyr123Ser",
    "p.C123Y,p.Cys123Tyr",
    "p.W123C,p.Trp123Cys",
    "p.P123W,p.Pro123Trp",
    "p.H123P,p.His123Pro",
    "p.Q123H,p.Gln123His",
    "p.R123Q,p.Arg123Gln",
    "p.I123R,p.Ile123Arg",
    "p.M123I,p.Met123Ile",
    "p.T123M,p.Thr123Met",
    "p.N123T,p.Asn123Thr",
    "p.K123N,p.Lys123Asn",
    "p.V123K,p.Val123Lys",
    "p.A123V,p.Ala123Val",
    "p.D123A,p.Asp123Ala",
    "p.E123D,p.Glu123Asp",
    "p.G123E,p.Gly123Glu",
    // Examples from Onkostar Notices
    "p.L858R,p.Leu858Arg",
    "p.*del*,p.*del*",
    "p.V600*,p.Val600*",
    // Not mappable - keep as is
    "p.X123X,p.X123X",
    "c.123A>C,c.123A>C"
  })
  void shouldMapProteinChangeTo3LetterFormat(final String shortValue, final String expectedValue) {
    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return TestResultSet.withColumns(
                  Column.name(Column.ID).value(id),
                  Column.name(Column.PATIENTEN_ID).value(4711),
                  PropcatColumn.name("AnalyseMethoden").values("S"),
                  PropcatColumn.name("entnahmemethode").value("B"),
                  PropcatColumn.name("probenmaterial").value("T"));
            })
        .when(molekulargenetikCatalogue)
        .getById(eq(1));

    doAnswer(
            invocationOnMock -> {
              var id = invocationOnMock.getArgument(0, Integer.class);
              return List.of(
                  TestResultSet.withColumns(
                      Column.name(Column.ID).value(id),
                      Column.name(Column.PATIENTEN_ID).value(4711),
                      Column.name(Column.HAUPTPROZEDUR_ID).value(1),
                      PropcatColumn.name("ergebnis").value("P"),
                      Column.name("untersucht").value("BRAF"),
                      Column.name("EVStart").value(123),
                      Column.name("evaltnucleotide").value("C"),
                      Column.name("evrefnucleotide").value("A"),
                      Column.name("proteinebenenomenklatur").value(shortValue)));
            })
        .when(molekulargenuntersuchungCatalogue)
        .getAllByParentId(anyInt());

    when(molekulargenetikCatalogue.isOfTypeSeqencing(anyInt())).thenReturn(true);

    var actual = this.mapper.getById(1);

    assertThat(actual).isInstanceOf(SomaticNgsReport.class);
    assertThat(actual.getResults()).isNotNull();
    assertThat(actual.getResults())
        .satisfies(
            results -> {
              assertThat(results).isNotNull();
              assertThat(results.getSimpleVariants())
                  .satisfies(
                      simpleVariants -> {
                        assertThat(simpleVariants).hasSize(1);
                        assertThat(simpleVariants.get(0).getProteinChange())
                            .isEqualTo(expectedValue);
                      });
            });
  }
}
