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

import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'dk_dnpm_uf_tumorgrading'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class TumorgradingCatalogue extends AbstractSubformDataCatalogue {

  private TumorgradingCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected String getTableName() {
    return "dk_dnpm_uf_tumorgrading";
  }

  @NullMarked
  public static TumorgradingCatalogue create(JdbcTemplate jdbcTemplate) {
    return new TumorgradingCatalogue(jdbcTemplate);
  }
}
