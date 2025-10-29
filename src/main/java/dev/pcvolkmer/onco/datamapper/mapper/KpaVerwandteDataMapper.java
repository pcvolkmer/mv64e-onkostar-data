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

import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistory;
import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistoryRelationshipTypeCoding;
import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistoryRelationshipTypeCodingCode;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.VerwandteCatalogue;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_uf_verwandte'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaVerwandteDataMapper extends AbstractSubformDataMapper<FamilyMemberHistory> {

  public KpaVerwandteDataMapper(final VerwandteCatalogue catalogue) {
    super(catalogue);
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded data set
   */
  @Override
  public FamilyMemberHistory getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  @Override
  protected FamilyMemberHistory map(final ResultSet resultSet) {
    var builder = FamilyMemberHistory.builder();
    builder
        .id(resultSet.getId().toString())
        .patient(resultSet.getPatientReference())
        .relationship(
            getFamilyMemberHistoryRelationshipTypeCoding(
                resultSet.getString("verwandtschaftsgrad")));

    return builder.build();
  }

  private FamilyMemberHistoryRelationshipTypeCoding getFamilyMemberHistoryRelationshipTypeCoding(
      final String value) {
    if (value == null
        || !Arrays.stream(FamilyMemberHistoryRelationshipTypeCodingCode.values())
            .map(FamilyMemberHistoryRelationshipTypeCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        FamilyMemberHistoryRelationshipTypeCoding.builder()
            .system("dnpm-dip/mtb/family-meber-history/relationship-type");

    switch (value) {
      case "FAMMEMB":
        resultBuilder
            .code(FamilyMemberHistoryRelationshipTypeCodingCode.FAMMEMB)
            .display("Verwandter ersten Grades");
        break;
      case "EXT":
        resultBuilder
            .code(FamilyMemberHistoryRelationshipTypeCodingCode.EXT)
            .display("Verwandter weiteren Grades");
        break;
      default:
        throw new IllegalArgumentException(
            "Unknown family member history relationship type: " + value);
    }

    return resultBuilder.build();
  }
}
