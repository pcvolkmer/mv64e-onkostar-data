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

package dev.pcvolkmer.mv64e.datamapper.datacatalogues;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Common implementations for all data catalogues used in subforms
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullMarked
public abstract class AbstractSubformDataCatalogue extends AbstractDataCatalogue
    implements DataCatalogue {

  protected AbstractSubformDataCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected abstract String getTableName();

  /**
   * Get procedure result sets by parent procedure id
   *
   * @param id The parents procedure id
   * @return The sub procedures
   */
  public List<ResultSet> getAllByParentId(int id) {
    return this.jdbcTemplate
        .queryForList(
            String.format(
                "SELECT patient.patienten_id, %s.*, prozedur.patient_id, prozedur.hauptprozedur_id FROM %s JOIN prozedur ON (prozedur.id = %s.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND hauptprozedur_id = ?",
                getTableName(), getTableName(), getTableName()),
            id)
        .stream()
        .filter(resultSet -> resultSet.containsKey("id"))
        .map(ResultSet::from)
        .peek(
            resultSet -> {
              var merkmale = getMerkmaleById(resultSet.getId());
              if (merkmale.isEmpty()) {
                return;
              }
              merkmale.forEach((key, value) -> resultSet.getRawData().put(key, value));
            })
        .collect(Collectors.toList());
  }

  /**
   * Get parent procedure by procedure id
   *
   * @param id The procedure id
   * @return The procedure
   */
  @NullMarked
  public int getParentIdById(int id) {
    try {
      return this.jdbcTemplate.queryForObject(
          "SELECT prozedur.hauptprozedur_id FROM prozedur WHERE geloescht = 0 AND prozedur.id = ?",
          new Integer[] {id},
          Integer.class);
    } catch (Exception e) {
      throw new DataAccessException(
          String.format("No parent found for id '%d': %s", id, e.getMessage()));
    }
  }
}
