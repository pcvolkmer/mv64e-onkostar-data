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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.*;
import dev.pcvolkmer.mv64e.datamapper.genes.GeneUtils;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map prozedur data from database table 'dk_molekulargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MolekulargenetikNgsDataMapper implements DataMapper<SomaticNgsReport> {

  private static final Logger logger = LoggerFactory.getLogger(MolekulargenetikNgsDataMapper.class);
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

    if (!catalogue.isOfTypeSeqencing(id)) {
      logger.warn(
          "Molekulargenetik record with id "
              + id
              + " is not of sequencing type. Aborting NGS mapping.");
      return null;
    }
    var builder = SomaticNgsReport.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .issuedOn(data.getDate("datum"))
        .specimen(Reference.builder().id(data.getString("id")).type("Specimen").build())
        .results(this.getNgsReportResults(data));

    final var artdersequenzierung = data.getString("artdersequenzierung");
    if (null != artdersequenzierung) {
      builder.type(getNgsReportCoding(artdersequenzierung));
      builder.metadata(List.of(getNgsReportMetadata(artdersequenzierung)));
    }

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
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Loads and maps all Prozedur related by KPA database id
   *
   * @param kpaId The database id of the KPA procedure data set
   * @param molgenIdsFromHisto List of procedure IDs for related histology forms
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
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  private NgsReportResults getNgsReportResults(ResultSet resultSet) {
    var subforms = this.untersuchungCatalogue.getAllByParentId(resultSet.getId());

    var resultBuilder = NgsReportResults.builder();

    final var tumorzellgehalt = resultSet.getLong("tumorzellgehalt");
    if (null != tumorzellgehalt) {
      var tumorcellContentBuilder =
          TumorCellContent.builder()
              .id(resultSet.getId().toString())
              .patient(resultSet.getPatientReference())
              .specimen(Reference.builder().id(resultSet.getString("id")).type("Specimen").build())
              .value(tumorzellgehalt / 100.0);

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
                  final var untersucht = subform.getString("untersucht");
                  if (null == untersucht) return null;
                  final var geneOptional = GeneUtils.findBySymbol(untersucht);
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

                  final var exon = subform.getString("exon");
                  if (null != exon) {
                    snvBuilder.exonId(exon);
                  }
                  final var cdnanomenklatur = subform.getString("cdnanomenklatur");
                  if (null != cdnanomenklatur) {
                    snvBuilder.dnaChange(cdnanomenklatur);
                  }
                  final var proteinebenenomenklatur = subform.getString("proteinebenenomenklatur");
                  if (null != proteinebenenomenklatur) {
                    snvBuilder.proteinChange(proteinebenenomenklatur);
                  }
                  final var allelfrequenz = subform.getLong("allelfrequenz");
                  if (null != allelfrequenz) {
                    snvBuilder.allelicFrequency(allelfrequenz);
                  }
                  final var evreaddepth = subform.getLong("evreaddepth");
                  if (null != evreaddepth) {
                    snvBuilder.readDepth(evreaddepth);
                  }
                  final var evaltnucleotide = subform.getString("evaltnucleotide");
                  if (null != evaltnucleotide) {
                    snvBuilder.altAllele(evaltnucleotide);
                  }
                  final var evrefnucleotide = subform.getString("evrefnucleotide");
                  if (null != subform.getString("evrefnucleotide")) {
                    snvBuilder.refAllele(evrefnucleotide);
                  }

                  var posStart = subform.getDouble("EVStart");
                  var posEnd = subform.getDouble("EVEnde");
                  if (null != posStart && null != posEnd) {
                    snvBuilder.position(Position.builder().start(posStart).end(posEnd).build());
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
                  final var untersucht = subform.getString("untersucht");
                  if (null == untersucht) return null;

                  final var geneOptional = GeneUtils.findBySymbol(untersucht);
                  if (geneOptional.isEmpty()) {
                    return null;
                  }

                  final var reportedAffectedGenes = new ArrayList<String>();
                  reportedAffectedGenes.add(untersucht);

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

  @Nullable
  private CnvCoding getCnvTypeCoding(ResultSet osMolResultSet) {

    var cnvFromString = osMolResultSet.getString("CopyNumberVariation");
    if (cnvFromString == null || cnvFromString.trim().isEmpty()) return null;

    CnvCodingCode cnvCode = getCodeFromString(cnvFromString.trim().toUpperCase());
    if (cnvCode == null) return null;

    return CnvCoding.builder().code(cnvCode).build();
  }

  @Nullable
  private CnvCodingCode getCodeFromString(String value) {
    if (value.equals("G")) {
      return CnvCodingCode.HIGH_LEVEL_GAIN;
    } else if (value.equals("L")) {
      return CnvCodingCode.LOSS;
    } else if (value.equals("LLG")) {
      return CnvCodingCode.LOW_LEVEL_GAIN;
    } else {
      logger.error("No supported CNV Code for {} found.", value);
      return null;
    }
  }

  private NgsReportCoding getNgsReportCoding(@NonNull final String artdersequenzierung) {
    final var builder =
        NgsReportCoding.builder().system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type");

    switch (artdersequenzierung) {
      case "WES":
        return builder.code(NgsReportCodingCode.EXOME).display("Exome").build();
      case "PanelKit":
        return builder.code(NgsReportCodingCode.PANEL).display("Panel").build();
      case "genome-long-read":
        return builder
            .code(NgsReportCodingCode.GENOME_LONG_READ)
            .display("Genome long-read")
            .build();
      case "genome-short-read":
        return builder
            .code(NgsReportCodingCode.GENOME_SHORT_READ)
            .display("Genome short-read")
            .system("http://bwhc.de/mtb/somatic-ngs-report/sequencing-type")
            .build();
      default:
        return builder.code(NgsReportCodingCode.OTHER).display("Other").build();
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
