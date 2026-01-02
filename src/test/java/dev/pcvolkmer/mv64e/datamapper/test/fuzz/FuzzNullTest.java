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

import static org.apiguardian.api.API.Status.INTERNAL;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import java.lang.annotation.*;
import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;

/**
 * Annotation for fuzzing null values in database queries.
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.3
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = INTERNAL, since = "0.3.3")
@TestTemplate
public @interface FuzzNullTest {
  /**
   * Specifies the name of a static method that initializes and returns a {@link ResultSet} for use
   * in fuzz null testing.
   *
   * @return the name of the static initialization method that provides a {@link ResultSet} instance
   *     for testing with columns set to null.
   */
  String initMethod();

  /**
   * Specifies columns to be included in null fuzzing. Including a column will override any
   * exclusion specified by {@link #excludeColumns()}.
   *
   * @return an array of column names to be included in null fuzzing.
   */
  String[] includeColumns() default {};

  /**
   * Specifies columns to exclude from null fuzzing.
   *
   * @return an array of column names to exclude from null fuzzing.
   */
  String[] excludeColumns() default {};

  /**
   * Maximum columns to be set to null in a single test. Defaults to 1 and should not exceed the
   * number of columns in the ResultSet.
   *
   * @return the maximum number of null columns to test
   */
  int maxNullColumns() default 1;
}
