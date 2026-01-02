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

package dev.pcvolkmer.mv64e.datamapper.test.fuzz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.DataCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import dev.pcvolkmer.mv64e.datamapper.mapper.DataMapper;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class FuzzNullExtensionTest {

  DataCatalogue catalogue;
  TestMapper mapper;

  @BeforeEach
  void setUp(@Mock DataCatalogue catalogue) {
    this.catalogue = catalogue;
    this.mapper = new TestMapper(catalogue);
  }

  @FuzzNullTest(initMethod = "testData")
  void shouldNotSetIdToNull(final ResultSet resultSet) {
    assertThat(resultSet.getId()).isEqualTo(1);
  }

  @FuzzNullTest(initMethod = "testData")
  void shouldThrowIgnorableMappingExceptionOnNullColumnValue(final ResultSet resultSet) {
    when(this.catalogue.getById(anyInt())).thenReturn(resultSet);

    // getById(..) throws an IgnorableMappingException if any value other than ID is null
    var exception = assertThrows(IgnorableMappingException.class, () -> this.mapper.getById(1));
    assertThat(exception.getMessage()).isEqualTo("...");
  }

  @FuzzNullTest(
      initMethod = "testData",
      excludeColumns = {"date"})
  void shouldNotSetDateColumnToNull(final ResultSet resultSet) {
    assertThat(resultSet.getId()).isEqualTo(1);
    assertThat(resultSet.getDate("date")).isNotNull();
    assertThat(resultSet.getString("value")).isIn("Test", null);
  }

  @FuzzNullTest(
      initMethod = "testData",
      includeColumns = {"date"})
  void shouldOnlySetDateColumnToNull(final ResultSet resultSet) {
    assertThat(resultSet.getId()).isEqualTo(1);
    assertThat(resultSet.getDate("date")).isNull();
    assertThat(resultSet.getString("value")).isEqualTo("Test");
  }

  @FuzzNullTest(
      initMethod = "testData",
      includeColumns = {"date"},
      excludeColumns = {"date", "value"})
  void shouldIncludeOverExclude(final ResultSet resultSet) {
    assertThat(resultSet.getId()).isEqualTo(1);
    assertThat(resultSet.getDate("date")).isNull();
    assertThat(resultSet.getString("value")).isNotNull();
  }

  @FuzzNullTest(initMethod = "testData", maxNullColumns = -2)
  void testDefaultMaxNullColumnsPermutations(ResultSet resultSet) {
    assertThat(resultSet)
        .isIn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1), Column.name("value").value("Test")),
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1), DateColumn.name("date").value("2025-07-11")));
    assertThat(resultSet)
        .isNotIn(testData(), TestResultSet.withColumns(Column.name(Column.ID).value(1)));
  }

  @FuzzNullTest(initMethod = "testData", maxNullColumns = 2)
  void testAllPermutations(ResultSet resultSet) {
    assertThat(resultSet)
        .isIn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1), Column.name("value").value("Test")),
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1), DateColumn.name("date").value("2025-07-11")),
            TestResultSet.withColumns(Column.name(Column.ID).value(1)));
    assertThat(resultSet).isNotIn(testData());
  }

  @FuzzNullTest(
      initMethod = "testData",
      excludeColumns = {"date", "value"},
      maxNullColumns = 2)
  void shouldExcludeAllNonIdColumns(final ResultSet resultSet) {
    assertThat(resultSet.getId()).isEqualTo(1);
    assertThat(resultSet.getDate("date")).isNotNull();
    assertThat(resultSet.getString("value")).isNotNull();
  }

  static ResultSet testData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(1),
        DateColumn.name("date").value("2025-07-11"),
        Column.name("value").value("Test"));
  }

  static class TestMapper implements DataMapper<String> {

    private final DataCatalogue catalogue;

    public TestMapper(DataCatalogue dataSource) {
      this.catalogue = dataSource;
    }

    @Override
    public String getById(int id) {
      var resultSet = this.catalogue.getById(id);

      if (null == resultSet.getDate("date") || null == resultSet.getString("value")) {
        throw new IgnorableMappingException("...");
      }

      return String.format("ID: %d", this.catalogue.getById(id).getId());
    }
  }
}
