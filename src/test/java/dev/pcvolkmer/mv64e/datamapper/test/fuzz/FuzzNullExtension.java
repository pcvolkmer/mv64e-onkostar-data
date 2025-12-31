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
 * FuzzNullTest} by providing different null values to the method parameters. It uses the initMethod
 * specified in the annotation to provide a ResultSet for testing with different columns set to
 * null.
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
        return nulledResultSets((ResultSet) source).entrySet().stream()
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

  private Map<String, ResultSet> nulledResultSets(ResultSet resultSet) {
    var resultSets = new TreeMap<String, ResultSet>();
    for (var column : resultSet.getRawData().keySet()) {
      // Skip ID/primary key
      if ("id".equals(column)) {
        continue;
      }
      var fuzzyNullMap = new HashMap<String, Object>();
      for (var c : resultSet.getRawData().keySet()) {
        if (!column.equals(c)) {
          fuzzyNullMap.put(c, resultSet.getRawData().get(c));
        }
      }
      resultSets.put(column, TestResultSet.from(fuzzyNullMap));
    }

    return resultSets;
  }

  private TestTemplateInvocationContext invocationContext(String columnName, ResultSet resultSet) {
    return new TestTemplateInvocationContext() {
      @Override
      public String getDisplayName(int invocationIndex) {
        return String.format("[%d] with column '%s' set to null", invocationIndex, columnName);
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
