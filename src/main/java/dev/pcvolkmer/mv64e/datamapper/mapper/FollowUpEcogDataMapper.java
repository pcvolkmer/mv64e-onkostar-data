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
import dev.pcvolkmer.mv64e.model.PerformanceStatus;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_followup'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class FollowUpEcogDataMapper implements DataMapper<PerformanceStatus>, EcogMapper {

  private final FollowUpCatalogue catalogue;

  public FollowUpEcogDataMapper(final FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The patient id of the procedure data set
   * @return The loaded data set
   */
  @Nullable
  @Override
  public PerformanceStatus getById(final int id) {
    final var data = catalogue.getById(id);

    final var patientReference = data.getPatientReference();
    final var date = data.getDate("datumfollowup");
    final var ecog = data.getString("ecog");

    if (null == date || null == ecog || ecog.isBlank()) {
      return null;
    }

    return PerformanceStatus.builder()
        .id(String.format("%d", id))
        .patient(patientReference)
        .effectiveDate(date)
        .value(getEcogCoding(ecog))
        .build();
  }
}
