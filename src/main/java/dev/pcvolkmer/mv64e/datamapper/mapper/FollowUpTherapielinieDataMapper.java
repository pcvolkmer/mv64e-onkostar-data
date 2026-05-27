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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapielinieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.mtb.Reference;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.8
 */
public class FollowUpTherapielinieDataMapper extends AbstractTherapielinieDataMapper {

  private final EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  private final TherapieplanCatalogue therapieplanCatalogue;

  public FollowUpTherapielinieDataMapper(
      final TherapielinieCatalogue catalogue,
      final EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      final TherapieplanCatalogue therapieplanCatalogue,
      final PropertyCatalogue propertyCatalogue) {
    super(catalogue, propertyCatalogue);
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.therapieplanCatalogue = therapieplanCatalogue;
  }

  @Override
  protected Reference getDiagnosisReference(ResultSet resultSet) {
    final var einzelempfehlungId = resultSet.getInteger("ref_einzelempfehlung");
    if (null == einzelempfehlungId) {
      throw new IllegalStateException("No reference to einzelempfehlung found");
    }

    final var einzelempfehlung = einzelempfehlungCatalogue.getById(einzelempfehlungId);
    final var therapieplanId = einzelempfehlung.getParentId();
    if (null == therapieplanId) {
      throw new IllegalStateException("No reference to therapieplan found");
    }

    final var therapieplan = therapieplanCatalogue.getById(therapieplanId);

    final var kpaId = therapieplan.getString("ref_dnpm_klinikanamnese");
    if (null == kpaId) {
      throw new IllegalStateException("No reference to kpa found");
    }

    return Reference.builder().id(kpaId).type("MTBDiagnosis").build();
  }
}
