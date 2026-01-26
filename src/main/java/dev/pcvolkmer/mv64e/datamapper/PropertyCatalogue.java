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

package dev.pcvolkmer.mv64e.datamapper;

import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Data access to property catalogues in Onkostar database
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullMarked
public class PropertyCatalogue {

  private final JdbcTemplate jdbcTemplate;
  private static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(PropertyCatalogue.class);

  private PropertyCatalogue(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Nullable private static PropertyCatalogue obj;

  public static synchronized PropertyCatalogue initialize(final JdbcTemplate jdbcTemplate) {
    if (null == obj) {
      obj = new PropertyCatalogue(jdbcTemplate);
    }
    return obj;
  }

  public static synchronized PropertyCatalogue instance() {
    if (null == obj) {
      throw new IllegalStateException("PropertyCatalogue not initialized");
    }
    return obj;
  }

  /**
   * Get procedure result sets by parent procedure id
   *
   * @param code The entries code
   * @param version The entries version
   * @return The sub procedures
   */
  @NullMarked
  public Entry getByCodeAndVersion(String code, int version) {
    try {
      return this.jdbcTemplate.queryForObject(
          "SELECT code, shortdesc, e.description, v.oid AS version_oid, v.description AS version_description FROM property_catalogue_version_entry e"
              + " JOIN property_catalogue_version v ON (e.property_version_id = v.id)"
              + " WHERE code = ? AND property_version_id = ?",
          (rs, rowNum) ->
              new Entry(
                  rs.getString("code"),
                  rs.getString("shortdesc"),
                  rs.getString("description"),
                  rs.getString("version_oid"),
                  rs.getString("version_description")),
          code,
          version);
    } catch (RuntimeException e) {
      throw new DataAccessException(
          String.format(
              "Cannot request property catalogue entry for '%s' version '%d'", code, version));
    }
  }

  @NullMarked
  public String getShortdescOrEmptyByCodeAndVersion(final String code, final Integer version) {
    try {
      return (Objects.requireNonNullElse(getByCodeAndVersion(code, version).getShortdesc(), ""));
    } catch (DataAccessException e) {
      logger.warn(
          "Cannot get property catalogue entry for code '{}' and version '{}'. Returning empty string.",
          code,
          version);
      return "";
    }
  }

  /** A property catalogue entry */
  public static class Entry {
    private final String code;
    private final String shortdesc;
    private final String description;
    @Nullable private final String versionOid;
    @Nullable private final String versionDescription;

    public Entry(String code, String shortdesc, String description) {
      this(code, shortdesc, description, null, null);
    }

    @NullUnmarked
    public Entry(
        String code,
        String shortdesc,
        String description,
        @Nullable String versionOid,
        @Nullable String versionDescription) {
      this.code = code;
      this.shortdesc = shortdesc;
      this.description = description;
      this.versionOid = versionOid;
      this.versionDescription = versionDescription;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }

    public String getShortdesc() {
      return shortdesc;
    }

    @Nullable
    public String getVersionOid() {
      return versionOid;
    }

    @Nullable
    public String getVersionDescription() {
      return versionDescription;
    }
  }
}
