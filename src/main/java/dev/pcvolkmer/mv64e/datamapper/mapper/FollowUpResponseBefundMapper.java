/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
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
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import dev.pcvolkmer.mv64e.mtb.*;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map Response Befund data from database table 'dk_dnpm_followup'
 *
 * @author Paul-Christian Volkmer
 * @since 0.6
 */
public class FollowUpResponseBefundMapper implements DataMapper<Response> {

  private final FollowUpCatalogue catalogue;

  public FollowUpResponseBefundMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads and maps follow-up data using the dnpm follow-up procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded follow-up data
   */
  @Override
  public Response getById(final int id) {
    final var data = catalogue.getById(id);

    final var builder =
        Response.builder()
            .id(String.format("%d", id))
            // Therapy is in same procedure/form
            .therapy(
                Reference.builder().id(String.format("%d", id)).type("MTBSystemicTherapy").build())
            .patient(data.getPatientReference());

    final var date = data.getDate("datumfollowup");
    final var method = data.getString("beurteilungsmethode");

    if (null == date || null == method || null == getResponseMethodCoding(method)) {
      throw new IgnorableMappingException("Missing required response method or date");
    }

    builder.effectiveDate(date);
    builder.method(getResponseMethodCoding(method));

    final var bestresponse = data.getString("bestresponse");
    if (null != bestresponse) {
      builder.value(getRecistCoding(bestresponse));
    }

    return builder.build();
  }

  @Nullable
  private ResponseMethodCoding getResponseMethodCoding(final String value) {
    if (null == value) {
      return null;
    }
    try {
      return ResponseMethodCoding.builder()
          .code(ResponseMethodCodingCode.forValue(value))
          .display(value)
          .system("dnpm-dip/mtb/response/method")
          .build();
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  private RecistCoding getRecistCoding(final String value) {
    if (null == value) {
      return null;
    }
    try {
      return RecistCoding.builder()
          .code(RecistCodingCode.forValue(value))
          .display(value)
          .system("RECIST")
          .build();
    } catch (Exception e) {
      return null;
    }
  }
}
