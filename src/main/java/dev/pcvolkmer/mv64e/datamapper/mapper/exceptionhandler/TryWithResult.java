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

import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A try/catch chain with an expected result
 *
 * @author Paul-Christian Volkmer
 * @since 0.2
 */
@NullMarked
public class TryWithResult<O> extends AbstractLoggable implements Catchable {

  @Nullable private final O value;
  @Nullable private final IgnorableMappingException exception;

  private TryWithResult(
      @Nullable final O value, @Nullable final IgnorableMappingException exception, Logger logger) {
    super(logger);
    this.value = value;
    this.exception = exception;
  }

  static <O> TryWithResult<O> empty(Logger logger) {
    return new TryWithResult<>(null, null, logger);
  }

  static <O> TryWithResult<O> withValue(O value, Logger logger) {
    return new TryWithResult<>(value, null, logger);
  }

  static <O> TryWithResult<O> withException(IgnorableMappingException exception, Logger logger) {
    return new TryWithResult<>(null, exception, logger);
  }

  /**
   * Try another function and update result or resend exception.
   *
   * @param function The function to be executed
   * @return TryWithResult for further processing
   * @param <T> The expected return type
   */
  public <T> TryWithResult<T> andTryWithResult(Function<O, T> function) {
    if (null != exception) {
      return withException(exception, this.logger);
    }

    try {
      if (null != this.value) {
        return withValue(function.apply(this.value), this.logger);
      }
      return empty(this.logger);
    } catch (IgnorableMappingException e) {
      logger.error(e.getMessage(), e);
      return withException(e, this.logger);
    }
  }

  /**
   * Try to consume last result or resend exception.
   *
   * @param consumer The consumer to be executed
   * @return Try for further processing
   */
  public Try andTry(Consumer<O> consumer) {
    if (null != exception) {
      return Try.withException(exception, this.logger);
    }

    try {
      if (null != this.value) {
        consumer.accept(this.value);
      }
      return Try.clean(this.logger);
    } catch (IgnorableMappingException e) {
      logger.error(e.getMessage(), e);
      return Try.withException(e, this.logger);
    }
  }

  /**
   * Throws exception if present.
   *
   * @return TryWithResult for further processing
   */
  public TryWithResult<O> elseThrow() {
    if (null != exception) {
      throw exception;
    }
    return this;
  }

  /**
   * Consume exception if present.
   *
   * @param exceptionConsumer The consumer to be executed with the exception
   * @return TryWithResult for further processing
   */
  public TryWithResult<O> elseCatch(Consumer<IgnorableMappingException> exceptionConsumer) {
    if (null != this.getException()) {
      exceptionConsumer.accept(this.getException());
    }
    return empty(this.logger);
  }

  /**
   * Get the result as optional.
   *
   * @return The result as optional
   */
  public Optional<@Nullable O> ok() {
    return Optional.ofNullable(value);
  }

  /**
   * Get the result or null.
   *
   * @return The result as optional
   */
  @Nullable
  public O okOrNull() {
    return value;
  }

  /**
   * Get the exception if any.
   *
   * @return The exception if any
   */
  @Override
  @Nullable
  public IgnorableMappingException getException() {
    return exception;
  }
}
