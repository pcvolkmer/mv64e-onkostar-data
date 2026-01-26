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
import dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.tuples.Tuple;
import dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.tuples.Tuple2;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
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
  private final PropertyCatalogue propertyCatalogue;

  public MolekulargenetikNgsDataMapper(
      final MolekulargenetikCatalogue catalogue,
      final MolekulargenuntersuchungCatalogue untersuchungCatalogue,
      final PropertyCatalogue propertyCatalogue,
      final TumorCellContentMethodCodingCode tumorCellContentMethod) {
    this.catalogue = catalogue;
    this.untersuchungCatalogue = untersuchungCatalogue;
    this.tumorCellContentMethod = tumorCellContentMethod;
    this.propertyCatalogue = propertyCatalogue;
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Procedure
   */
  @Nullable
  @Override
  public SomaticNgsReport getById(final int id) {
    var data = catalogue.getById(id);

    if (!catalogue.isOfTypeSeqencing(id)) {
      logger.warn(
          "Molekulargenetik record with id '{}' is not of sequencing type. Aborting NGS mapping.",
          id);
      return null;
    }
    var builder = SomaticNgsReport.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .issuedOn(data.getDate("datum"))
        .specimen(Reference.builder().id(data.getString("id")).type("Specimen").build())
        .results(this.getNgsReportResults(data))
        .metadata(List.of(getNgsReportMetadata(data)));

    final var artdersequenzierung = data.getString("artdersequenzierung");
    if (null != artdersequenzierung) {
      builder.type(getNgsReportCoding(artdersequenzierung));
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
                  if (null == untersucht) {
                    logger.warn("No gene symbol found for simple variant {}", subform);
                    return null;
                  }

                  final var snvBuilder =
                      Snv.builder()
                          .id(subform.getString("id"))
                          .patient(subform.getPatientReference());

                  var chromosome = subform.getString("evchromosom");
                  var hgncId = subform.getString("evhgncid");
                  var ensemblId = subform.getString("evensemblid");

                  if (null != chromosome && null != hgncId && null != ensemblId) {
                    try {
                      snvBuilder.chromosome(Chromosome.forValue(chromosome));
                    } catch (Exception e) {
                      logger.warn("No chromosome found for '{}'", chromosome);
                    }
                    snvBuilder.gene(
                        Coding.builder()
                            .code(hgncId)
                            .display(untersucht)
                            .system("https://www.genenames.org/")
                            .build());
                    snvBuilder.transcriptId(
                        TranscriptId.builder()
                            .value(ensemblId)
                            .system(TranscriptIdSystem.ENSEMBL_ORG)
                            .build());
                  } else {
                    final var geneOptional = GeneUtils.findBySymbol(untersucht);
                    if (geneOptional.isEmpty()) {
                      logger.warn("Gene symbol '{}' not found in gene catalogue", untersucht);
                      return null;
                    }
                    geneOptional.ifPresent(
                        gene -> {
                          // Add hgncId and symbol from gene list if no HGNC ID is available
                          snvBuilder.gene(GeneUtils.toCoding(gene));
                          // Add transcriptId from gene list if no EnsemblID is available
                          snvBuilder.transcriptId(
                              TranscriptId.builder()
                                  .value(gene.getEnsemblId())
                                  .system(TranscriptIdSystem.ENSEMBL_ORG)
                                  .build());
                          // Add chromosome
                          gene.getSingleChromosomeInPropertyForm()
                              .ifPresent(snvBuilder::chromosome);
                        });
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
                    snvBuilder.proteinChange(mapProteinChangeToLongFormat(proteinebenenomenklatur));
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
                  if (null != posStart) {
                    snvBuilder.position(Position.builder().start(posStart).end(posEnd).build());
                  }

                  return snvBuilder.build();
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));

    resultBuilder.copyNumberVariants(
        subforms.stream()
            .filter(subform -> "CNV".equals(subform.getString("ergebnis")))
            .map(
                subform -> {
                  final var untersucht = subform.getString("untersucht");
                  if (null == untersucht) {
                    logger.warn("No gene symbol found for CNV {}", subform);
                    return null;
                  }
                  final var geneOptional = GeneUtils.findBySymbol(untersucht);
                  if (geneOptional.isEmpty()) {
                    logger.warn("Gene symbol {} not found in gene catalogue", untersucht);
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

  @NullMarked
  private NgsReportMetadata getNgsReportMetadata(final ResultSet osMolResultSet) {

    var builder = NgsReportMetadata.builder();

    var sequenziergeraet = osMolResultSet.getString("sequenziergeraet");
    var sequenziergeraetPv = osMolResultSet.getInteger("sequenziergeraet_propcat_version");
    if (null != sequenziergeraet && null != sequenziergeraetPv) {
      builder.sequencer(
          propertyCatalogue.getShortdescOrEmptyByCodeAndVersion(
              sequenziergeraet, sequenziergeraetPv));
    }

    var seqKitType = osMolResultSet.getString("SeqKitTyp");
    var seqKitTypePv = osMolResultSet.getInteger("seqkittyp_propcat_version");
    if (null != seqKitType && null != seqKitTypePv) {
      builder.kitType(
          propertyCatalogue.getShortdescOrEmptyByCodeAndVersion(seqKitType, seqKitTypePv));
    }

    var seqKitManufacturer = osMolResultSet.getString("SeqKitHersteller");
    var seqKitManufacturerPv = osMolResultSet.getInteger("seqkithersteller_propcat_version");
    if (null != seqKitManufacturer && null != seqKitManufacturerPv) {
      builder.kitManufacturer(
          propertyCatalogue.getShortdescOrEmptyByCodeAndVersion(
              seqKitManufacturer, seqKitManufacturerPv));
    }

    var seqPipeline = osMolResultSet.getString("SeqPipeline");
    var seqPipelinePv = osMolResultSet.getInteger("seqpipeline_propcat_version");
    if (null != seqPipeline && null != seqPipelinePv) {
      builder.pipeline(
          propertyCatalogue.getShortdescOrEmptyByCodeAndVersion(seqPipeline, seqPipelinePv));
    }

    var referenceGenome = osMolResultSet.getString("referenzgenom");
    var referenceGenomePv = osMolResultSet.getInteger("referenzgenom_propcat_version");
    if (null != referenceGenome && null != referenceGenomePv) {
      builder.referenceGenome(
          propertyCatalogue.getShortdescOrEmptyByCodeAndVersion(
              referenceGenome, referenceGenomePv));
    }

    return builder.build();
  }

  private static String mapProteinChangeToLongFormat(final String input) {
    final var mappingTable =
        List.of(
            Tuple.from("*", "*"),
            Tuple.from("F", "Phe"),
            Tuple.from("L", "Leu"),
            Tuple.from("S", "Ser"),
            Tuple.from("Y", "Tyr"),
            Tuple.from("C", "Cys"),
            Tuple.from("W", "Trp"),
            Tuple.from("P", "Pro"),
            Tuple.from("H", "His"),
            Tuple.from("Q", "Gln"),
            Tuple.from("R", "Arg"),
            Tuple.from("I", "Ile"),
            Tuple.from("M", "Met"),
            Tuple.from("T", "Thr"),
            Tuple.from("N", "Asn"),
            Tuple.from("K", "Lys"),
            Tuple.from("V", "Val"),
            Tuple.from("A", "Ala"),
            Tuple.from("D", "Asp"),
            Tuple.from("E", "Glu"),
            Tuple.from("G", "Gly"));

    final var pattern =
        Pattern.compile(
            "p\\.(?<ref>[*FLSYCWPHQRIMTNKVADEG])(?<pos>\\d+|del)(?<alt>[*FLSYCWPHQRIMTNKVADEG])");

    final var matcher = pattern.matcher(input);

    if (matcher.matches()) {
      var ref = matcher.group("ref");
      var pos = matcher.group("pos");
      var alt = matcher.group("alt");

      var longRef =
          mappingTable.stream()
              .filter(value -> value.get1().equals(ref))
              .map(Tuple2::get2)
              .findFirst();
      var longAlt =
          mappingTable.stream()
              .filter(value -> value.get1().equals(alt))
              .map(Tuple2::get2)
              .findFirst();

      if (longRef.isEmpty() || longAlt.isEmpty()) {
        return input;
      }

      return String.format("p.%s%s%s", longRef.get(), pos, longAlt.get());
    }

    return input;
  }
}
