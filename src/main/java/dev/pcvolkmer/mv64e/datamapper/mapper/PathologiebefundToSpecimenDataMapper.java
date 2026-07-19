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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.PathologiebefundCatalogue;
import dev.pcvolkmer.mv64e.model.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map patient data from database table 'dk_molekulargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.9
 */
public class PathologiebefundToSpecimenDataMapper implements DataMapper<TumorSpecimen> {

  private final PathologiebefundCatalogue pathologiebefundCatalogue;

  private final HistologieCatalogue histologieCatalogue;

  public PathologiebefundToSpecimenDataMapper(
      final PathologiebefundCatalogue pathologiebefundCatalogue,
      final HistologieCatalogue histologieCatalogue) {
    this.pathologiebefundCatalogue = pathologiebefundCatalogue;
    this.histologieCatalogue = histologieCatalogue;
  }

  /**
   * Loads and maps a specimen using the database id Not intended for direct use! The result does
   * not include a diagnosis reference!
   *
   * @param id The database id of the procedure data set
   * @return The loaded Patient data
   */
  @NullMarked
  @Override
  public TumorSpecimen getById(int id) {
    var data = pathologiebefundCatalogue.getById(id);

    var builder = TumorSpecimen.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .type(getTumorSpecimenCoding())
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
  @NullMarked
  public List<TumorSpecimen> getAllByKpaId(int kpaId, Reference diagnoseReferenz) {

    // Histologie
    return histologieCatalogue.getAllByParentId(kpaId).stream()
        .map(rs -> rs.getInteger("histologie"))
        .filter(Objects::nonNull)
        .filter(pathologiebefundCatalogue::isAvailable)
        .map(pathologiebefundCatalogue::getById)
        .map(ResultSet::getId)
        .filter(Objects::nonNull)
        .distinct()
        .map(this::getById)
        .peek(it -> it.setDiagnosis(diagnoseReferenz))
        .collect(Collectors.toList());
  }

  // TODO: Keine Angabe in Formular OS.Pathologiebefund möglich - best effort: Unknown
  @Nullable
  private TumorSpecimenTypeCoding getTumorSpecimenCoding() {
    return TumorSpecimenTypeCoding.builder()
        .system("dnpm-dip/mtb/tumor-specimen/type")
        .code(TumorSpecimenTypeCoding.CodeEnum.UNKNOWN)
        .display("Unbekannt")
        .build();
  }

  @Nullable
  private TumorSpecimenCollection getCollection(@NonNull ResultSet data) {
    final var entnahmemethode = data.getString("Praeparat");
    final var probenmaterial = data.getString("EntnahmestellederBiopsie");

    if (null == entnahmemethode || null == probenmaterial) {
      return null;
    }

    var methodBuilder =
        TumorSpecimenCollectionMethodCoding.builder()
            .system("dnpm-dip/mtb/tumor-specimen/collection/method");

    switch (entnahmemethode) {
      case "B":
        methodBuilder.code(TumorSpecimenCollectionMethodCoding.CodeEnum.BIOPSY).display("Biopsie");
        break;
      case "R":
        methodBuilder
            .code(TumorSpecimenCollectionMethodCoding.CodeEnum.RESECTION)
            .display("Resektat");
        break;
      case "U":
      default:
        methodBuilder
            .code(TumorSpecimenCollectionMethodCoding.CodeEnum.UNKNOWN)
            .display("Unbekannt");
        break;
    }

    // TODO: Kein genaues Mapping mit Formular OS.Pathologiebefund möglich - best effort
    var localizationBuilder =
        TumorSpecimenCollectionLocalizationCoding.builder()
            .system("dnpm-dip/mtb/tumor-specimen/collection/localization");

    switch (probenmaterial) {
      case "P":
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCoding.CodeEnum.PRIMARY_TUMOR)
            .display("Primärtumor");
        break;
      case "M":
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCoding.CodeEnum.METASTASIS)
            .display("Metastase");
        break;
      case "L":
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCoding.CodeEnum.REGIONAL_LYMPH_NODES)
            .display("Lymphknoten");
        break;
      default:
        localizationBuilder
            .code(TumorSpecimenCollectionLocalizationCoding.CodeEnum.UNKNOWN)
            .display("Unbekannt");
        break;
    }

    final var collectionBuilder =
        TumorSpecimenCollection.builder()
            .method(methodBuilder.build())
            .localization(localizationBuilder.build());

    if (!data.isNull("HistologieDatum")) {
      collectionBuilder.date(data.getDate("HistologieDatum"));
    }

    return collectionBuilder.build();
  }
}
