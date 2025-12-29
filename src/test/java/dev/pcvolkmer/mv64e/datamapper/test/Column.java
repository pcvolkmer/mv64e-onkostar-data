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

/**
 * Represents a column in a test result set.
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.2
 */
public class Column {

  public static final String ID = "id";
  public static final String HAUPTPROZEDUR_ID = "hauptprozedur_id";
  public static final String PATIENTEN_ID = "patienten_id";

  String name;
  Object value;

  public Column(String name) {
    this.name = name;
  }

  /**
   * Creates a new column with the given name.
   *
   * @param name The name of the column.
   * @return A new column instance.
   */
  public static Column name(String name) {
    return new Column(name);
  }

  /**
   * Sets the value of the column.
   *
   * @param value The value to set.
   * @return The column instance.
   */
  public Column value(Object value) {
    this.value = value;
    return this;
  }
}
