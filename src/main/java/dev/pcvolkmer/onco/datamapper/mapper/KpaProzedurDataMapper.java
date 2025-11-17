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

import dev.pcvolkmer.mv64e.mtb.OncoProcedure;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ProzedurCatalogue;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_uf_prozedur'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaProzedurDataMapper extends AbstractKpaTherapieverlaufDataMapper<OncoProcedure> {

  private final Logger logger = LoggerFactory.getLogger(KpaProzedurDataMapper.class);

  public KpaProzedurDataMapper(
      final ProzedurCatalogue catalogue, final PropertyCatalogue propertyCatalogue) {
    super(catalogue, propertyCatalogue);
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded MtbDiagnosis file
   */
  @Override
  public OncoProcedure getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  /**
   * Maps result set into OncoProcedure
   *
   * @param resultSet The result set to start from
   * @return the OncoProcedure or null if not mappable
   */
  @Nullable
  @Override
  protected OncoProcedure map(@NonNull final ResultSet resultSet) {
    var diseases = catalogue.getDiseases(resultSet.getId());

    if (diseases.size() != 1) {
      throw new IllegalStateException(
          String.format("No unique disease for procedure %s", resultSet.getId()));
    }

    var start = resultSet.getDate("beginn");
    var erfassungsdatum = resultSet.getDate("erfassungsdatum");
    // Do not map procedures without start and end set
    if (null == start || null == erfassungsdatum) {
      logger.warn(
          "Cannot map procedure period date as 'beginn' date and erfassungsdatum are missing");
      return null;
    }

    var builder = OncoProcedure.builder();
    builder
        .id(resultSet.getString("id"))
        .patient(resultSet.getPatientReference())
        .reason(
            Reference.builder()
                .id(resultSet.getString("hauptprozedur_id"))
                .type("MTBDiagnosis")
                .build())
        .recordedOn(erfassungsdatum)
        .intent(
            getMtbTherapyIntentCoding(
                resultSet.getString("intention"),
                resultSet.getInteger("intention_propcat_version")))
        .status(
            getTherapyStatusCoding(
                resultSet.getString("status"), resultSet.getInteger("status_propcat_version")))
        .statusReason(
            getMtbTherapyStatusReasonCoding(
                resultSet.getString("statusgrund"),
                resultSet.getInteger("statusgrund_propcat_version")))
        .period(PeriodDate.builder().start(start).end(resultSet.getDate("ende")).build())
        .code(
            getOncoProcedureCoding(
                resultSet.getString("typ"), resultSet.getInteger("typ_propcat_version")));

    if (!resultSet.isNull("therapielinie")) {
      builder.therapyLine(resultSet.getLong("therapielinie"));
    }

    if (resultSet.getString("ref_einzelempfehlung") != null) {
      builder.basedOn(Reference.builder().id(resultSet.getString("ref_einzelempfehlung")).build());
    }

    if (resultSet.getString("anmerkungen") != null) {
      builder.notes(List.of(resultSet.getString("anmerkungen")));
    }

    return builder.build();
  }
}
