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
import dev.pcvolkmer.mv64e.model.ClaimResponse;
import dev.pcvolkmer.mv64e.model.ClaimResponseStatusCoding;
import dev.pcvolkmer.mv64e.model.ClaimResponseStatusReasonCoding;
import dev.pcvolkmer.mv64e.model.Reference;
import java.util.Set;

public class FollowUpClaimResponseMapper implements DataMapper<ClaimResponse> {

  private final FollowUpCatalogue catalogue;

  public FollowUpClaimResponseMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads and maps follow-up data using the dnpm follow-up procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded follow-up data
   */
  @Override
  public ClaimResponse getById(final int id) {
    final var data = catalogue.getById(id);

    final var builder =
        ClaimResponse.builder()
            .id(String.format("%d", id))
            .claim(Reference.builder().id(String.format("%d", id)).build())
            .patient(data.getPatientReference());

    final var date = data.getDate("datum_antwortkueantrag");
    if (null == date) {
      throw new IgnorableMappingException("No date found for claim response");
    }
    builder.issuedOn(date);

    final var status = data.getString("statuskostenuebernahme");
    if (null != status) {
      builder.status(getClaimResponseStatusCoding(status));
    }

    final var statusReason = data.getString("ablehnungkosten");
    if (null != statusReason) {
      builder.statusReason(getClaimResponseStatusReasonCoding(statusReason));
    }

    return builder.build();
  }

  private ClaimResponseStatusCoding getClaimResponseStatusCoding(final String stage) {
    try {
      return ClaimResponseStatusCoding.builder()
          .code(ClaimResponseStatusCoding.CodeEnum.fromValue(stage))
          .display(stage)
          .system("dnpm-dip/mtb/claim-response/status")
          .build();
    } catch (Exception e) {
      return ClaimResponseStatusCoding.builder()
          .code(ClaimResponseStatusCoding.CodeEnum.UNKNOWN)
          .display(stage)
          .system("dnpm-dip/mtb/claim-response/stage")
          .build();
    }
  }

  private Set<ClaimResponseStatusReasonCoding> getClaimResponseStatusReasonCoding(
      final String stage) {
    try {
      return Set.of(
          ClaimResponseStatusReasonCoding.builder()
              .code(ClaimResponseStatusReasonCoding.CodeEnum.fromValue(stage))
              .display(stage)
              .system("dnpm-dip/mtb/claim-response/status-reason")
              .build());
    } catch (Exception e) {
      return Set.of(
          ClaimResponseStatusReasonCoding.builder()
              .code(ClaimResponseStatusReasonCoding.CodeEnum.UNKNOWN)
              .display(stage)
              .system("dnpm-dip/mtb/claim-response/stage-reason")
              .build());
    }
  }
}
