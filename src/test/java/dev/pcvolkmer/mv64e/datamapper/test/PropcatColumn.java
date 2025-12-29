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

import java.util.Arrays;

/**
 * Represents a propcat column in a test result set, having a value and a propcat version.
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.2
 */
public class PropcatColumn extends Column {

  public PropcatColumn(String name) {
    super(name);
  }

  /**
   * Creates a new column with the given name.
   *
   * @param name The name of the column.
   * @return A new column instance.
   */
  public static PropcatColumn name(String name) {
    return new PropcatColumn(name);
  }

  /**
   * Sets multiple propcat values for the column. This reflects values in '..._merkmale' tables in
   * the database.
   *
   * @param values The values to set.
   * @return The column instance.
   */
  public Column values(String... values) {
    this.value = Arrays.asList(values);
    return this;
  }
}
