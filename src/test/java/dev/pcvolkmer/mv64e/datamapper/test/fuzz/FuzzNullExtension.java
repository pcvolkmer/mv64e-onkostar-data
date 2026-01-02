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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Extension for JUnit 5 that provides fuzz testing for methods annotated with {@link FuzzNullTest}.
 * The extension generates multiple test invocations for a method annotated with {@link
 * FuzzNullTest} by providing different null values in the method parameter. It uses the initMethod
 * specified in the annotation to provide a ResultSet for testing with different columns set to
 * null.
 *
 * <p>Columns in {@link FuzzNullTest#excludeColumns()} are excluded from nullification.
 *
 * <p>Columns in {@link FuzzNullTest#includeColumns()} are the only columns that should be set to
 * null. This overrides columns in the exclusion list.
 *
 * <p>{@link FuzzNullTest#maxNullColumns()} specifies the maximum number of columns to be set to
 * null in a single test invocation. The default value is 1 column per test invocation. <b>Warning:
 * Be careful with large values of {@link FuzzNullTest#maxNullColumns()}. This can lead to a very
 * large number of combinations and therefore test invocations and may slow down test execution.</b>
 *
 * @author Paul-Christian Volkmer
 * @since 0.3.3
 */
public class FuzzNullExtension implements TestTemplateInvocationContextProvider {

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getRequiredTestMethod().isAnnotationPresent(FuzzNullTest.class);
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
      ExtensionContext context) {
    var method = context.getRequiredTestMethod();
    method.setAccessible(true);
    var fuzzyTest = method.getAnnotation(FuzzNullTest.class);
    try {
      var sourceMethod = context.getRequiredTestClass().getDeclaredMethod(fuzzyTest.initMethod());
      sourceMethod.setAccessible(true);
      var source = sourceMethod.invoke(null);
      if (source instanceof ResultSet) {
        var includeList = Arrays.asList(fuzzyTest.includeColumns());
        var excludeList = Arrays.asList(fuzzyTest.excludeColumns());
        var maxNullColumns =
            Math.min(
                Math.max(fuzzyTest.maxNullColumns(), 1), ((ResultSet) source).getRawData().size());
        return nulledResultSets((ResultSet) source, maxNullColumns).entrySet().stream()
            .filter(entry -> includeList.isEmpty() || includeList.contains(entry.getKey()))
            .filter(
                entry ->
                    !excludeList.contains(entry.getKey()) || includeList.contains(entry.getKey()))
            .map(entry -> this.invocationContext(entry.getKey(), entry.getValue()));
      }
    } catch (Exception e) {
      // Nop
    }

    throw new PreconditionViolationException(
        "You must configure a valid static initMethod that returns a ResultSet for @FuzzyNullTest");
  }

  private static Set<Set<String>> selections(Set<String> columns, int maxColumns) {
    Set<Set<String>> result = new HashSet<>();
    for (var column : columns) {
      // Ignore column "id"
      if ("id".equals(column)) {
        continue;
      }

      if (maxColumns == 1) {
        var permutation = new HashSet<String>();
        permutation.add(column);
        result.add(permutation);
        continue;
      }

      for (var selection : selections(columns, maxColumns - 1)) {
        selection.add(column);
        result.add(selection);
      }
    }
    return result;
  }

  private Map<String, ResultSet> nulledResultSets(ResultSet resultSet, int maxColumnsNulled) {
    var resultSets = new TreeMap<String, ResultSet>();

    for (var selection : selections(resultSet.getRawData().keySet(), maxColumnsNulled)) {
      var fuzzyNullMap = new HashMap<String, Object>();
      for (var column : resultSet.getRawData().keySet()) {
        if (!selection.contains(column)) {
          fuzzyNullMap.put(column, resultSet.getRawData().get(column));
        }
      }
      resultSets.put(String.join(",", selection), TestResultSet.from(fuzzyNullMap));
    }
    return resultSets;
  }

  private TestTemplateInvocationContext invocationContext(String columnNames, ResultSet resultSet) {
    return new TestTemplateInvocationContext() {
      @Override
      public String getDisplayName(int invocationIndex) {
        return String.format("[%d] with column(s) [%s] set to null", invocationIndex, columnNames);
      }

      @Override
      public List<Extension> getAdditionalExtensions() {
        return List.of(
            new ParameterResolver() {
              @Override
              public boolean supportsParameter(
                  ParameterContext parameterContext, ExtensionContext extensionContext) {
                return parameterContext.getParameter().getType().equals(ResultSet.class);
              }

              @Override
              public Object resolveParameter(
                  ParameterContext parameterContext, ExtensionContext extensionContext)
                  throws ParameterResolutionException {
                return resultSet;
              }
            });
      }
    };
  }
}
