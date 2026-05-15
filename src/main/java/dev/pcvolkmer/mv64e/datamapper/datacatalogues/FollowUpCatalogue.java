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

package dev.pcvolkmer.mv64e.datamapper.datacatalogues;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.6
 */
public class FollowUpCatalogue extends AbstractDataCatalogue {

  private FollowUpCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected String getTableName() {
    return "dk_dnpm_followup";
  }

  @NullMarked
  public static FollowUpCatalogue create(JdbcTemplate jdbcTemplate) {
    return new FollowUpCatalogue(jdbcTemplate);
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
            "SELECT DISTINCT fup.id FROM dk_dnpm_therapieplan tp "
                + "JOIN prozedur tpp ON (tp.id = tpp.id AND tpp.geloescht = 0) "
                + "JOIN prozedur eep ON (eep.hauptprozedur_id = tpp.id AND eep.geloescht = 0) "
                + "JOIN dk_dnpm_uf_einzelempfehlung ee ON (ee.id = eep.id) "
                + "JOIN dk_dnpm_followup fu ON (fu.linktherapieempfehlung = ee.id) "
                + "JOIN prozedur fup ON (fu.id = fup.id AND fup.geloescht = 0) "
                + "WHERE tp.ref_dnpm_klinikanamnese = ?",
            kpaId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("id"))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Get procedure IDs by related DNPM Einzelempfehlung procedure id
   *
   * @param recommendationId The procedure id
   * @return The procedure ids
   */
  public List<Integer> getByRecommendationId(int recommendationId) {
    return this.jdbcTemplate
        .queryForList(
            "SELECT DISTINCT fup.id FROM dk_dnpm_followup fu "
                + "JOIN prozedur fup ON (fu.id = fup.id AND fup.geloescht = 0) "
                + "WHERE fu.linktherapieempfehlung = ?",
            recommendationId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("id"))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
