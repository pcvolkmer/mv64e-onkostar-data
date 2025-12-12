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

package dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler;

import static dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryWithResult.withException;
import static dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryWithResult.withValue;

import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods to build a try/catch chain.
 *
 * @author Paul-Christian Volkmer
 * @since 0.2
 */
public class TryAndLog {

  private TryAndLog() {
    // utility class
  }

  /**
   * Executes supplier and returns TryWithResult to build a try/catch chain.
   *
   * @param supplier The supplier to be executed
   * @return TryWithResult for further processing
   * @param <T> The type of the supplied value
   */
  public static <T> TryWithResult<T> tryAndLogWithResult(Supplier<T> supplier) {
    return tryAndLogWithResult(supplier, LoggerFactory.getLogger(TryAndLog.class));
  }

  /**
   * Executes supplier and returns TryWithResult to build a try/catch chain.
   *
   * @param supplier The supplier to be executed
   * @param logger The logger to be used for logging the exception message
   * @return TryWithResult for further processing
   * @param <T> The type of the supplied value
   */
  public static <T> TryWithResult<T> tryAndLogWithResult(Supplier<T> supplier, Logger logger) {
    try {
      final var value = supplier.get();
      return withValue(value, logger);
    } catch (IgnorableMappingException e) {
      logger.error(e.getMessage(), e);
      return withException(e, logger);
    }
  }

  /**
   * Executes runnable and returns Try for further processing.
   *
   * @param runnable The runnable to be executed
   * @return TryWithResult for further processing
   */
  public static Try tryAndLog(Runnable runnable) {
    return tryAndLog(runnable, LoggerFactory.getLogger(TryAndLog.class));
  }

  /**
   * Executes runnable and returns Try for further processing.
   *
   * @param runnable The runnable to be executed
   * @param logger The logger to be used for logging the exception message
   * @return TryWithResult for further processing
   */
  public static Try tryAndLog(Runnable runnable, Logger logger) {
    try {
      runnable.run();
      return Try.clean(logger);
    } catch (IgnorableMappingException e) {
      logger.error(e.getMessage(), e);
      return Try.withException(e, logger);
    }
  }
}
