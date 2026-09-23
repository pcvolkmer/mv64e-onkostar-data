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

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.model.MtbSystemicTherapy;
import dev.pcvolkmer.mv64e.model.MtbTherapyStatusReasonCoding;
import dev.pcvolkmer.mv64e.model.Reference;
import dev.pcvolkmer.mv64e.model.TherapyStatusCoding;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load create a dummy therapielinie for lost to follow up
 *
 * @author Paul-Christian Volkmer
 * @since 0.10
 */
public class FollowUpLostTherapielinieDataMapper implements DataMapper<MtbSystemicTherapy> {

  private final FollowUpCatalogue catalogue;

  public FollowUpLostTherapielinieDataMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads follow-up data with losttofollowup = true and maps into a dummy therapy. Reason must be
   * set an addition!
   *
   * @param id The database id of the procedure data set
   * @return The generated therapy data
   */
  @Override
  public @Nullable MtbSystemicTherapy getById(final int id) {
    final var data = catalogue.getById(id);

    final var date = data.getDate("datumfollowup");

    if (!data.isTrue("losttofollowup") || data.isNull("linktherapieempfehlung") || null == date) {
      return null;
    }

    return MtbSystemicTherapy.builder()
        .id(data.getId().toString())
        .recordedOn(date)
        .patient(data.getPatientReference())
        .basedOn(Reference.builder().id(data.getString("linktherapieempfehlung")).build())
        .status(
            TherapyStatusCoding.builder()
                .code(TherapyStatusCoding.CodeEnum.UNKNOWN)
                .display("Nicht durchgeführt")
                .system("dnpm-dip/therapy/status")
                .build())
        .statusReason(
            MtbTherapyStatusReasonCoding.builder()
                .code(MtbTherapyStatusReasonCoding.CodeEnum.LOST_TO_FU)
                .display("Lost to follow-up")
                .system("dnpm-dip/therapy/status-reason")
                .build())
        .medication(Set.of())
        .build();
  }
}
