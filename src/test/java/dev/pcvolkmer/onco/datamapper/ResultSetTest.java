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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResultSetTest {

  @Test
  void shouldReturnStringValues() {
    var data = getTestData();

    assertThat(data.getString("null")).isNull();
    assertThat(data.getString("string")).isEqualTo("TestString");
    assertThat(data.getString("int")).isEqualTo("42");
  }

  @Test
  void shouldReturnIntegerValues() {
    var data = getTestData();

    assertThat(data.getInteger("int")).isEqualTo(42);
  }

  @Test
  void shouldReturnLongValues() {
    var data = getTestData();

    assertThat(data.getLong("int")).isEqualTo(42L);
  }

  @Test
  void shouldReturnDoubleValues() {
    var data = getTestData();

    assertThat(data.getDouble("int")).isEqualTo(42);
  }

  @Test
  void shouldReturnDateValues() {
    var data = getTestData();

    assertThat(data.getDate("date"))
        .isEqualTo(new Date(Date.from(Instant.parse("2025-06-21T00:00:00Z")).getTime()));
  }

  @Test
  void shouldHandleBooleanValues() {
    var data = getTestData();

    assertTrue(data.isTrue("true"));
    assertFalse(data.isTrue("false"));
  }

  @Test
  void shouldReturnIfNullValue() {
    var data = getTestData();

    assertFalse(data.isNull("string"));
    assertTrue(data.isNull("null"));
  }

  static ResultSet getTestData() {
    return ResultSet.from(
        Map.of(
            "string", "TestString",
            "int", 42,
            "date", new Date(Date.from(Instant.parse("2025-06-21T02:00:00Z")).getTime()),
            "true", 1,
            "false", 0));
  }
}
