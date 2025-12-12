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
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A try/catch chain without any expected result
 *
 * @author Paul-Christian Volkmer
 * @since 0.2
 */
@NullMarked
public class Try extends AbstractLoggable implements Catchable {

  @Nullable private final IgnorableMappingException exception;

  private Try(@Nullable final IgnorableMappingException exception, Logger logger) {
    super(logger);
    this.exception = exception;
  }

  static Try withException(IgnorableMappingException exception, Logger logger) {
    return new Try(exception, logger);
  }

  static Try clean(Logger logger) {
    return new Try(null, logger);
  }

  /** Throws exception if present. */
  public void elseThrow() {
    if (null != exception) {
      throw exception;
    }
  }

  /**
   * Consume exception if present.
   *
   * @param exceptionConsumer The consumer to be executed with the exception
   */
  public Try elseCatch(Consumer<IgnorableMappingException> exceptionConsumer) {
    if (null != this.getException()) {
      exceptionConsumer.accept(this.getException());
    }
    return clean(this.logger);
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
