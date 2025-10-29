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

package dev.pcvolkmer.onco.datamapper.datacatalogues;

import dev.pcvolkmer.onco.datamapper.ResultSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'dk_dnpm_therapieplan'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class TherapieplanCatalogue extends AbstractDataCatalogue {

  private TherapieplanCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected String getTableName() {
    return "dk_dnpm_therapieplan";
  }

  public static TherapieplanCatalogue create(JdbcTemplate jdbcTemplate) {
    return new TherapieplanCatalogue(jdbcTemplate);
  }

  /**
   * Get procedure IDs by related Klinik/Anamnese procedure id
   *
   * @param kpaId The procedure id
   * @return The procedure ids
   */
  public List<Integer> getByKpaId(int kpaId) {
    return this.jdbcTemplate
        .queryForList(
            String.format(
                "SELECT prozedur.id AS procedure_id FROM %s JOIN prozedur ON (prozedur.id = %s.id) WHERE geloescht = 0 AND ref_dnpm_klinikanamnese = ?",
                getTableName(), getTableName()),
            kpaId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("procedure_id"))
        .collect(Collectors.toList());
  }
}
