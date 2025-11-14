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

package dev.pcvolkmer.onco.datamapper;

import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

/**
 * Result set type to wrap <code>Map<String, Object></code>
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullUnmarked
public class ResultSet {

  private final Map<String, Object> rawData;

  private ResultSet(final Map<String, Object> rawData) {
    this.rawData = rawData;
  }

  public static ResultSet from(final Map<String, Object> rawData) {
    return new ResultSet(rawData);
  }

  public Map<String, Object> getRawData() {
    return rawData;
  }

  /**
   * Get the id
   *
   * @return The procedures id
   */
  public Integer getId() {
    var procedureId = this.getInteger("id");
    if (procedureId == null) {
      throw new DataAccessException("No procedure id found");
    }
    return procedureId;
  }

  /**
   * Get the id
   *
   * @return The procedures id
   */
  public Reference getPatientReference() {
    if (this.getString("patienten_id") == null) {
      throw new DataAccessException("No patient id found");
    }
    return Reference.builder()
        .id(this.getString("patienten_id"))
        // Use "Patient" since Onkostar only provides patient data
        .type("Patient")
        .build();
  }

  /**
   * Get column value as String and cast value if possible
   *
   * @param columnName The name of the column
   * @return The column value as String
   */
  @Nullable
  public String getString(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return null;
    } else if (raw instanceof String) {
      return raw.toString();
    } else if (raw instanceof Integer) {
      return ((Integer) raw).toString();
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to String");
  }

  /**
   * Get column value as Integer and cast value if possible
   *
   * @param columnName The name of the column
   * @return The column value as Integer
   */
  @Nullable
  public Integer getInteger(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return null;
    } else if (raw instanceof Integer) {
      return ((Integer) raw);
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to Integer");
  }

  /**
   * Get column value as Long and cast value if possible
   *
   * @param columnName The name of the column
   * @return The column value as Integer
   */
  @Nullable
  public Long getLong(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return null;
    } else if (raw instanceof Integer) {
      return ((Integer) raw).longValue();
    } else if (raw instanceof Double) {
      return ((Double) raw).longValue();
    } else if (raw instanceof Long) {
      return ((Long) raw);
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to Integer");
  }

  /**
   * Get column value as Double and cast value if possible
   *
   * @param columnName The name of the column
   * @return The column value as Integer
   */
  @Nullable
  public Double getDouble(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return null;
    } else if (raw instanceof Integer) {
      return ((Integer) raw).doubleValue();
    } else if (raw instanceof Long) {
      return ((Long) raw).doubleValue();
    } else if (raw instanceof Double) {
      return ((Double) raw);
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to Integer");
  }

  /**
   * Get column value as Date and cast value if possible
   *
   * @param columnName The name of the column
   * @return The column value as Date
   */
  @Nullable
  public Date getDate(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return null;
    }
    if (raw instanceof Date) {
      var localDate = LocalDate.parse(raw.toString());
      // JSON Converter uses UTC timezone
      return Date.from(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to Date");
  }

  /**
   * Check column value is equal to true
   *
   * @param columnName The name of the column
   * @return True if column value is equal to true
   */
  public boolean isTrue(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return false;
    }
    if (raw instanceof Boolean) {
      return ((Boolean) raw);
    } else if (raw instanceof Integer) {
      return ((Integer) raw) == 1;
    } else if (raw instanceof Long) {
      return ((Long) raw) == 1;
    } else if (raw instanceof String) {
      return raw.toString().equals("1");
    }

    throw new IllegalArgumentException("Cannot convert " + raw.getClass() + " to Boolean");
  }

  /**
   * Checks if column value has non null value
   *
   * @param columnName The name of the column
   * @return true or false
   */
  public boolean isNull(String columnName) {
    return null == this.rawData.get(columnName);
  }

  /**
   * Runs given function if value is not null
   *
   * @param columnName The name of the column
   * @param clazz The expected column type
   * @param f The function to be used if a value is present
   * @param <T> The type of the given column value
   */
  @SuppressWarnings("unchecked")
  public <T> void ifValueNotNull(String columnName, Class<T> clazz, Consumer<T> f) {
    if (this.isNull(columnName)) {
      return;
    }

    if (String.class == clazz) {
      f.accept((T) this.getString(columnName));
    } else if (Integer.class == clazz) {
      f.accept((T) this.getInteger(columnName));
    } else if (Long.class == clazz) {
      f.accept((T) this.getLong(columnName));
    } else if (Double.class == clazz) {
      f.accept((T) this.getDouble(columnName));
    } else if (Date.class == clazz) {
      f.accept((T) this.getDate(columnName));
    } else if (Boolean.class == clazz) {
      f.accept((T) (Boolean) this.isTrue(columnName));
    }
  }

  /**
   * Runs given function if value is not null or throws exception
   *
   * @param columnName The name of the column
   * @param clazz The expected column type
   * @param f The function to be used if a value is present
   * @param e The exception to be thrown if value is null
   * @param <T> The type of the given column value
   */
  public <T> void ifValueNotNull(
      String columnName, Class<T> clazz, Consumer<T> f, DataAccessException e) {
    if (this.isNull(columnName)) {
      throw e;
    }
    ifValueNotNull(columnName, clazz, f);
  }

  /**
   * Get Merkmal values as List of Strings
   *
   * @param columnName The name of the column
   * @return The related Merkmal value(s) as List of Strings
   */
  @SuppressWarnings("unchecked")
  public List<String> getMerkmalList(String columnName) {
    var raw = this.rawData.get(columnName);

    if (raw == null) {
      return List.of();
    } else if (raw instanceof List) {
      return (List<String>) raw;
    }

    throw new IllegalArgumentException("Cannot get " + columnName + " as List of Strings");
  }
}
