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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapielinieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.mtb.MtbSystemicTherapy;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaTherapielinieDataMapper
    extends AbstractKpaTherapieverlaufDataMapper<MtbSystemicTherapy> {

  public KpaTherapielinieDataMapper(
      final TherapielinieCatalogue catalogue, final PropertyCatalogue propertyCatalogue) {
    super(catalogue, propertyCatalogue);
  }

  private final Logger logger = LoggerFactory.getLogger(KpaTherapielinieDataMapper.class);

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded MtbDiagnosis file
   */
  @Override
  public MtbSystemicTherapy getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  @Nullable
  @Override
  protected MtbSystemicTherapy map(@NonNull final ResultSet resultSet) {
    var diseases = catalogue.getDiseases(resultSet.getId());

    if (diseases.size() != 1) {
      throw new IllegalStateException(
          String.format("No unique disease for procedure %s", resultSet.getId()));
    }

    var builder = MtbSystemicTherapy.builder();
    try {
      // Determine if the therapy line is empty.
      // A therapy line is considered empty if both 'beginn' (start date) and
      // 'erfassungsdatum' (recorded date) are missing.
      // If so, log a warning and skip mapping for this record withouth breaking the
      // whole data
      // mapping.
      var start = resultSet.getDate("beginn");
      var erfassungsdatum = resultSet.getDate("erfassungsdatum");
      // Do not map procedures without start and end set
      if (null == start || null == erfassungsdatum) {
        logger.warn(
            "Cannot map therapyline period date as 'beginn' date and erfassungsdatum are missing");
        return null;
      }

      builder
          .id(resultSet.getString("id"))
          .patient(resultSet.getPatientReference())
          .reason(
              Reference.builder()
                  .id(resultSet.getString("hauptprozedur_id"))
                  .type("MTBDiagnosis")
                  .build())
          .recordedOn(erfassungsdatum)
          .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffcodes")));

      // --- Codings with null checks ---
      resultSet.ifPropertyNotNull(
          "intention",
          String.class,
          (value, version) -> builder.intent(getMtbTherapyIntentCoding(value, version)));

      resultSet.ifPropertyNotNull(
          "status",
          String.class,
          (value, version) -> builder.status(getTherapyStatusCoding(value, version)));

      resultSet.ifPropertyNotNull(
          "statusgrund",
          String.class,
          (value, version) ->
              builder.statusReason(getMtbTherapyStatusReasonCoding(value, version)));

      // --- Period Date with null checks ---
      var pdb = PeriodDate.builder().start(start);
      if (resultSet.getDate("ende") != null) pdb.end(resultSet.getDate("ende"));
      builder.period(pdb.build());

      if (!resultSet.isNull("nummer")) {
        builder.therapyLine(resultSet.getLong("nummer"));
      }

      if (!resultSet.isNull("ref_einzelempfehlung")) {
        builder.basedOn(
            Reference.builder().id(resultSet.getString("ref_einzelempfehlung")).build());
      }

      var stellungPropcatVersion = resultSet.getInteger("stellung_propcat_version");
      if (null != stellungPropcatVersion) {
        builder.category(
            getMtbSystemicTherapyCategoryCoding(
                resultSet.getString("stellung"), stellungPropcatVersion));
      }

      var dosisdichtePropcatVersion = resultSet.getInteger("dosisdichte_propcat_version");
      if (null != dosisdichtePropcatVersion) {
        builder.dosage(
            getMtbSystemicTherapyDosageDensityCoding(
                resultSet.getString("dosisdichte"), dosisdichtePropcatVersion));
      }

      var umsetzungPropcatVersion = resultSet.getInteger("umsetzung_propcat_version");
      if (null != umsetzungPropcatVersion) {
        builder.recommendationFulfillmentStatus(
            getMtbSystemicTherapyRecommendationFulfillmentStatusCoding(
                resultSet.getString("umsetzung"), umsetzungPropcatVersion));
      }

      var anmerkung = resultSet.getString("anmerkung");
      if (null != anmerkung) {
        builder.notes(List.of(anmerkung));
      }

      return builder.build();
    } catch (Exception e) {
      throw new DataAccessException(
          String.format("Cannot map MtbSystemicTherapy! %s", e.getMessage()));
    }
  }
}
