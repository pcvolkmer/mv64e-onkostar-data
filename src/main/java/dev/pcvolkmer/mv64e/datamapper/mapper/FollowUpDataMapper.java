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
import dev.pcvolkmer.mv64e.mtb.FollowUp;
import dev.pcvolkmer.mv64e.mtb.FollowUpPatientStatusCoding;
import dev.pcvolkmer.mv64e.mtb.FollowUpPatientStatusCodingCode;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map patient data from database table 'dk_dnpm_followup'
 *
 * @author Paul-Christian Volkmer
 * @since 0.6
 */
public class FollowUpDataMapper implements DataMapper<FollowUp> {

  private final FollowUpCatalogue catalogue;

  public FollowUpDataMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads and maps follow-up data using the dnpm follow-up procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded follow-up data
   */
  @Override
  public @Nullable FollowUp getById(final int id) {
    final var data = catalogue.getById(id);

    final var builder = FollowUp.builder().patient(data.getPatientReference());

    final var date = data.getDate("datumfollowup");
    if (null != date) {
      builder.date(date);
    }

    final var lastContactDate = data.getDate("datumletzterkontakt");
    if (null != lastContactDate) {
      builder.lastContactDate(lastContactDate);
    }

    if (data.isTrue("losttofollowup")) {
      builder.patientStatus(
          FollowUpPatientStatusCoding.builder()
              .code(FollowUpPatientStatusCodingCode.LOST_TO_FU)
              .display(FollowUpPatientStatusCodingCode.LOST_TO_FU.toValue())
              .system("dnpm-dip/follow-up/patient-status")
              .build());
    }

    return builder.build();
  }
}
