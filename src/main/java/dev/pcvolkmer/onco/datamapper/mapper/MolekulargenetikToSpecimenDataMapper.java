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
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper class to load and map patient data from database table 'dk_molekulargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MolekulargenetikToSpecimenDataMapper implements DataMapper<TumorSpecimen> {

  private final MolekulargenetikCatalogue molekulargenetikCatalogue;
  private final TherapieplanCatalogue therapieplanCatalogue;
  private final RebiopsieCatalogue rebiopsieCatalogue;
  private final ReevaluationCatalogue reevaluationCatalogue;
  private final EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  private final VorbefundeCatalogue vorbefundeCatalogue;
  private final HistologieCatalogue histologieCatalogue;

  public MolekulargenetikToSpecimenDataMapper(
      final MolekulargenetikCatalogue molekulargenetikCatalogue,
      final TherapieplanCatalogue therapieplanCatalogue,
      final RebiopsieCatalogue rebiopsieCatalogue,
      final ReevaluationCatalogue reevaluationCatalogue,
      final EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      final VorbefundeCatalogue vorbefundeCatalogue,
      final HistologieCatalogue histologieCatalogue) {
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.rebiopsieCatalogue = rebiopsieCatalogue;
    this.reevaluationCatalogue = reevaluationCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.vorbefundeCatalogue = vorbefundeCatalogue;
    this.histologieCatalogue = histologieCatalogue;
  }

  /**
   * Loads and maps a specimen using the database id Not intended for direct use! The result does
   * not include a diagnosis reference!
   *
   * @param id The database id of the procedure data set
   * @return The loaded Patient data
   */
  @Override
  public TumorSpecimen getById(int id) {
    var data = molekulargenetikCatalogue.getById(id);

    var builder = TumorSpecimen.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .type(getTumorSpecimenCoding(data.getString("materialfixierung")))
        .collection(getCollection(data))
    // diagnosis is added in getAllByKpaId()
    ;

    return builder.build();
  }

  /**
   * Loads and maps specimens by using the referencing KPA database id
   *
   * @param kpaId The database id of the referencing KPA procedure data set
   * @param diagnoseReferenz The reference object to the diagnosis
   * @return The loaded Patient data
   */
  public List<TumorSpecimen> getAllByKpaId(int kpaId, Reference diagnoseReferenz) {
    var therapieplanIds = therapieplanCatalogue.getByKpaId(kpaId);

    var osMolGen =
        therapieplanIds.stream()
            .map(einzelempfehlungCatalogue::getAllByParentId)
            .flatMap(
                einzelempfehlungen ->
                    einzelempfehlungen.stream()
                        .map(
                            einzelempfehlung ->
                                einzelempfehlung.getInteger("ref_molekulargenetik")))
            .collect(Collectors.toSet());

    // Addition: Rebiopsie
    osMolGen.addAll(
        therapieplanIds.stream()
            .map(rebiopsieCatalogue::getAllByParentId)
            .flatMap(
                einzelempfehlungen ->
                    einzelempfehlungen.stream()
                        .map(
                            einzelempfehlung ->
                                einzelempfehlung.getInteger("ref_molekulargenetik")))
            .collect(Collectors.toSet()));

    // Addition: Reevaluation
    osMolGen.addAll(
        therapieplanIds.stream()
            .map(reevaluationCatalogue::getAllByParentId)
            .flatMap(
                einzelempfehlungen ->
                    einzelempfehlungen.stream()
                        .map(
                            einzelempfehlung ->
                                einzelempfehlung.getInteger("ref_molekulargenetik")))
            .collect(Collectors.toSet()));

    // Vorbefunde anhand Einsendenummer
    osMolGen.addAll(
        vorbefundeCatalogue.getAllByParentId(kpaId).stream()
            .map(rs -> rs.getString("befundnummer"))
            .map(molekulargenetikCatalogue::getByEinsendenummer)
            .map(ResultSet::getId)
            .collect(Collectors.toList()));

    // Histologie
    osMolGen.addAll(
        histologieCatalogue.getAllByParentId(kpaId).stream()
            .map(rs -> rs.getInteger("histologie"))
            .map(molekulargenetikCatalogue::getById)
            .map(ResultSet::getId)
            .collect(Collectors.toList()));

    return osMolGen.stream()
        .filter(Objects::nonNull)
        .distinct()
        .map(this::getById)
        .peek(it -> it.setDiagnosis(diagnoseReferenz))
        .collect(Collectors.toList());
  }

  // TODO: Kein genaues Mapping mit Formular OS.Molekulargenetik möglich - best effort
  private TumorSpecimenCoding getTumorSpecimenCoding(String value) {
    if (value == null) {
      return null;
    }

    var resultBuilder = TumorSpecimenCoding.builder().system("dnpm-dip/mtb/tumor-specimen/type");

    switch (value) {
      case "2":
        resultBuilder.code(TumorSpecimenCodingCode.CRYO_FROZEN).display("Cryo-frozen");
        break;
      case "3":
        resultBuilder.code(TumorSpecimenCodingCode.FFPE).display("FFPE");
        break;
      default:
        resultBuilder.code(TumorSpecimenCodingCode.UNKNOWN).display("Unbekannt");
        break;
    }

    return resultBuilder.build();
  }

  private Collection getCollection(ResultSet data) {
    if (data == null
        || data.getString("entnahmemethode") == null
        || data.getString("probenmaterial") == null) {
      return null;
    }

    var methodBuilder =
        TumorSpecimenCollectionMethodCoding.builder()
            .system("dnpm-dip/mtb/tumor-specimen/collection/method");

    switch (data.getString("entnahmemethode")) {
      case "B":
        methodBuilder.code(TumorSpecimenCollectionMethodCodingCode.BIOPSY).display("Biopsie");
        break;
      case "R":
        methodBuilder.code(TumorSpecimenCollectionMethodCodingCode.RESECTION).display("Resektat");
        break;
      case "LB":
        methodBuilder
            .code(TumorSpecimenCollectionMethodCodingCode.LIQUID_BIOPSY)
            .display("Liquid Biopsy");
        break;
      case "Z":
        methodBuilder.code(TumorSpecimenCollectionMethodCodingCode.CYTOLOGY).display("Zytologie");
        break;
      case "U":
      default:
        methodBuilder.code(TumorSpecimenCollectionMethodCodingCode.UNKNOWN).display("Unbekannt");
        break;
    }

    // TODO: Kein genaues Mapping mit Formular OS.Molekulargenetik möglich - best effort
    var localizationBuilder =
        TumorSpecimenCollectionLocalizationCoding.builder()
            .system("dnpm-dip/mtb/tumor-specimen/collection/localization");

    switch (data.getString("probenmaterial")) {
      case "T":
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCodingCode.PRIMARY_TUMOR)
            .display("Primärtumor");
        break;
      case "LK":
      case "M":
      case "ITM":
      case "SM":
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCodingCode.METASTASIS)
            .display("Metastase");
        break;
      default:
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCodingCode.UNKNOWN)
            .display("Unbekannt");
        break;
    }

    return Collection.builder()
        .method(methodBuilder.build())
        .localization(localizationBuilder.build())
        .build();
  }
}
