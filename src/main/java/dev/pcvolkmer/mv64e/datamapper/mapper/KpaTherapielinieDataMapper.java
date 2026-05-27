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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapielinieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaTherapielinieDataMapper extends AbstractTherapielinieDataMapper {

  public KpaTherapielinieDataMapper(
      final TherapielinieCatalogue catalogue, final PropertyCatalogue propertyCatalogue) {
    super(catalogue, propertyCatalogue);
  }

  @Override
  protected Reference getDiagnosisReference(ResultSet resultSet) {
    return Reference.builder()
        .id(resultSet.getString("hauptprozedur_id"))
        .type("MTBDiagnosis")
        .build();
  }

  @Override
  @NullMarked
  protected Optional<PeriodDate> getPeriodDate(ResultSet resultSet) {
    var pdb = PeriodDate.builder();
    final var beginn = resultSet.getDate("beginn");
    if (null == beginn) {
      throw new IgnorableMappingException("Cannot map 'Therapielinie': 'Beginn' is missing");
    }
    pdb.start(beginn);
    if (resultSet.getDate("ende") != null) pdb.end(resultSet.getDate("ende"));
    return Optional.of(pdb.build());
  }
}
