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

package dev.pcvolkmer.mv64e.datamapper.mapper;

import static dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryAndLog.tryAndLogWithResult;

import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some basic util methods for Data Mappers
 *
 * @author Paul-Christian Volkmer
 * @since 0.2
 */
public class MapperUtils {

  private static final Logger logger = LoggerFactory.getLogger(MapperUtils.class);

  private MapperUtils() {
    // No content
  }

  /**
   * Executes supplier and returns Optional.empty() if an IgnorableMappingException occurs. The
   * exceptions message will be logged as error.
   *
   * @param supplier The supplier to be executed
   * @return An optional containing the supplied value or empty option.
   * @param <T> The type of the supplied value
   */
  public static <T> Optional<T> tryAndReturnOrLog(@NonNull final Supplier<T> supplier) {
    return tryAndLogWithResult(supplier, logger).ok();
  }

  /**
   * Executes supplier and returns Optional.empty() if an IgnorableMappingException occurs. The
   * exceptions message will be logged as error.
   *
   * @param supplier The supplier to be executed
   * @param logger The logger to be used for logging the exception message
   * @return An optional containing the supplied value or empty option.
   * @param <T> The type of the supplied value
   */
  public static <T> Optional<T> tryAndReturnOrLog(
      @NonNull final Supplier<T> supplier, @NonNull final Logger logger) {
    return tryAndLogWithResult(supplier, logger).ok();
  }
}
