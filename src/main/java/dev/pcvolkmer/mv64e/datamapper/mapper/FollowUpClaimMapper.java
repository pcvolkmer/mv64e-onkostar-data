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
import dev.pcvolkmer.mv64e.mtb.*;
import org.jspecify.annotations.Nullable;

public class FollowUpClaimMapper implements DataMapper<Claim> {

  private final FollowUpCatalogue catalogue;

  public FollowUpClaimMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads and maps follow-up data using the dnpm follow-up procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded follow-up data
   */
  @Override
  public Claim getById(final int id) {
    final var data = catalogue.getById(id);

    final var builder =
        Claim.builder().id(String.format("%d", id)).patient(data.getPatientReference());

    final var date = data.getDate("ausstellungsdatumantrag");
    if (null != date) {
      builder.issuedOn(date);
    }

    final var stage = data.getString("antragsstadium");
    if (null != stage) {
      builder.stage(getClaimStageCoding(stage));
    }

    final var recommendation = data.getString("linktherapieempfehlung");
    if (null != recommendation) {
      builder.recommendation(Reference.builder().id(recommendation).build());
    }

    // Ignore "requestedMedication" as it is not present in
    // https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783

    return builder.build();
  }

  private ClaimStageCoding getClaimStageCoding(final String stage) {
    try {
      return ClaimStageCoding.builder()
          .code(ClaimStageCodingCode.forValue(stage))
          .display(stage)
          .system("dnpm-dip/mtb/claim/stage")
          .build();
    } catch (Exception e) {
      return ClaimStageCoding.builder()
          .code(ClaimStageCodingCode.UNKNOWN)
          .display(stage)
          .system("dnpm-dip/mtb/claim/stage")
          .build();
    }
  }
}
