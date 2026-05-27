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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.ProzedurCatalogue;
import dev.pcvolkmer.mv64e.mtb.OncoProcedure;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
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
  @Nullable
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

    var erfassungsdatum = resultSet.getDate("erfassungsdatum");
    if (null == erfassungsdatum) {
      logger.warn("Cannot map 'Therapielinie': 'Erfassungsdatum' is missing");
      return null;
    }

    var builder = OncoProcedure.builder();
    this.getPeriodDate(resultSet).ifPresent(builder::period);

    builder
        .id(resultSet.getString("id"))
        .patient(resultSet.getPatientReference())
        .reason(
            Reference.builder()
                .id(resultSet.getString("hauptprozedur_id"))
                .type("MTBDiagnosis")
                .build())
        .recordedOn(erfassungsdatum);

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
        (value, version) -> builder.statusReason(getMtbTherapyStatusReasonCoding(value, version)));

    resultSet.ifPropertyNotNull(
        "typ",
        String.class,
        (value, version) -> builder.code(getOncoProcedureCoding(value, version)));

    resultSet.ifValueNotNull("therapielinie", Long.class, builder::therapyLine);

    resultSet.ifValueNotNull(
        "ref_einzelempfehlung",
        String.class,
        value -> builder.basedOn(Reference.builder().id(value).build()));

    final var anmerkungen = resultSet.getString("anmerkungen");
    if (null != anmerkungen && !anmerkungen.isBlank()) {
      builder.notes(List.of(anmerkungen.trim()));
    }

    return builder.build();
  }

  @Override
  @NullMarked
  protected Optional<PeriodDate> getPeriodDate(ResultSet resultSet) {
    var pdb = PeriodDate.builder();
    final var beginn = resultSet.getDate("beginn");
    if (null == beginn) {
      return Optional.empty();
    }
    pdb.start(beginn);
    if (resultSet.getDate("ende") != null) pdb.end(resultSet.getDate("ende"));
    return Optional.of(pdb.build());
  }
}
