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

import dev.pcvolkmer.mv64e.mtb.MtbEpisodeOfCare;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.KpaCatalogue;
import java.util.List;

/**
 * Mapper class to load and map patient data from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MtbEpisodeDataMapper implements DataMapper<MtbEpisodeOfCare> {

  private final KpaCatalogue kpaCatalogue;
  private final PropertyCatalogue propertyCatalogue;

  public MtbEpisodeDataMapper(
      final KpaCatalogue kpaCatalogue, final PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.propertyCatalogue = propertyCatalogue;
  }

  /**
   * Loads and maps a ca plan using the database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Patient data
   */
  @Override
  public MtbEpisodeOfCare getById(int id) {
    var kpaData = kpaCatalogue.getById(id);

    var builder = MtbEpisodeOfCare.builder();
    builder
        .id(kpaData.getString("id"))
        .patient(kpaData.getPatientReference())
        .diagnoses(
            List.of(Reference.builder().id(kpaData.getString("id")).type("Diagnose").build()))
        .period(PeriodDate.builder().start(kpaData.getDate("anmeldedatummtb")).build())
        .build();
    return builder.build();
  }
}
