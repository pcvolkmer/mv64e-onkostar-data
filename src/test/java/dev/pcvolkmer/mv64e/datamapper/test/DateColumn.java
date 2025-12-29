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

package dev.pcvolkmer.mv64e.datamapper.test;

import java.sql.Date;
import java.time.Instant;

/**
 * Represents a date column in a test result set.
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.2
 */
public class DateColumn extends Column {
  public DateColumn(String name) {
    super(name);
  }

  /**
   * Creates a new column with the given name.
   *
   * @param name The name of the column.
   * @return A new column instance.
   */
  public static DateColumn name(String name) {
    return new DateColumn(name);
  }

  /**
   * Sets the {@see java.sql.Date} value of the column based on date string.
   *
   * @param value The value to set.
   * @return The column instance.
   */
  @Override
  public DateColumn value(Object value) {
    if (value instanceof String) {
      this.value = this.fromString((String) value);
    }
    return this;
  }

  private Date fromString(String value) {
    return new java.sql.Date(Date.from(Instant.parse(value + "T00:00:00Z")).getTime());
  }
}
