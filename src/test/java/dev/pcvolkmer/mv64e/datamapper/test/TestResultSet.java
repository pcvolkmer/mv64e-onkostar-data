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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a test result set for testing purposes.
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.2
 */
public class TestResultSet extends ResultSet {

  private TestResultSet(final Map<String, Object> rawData) {
    super(rawData);
  }

  /**
   * Creates an empty result set.
   *
   * @return An empty result set.
   */
  public static TestResultSet empty() {
    return new TestResultSet(Map.of());
  }

  /**
   * Creates a result set with the given columns.
   *
   * @param columns The columns to include in the result set.
   * @return A result set with the specified columns.
   */
  public static TestResultSet withColumns(final Column... columns) {
    var rawData = new HashMap<String, Object>();
    for (Column column : columns) {
      rawData.put(column.name, column.value);
      if (column instanceof PropcatColumn) {
        var propcatVersion = (column.name + column.value).hashCode();
        rawData.put(
            column.name + "_propcat_version",
            propcatVersion >= 0 ? propcatVersion : -propcatVersion);
      }
    }
    return new TestResultSet(rawData);
  }
}
