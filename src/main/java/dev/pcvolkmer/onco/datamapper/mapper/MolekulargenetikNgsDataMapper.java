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

import dev.pcvolkmer.mv64e.mtb.*;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.*;
import dev.pcvolkmer.onco.datamapper.genes.GeneUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map prozedur data from database table 'dk_molekulargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MolekulargenetikNgsDataMapper implements DataMapper<SomaticNgsReport> {

  private static final Logger logger = LoggerFactory.getLogger(GeneUtils.class);
  private final MolekulargenetikCatalogue catalogue;
  private final MolekulargenuntersuchungCatalogue untersuchungCatalogue;
  private final TumorCellContentMethodCodingCode tumorCellContentMethod;

  public MolekulargenetikNgsDataMapper(
      final MolekulargenetikCatalogue catalogue,
      final MolekulargenuntersuchungCatalogue untersuchungCatalogue,
      final PropertyCatalogue propertyCatalogue,
      final TumorCellContentMethodCodingCode tumorCellContentMethod) {
    this.catalogue = catalogue;
    this.untersuchungCatalogue = untersuchungCatalogue;
    this.tumorCellContentMethod = tumorCellContentMethod;
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Procedure
   */
  @Override
  public SomaticNgsReport getById(final int id) {
    var data = catalogue.getById(id);

    var builder = SomaticNgsReport.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .issuedOn(data.getDate("datum"))
        .specimen(Reference.builder().id(data.getString("id")).type("Specimen").build())
        .type(getNgsReportCoding(data.getString("artdersequenzierung")))
        .metadata(List.of(getNgsReportMetadata(data.getString("artdersequenzierung"))))
        .results(this.getNgsReportResults(data));

    return builder.build();
  }

  /**
   * Loads and maps all Prozedur related by KPA database id
   *
   * @param kpaId The database id of the KPA procedure data set
   * @return The loaded Procedures
   */
  public List<SomaticNgsReport> getAllByKpaId(final int kpaId) {
    return this.catalogue.getIdsByKpaId(kpaId).stream()
        .distinct()
        .map(this::getById)
        .collect(Collectors.toList());
  }

  /**
   * Loads and maps all Prozedur related by KPA database id
   *
   * @param kpaId The database id of the KPA procedure data set
   * @return The loaded Procedures
   */
  public List<SomaticNgsReport> getAllByKpaIdWithHisto(
      final int kpaId, final List<Integer> molgenIdsFromHisto) {

    var molgenIdsFromTherapyPlan = this.catalogue.getIdsByKpaId(kpaId);

    // Merge both lists, remove duplicates
    return Stream.concat(
            molgenIdsFromTherapyPlan.stream(),
            molgenIdsFromHisto != null ? molgenIdsFromHisto.stream() : Stream.empty())
        .distinct()
        .map(this::getById)
        .distinct()
        .collect(Collectors.toList());
  }

  private NgsReportResults getNgsReportResults(ResultSet resultSet) {
    var subforms = this.untersuchungCatalogue.getAllByParentId(resultSet.getId());

    var resultBuilder = NgsReportResults.builder();

    if (null != resultSet.getLong("tumorzellgehalt")) {
      var tumorcellContentBuilder =
          TumorCellContent.builder()
              .id(resultSet.getId().toString())
              .patient(resultSet.getPatientReference())
              .specimen(Reference.builder().id(resultSet.getString("id")).type("Specimen").build())
              .value(resultSet.getLong("tumorzellgehalt") / 100.0);

      // Der Tumorcellcontent kann für NGS-Reports ausschließlich bioinformatisch
      // ermittelt werden.
      // Entsprechend wird er nur für diese Methode gemeldet.
      // Erfolgt eine histologische Ermittlung des Tumorcellcounts kann dieser über
      // einen histologischen Report gemeldet werden.
      if (tumorCellContentMethod == TumorCellContentMethodCodingCode.BIOINFORMATIC) {
        tumorcellContentBuilder.method(
            TumorCellContentMethodCoding.builder().code(tumorCellContentMethod).build());
        resultBuilder.tumorCellContent(tumorcellContentBuilder.build());
      }
    }

    resultBuilder.simpleVariants(
        subforms.stream()
            // P => Einfache Variante
            .filter(subform -> "P".equals(subform.getString("ergebnis")))
            .map(
                subform -> {
                  if (subform.getString("untersucht") == null) return null;

                  final var geneOptional = GeneUtils.findBySymbol(subform.getString("untersucht"));
                  if (geneOptional.isEmpty()) {
                    return null;
                  }

                  var gene = geneOptional.get();

                  final var snvBuilder =
                      Snv.builder()
                          .id(subform.getString("id"))
                          .patient(subform.getPatientReference());

                  // Check conversion
                  var coding = GeneUtils.toCoding(gene);
                  if (coding != null) snvBuilder.gene(coding);

                  // Add transcriptId from gene list if no EnsemblID is available
                  var ensemblId = subform.getString("evensemblid");
                  if (ensemblId != null) {
                    snvBuilder.transcriptId(
                        TranscriptId.builder()
                            .value(ensemblId)
                            .system(TranscriptIdSystem.ENSEMBL_ORG)
                            .build());
                  } else {
                    snvBuilder.transcriptId(
                        TranscriptId.builder()
                            .value(gene.getEnsemblId())
                            .system(TranscriptIdSystem.ENSEMBL_ORG)
                            .build());
                  }

                  if (subform.getString("exon") != null) {
                    snvBuilder.exonId(subform.getString("exon"));
                  }
                  if (subform.getString("cdnanomenklatur") != null) {
                    snvBuilder.dnaChange(subform.getString("cdnanomenklatur"));
                  }
                  if (subform.getString("proteinebenenomenklatur") != null) {
                    snvBuilder.proteinChange(subform.getString("proteinebenenomenklatur"));
                  }
                  if (null != subform.getLong("allelfrequenz")) {
                    snvBuilder.allelicFrequency(subform.getLong("allelfrequenz"));
                  }
                  if (null != subform.getLong("evreaddepth")) {
                    snvBuilder.readDepth(subform.getLong("evreaddepth"));
                  }
                  if (null != subform.getString("evaltnucleotide")) {
                    snvBuilder.altAllele(subform.getString("evaltnucleotide"));
                  }
                  if (null != subform.getString("evrefnucleotide")) {
                    snvBuilder.refAllele(subform.getString("evrefnucleotide"));
                  }

                  gene.getSingleChromosomeInPropertyForm().ifPresent(snvBuilder::chromosome);

                  return snvBuilder.build();
                })
            .filter(Objects::nonNull)
            // TODO: Filter missing position, altAllele, refAllele
            .filter(
                snv ->
                    snv.getPosition() != null
                        && snv.getAltAllele() != null
                        && snv.getRefAllele() != null)
            .collect(Collectors.toList()));

    resultBuilder.copyNumberVariants(
        subforms.stream()
            .filter(subform -> "CNV".equals(subform.getString("ergebnis")))
            .map(
                subform -> {
                  final var geneOptional = GeneUtils.findBySymbol(subform.getString("untersucht"));
                  if (geneOptional.isEmpty()) {
                    return null;
                  }

                  final var reportedAffectedGenes = new ArrayList<String>();
                  reportedAffectedGenes.add(subform.getString("untersucht"));

                  // Weitere betroffene Gene aus Freitextfeld?
                  if (null != subform.getString("cnvbetroffenegene")) {
                    reportedAffectedGenes.addAll(
                        Arrays.stream(subform.getString("cnvbetroffenegene").split("\\s"))
                            .collect(Collectors.toList()));
                  }

                  final var cnvBuilder =
                      Cnv.builder()
                          .id(subform.getString("id"))
                          .patient(subform.getPatientReference())
                          .reportedAffectedGenes(
                              reportedAffectedGenes.stream()
                                  .distinct()
                                  .map(GeneUtils::findBySymbol)
                                  .filter(Optional::isPresent)
                                  .map(gene -> GeneUtils.toCoding(gene.get()))
                                  .collect(Collectors.toList()))
                          .totalCopyNumber(subform.getLong("cnvtotalcn"));

                  if (getCnvTypeCoding(subform) != null) cnvBuilder.type(getCnvTypeCoding(subform));

                  geneOptional
                      .get()
                      .getSingleChromosomeInPropertyForm()
                      .ifPresent(cnvBuilder::chromosome);

                  return cnvBuilder.build();
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
    return resultBuilder.build();
  }

  private CnvCoding getCnvTypeCoding(ResultSet osMolResultSet) {

    var cnvFromString = osMolResultSet.getString("CopyNumberVariation");
    if (cnvFromString == null || cnvFromString.trim().isEmpty()) return null;

    CnvCodingCode cnvCode = getCodeFromString(cnvFromString.trim().toUpperCase());
    if (cnvCode == null) return null;

    return CnvCoding.builder().code(cnvCode).build();
  }

  private CnvCodingCode getCodeFromString(String value) {
    if (value.equals("G")) {
      return CnvCodingCode.HIGH_LEVEL_GAIN;
    } else if (value.equals("L")) {
      return CnvCodingCode.LOSS;
    } else if (value.equals("LLG")) {
      return CnvCodingCode.LOW_LEVEL_GAIN;
    } else {
      logger.error("No supported CNV Code for " + value + "found.");
      return null;
    }
  }

  private NgsReportCoding getNgsReportCoding(final String artdersequenzierung) {
    switch (artdersequenzierung) {
      case "WES":
        return NgsReportCoding.builder()
            .code(NgsReportCodingCode.EXOME)
            .display("Exome")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
      case "PanelKit":
        return NgsReportCoding.builder()
            .code(NgsReportCodingCode.PANEL)
            .display("Panel")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
      case "genome-long-read":
        return NgsReportCoding.builder()
            .code(NgsReportCodingCode.GENOME_LONG_READ)
            .display("Genome long-read")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
      case "genome-short-read":
        return NgsReportCoding.builder()
            .code(NgsReportCodingCode.GENOME_SHORT_READ)
            .display("Genome short-read")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
      default:
        return NgsReportCoding.builder()
            .code(NgsReportCodingCode.OTHER)
            .display("Other")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
    }
  }

  private NgsReportMetadata getNgsReportMetadata(final String artdersequenzierung) {
    var resultBuilder = NgsReportMetadata.builder();

    switch (artdersequenzierung) {
      // TODO: Replace with real data in properties file
      default:
        resultBuilder
            .sequencer("")
            .kitManufacturer("")
            .pipeline("")
            .kitType("")
            .kitManufacturer("")
            .referenceGenome("");
    }

    return resultBuilder.build();
  }
}
