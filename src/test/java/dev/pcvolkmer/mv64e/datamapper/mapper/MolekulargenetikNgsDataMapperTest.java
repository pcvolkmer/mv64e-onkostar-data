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
import dev.pcvolkmer.mv64e.mtb.SomaticNgsReport;
import dev.pcvolkmer.mv64e.mtb.TumorCellContentMethodCodingCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  void shouldContainSimpleVariant() {
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
}
